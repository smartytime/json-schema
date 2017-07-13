package org.everit.jsonschema.api;

import org.everit.json.JsonArray;
import org.everit.json.JsonObject;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Deep-equals implementation on primitive wrappers, {@link JsonObject} and {@link JsonArray}.
 */
public final class ObjectComparator {

    private ObjectComparator() {
    }

    /**
     * Deep-equals implementation on primitive wrappers, {@link JsonObject} and {@link JsonArray}.
     *
     * @param obj1 the first object to be inspected
     * @param obj2 the second object to be inspected
     * @return {@code true} if the two objects are equal, {@code false} otherwise
     */
    public static boolean deepEquals(final Object obj1, final Object obj2) {
        if (obj1 instanceof JsonArray) {
            if (!(obj2 instanceof JsonArray)) {
                return false;
            }
            return deepEqualArrays((JsonArray) obj1, (JsonArray) obj2);
        } else if (obj1 instanceof JsonObject) {
            if (!(obj2 instanceof JsonObject)) {
                return false;
            }
            return deepEqualObjects((JsonObject) obj1, (JsonObject) obj2);
        }
        return Objects.equals(obj1, obj2);
    }

    private static boolean deepEqualArrays(final JsonArray arr1, final JsonArray arr2) {
        if (arr1.length() != arr2.length()) {
            return false;
        }
        for (int i = 0; i < arr1.length(); ++i) {
            if (!deepEquals(arr1.get(i), arr2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static Set<String> sortedNamesOf(final JsonObject obj) {
        Set<String> raw = obj.properties();
        if (raw == null) {
            return null;
        }
        return new HashSet<>(raw);
    }

    private static boolean deepEqualObjects(final JsonObject jsonObj1, final JsonObject jsonObj2) {
        Set<String> obj1Names = sortedNamesOf(jsonObj1);

        if (!Objects.equals(obj1Names, sortedNamesOf(jsonObj2))) {
            return false;
        }
        if (obj1Names == null) {
            return true;
        }
        for (String name : obj1Names) {
            if (!deepEquals(jsonObj1.git(name), jsonObj2.git(name))) {
                return false;
            }
        }
        return true;
    }

}
