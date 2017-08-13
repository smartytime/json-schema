package io.sbsp.jsonschema;

import javax.json.JsonNumber;
import javax.json.JsonValue;
import java.util.Objects;

/**
 * Equals helpers
 */
public final class ObjectComparator {

    /**
     * Checks that elements are lexically equivalent.  This is to handle the case that enum values that contain
     * numbers should not be considered equal if their lexical representation is different, eg:
     *
     * 1.0, 1, 1.00
     *
     * These would be equal mathematically, but should not be considered to be lexically equivalent.
     *
     */
    public static boolean lexicalEquivalent(final JsonValue obj1, final JsonValue obj2) {
        if (obj1 instanceof JsonNumber && obj2 instanceof JsonNumber) {
            JsonNumber n1 = ((JsonNumber) obj1);
            JsonNumber n2 = ((JsonNumber) obj2);

            return n1.bigDecimalValue().equals(n2.bigDecimalValue());
        } else {
            return Objects.equals(obj1, obj2);
        }
    }
}
