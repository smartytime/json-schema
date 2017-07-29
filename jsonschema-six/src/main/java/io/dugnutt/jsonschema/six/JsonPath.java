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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static io.dugnutt.jsonschema.six.CharUtils.escapeForJsonPointerSegment;
import static io.dugnutt.jsonschema.six.CharUtils.forwardSlashSeparator;
import static io.dugnutt.jsonschema.six.CharUtils.jsonPointerSegmentEscaper;
import static io.dugnutt.jsonschema.six.CharUtils.jsonPointerSegmentUnescaper;
import static io.dugnutt.jsonschema.six.CharUtils.urlSegmentUnescaper;

public class JsonPath {

    @NonNull
    @Getter
    private final List<PathPart> path;

    private URI uriFragment;

    private String jsonPointerString;

    /**
     * This method ingests a path-separated string intended as a json-pointer.  The string may be based on a URL fragment,
     * and as such may contain escape sequences, (such as %25 to escape /).
     * <p>
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

        this.path = forwardSlashSeparator().splitToList(input).stream()
                .map(rawPart -> {
                    String toUnescape = rawPart;
                    for (Unescaper unescaper : unescapers) {
                        toUnescape = unescaper.unescape(toUnescape);
                    }

                    final String pathPart = jsonPointerSegmentUnescaper().unescape(toUnescape);

                    //Re-escape, because we can't rely on what was escaped coming in.
                    String pointerEscaped = jsonPointerSegmentEscaper().escape(pathPart);

                    jsonPointer.append("/").append(pointerEscaped);
                    fragmentURI.append("/").append(urlPathSegmentEscaper().escape(pointerEscaped));

                    return new PathPart(pathPart);
                }).collect(StreamUtils.toImmutableList());

        this.uriFragment = URI.create(fragmentURI.toString());
        this.jsonPointerString = jsonPointer.toString();
    }

    JsonPath(List<PathPart> parts, Object toBeAppended) {
        checkNotNull(parts, "parts must not be null");
        checkNotNull(toBeAppended, "toBeAppended must not be null");

        List<PathPart> tmp = new ArrayList<>(parts);
        if (toBeAppended instanceof Integer) {
            tmp.add(new PathPart((int) toBeAppended));
        } else {
            tmp.add(new PathPart(toBeAppended.toString()));
        }
        this.path = Collections.unmodifiableList(tmp);
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

        return new JsonPath(uriFragment.substring(1), urlSegmentUnescaper());
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
        return new JsonPath(this.path, unescapedPath);
    }

    public JsonPath child(int index) {
        return new JsonPath(this.path,index);
    }

    public Optional<String> getLastPath() {
        if (path.size() > 0) {
            return Optional.of(String.valueOf(path.get(path.size() - 1).getNameOrIndex()));
        } else {
            return Optional.empty();
        }
    }

    public String toString() {
        return toURIFragment().toString();
    }

    public List<String> toStringPath() {
        return path.stream()
                .map(PathPart::toString)
                .collect(StreamUtils.toImmutableList());
    }

    public URI toURIFragment() {
        if (this.uriFragment == null) {
            StringBuilder uriFragment = new StringBuilder("#");
            for (PathPart pathPart : path) {
                String escaped = CharUtils.escapeForURIPointerSegment(pathPart.toString());
                uriFragment.append("/").append(escaped);
            }
            this.uriFragment = URI.create(uriFragment.toString());
        }
        return this.uriFragment;
    }

    public String toJsonPointer() {
        if (this.jsonPointerString == null) {
            StringBuilder jsonPointer = new StringBuilder("");
            for (PathPart pathPart : path) {
                String segment = String.valueOf(pathPart.getNameOrIndex());
                jsonPointer.append("/").append(escapeForJsonPointerSegment(segment));
            }
            this.jsonPointerString = jsonPointer.toString();
        }
        return this.jsonPointerString;
    }

    @Value
    static class PathPart {
        private final String name;
        private final Integer index;

        public PathPart(String name) {
            checkNotNull(name, "name must not be null");
            this.index = Ints.tryParse(name);
            this.name = this.index == null ? name : null;
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
