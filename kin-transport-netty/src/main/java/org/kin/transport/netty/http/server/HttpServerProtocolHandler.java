package org.kin.transport.netty.http.server;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.kin.framework.concurrent.ExecutionContext;
import org.kin.framework.concurrent.actor.PinnedThreadSafeHandler;
import org.kin.framework.utils.ClassUtils;
import org.kin.framework.utils.StringUtils;
import org.kin.transport.netty.ProtocolHandler;
import org.kin.transport.netty.http.HttpResponseBody;
import org.kin.transport.netty.http.HttpUrl;
import org.kin.transport.netty.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * http server接受到请求包逻辑处理
 *
 * @author huangjianqin
 * @date 2020/9/10
 */
public class HttpServerProtocolHandler extends ProtocolHandler<ServletTransportEntity> {
    private static final ExecutionContext EXECUTION_CONTEXT = ExecutionContext.cache("http-servet");
    private static final AttributeKey<ChannelServletRequestHandler> CHANNEL_SERVLET_REQUEST_HANDLER_KEY =
            AttributeKey.newInstance("ChannelServletRequestHandler");
    private static final KinHttpServer.FilterConfig SERVLET_SERVICE_FILTER_CONFIG =
            new KinHttpServer.FilterConfig("/", ServletServiceFilter.class);
    /** 配置 */
    private final KinHttpServer kinHttpServer;
    private final HttpSessionManager sessionManager;
    /** filter 缓存 */
    private Cache<Class<? extends Filter>, Filter> filterCache = CacheBuilder.newBuilder().build();
    /** servlet 缓存 */
    private Cache<Class<? extends Servlet>, Servlet> servletCache = CacheBuilder.newBuilder().build();

    public HttpServerProtocolHandler(KinHttpServer kinHttpServer) {
        this.kinHttpServer = kinHttpServer;
        this.sessionManager = kinHttpServer.getSessionManager();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        Attribute<ChannelServletRequestHandler> attribute = channel.attr(CHANNEL_SERVLET_REQUEST_HANDLER_KEY);
        ChannelServletRequestHandler channelServletRequestHandler = attribute.setIfAbsent(new ChannelServletRequestHandler());
        if (Objects.nonNull(channelServletRequestHandler)) {
            //已经有值, 说明逻辑有问题
            channel.close();
        }
    }

    /**
     * 获取该channel对应的{@link ChannelServletRequestHandler}
     */
    private ChannelServletRequestHandler channelServletRequestHandler(Channel channel) {
        Attribute<ChannelServletRequestHandler> attribute = channel.attr(CHANNEL_SERVLET_REQUEST_HANDLER_KEY);
        return attribute.get();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ChannelServletRequestHandler channelServletRequestHandler = channelServletRequestHandler(ctx.channel());
        channelServletRequestHandler.stop();
    }

    @Override
    public void handle(ChannelHandlerContext ctx, ServletTransportEntity servletTransportEntity) {
        if (!(servletTransportEntity instanceof ServletRequest)) {
            return;
        }

        ServletRequest request = (ServletRequest) servletTransportEntity;
        Channel channel = ctx.channel();
        ChannelServletRequestHandler channelServletRequestHandler = channelServletRequestHandler(channel);
        if (Objects.isNull(channelServletRequestHandler)) {
            //没有值, 说明逻辑有问题
            channel.close();
        }
        channelServletRequestHandler.handle(handler -> handle(channel, request));
    }

    /**
     * url 匹配
     * 支持*通配符, 即/a/b/* 匹配 /a/b/c/d/e
     */
    private boolean urlMatched(String source, String target) {
        String[] sourceSplits = source.split("/");
        String[] targetSplits = target.split("/");

        if (sourceSplits.length == 0) {
            // url = '/', 适合所有url
            return true;
        }

        if (sourceSplits.length > targetSplits.length) {
            //source items比target items多, 肯定不匹配
            return false;
        }

        for (int i = 0; i < sourceSplits.length; i++) {
            String sourceItem = sourceSplits[i];
            String targetItem = targetSplits[i];
            if (HttpServerConstants.URL_ALL_MATCH.equals(sourceItem)) {
                //遇到*通配符, 直接返回匹配
                return true;
            }
            if (!sourceItem.equals(targetItem)) {
                //url某个path不匹配, 就不匹配
                return false;
            }
        }

        //source 所有item 与target 部分item匹配, 但target items可能存在比source items要多, 则不匹配
        return sourceSplits.length == targetSplits.length;
    }

