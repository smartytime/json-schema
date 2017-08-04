package io.sbsp.jsonschema;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import io.sbsp.jsonschema.utils.CharUtils;
import io.sbsp.jsonschema.utils.Unescaper;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.net.UrlEscapers.urlPathSegmentEscaper;
import static io.sbsp.jsonschema.utils.CharUtils.escapeForJsonPointerSegment;
import static io.sbsp.jsonschema.utils.CharUtils.forwardSlashSeparator;
import static io.sbsp.jsonschema.utils.CharUtils.jsonPointerSegmentEscaper;
import static io.sbsp.jsonschema.utils.CharUtils.jsonPointerSegmentUnescaper;
import static io.sbsp.jsonschema.utils.CharUtils.urlSegmentUnescaper;

public class JsonPath {

    @NonNull
    private final String[] segments;

    private URI uriFragment;

    private String jsonPointerString;

    /**
     * This method ingests a segments-separated string intended as a json-pointer.  The string may be based on a URL fragment,
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
        checkArgument(input.isEmpty() || input.startsWith("/"), "invalid json-pointer syntax.  Must either be blank or start with a /");

        StringBuilder fragmentURI = new StringBuilder("#");
        StringBuilder jsonPointer = new StringBuilder("");

        List<String> parts = new ArrayList<>();
        for (String rawPart : forwardSlashSeparator().split(input)) {
            if (Strings.isNullOrEmpty(rawPart) && !parts.isEmpty()) {
                throw new IllegalArgumentException("invalid blank segment in json-pointer");
            } else if (Strings.isNullOrEmpty(rawPart)) {
                continue;
            }

            String toUnescape = rawPart;
            for (Unescaper unescaper : unescapers) {
                toUnescape = unescaper.unescape(toUnescape);
            }

            final String pathPart = jsonPointerSegmentUnescaper().unescape(toUnescape);

            //Re-escape, because we can't rely on what was escaped coming in.
            String pointerEscaped = jsonPointerSegmentEscaper().escape(pathPart);

            jsonPointer.append("/").append(pointerEscaped);
            fragmentURI.append("/").append(urlPathSegmentEscaper().escape(pointerEscaped));

            parts.add(pathPart);
        }
        this.segments = parts.toArray(new String[parts.size()]);
        this.uriFragment = URI.create(fragmentURI.toString());
        this.jsonPointerString = jsonPointer.toString();
    }

    JsonPath(String[] parts, int toBeAppended) {
        checkNotNull(parts, "parts must not be null");
        this.segments = Arrays.copyOf(parts, parts.length + 1);
        this.segments[parts.length] = String.valueOf(toBeAppended);
    }

    JsonPath(String[] parts, String... toBeAppended) {
        checkNotNull(parts, "parts must not be null");
        checkNotNull(toBeAppended, "toBeAppended must not be null");
        int partsLength = parts.length;
        this.segments = Arrays.copyOf(parts, partsLength + toBeAppended.length);

        for (int i = 0; i < toBeAppended.length; i++) {
            this.segments[partsLength + i] = toBeAppended[i];
        }
    }

    JsonPath(String[] parts, String toBeAppended) {
        checkNotNull(toBeAppended, "toBeAppended must not be null");
        checkNotNull(parts, "parts must not be null");
        this.segments = Arrays.copyOf(parts, parts.length + 1);
        this.segments[parts.length] = toBeAppended;
    }

    public String getLastPath() {
        final int length = this.segments.length;
        if (length > 0) {
            return this.segments[length - 1];
        } else {
            return null;
        }
    }

    public String getFirstPath() {
        final int length = this.segments.length;
        if (length > 0) {
            return this.segments[0];
        } else {
            return null;
        }
    }

    public JsonPath child(String unescapedPath) {
        checkNotNull(unescapedPath, "unescapedPath must not be null");
        return new JsonPath(this.segments, unescapedPath);
    }

    public JsonPath child(String... unescapedPath) {
        checkNotNull(unescapedPath, "unescapedPath must not be null");
        return new JsonPath(this.segments, unescapedPath);
    }

    public JsonPath child(int index) {
        return new JsonPath(this.segments, index);
    }

    public String toJsonPointer() {
        if (this.jsonPointerString == null) {
            StringBuilder jsonPointer = new StringBuilder("");
            for (String segment : segments) {
                jsonPointer.append("/").append(escapeForJsonPointerSegment(segment));
            }
            this.jsonPointerString = jsonPointer.toString();
        }
        return this.jsonPointerString;
    }

    public String toString() {
        return toURIFragment().toString();
    }

    public List<String> toStringPath() {
        return Arrays.asList(this.segments);
    }

    public URI toURIFragment() {
        if (this.uriFragment == null) {
            StringBuilder uriFragment = new StringBuilder("#");
            for (String pathPart : segments) {
                String escaped = CharUtils.escapeForURIPointerSegment(pathPart);
                uriFragment.append("/").append(escaped);
            }
            this.uriFragment = URI.create(uriFragment.toString());
        }
        return this.uriFragment;
    }

    public String toString(Joiner joiner) {
        return joiner.join(segments);
    }

    public void forEach(Consumer<String> consumer) {
        for (String segment : segments) {
            consumer.accept(segment);
        }
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
}
