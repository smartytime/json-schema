package org.martysoft.jsonschema.v6;

import com.google.common.collect.Streams;
import lombok.SneakyThrows;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class JsonPointerPath {

    private final String uriFragment;
    private final JsonPath path;

    @SneakyThrows
    public JsonPointerPath(String uriFragment) {
        this.uriFragment = checkNotNull(uriFragment);
        checkArgument(uriFragment.startsWith("#"), "JSON Pointer must start with #");
        if (uriFragment.length() > 1) {
            checkArgument(uriFragment.startsWith("#/"));
        }

        String decodedURL = URLDecoder.decode(uriFragment, "UTF-8");

        String[] path = decodedURL.split("/");
        LinkedList<String> pathList = Arrays.stream(path)
                .map(this::unescape)
                .collect(Collectors.toCollection(LinkedList::new));
        pathList.pop(); //Remove the #

        this.path = JsonPath.jsonPath(pathList.toArray(new String[0]));
    }

    @SneakyThrows
    public JsonPointerPath(JsonPath path) {
        this.path = path;
        this.uriFragment = Streams.concat(Stream.of("#"), Arrays.stream(path.toArray()))
                .map(String::valueOf)
                .map(this::escape)
                .collect(Collectors.joining("/"));
    }

    public JsonPointerPath child(String name) {
        return new JsonPointerPath(path.child(name));
    }

    public JsonPointerPath child(int index) {
        return new JsonPointerPath(path.child(index));
    }

    public JsonPath jsonPath() {
        return path;
    }

    public String toURIFragment() {
        return uriFragment;
    }

    private String unescape(String token) {
        return token.replace("~1", "/").replace("~0", "~").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private String escape(String token) {
        return token.replace("~", "~0").replace("/", "~1").replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