    /**
     * 获取url映射匹配的filter
     */
    private List<Filter> matchedFilters(String url) {
        Queue<KinHttpServer.FilterConfig> filterConfigs = new LinkedList<>(kinHttpServer.getFilterConfigs());
        filterConfigs.add(SERVLET_SERVICE_FILTER_CONFIG);
        List<Filter> matchedFilters = new ArrayList<>(filterConfigs.size());
        for (KinHttpServer.FilterConfig filterConfig : filterConfigs) {
            if (!urlMatched(url, filterConfig.getUrl())) {
                continue;
            }

            Class<? extends Filter> filterClass = filterConfig.getFilterClass();
            try {
                Filter filter = filterCache.get(filterClass, () -> {
                    Filter instance = ClassUtils.instance(filterClass);
                    instance.init();
                    return instance;
                });
                matchedFilters.add(filter);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        return matchedFilters;
    }

    /**
     * 获取匹配的Servlet, 按配置顺序, 谁先匹配到, 谁就用于处理逻辑
     */
    private Servlet matchedServlet(String url) {
        Queue<KinHttpServer.ServletConfig> servletConfigs = kinHttpServer.getServletConfigs();
        KinHttpServer.ServletConfig matchedServletConfig = null;
        for (KinHttpServer.ServletConfig servletConfig : servletConfigs) {
            if (urlMatched(url, servletConfig.getUrl())) {
                matchedServletConfig = servletConfig;
                break;
            }
        }

        if (Objects.nonNull(matchedServletConfig)) {
            Class<? extends Servlet> servletClass = matchedServletConfig.getServletClass();
            try {
                return servletCache.get(servletClass, () -> {
                    Servlet instance = ClassUtils.instance(servletClass);
                    instance.init();
                    return instance;
                });
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        throw new ServletException(String.format("can't find matched servlet, url path is '%s'", url));
    }

    /**
     * {@link ServletRequest}逻辑处理
     */
    private void handle(Channel channel, ServletRequest request) {
        HttpUrl httpUrl = request.getUrl();

        List<Cookie> respCookies = new ArrayList<>(request.getCookies());
        String sessionId = request.cookie(HttpSession.SESSION_ID);

        //附上服务端session
        HttpSession session = sessionManager.session(sessionId);
        request.setSession(session);

        if (StringUtils.isBlank(sessionId)) {
            //第一次请求没有session id
            //添加对应的session id cookie
            respCookies.add(Cookie.of(HttpSession.SESSION_ID, session.getSessionId()));
        }

        ServletResponse response = new ServletResponse(httpUrl, respCookies, request.isKeepAlive());
        try {
            String rawUrl = httpUrl.rawUrl();
            List<Filter> filters = matchedFilters(rawUrl);

            //顺序filter
            for (int i = 0; i < filters.size(); i++) {
                filters.get(i).doFilter(request, response);
                //filter中设置了response, 故servlet不作任何处理
                if (response.getStatusCode() > 0) {
                    break;
                }
            }
        } catch (Exception e) {
            handleException(channel, response, e);
        } finally {
            channel.writeAndFlush(response);
        }
    }

    @Override
    public void handleException(ChannelHandlerContext ctx, Throwable cause) {
        ServletResponse response = new ServletResponse(null, Collections.emptyList(), false);
        Channel channel = ctx.channel();
        handleException(channel, response, cause);
        channel.writeAndFlush(response);
    }

    /**
     * 处理异常, 修改response1的内容, 不作channel write处理
     */
    private void handleException(Channel channel, ServletResponse response, Throwable cause) {
        response.setStatusCode(ServletResponse.SC_INTERNAL_SERVER_ERROR);
        ByteBuf buffer = channel.alloc().buffer();
        buffer.writeBytes(cause.toString().getBytes());
        response.setResponseBody(HttpResponseBody.of(buffer, MediaType.PLAIN_TEXT.transfer(StandardCharsets.UTF_8.name())));
    }

    //--------------------------------------------------------------------------------------------------------------

    /**
     * 每个channel一条线程处理逻辑
     */
    private class ChannelServletRequestHandler extends PinnedThreadSafeHandler<ChannelServletRequestHandler> {
        public ChannelServletRequestHandler() {
            super(EXECUTION_CONTEXT);
        }
    }

    /**
     * 执行servlet匹配并执行servlet逻辑的filter
     */
    private class ServletServiceFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response) {
            Servlet matchedServlet = matchedServlet(request.getUrl().rawUrl());
            //servlet 处理
            matchedServlet.service(request, response);
        }
    }
}
