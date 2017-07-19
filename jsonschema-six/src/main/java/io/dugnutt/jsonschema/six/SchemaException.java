package io.dugnutt.jsonschema.six;

import lombok.Getter;

import java.net.URI;

/**
 * Thrown by {@link SchemaLoader#load()} when it encounters
 * un-parseable schema JSON definition.
 *
 * @author erosb
 */
@Getter
public class SchemaException extends RuntimeException {

    private static final long serialVersionUID = 5987489689035036987L;
    private final String schemaLocation;

    public SchemaException(URI schemaLocation, String message) {
        this(schemaLocation.toString(), message);
    }
    
    public SchemaException(String schemaLocation, String message) {
        super(schemaLocation == null
                ? "<unknown location>: " + message
                : schemaLocation + ": " + message);
        this.schemaLocation = schemaLocation;
    }

    @Deprecated
    public SchemaException(String message) {
        this((String) null, message);
    }
}
