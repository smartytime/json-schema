package org.everit.jsonschema.loader;

import org.everit.jsonschema.api.StringSchema;
import org.everit.json.JsonElement;

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
        ls.schemaJson.find(MIN_LENGTH).map(JsonElement::asInteger).ifPresent(builder::minLength);
        ls.schemaJson.find(MAX_LENGTH).map(JsonElement::asInteger).ifPresent(builder::maxLength);
        ls.schemaJson.find(PATTERN).map(JsonElement::asString).ifPresent(builder::pattern);
        ls.schemaJson.find(FORMAT).map(JsonElement::asString).ifPresent(builder::format);
        return builder;
    }
}
