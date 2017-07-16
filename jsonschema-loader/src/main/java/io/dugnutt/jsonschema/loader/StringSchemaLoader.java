package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.JsonSchemaProperty;
import io.dugnutt.jsonschema.six.StringSchema;

import javax.json.JsonString;

import static java.util.Objects.requireNonNull;

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
        ls.schemaJson.findInt(JsonSchemaProperty.MIN_LENGTH).ifPresent(builder::minLength);
        ls.schemaJson.findInt(JsonSchemaProperty.MAX_LENGTH).ifPresent(builder::maxLength);
        ls.schemaJson.findString(JsonSchemaProperty.PATTERN).map(JsonString::getString).ifPresent(builder::pattern);
        ls.schemaJson.findString(JsonSchemaProperty.FORMAT).map(JsonString::getString).ifPresent(builder::format);
        return builder;
    }
}
