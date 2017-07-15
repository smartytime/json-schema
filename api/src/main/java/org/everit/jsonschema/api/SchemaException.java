package org.everit.jsonschema.api;


import java.util.Collection;
import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * Thrown by {@link SchemaLoader#load()} when it encounters
 * un-parseable schema JSON definition.
 *
 * @author erosb
 */
public class SchemaException extends RuntimeException {

    private static final long serialVersionUID = 5987489689035036987L;
    private final String schemaLocation;

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
