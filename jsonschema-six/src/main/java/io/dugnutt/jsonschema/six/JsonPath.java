package io.dugnutt.jsonschema.six;

import com.google.common.base.Strings;
import com.google.common.primitives.Ints;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static io.dugnutt.jsonschema.six.CharUtils.FORWARD_SLASH_SEPARATOR;
import static io.dugnutt.jsonschema.six.CharUtils.JSON_POINTER_SEGMENT_ESCAPER;
import static io.dugnutt.jsonschema.six.CharUtils.JSON_POINTER_SEGMENT_UNESCAPER;
import static io.dugnutt.jsonschema.six.CharUtils.URL_SEGMENT_UNESCAPER;

public class JsonPath {

    @NonNull
    @Getter
    private final List<PathPart> path;

    @NonNull
    private final URI uriFragment;

    @NonNull
    private final String jsonPointerString;

    /**
     * This method ingests a path-separated string intended as a json-pointer.  The string may be based on a URL fragment,
     * and as such may contain escape sequences, (such as %25 to escape /).
     *
     * This method assumes that any json-pointer segments are escaped.  Any other escaping, eg URL encoding must be specified
     * by providing appropriate unescapers.  Any provided unescapers will be processed before the json-pointer unescapers
     *
     * @param input      Valid escaped json-pointer string
     * @param unescapers Additional unescapers for each segment, ie url segment decoder.
     */
    JsonPath(String input, Unescaper... unescapers) {
        checkNotNull(input, "input must not be null");
        checkArgument(input.isEmpty() || input.startsWith("/"));

        StringBuilder fragmentURI = new StringBuilder("#");
        StringBuilder jsonPointer = new StringBuilder("");

        this.path = FORWARD_SLASH_SEPARATOR.splitToList(input).stream()
                .map(rawPart -> {
                    String toUnescape = rawPart;
                    for (Unescaper unescaper : unescapers) {
                        toUnescape = unescaper.unescape(toUnescape);
                    }

                    final String pathPart = JSON_POINTER_SEGMENT_UNESCAPER.unescape(toUnescape);

                    //Re-escape, because we can't rely on what was escaped coming in.
                    String pointerEscaped = JSON_POINTER_SEGMENT_ESCAPER.escape(pathPart);

                    jsonPointer.append("/").append(pointerEscaped);
                    fragmentURI.append("/").append(urlPathSegmentEscaper().escape(pointerEscaped));

                    return new PathPart(pathPart);
                }).collect(StreamUtils.toImmutableList());

        this.uriFragment = URI.create(fragmentURI.toString());
        this.jsonPointerString = jsonPointer.toString();
    }

    JsonPath(List<PathPart> parts, URI uriFragment, String jsonPointerString, Object newPath) {
        checkNotNull(parts, "parts must not be null");
        checkNotNull(uriFragment, "uriFragment must not be null");
        checkNotNull(jsonPointerString, "jsonPointerString must not be null");
        checkNotNull(newPath, "newPath must not be null");

        List<PathPart> tmp = new ArrayList<>(parts);
        if (newPath instanceof Integer) {
            tmp.add(new PathPart((int) newPath));
        } else {
            tmp.add(new PathPart(newPath.toString()));
        }
        this.path = Collections.unmodifiableList(tmp);

        String newPathVal = newPath.toString();
        final String jsonPointerEscaped = JSON_POINTER_SEGMENT_ESCAPER.escape(newPathVal);
        this.jsonPointerString = jsonPointerString + "/" + jsonPointerEscaped;
        final String urlEscapedPath = urlPathSegmentEscaper().escape(jsonPointerEscaped);
        this.uriFragment = URI.create(uriFragment.toString() + "/" + urlEscapedPath);
    }

    public static JsonPath parseFromURIFragment(URI uriFragment) {
        checkNotNull(uriFragment, "uriFragment must not be null");
        return parseFromURIFragment(uriFragment.toString());
    }

    public static JsonPath parseFromURIFragment(String uriFragment) {
        checkNotNull(uriFragment, "uriFragment must not be null");

        checkArgument("".equals(uriFragment) || "#".equals(uriFragment) || uriFragment.startsWith("#/"),
                "URI Fragment invalid: " + uriFragment);
        if (Strings.isNullOrEmpty(uriFragment) || "#".equals(uriFragment)) {
            return JsonPath.rootPath();
        }

        return new JsonPath(uriFragment.substring(1), URL_SEGMENT_UNESCAPER);
    }

    @SneakyThrows
    public static JsonPath parseJsonPointer(String jsonPointer) {
        checkNotNull(jsonPointer, "jsonPointer must not be null");
        return new JsonPath(jsonPointer);
    }

    public static JsonPath rootPath() {
        return new JsonPath("");
    }

    public JsonPath child(String unescapedPath) {
        checkNotNull(unescapedPath, "unescapedPath must not be null");
        return new JsonPath(this.path, this.uriFragment, this.jsonPointerString, unescapedPath);
    }

    public JsonPath child(int index) {
        return new JsonPath(this.path, this.uriFragment, this.jsonPointerString, index);
    }

    public String toJsonPointer() {
        return jsonPointerString;
    }

    public Optional<String> getLastPath() {
        if (path.size() > 0) {
            return Optional.of(String.valueOf(path.get(path.size() - 1).getNameOrIndex()));
        } else {
            return Optional.empty();
        }
    }

    public URI toURIFragment() {
        return uriFragment;
    }

    public Object[] toArray() {
        return path.stream().map(PathPart::getNameOrIndex).toArray();
    }

    public String toString() {
        return uriFragment.toString();
    }

    public List<String> toStringPath() {
        return path.stream()
                .map(PathPart::toString)
                .collect(Collectors.toList());
    }

    @Value
    static class PathPart {
        private final String name;
        private final Integer index;

        public PathPart(String name) {
            checkNotNull(name, "name must not be null");
            Integer integer = Ints.tryParse(name);
            if (integer == null) {
                this.name = name;
                this.index = null;
            } else {
                this.name = null;
                this.index = integer;
            }
        }

        public PathPart(Integer index) {
            checkNotNull(index, "index must not be null");
            this.name = null;
            this.index = index;
        }

        public Object getNameOrIndex() {
            return name != null ? name : index;
        }

        public String toString() {
            return getNameOrIndex().toString();
        }
    }
}
