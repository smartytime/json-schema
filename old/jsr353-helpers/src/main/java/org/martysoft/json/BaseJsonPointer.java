package io.dugnutt.json;

import com.google.common.base.Strings;
import com.google.common.collect.Streams;
import lombok.SneakyThrows;

import javax.json.JsonException;
import javax.json.JsonPointer;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


public class BaseJsonPointer implements JsonPointer {

    private final String uriFragment;
    private final JsonPath path;

    @SneakyThrows
    public BaseJsonPointer(String pointerString) {
        this.uriFragment = checkNotNull(pointerString);
        checkArgument(pointerString.startsWith("/"), String.format("JSON Pointer must start with '/' but was '%s'", pointerString));

        String decodedURL = URLDecoder.decode(pointerString, "UTF-8");

        String[] path = decodedURL.split("/");

        //This chops the "#" off the path and unescapes everything per the spec.
        this.path = JsonPath.jsonPath(
                Arrays.stream(path)
                        .filter(v -> !Strings.isNullOrEmpty(v))
                        .map(this::unescape)
                        .toArray(String[]::new)
        );
    }

    @SneakyThrows
    public BaseJsonPointer(JsonPath path) {
        this.path = path;
        this.uriFragment = Streams.concat(Stream.of("#"), Arrays.stream(path.toArray()))
                .map(String::valueOf)
                .map(this::escape)
                .collect(Collectors.joining("/"));
    }

    /**
     * Adds or replaces a value at the referenced location in the specified
     * {@code target} with the specified {@code value}.
     * <ol>
     * <li>If the reference is the target (empty JSON Pointer string),
     * the specified {@code value}, which must be the same type as
     * specified {@code target}, is returned.</li>
     * <li>If the reference is an array element, the specified {@code value} is inserted
     * into the array, at the referenced index. The value currently at that location, and
     * any subsequent values, are shifted to the right (adds one to the indices).
     * Index starts with 0. If the reference is specified with a "-", or if the
     * index is equal to the size of the array, the value is appended to the array.</li>
     * <li>If the reference is a name/value pair of a {@code JsonObject}, and the
     * referenced value exists, the value is replaced by the specified {@code value}.
     * If the value does not exist, a new name/value pair is added to the object.</li>
     * </ol>
     *
     * @param <T>    the target type, must be a subtype of {@link JsonValue}
     * @param target the target referenced by this {@code JsonPointer}
     * @param value  the value to be added
     * @return the transformed {@code target} after the value is added.
     * @throws NullPointerException if {@code target} is {@code null}
     * @throws JsonException        if the reference is an array element and
     *                              the index is out of range ({@code index < 0 || index > array size}),
     *                              or if the pointer contains references to non-existing objects or arrays.
     */
    @Override
    public <T extends JsonStructure> T add(T target, JsonValue value) {
        checkNotNull(target, "target must not be null");

        getLastStructure(target)
                .ifPresent(jsonStructure ->
                        addNextValue(jsonStructure, path.getLastPath(), value));

        return target;
    }

    /**
     * Removes the value at the reference location in the specified {@code target}.
     *
     * @param <T>    the target type, must be a subtype of {@link JsonValue}
     * @param target the target referenced by this {@code JsonPointer}
     * @return the transformed {@code target} after the value is removed.
     * @throws NullPointerException if {@code target} is {@code null}
     * @throws JsonException        if the referenced value does not exist,
     *                              or if the reference is the target.
     */
    @Override
    public <T extends JsonStructure> T remove(T target) {
        //todo:ericm Finish
        throw new UnsupportedOperationException();
    }

    /**
     * Replaces the value at the referenced location in the specified
     * {@code target} with the specified {@code value}.
     *
     * @param target the target referenced by this {@code JsonPointer}
     * @param value  the value to be stored at the referenced location
     * @return the transformed {@code target} after the value is replaced.
     * @throws NullPointerException if {@code target} is {@code null}
     * @throws JsonException        if the referenced value does not exist,
     *                              or if the reference is the target.
     */
    @Override
    public <T extends JsonStructure> T replace(T target, JsonValue value) {
        if (path.isRoot()) {
            System.out.println();
        }

        JsonStructure lastStructure = getLastStructure(target).orElseThrow(missingValueException());

        JsonValue nextValue = getNextValue(lastStructure, path.getLastPath());
        if (nextValue == null) {
            throw new JsonException("Unable to find value at path: " + this.toURIFragment());
        }
        replaceNextValue(lastStructure, path.getLastPath(), value);
        return target;
    }

