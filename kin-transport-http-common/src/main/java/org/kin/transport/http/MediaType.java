package org.kin.transport.http;

/**
 * @author huangjianqin
 * @date 2022/1/1
 */
public final class MediaType {
    /**
     * Public constant media type that includes all media ranges (i.e. "&#42;/&#42;").
     */
    public static final String ALL = "*/*";

    /**
     * Public constant media type for {@code application/atom+xml}.
     */
    public static final String APPLICATION_ATOM_XML = "application/atom+xml";

    /**
     * Public constant media type for {@code application/cbor}.
     */
    public static final String APPLICATION_CBOR = "application/cbor";

    /**
     * Public constant media type for {@code application/x-www-form-urlencoded}.
     */
    public static final String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded";

    /**
     * Public constant media type for {@code application/json}.
     */
    public static final String APPLICATION_JSON = "application/json";

    /**
     * Public constant media type for {@code application/octet-stream}.
     */
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    /**
     * Public constant media type for {@code application/pdf}.
     */
    public static final String APPLICATION_PDF = "application/pdf";

    /**
     * Public constant media type for {@code application/problem+json}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7807#section-6.1">
     * Problem Details for HTTP APIs, 6.1. application/problem+json</a>
     */
    public static final String APPLICATION_PROBLEM_JSON = "application/problem+json";

    /**
     * Public constant media type for {@code application/problem+xml}.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7807#section-6.2">
     * Problem Details for HTTP APIs, 6.2. application/problem+xml</a>
     */
    public static final String APPLICATION_PROBLEM_XML = "application/problem+xml";

    /**
     * Public constant media type for {@code application/rss+xml}.
     */
    public static final String APPLICATION_RSS_XML = "application/rss+xml";

    /**
     * Public constant media type for {@code application/x-ndjson}.
     */
    public static final String APPLICATION_NDJSON = "application/x-ndjson";

    /**
     * Public constant media type for {@code application/xhtml+xml}.
     */
    public static final String APPLICATION_XHTML_XML = "application/xhtml+xml";

    /**
     * Public constant media type for {@code application/xml}.
     */
    public static final String APPLICATION_XML = "application/xml";

    /**
     * Public constant media type for {@code image/gif}.
     */
    public static final String IMAGE_GIF = "image/gif";

    /**
     * Public constant media type for {@code image/jpeg}.
     */
    public static final String IMAGE_JPEG = "image/jpeg";

    /**
     * Public constant media type for {@code image/png}.
     */
    public static final String IMAGE_PNG = "image/png";

    /**
     * Public constant media type for {@code multipart/form-data}.
     */
    public static final String MULTIPART_FORM_DATA = "multipart/form-data";

    /**
     * Public constant media type for {@code multipart/mixed}.
     */
    public static final String MULTIPART_MIXED = "multipart/mixed";

    /**
     * Public constant media type for {@code multipart/related}.
     */
    public static final String MULTIPART_RELATED = "multipart/related";

    /**
     * Public constant media type for {@code text/event-stream}.
     */
    public static final String TEXT_EVENT_STREAM = "text/event-stream";

    /**
     * Public constant media type for {@code text/html}.
     */
    public static final String TEXT_HTML = "text/html";

    /**
     * Public constant media type for {@code text/markdown}.
     */
    public static final String TEXT_MARKDOWN = "text/markdown";

    /**
     * Public constant media type for {@code text/plain}.
     */
    public static final String TEXT_PLAIN = "text/plain";

    /**
     * Public constant media type for {@code text/xml}.
     */
    public static final String TEXT_XML = "text/xml";

    private static final String PARAM_QUALITY_FACTOR = "q";
}
