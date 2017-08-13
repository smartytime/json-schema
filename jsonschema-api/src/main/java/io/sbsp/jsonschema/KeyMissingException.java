package io.sbsp.jsonschema;

import lombok.Getter;

import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
public class KeyMissingException extends SchemaException {

    private static final long serialVersionUID = 1L;
    private final String key;

    public KeyMissingException(SchemaLocation schemaLocation, String key) {
        super(checkNotNull(schemaLocation, "location must not be null").getJsonPointerFragment(), "Missing value at key [%s]", key);
        checkNotNull(key, "key must not be null");
        this.key = key;
    }

    public static Supplier<KeyMissingException> missingProperty(Schema schema, String key) {
        return () -> new KeyMissingException(schema.getLocation(), key);
    }
}
