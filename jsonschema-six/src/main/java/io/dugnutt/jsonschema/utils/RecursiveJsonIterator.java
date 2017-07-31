package io.dugnutt.jsonschema.utils;

import io.dugnutt.jsonschema.six.JsonPath;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;

public class RecursiveJsonIterator {
    public static void visitDocument(JsonObject object, Visitor iterator) {
        checkNotNull(object, "object must not be null");
        checkNotNull(iterator, "iterator must not be null");
        final JsonPath rootPath = JsonPath.rootPath();
        visitObject(object, rootPath, iterator);
    }

    static void visitArray(JsonArray array, JsonPath path, Visitor iterator) {
        AtomicInteger idx = new AtomicInteger();
        array.forEach(v -> {
            final int currIdx = idx.getAndIncrement();
            iterator.visitProperty(currIdx, array, path);
            // iterator.visitArrayElement(currIdx, v, path);
            if (v.getValueType() == JsonValue.ValueType.OBJECT) {
                visitObject(v.asJsonObject(), path.child(currIdx), iterator);
            } else if (v.getValueType() == JsonValue.ValueType.ARRAY) {
                visitArray(v.asJsonArray(), path.child(currIdx), iterator);
            }
        });
    }

    static void visitObject(JsonObject object, JsonPath path, Visitor iterator) {
        object.forEach((k, v) -> {
            iterator.visitProperty(k, v, path);
            if (v.getValueType() == JsonValue.ValueType.OBJECT) {
                visitObject(v.asJsonObject(), path.child(k), iterator);
            } else if (v.getValueType() == JsonValue.ValueType.ARRAY) {
                visitArray(v.asJsonArray(), path.child(k), iterator);
            }
        });
    }

    @FunctionalInterface
    public interface Visitor {
        void visitProperty(Object key, JsonValue value, JsonPath path);
    }
}
