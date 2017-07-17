package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.StringSchema;

import javax.json.JsonString;

import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
public class StringSchemaFactory {

    private SchemaLoaderModel ls;

    public StringSchemaFactory(SchemaLoaderModel ls) {
        this.ls = requireNonNull(ls, "ls cannot be null");
    }

    public StringSchema.Builder load() {
        StringSchema.Builder builder = StringSchema.builder();
        ls.schemaJson.findInt(JsonSchemaKeyword.MIN_LENGTH).ifPresent(builder::minLength);
        ls.schemaJson.findInt(JsonSchemaKeyword.MAX_LENGTH).ifPresent(builder::maxLength);
        ls.schemaJson.findString(JsonSchemaKeyword.PATTERN).map(JsonString::getString).ifPresent(builder::pattern);
        ls.schemaJson.findString(JsonSchemaKeyword.FORMAT).map(JsonString::getString).ifPresent(builder::format);
        return builder;
    }
}