    /**
     * Returns {@code true} if there is a value at the referenced location in the specified {@code target}.
     *
     * @param target the target referenced by this {@code JsonPointer}
     * @return {@code true} if this pointer points to a value in a specified {@code target}.
     */
    @Override
    public boolean containsValue(JsonStructure target) {
        return findValue(target).isPresent();
    }

    /**
     * Returns the value at the referenced location in the specified {@code target}.
     *
     * @param target the target referenced by this {@code JsonPointer}
     * @return the referenced value in the target.
     * @throws NullPointerException if {@code target} is null
     * @throws JsonException        if the referenced value does not exist
     */
    @Override
    public JsonValue getValue(JsonStructure target) {
        return findValue(target).orElseThrow(missingValueException());
    }

    private Supplier<JsonException> missingValueException() {
        return () -> new JsonException("Unable to find value at path: " + this.toURIFragment());
    }

    private Optional<JsonValue> findValue(JsonStructure target) {
        checkNotNull(target, "target must not be null");

        if (path.isRoot()) {
            return Optional.of(target);
        }

        return getLastStructure(target)
                .map(jsonStructure -> getNextValue(jsonStructure, path.getLastPath()));
    }

    public BaseJsonPointer child(int index) {
        return new BaseJsonPointer(path.child(index));
    }

    public BaseJsonPointer child(String name) {
        return new BaseJsonPointer(path.child(name));
    }

    private JsonPath jsonPath() {
        return path;
    }

    private String toURIFragment() {
        return uriFragment;
    }

    private Optional<JsonStructure> getLastStructure(JsonStructure target) {
        checkNotNull(target, "target must not be null");
        JsonStructure nextStructure = target;
        for (JsonPath.PathPart part : path.getBasePathParts()) {
            JsonValue nextValue = getNextValue(nextStructure, part);
            if (nextValue == null || nextValue.getValueType() == JsonValue.ValueType.NULL) {
                return Optional.empty();
            }

            if (!(nextValue instanceof JsonStructure)) {
                throw new ClassCastException("Found a non-structure value in the path: " + part.getNameOrIndex());
            }
            nextStructure = (JsonStructure) nextValue;
        }
        return Optional.of(nextStructure);
    }

    private JsonValue getNextValue(JsonStructure current, JsonPath.PathPart nextPath) {
        JsonValue.ValueType valueType = current.getValueType();
        if (valueType == JsonValue.ValueType.ARRAY) {
            if (!nextPath.isIndex()) {
                throw new JsonException("Error evaluating pointer.  Expected an array index, but got a string instead: " + nextPath.getNameOrIndex());
            } else {
                return current.asJsonArray().get(nextPath.getIndex());
            }
        } else {
            String key = nextPath.getNameOrIndex().toString(); //It's technically possible to have a string path that got parsed as a number
            return current.asJsonObject().get(key);
        }
    }

    private void replaceNextValue(JsonStructure current, JsonPath.PathPart nextPath, JsonValue newValue) {
        validateStructureAndPath(current, nextPath);

        if (current.getValueType() == JsonValue.ValueType.ARRAY) {
            current.asJsonArray().set(nextPath.getIndex(), newValue);
        } else {
            String key = nextPath.getNameOrIndex().toString(); //It's technically possible to have a string path that got parsed as a number
            current.asJsonObject().put(key, newValue);
        }
    }

    private void addNextValue(JsonStructure current, JsonPath.PathPart nextPath, JsonValue newValue) {
        validateStructureAndPath(current, nextPath);

        if (current.getValueType() == JsonValue.ValueType.ARRAY) {
            current.asJsonArray().add(nextPath.getIndex(), newValue);
        } else {
            String key = nextPath.getNameOrIndex().toString(); //It's technically possible to have a string path that got parsed as a number
            current.asJsonObject().put(key, newValue);
        }
    }

    private void validateStructureAndPath(JsonStructure structure, JsonPath.PathPart nextPath) {
        JsonValue.ValueType valueType = structure.getValueType();
        if (valueType == JsonValue.ValueType.ARRAY) {
            if (!nextPath.isIndex()) {
                throw new JsonException("Error evaluating pointer.  Expected an array index, but got a string instead: " + nextPath.getNameOrIndex());
            }
        }
    }

    private String unescape(String token) {
        return token.replace("~1", "/").replace("~0", "~").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private String escape(String token) {
        return token.replace("~", "~0").replace("/", "~1").replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
