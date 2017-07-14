package org.everit.json;

import org.everit.jsonschema.api.JsonSchemaType;
import org.everit.jsonschema.api.JsonWriter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;

public interface JsonApi<X> {

    default JsonElement<?> readJson(String jsonValue) {
        Charset utf8 = Charset.forName("UTF-8");
        ByteArrayInputStream bytes = new ByteArrayInputStream(jsonValue.getBytes(utf8));
        return readJson(bytes, utf8);
    }

    JsonElement<?> readJson(InputStream stream, Charset charset);

    JsonWriter getWriter();

    JsonSchemaType schemaType(X x);

    // boolean isNull(X subject);

    JsonValue<?> of(X raw, JsonPath path);

    Optional<JsonObject<?>> query(JsonObject<?> toBeQueried, JsonPointer pointer);

    JsonObject<?> fromMap(Map<String, Object> map, JsonPath path);
}
