package org.everit.json;

import com.google.common.base.Preconditions;
import com.google.common.collect.Streams;
import com.google.common.primitives.Ints;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

import javax.json.JsonException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class JsonPath {
    @NonNull
    @Getter
    private final PathPart[] path;

    private JsonPath(String... path) {
        this.path = Arrays.stream(path).map(PathPart::new).toArray(PathPart[]::new);
    }

    private JsonPath(PathPart... path) {
        this.path = path;
    }

    private JsonPath() {
        this.path = new PathPart[0];
    }

    public static JsonPath jsonPath(String... path) {
        return new JsonPath(path);
    }

    public static JsonPath jsonPath(List<String> base, String name) {
        checkNotNull(base, "base must not be null");
        Preconditions.checkNotNull(name, "name must not be null");
        return new JsonPath(Streams.concat(base.stream(), Stream.of(name)).toArray(String[]::new));
    }

    public static JsonPath rootPath() {
        return new JsonPath();
    }

    public JsonPath child(int index) {
        PathPart[] newPath = Arrays.copyOf(path, path.length + 1);
        newPath[path.length] = new PathPart(index);
        return new JsonPath(newPath);
    }

    public JsonPath child(String pathToPush) {
        PathPart[] newPath = Arrays.copyOf(path, path.length + 1);
        newPath[path.length] = new PathPart(pathToPush);
        return new JsonPath(newPath);
    }

    public Object[] getBasePath() {
        if (path.length > 1) {
            return Arrays.stream(path, 0, path.length - 2)
                    .map(PathPart::getNameOrIndex)
                    .toArray();
        } else {
            return new Object[0];
        }
    }

    public PathPart[] getBasePathParts() {
        if (path.length > 1) {
            return Arrays.copyOf(path, path.length - 2);
        } else {
            return new PathPart[0];
        }
    }

    public boolean isRoot() {
        return path.length == 0;
    }
    
    public PathPart getLastPath() {
        if (path.length == 0) {
            throw new JsonException("This is the root path - there is no path.");
        }
        return path[path.length - 1];
    }

    public Object[] toArray() {
        return Arrays.stream(path).map(PathPart::getNameOrIndex).toArray();
    }

    @Value
    public static class PathPart {
        private final String name;
        private final Integer index;

        private PathPart(String name) {
            checkNotNull(name, "name must not be null");
            checkArgument(!"".equals(name), "name must not be blank");

            Integer integer = Ints.tryParse(name);
            if (integer == null) {
                this.name = name;
                this.index = null;
            } else {
                this.name = null;
                this.index = integer;
            }
        }

        private PathPart(Integer index) {
            checkNotNull(index, "index must not be null");
            this.name = null;
            this.index = index;
        }

        public int getIndex() {
            checkNotNull(index != null, "Index can't be null");
            return index;
        }

        public int getName() {
            checkNotNull(name != null, "Name can't be null");
            return index;
        }

        public Object getNameOrIndex() {
            return name != null ? name : index;
        }

        public boolean isIndex() {
            return index != null;
        }

        public boolean isName() {
            return name != null;
        }
    }
}
