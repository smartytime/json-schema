package io.dugnutt.jsonschema.six;

import lombok.SneakyThrows;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

public class URIUtils {

    @SneakyThrows
    public static URI withoutFragment(URI uri) {
        checkNotNull(uri, "uri must not be null");
        if ("".equals(uri.getFragment())) {
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(),
                    uri.getPath(), uri.getQuery(), null);
        }
        return uri;
    }
}
