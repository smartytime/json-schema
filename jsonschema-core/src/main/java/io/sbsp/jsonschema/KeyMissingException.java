package io.sbsp.jsonschema;

import lombok.Getter;

import java.util.function.Supplier;

@Getter
public class KeyMissingException extends SchemaException {

    private static final long serialVersionUID = 1L;
    private final String key;

    public KeyMissingException(SchemaLocation schemaLocation, String key) {
        super(schemaLocation.getJsonPointerFragment(), "Missing value at key %s", key);
        this.key = key;
    }

    public static Supplier<KeyMissingException> missingProperty(Schema schema, String key) {
        return () -> new KeyMissingException(schema.getLocation(), key);
    }
}
