package org.everit.jsonschema.loader;

import org.everit.jsonschema.api.StringSchema;

import javax.json.JsonString;

import static java.util.Objects.requireNonNull;
import static org.everit.jsonschema.api.JsonSchemaProperty.FORMAT;
import static org.everit.jsonschema.api.JsonSchemaProperty.MAX_LENGTH;
import static org.everit.jsonschema.api.JsonSchemaProperty.MIN_LENGTH;
import static org.everit.jsonschema.api.JsonSchemaProperty.PATTERN;

/**
 * @author erosb
 */
public class StringSchemaLoader {

    private LoadingState ls;

    public StringSchemaLoader(LoadingState ls) {
        this.ls = requireNonNull(ls, "ls cannot be null");
    }

    public StringSchema.Builder load() {
        StringSchema.Builder builder = StringSchema.builder();
        ls.schemaJson.findInt(MIN_LENGTH).ifPresent(builder::minLength);
        ls.schemaJson.findInt(MAX_LENGTH).ifPresent(builder::maxLength);
        ls.schemaJson.findString(PATTERN).map(JsonString::getString).ifPresent(builder::pattern);
        ls.schemaJson.findString(FORMAT).map(JsonString::getString).ifPresent(builder::format);
        return builder;
    }
}
