package org.everit.json;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
    private final List<JsonPathPart> path;

    public static JsonPath jsonPath(String... path) {
        return new JsonPath(Arrays.stream(path)
                .map(JsonPathPart::new)
                .collect(Collectors.toList()));
    }

    public static JsonPath jsonPath(List<String> base, String name) {
        checkNotNull(base, "base must not be null");
        List<JsonPathPart> baseParts = base.stream().map(JsonPathPart::new).collect(Collectors.toList());
        baseParts.add(new JsonPathPart(name));
        return new JsonPath(baseParts);
    }

    public static JsonPath rootPath() {
        return new JsonPath(emptyList());
    }

    public Object[] toArray() {
        return path.stream().map(JsonPathPart::getNameOrIndex).toArray();
    }

    public JsonPath child(String pathToPush) {
        Preconditions.checkNotNull(pathToPush, "pathToPush must not be null");
        List<JsonPathPart> newPath = new ArrayList<>(this.path);
        newPath.add(new JsonPathPart(pathToPush));
        return new JsonPath(newPath);
    }

    public JsonPath child(int index) {
        List<JsonPathPart> newPath = new ArrayList<>(this.path);
        newPath.add(new JsonPathPart(index));
        return new JsonPath(newPath);
    }

    @Value
    @AllArgsConstructor
    private static class JsonPathPart {
        private final String name;
        private final Integer index;

        public JsonPathPart(String name) {
            this(name, null);
        }

        public JsonPathPart(Integer index) {
            this(null, index);
        }

        public boolean isRoot() {
            return name == null && index == null;
        }

        public Object getNameOrIndex() {
            return name != null ? name : index;
        }
    }
}
