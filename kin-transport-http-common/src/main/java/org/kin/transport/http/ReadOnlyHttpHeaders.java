package org.kin.transport.http;

import com.google.common.collect.Multimap;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * 仅仅可读的{@link HttpHeaders}实现
 *
 * @author huangjianqin
 * @date 2021/12/29
 */
public final class ReadOnlyHttpHeaders extends HttpHeaders {
    private static final long serialVersionUID = -3094552111056871191L;

    public ReadOnlyHttpHeaders() {
    }

    public ReadOnlyHttpHeaders(Multimap<String, String> keyValues) {
        super(keyValues);
    }

    public ReadOnlyHttpHeaders(Map<String, List<String>> keyValues) {
        super(keyValues);
    }

    public ReadOnlyHttpHeaders(HttpHeaders headers) {
        super(headers);
    }

    @Override
    public void put(String key, @Nullable String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(String key, List<String> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<String, List<String>> keyValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Multimap<String, String> keyValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> set(String key, List<String> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAll(Map<String, List<String>> keyValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> remove(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}
