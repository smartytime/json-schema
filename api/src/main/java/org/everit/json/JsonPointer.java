package org.everit.json;

import java.util.Optional;

public interface JsonPointer {
    JsonPath jsonPath();
    String toURIFragment();
    Optional<JsonObject<?>> queryFrom(JsonObject<?> object);
}
