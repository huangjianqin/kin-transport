package org.kin.transport.netty.http.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.kin.framework.concurrent.ExecutionContext;
import org.kin.framework.concurrent.actor.PinnedThreadSafeHandler;
import org.kin.framework.log.LoggerOprs;
import org.kin.framework.utils.StringUtils;
import org.kin.framework.utils.SysUtils;
import org.kin.transport.netty.ProtocolHandler;
import org.kin.transport.netty.http.HttpResponseBody;
import org.kin.transport.netty.http.HttpUrl;
import org.kin.transport.netty.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * http server接受到请求包逻辑处理
 * 理论上, http请求时, 建立连接, 发送请求, 接受响应, 关闭连接, 期间该channel不应该有其他请求, server也不做处理, 直接抛弃
 *
 * @author huangjianqin
 * @date 2020/9/10
 */
class HttpServerProtocolHandler extends ProtocolHandler<ServletTransportEntity> implements LoggerOprs {
    private static final ExecutionContext EXECUTION_CONTEXT = ExecutionContext.elastic(SysUtils.CPU_NUM, SysUtils.CPU_NUM * 10, "kin-http-servlet");
    /** 每个channel存储一个handler */
    private static final AttributeKey<ChannelServletRequestHandler> CHANNEL_SERVLET_REQUEST_HANDLER_KEY =
            AttributeKey.newInstance("kin_channel_handler");
    /** channel状态, true表示正在处理请求, false表示空闲 */
    private static final AttributeKey<Boolean> CHANNEL_STATUS_KEY =
            AttributeKey.newInstance("kin_channel_status");

    /** 配置 */
    private final KinHttpServer kinHttpServer;
    /** http server session */
    private final HttpSessionManager sessionManager;

    HttpServerProtocolHandler(KinHttpServer kinHttpServer) {
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

        Attribute<Boolean> channelStatusAttribute = channel.attr(CHANNEL_STATUS_KEY);
        if (channelStatusAttribute.get()) {
            warn("channel '{}' '{}' is handling http request", channel.remoteAddress(), request.getUrl());
            return;
        }
        //设置channel busy
        channelStatusAttribute.set(true);
        channelServletRequestHandler.handle(handler -> handle(channel, request));
    }

    /**
     * url 匹配
     * 支持*通配符:
     * 1./a/b/* 匹配 /a/b/c/d/e
     * 2./a/`*`/d/e 匹配 /a/b/c/d/e
     * 3.全匹配
     *
     * @param source 注册的url
     * @param target 请求的url
     */
    private boolean pathMatched(String source, String target) {
        String[] sourceSplits = source.split("/");
        String[] targetSplits = target.split("/");

        if (sourceSplits.length == 0) {
            // url = '/', 适合所有url
            return true;
        }

        //source index
        int si = 0;
        for (int ti = 0; ti < targetSplits.length; ti++) {
            if (si >= sourceSplits.length) {
                //请求的url比较长
                return false;
            }
            String sourceItem = sourceSplits[si];
            String targetItem = targetSplits[ti];
            if (HttpServerConstants.URL_ALL_MATCH.equals(sourceItem)) {
                //遇到*通配符, 直接返回匹配
                int nextSi = si + 1;
                if (nextSi >= sourceSplits.length) {
                    //url最后一个item, 则直接返回true
                    return true;
                } else if (sourceSplits[nextSi].equals(targetItem)) {
                    //url下一个item匹配
                    si = nextSi + 1;
                }
                continue;
            }
            if (!sourceItem.equals(targetItem)) {
                //url某个path不匹配, 就不匹配
                return false;
            }
        }

        return true;
    }

    /**
     * 获取url映射匹配的filter
     */
    private List<Filter> matchedFilters(String path) {
        Queue<KinHttpServer.FilterConfig> filterConfigs = new LinkedList<>(kinHttpServer.getFilterConfigs());
        List<Filter> matchedFilters = new ArrayList<>(filterConfigs.size());
        for (KinHttpServer.FilterConfig filterConfig : filterConfigs) {
            if (!pathMatched(filterConfig.getPath(), path)) {
                continue;
            }

            matchedFilters.add(filterConfig.instance());
        }

        return matchedFilters;
    }

    /**
     * 获取匹配的Servlet, 按配置顺序, 谁先匹配到, 谁就用于处理逻辑
     */
    private Servlet matchedServlet(String path, RequestMethod method) {
        Queue<KinHttpServer.ServletConfig> servletConfigs = kinHttpServer.getServletConfigs();
        KinHttpServer.ServletConfig matchedServletConfig = null;
        for (KinHttpServer.ServletConfig servletConfig : servletConfigs) {
            RequestMethod cfgMethod = servletConfig.getMethod();
            if (pathMatched(servletConfig.getPath(), path) && (cfgMethod == null || cfgMethod.equals(method))) {
                matchedServletConfig = servletConfig;
                break;
            }
        }

        if (Objects.nonNull(matchedServletConfig)) {
            return matchedServletConfig.instance();
        }

        throw new ServletException(String.format("can't find matched servlet, path path is '%s'", path));
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
            URI uri = httpUrl.uri();
            String path = uri.getPath();
            List<Filter> filters = matchedFilters(path);

            //顺序filter
            for (Filter filter : filters) {
                filter.doFilter(request, response);
                //filter中设置了response, 故后续filter都不作任何处理
                if (response.getStatusCode() > 0) {
                    break;
                }
            }

            if (response.getStatusCode() <= 0) {
                //filter中没有设置response
                RequestMethod method = RequestMethod.valueOf(request.getMethod().name());
                Servlet matchedServlet = matchedServlet(path, method);
                //servlet 处理
                matchedServlet.service(request, response);
            }
        } catch (Exception e) {
            handleException(channel, response, e);
            error("", e);
        } finally {
            channel.writeAndFlush(response);
            //设置channel空闲
            Attribute<Boolean> channelStatusAttribute = channel.attr(CHANNEL_STATUS_KEY);
            channelStatusAttribute.set(false);
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

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cause.printStackTrace(new PrintWriter(baos));

        buffer.writeBytes(StandardCharsets.UTF_8.encode(baos.toString()));

        response.setResponseBody(HttpResponseBody.of(buffer, MediaType.PLAIN_TEXT.transfer(StandardCharsets.UTF_8.name())));
    }

    //--------------------------------------------------------------------------------------------------------------

    /**
     * 每个channel一条线程处理逻辑
     */
    private static class ChannelServletRequestHandler extends PinnedThreadSafeHandler<ChannelServletRequestHandler> {
        public ChannelServletRequestHandler() {
            super(EXECUTION_CONTEXT);
        }
    }
}
