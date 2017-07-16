package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.JsonPointerPath;
import io.dugnutt.jsonschema.six.UnexpectedValueException;
import io.dugnutt.jsonschema.utils.JsonUtils;

import javax.json.JsonValue;
import java.util.function.Function;

public class LoadingUtils {

    /**
     * Performs a cast that will also surface any errors in a friendly way.
     *
     * @param path       The path to the problematic element
     * @param targetType The type we are casting to
     * @param <Y>        Type parameter for the type we want
     * @return Cast of the function's input
     */
    public static <Y extends JsonValue> Function<JsonValue, Y> castTo(Class<Y> targetType, JsonPointerPath path) {
        Class<? extends JsonValue> targetTypeCompilerLikes = targetType;
        return value -> {
            if (!targetType.isAssignableFrom(value.getClass())) {
                final JsonValue.ValueType jsonType = JsonUtils.jsonTypeForClass(targetTypeCompilerLikes);
                throw new UnexpectedValueException(path, value, jsonType);
            } else {
                return (Y) value;
            }
        };
    }
}
