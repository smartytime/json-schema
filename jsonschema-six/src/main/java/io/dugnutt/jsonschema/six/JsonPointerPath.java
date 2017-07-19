package io.dugnutt.jsonschema.six;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import lombok.SneakyThrows;

import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class JsonPointerPath {

    private final URI uriFragment;
    private final JsonPath path;

    @SneakyThrows
    public JsonPointerPath(String uriFragment) {
        this.uriFragment = URI.create(checkNotNull(uriFragment));
        checkArgument(uriFragment.startsWith("#"), "URI fragment must start with #");
        if (uriFragment.length() > 1) {
            checkArgument(uriFragment.startsWith("#/"));
        }

        String decodedURL = URLDecoder.decode(uriFragment, "UTF-8");

        String[] path = decodedURL.split("/");
        LinkedList<String> pathList = Arrays.stream(path)
                .map(JsonPointerPath::unescape)
                .collect(Collectors.toCollection(LinkedList::new));
        pathList.pop(); //Remove the #

        this.path = JsonPath.jsonPath(pathList.toArray(new String[0]));
    }

    @SneakyThrows
    public JsonPointerPath(JsonPath path) {
        this.path = path;
        this.uriFragment = URI.create(Streams.concat(Stream.of("#"), Arrays.stream(path.toArray()))
                .map(String::valueOf)
                .map(JsonPointerPath::escape)
                .collect(Collectors.joining("/")));
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

    @VisibleForTesting
    public List<String> jsonPathParts() {
        return jsonPath().getPath().stream()
                .map(JsonPath.PathPart::getNameOrIndex)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    public URI toURIFragment() {
        return uriFragment;
    }

    public static String unescape(String token) {
        return token.replace("~1", "/").replace("~0", "~").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    public static String escape(String token) {
        return token.replace("~", "~0").replace("/", "~1").replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
