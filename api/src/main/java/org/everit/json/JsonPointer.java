package org.everit.json;

import java.util.List;
import java.util.Optional;

public interface JsonPointer {
    List<String> path();
    String toURIFragment();
    Optional<JsonObject<?>> queryFrom(JsonObject<?> object);
}
