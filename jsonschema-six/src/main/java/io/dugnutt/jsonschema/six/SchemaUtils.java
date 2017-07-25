package io.dugnutt.jsonschema.six;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class SchemaUtils {

    public static boolean isJsonPointer(URI uri) {
        return uri.getFragment() != null && uri.getFragment().startsWith("#/");
    }

    public static boolean isJsonPointer(String uri) {
        return uri != null && uri.startsWith("#/");
    }

    // @Nullable
    // public static Map<String, Schema> safeRelocateProperties(@Nullable Map<String, Schema> subject, SchemaLocation location) {
    //     if (subject == null) {
    //         return null;
    //     }
    //     Map<String, Schema> relocated = new HashMap<>();
    //     subject.forEach((propertyName, schema)->{
    //         final SchemaLocation propertyPath = location.withChildPath(propertyName);
    //         relocated.put(propertyName, schema.withNewLocation(propertyPath));
    //     });
    //     return relocated;
    // }

    // @Nullable
    // public static <K> Map<K, Schema> safeRelocate(@Nullable Map<K, Schema> subject, SchemaLocation location) {
    //     if (subject == null) {
    //         return null;
    //     }
    //     Map<K, Schema> relocated = new HashMap<>();
    //     subject.forEach((propertyName, schema)->{
    //         relocated.put(propertyName, schema.withNewLocation(location));
    //
    //     });
    //     return relocated;
    // }

    // @Nullable
    // public static Schema safeRelocate(@Nullable Schema subject, SchemaLocation location) {
    //     if (subject == null) {
    //         return null;
    //     }
    //     return subject.withNewLocation(location);
    // }
}
