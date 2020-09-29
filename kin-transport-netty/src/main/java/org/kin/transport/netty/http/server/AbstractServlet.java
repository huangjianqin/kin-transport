package org.kin.transport.netty.http.server;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import org.kin.framework.log.LoggerOprs;
import org.kin.framework.utils.StringUtils;
import org.kin.transport.netty.http.HttpResponseBody;
import org.kin.transport.netty.http.MediaType;
import org.kin.transport.netty.http.MediaTypeWrapper;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 提供给开发者实现的抽象类
 *
 * @author huangjianqin
 * @date 2020/9/14
 */
public abstract class AbstractServlet implements Servlet, LoggerOprs {
    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    /** 5min cache */
    public static final int HTTP_CACHE_SECONDS = (int) TimeUnit.MINUTES.toSeconds(5);

    @Override
    public final void service(ServletRequest request, ServletResponse response) {
        try {
            HttpMethod method = request.getMethod();
            if (HttpMethod.GET.equals(method)) {
                handleReturn(doGet(request, response), request, response);
            } else if (HttpMethod.POST.equals(method)) {
                handleReturn(doPost(request, response), request, response);
            } else if (HttpMethod.DELETE.equals(method)) {
                doDelete(request, response);
                response.setStatusCode(ServletResponse.SC_OK);
            } else if (HttpMethod.PUT.equals(method)) {
                doPut(request, response);
                response.setStatusCode(ServletResponse.SC_OK);
            }
        } catch (Exception e) {
            response.setStatusCode(ServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setResponseBody(MediaType.PLAIN_TEXT.toResponseBody(e.getMessage(), StandardCharsets.UTF_8));
            log().error("", e);
        }
    }

    /**
     * 处理servlet处理方法的返回值
     */
    private void handleReturn(Object returnObj, ServletRequest request, ServletResponse response) {
        if (returnObj instanceof String) {
            String content = (String) returnObj;

            //尝试去定位资源
            URL resource = getClass().getClassLoader().getResource(content);
            if (Objects.nonNull(resource)) {
                try {
                    File file = new File(resource.getFile());
                    long lastModified = file.lastModified();

                    //检查资源上次修改时间与ifModifiedSince字段一致, 则说明资源没有修改过, 不用返回
                    String ifModifiedSince = request.getHeaders().get(HttpHeaderNames.IF_MODIFIED_SINCE.toString());
                    if (StringUtils.isNotBlank(ifModifiedSince)) {
                        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
                        Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

                        long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
                        long fileLastModifiedSeconds = lastModified / 1000;
                        if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                            response.setStatusCode(ServletResponse.SC_NOT_MODIFIED);
                            return;
                        }
                    }

                    //set cache
                    SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
                    dateFormatter.setTimeZone(TimeZone.getDefault());
                    // Date header
                    Calendar time = new GregorianCalendar();
                    response.getHeaders().add(HttpHeaderNames.DATE.toString(), dateFormatter.format(time.getTime()));
                    // Add cache headers
                    time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
                    response.getHeaders().add(HttpHeaderNames.EXPIRES.toString(), dateFormatter.format(time.getTime()));
                    response.getHeaders().add(HttpHeaderNames.CACHE_CONTROL.toString(), "private, max-age=" + HTTP_CACHE_SECONDS);
                    response.getHeaders().add(HttpHeaderNames.LAST_MODIFIED.toString(), dateFormatter.format(new Date(lastModified)));

                    OutputStream outputStream = response.getOutputStream();
                    InputStream inputStream = resource.openStream();
                    try {
                        //每16k bytes 写一次
                        byte[] chunked = new byte[1024 * 16];
                        int readNum;
                        while ((readNum = inputStream.read(chunked)) > 0) {
                            outputStream.write(chunked, 0, readNum);
                        }
                        outputStream.flush();
                    } finally {
                        inputStream.close();
                    }

                    //set content type
                    MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

                    HttpResponseBody responseBody = response.getResponseBody();
                    response.setResponseBody(
                            HttpResponseBody.of(
                                    responseBody.getBuf(),
                                    new MediaTypeWrapper(mimeTypesMap.getContentType(file), StandardCharsets.UTF_8.name())));
                } catch (Exception e) {
                    response.setResponseBody(MediaType.PLAIN_TEXT.toResponseBody(e.getMessage(), StandardCharsets.UTF_8));
                    log().error("", e);
                }
            } else {
                //没有资源, 则直接当成字符串返回
                response.setResponseBody(MediaType.PLAIN_TEXT.toResponseBody(returnObj.toString(), StandardCharsets.UTF_8));
            }
        } else if (returnObj instanceof Map) {
            Map<String, Object> respContent =
                    ((Map<Object, Object>) returnObj).entrySet()
                            .stream()
                            .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue()));
            response.setResponseBody(MediaType.JSON.toResponseBody(respContent, StandardCharsets.UTF_8));
        }

        response.setStatusCode(ServletResponse.SC_OK);
    }

    //------------------------------------------------------------------------------------------------------

    /**
     * 处理get请求
     */
    protected Object doGet(ServletRequest request, ServletResponse response) {
        return null;
    }

    /**
     * 处理post请求
     */
    protected Object doPost(ServletRequest request, ServletResponse response) {
        return null;
    }

    /**
     * 处理delete请求
     */
    protected void doDelete(ServletRequest request, ServletResponse response) {
    }

    /**
     * 处理put请求
     */
    protected void doPut(ServletRequest request, ServletResponse response) {
    }
}
