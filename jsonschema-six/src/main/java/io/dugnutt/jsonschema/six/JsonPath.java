package io.dugnutt.jsonschema.six;

import com.google.common.base.Strings;
import com.google.common.primitives.Ints;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonPath {
    @NonNull
    @Getter
    private final List<PathPart> path;

    @SneakyThrows
    public JsonPath(String jsonPointer) {
        checkArgument(jsonPointer.startsWith("/"), "JSON pointer must start with /");

        final String[] parts = jsonPointer.split("/");
        if (parts.length > 0) {
            this.path = unmodifiableList(Arrays.stream(parts, 1, parts.length)
                    .map(PathPart::new)
                    .collect(Collectors.toList()));
        } else {
            this.path = emptyList();
        }
    }

    public static JsonPath jsonPath(String... path) {
        return new JsonPath(Arrays.stream(path)
                .map(PathPart::new)
                .collect(Collectors.toList()));
    }

    public static JsonPath jsonPath(List<String> base, String name) {
        checkNotNull(base, "base must not be null");
        List<PathPart> baseParts = base.stream().map(PathPart::new).collect(Collectors.toList());
        baseParts.add(new PathPart(name));
        return new JsonPath(baseParts);
    }

    public static JsonPath parse(String jsonPointer) {
        return new JsonPath(jsonPointer);
    }

    public List<String> toStringPath() {
        return path.stream()
                .map(PathPart::toString)
                .collect(Collectors.toList());
    }

    public static JsonPath parseFromURIFragment(String uriFragment) {
        checkNotNull(uriFragment, "uriFragment must not be null");
        uriFragment = sanitizeURIAsPointer(uriFragment);

        return parse(uriFragment);
    }

    public static JsonPath rootPath() {
        return new JsonPath(emptyList());
    }

    public JsonPath child(String pathToPush) {
        checkNotNull(pathToPush, "pathToPush must not be null");
        List<PathPart> newPath = new ArrayList<>(this.path);
        newPath.add(new PathPart(pathToPush));
        return new JsonPath(newPath);
    }

    public JsonPath child(int index) {
        List<PathPart> newPath = new ArrayList<>(this.path);
        newPath.add(new PathPart(index));
        return new JsonPath(newPath);
    }

    public Optional<String> getLastPath() {
        if (path.size() > 0) {
            return Optional.of(String.valueOf(path.get(path.size() - 1).getNameOrIndex()));
        } else {
            return Optional.empty();
        }
    }

    public Object[] toArray() {
        return path.stream().map(PathPart::getNameOrIndex).toArray();
    }

    public String toJsonPointer() {
        return path.isEmpty() ? "" : "/" + path.stream()
                .map(PathPart::getNameOrIndex)
                .map(Object::toString)
                .map(JsonPointerPath::escape)
                .collect(Collectors.joining("/"));
    }

    public String toString() {
        return toURIFragment();
    }

    public String toURIFragment() {
        return "#" + toJsonPointer();
    }

    @SneakyThrows
    static String sanitizeURIAsPointer(String fragment) {
        fragment = URLDecoder.decode(fragment, "UTF-8");
        checkArgument("".equals(fragment) || "#".equals(fragment) || fragment.startsWith("#/"), "URI Fragment invalid: " + fragment);
        if (Strings.isNullOrEmpty(fragment) || "#".equals(fragment)) {
            return "/";
        } else {
            return fragment.substring(1);
        }
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

        public boolean isRoot() {
            return name == null && index == null;
        }

        public String toString() {
            return getNameOrIndex().toString();
        }
    }
}
