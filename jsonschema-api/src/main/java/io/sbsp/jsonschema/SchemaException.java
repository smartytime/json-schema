package io.sbsp.jsonschema;

import io.sbsp.jsonschema.loading.SchemaFactory;
import io.sbsp.jsonschema.utils.JsonUtils;
import lombok.Getter;

import java.net.URI;

/**
 * Thrown by {@link SchemaFactory#load()} when it encounters
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

    public SchemaException(URI schemaLocation, String message, Object... params) {
        this(schemaLocation.toString(), String.format(message, JsonUtils.prettyPrintArgs(params)));
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
