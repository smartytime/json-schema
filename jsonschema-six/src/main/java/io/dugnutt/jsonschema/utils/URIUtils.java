package io.dugnutt.jsonschema.utils;

import lombok.SneakyThrows;

import java.net.URI;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class URIUtils {

    public static final String SCHEME_AUTOASSIGN = "dugg";
    public static final String SSP_AUTOASSIGN = "autoassign";

    public static URI withNewFragment(URI existing, URI newFragment) {
        checkState(isFragmentOnly(newFragment), "Must only be a fragment");
        return withFragment(existing, newFragment.getFragment());
    }

    public static boolean isFragmentOnly(URI uri) {
        checkNotNull(uri, "uri must not be null");
        return uri.getFragment() != null && CharUtils.areNullOrBlank(
                uri.getQuery(),
                uri.getPath(),
                uri.getSchemeSpecificPart(),
                uri.getQuery());
    }

    public static boolean isJsonPointer(URI uri) {
        if (isFragmentOnly(uri)) {
            final String fragment = uri.getFragment();
            return fragment.equals("") || fragment.startsWith("/");
        }
        return false;
    }

    public static URI trimEmptyFragment(URI uri) {
        checkNotNull(uri, "uri must not be null");
        if ("".equals(uri.getFragment())) {
            return withoutFragment(uri);
        }
        return uri;
    }

    @SneakyThrows
    private static URI withFragment(URI uri, String fragment) {
        checkNotNull(uri, "uri must not be null");
        if (uri.getFragment() == null) {
            return uri;
        }
        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                uri.getPath(), uri.getQuery(), fragment);
    }

    @SneakyThrows
    public static URI withoutFragment(URI uri) {
        return withFragment(uri, null);
    }

    @SneakyThrows
    public static URI generateUniqueURI() {
        return new URI(SCHEME_AUTOASSIGN, "//" + UUID.randomUUID().toString() + "/schema", null);
    }

    @SneakyThrows
    public static boolean isGeneratedURI(URI uri) {
        return SCHEME_AUTOASSIGN.equals(uri.getScheme());
    }
}
