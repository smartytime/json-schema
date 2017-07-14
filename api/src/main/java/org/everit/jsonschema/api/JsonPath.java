package org.everit.jsonschema.api;

import com.google.common.primitives.Ints;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.emptyList;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonPath {
    @NonNull
    @Getter
    private final List<PathPart> path;

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

    public Object[] toArray() {
        return path.stream().map(PathPart::getNameOrIndex).toArray();
    }

    @Value
    private static class PathPart {
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
    }
}
