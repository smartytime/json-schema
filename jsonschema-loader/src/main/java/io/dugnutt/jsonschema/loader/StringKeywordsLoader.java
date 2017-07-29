package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.StringKeywords;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.dugnutt.jsonschema.six.Schema.JsonSchemaBuilder;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.FORMAT;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MAX_LENGTH;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.MIN_LENGTH;
import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PATTERN;

public class StringKeywordsLoader implements KeywordsLoader {

    public static StringKeywordsLoader stringKeywordsLoader() {
        return new StringKeywordsLoader();
    }

    private StringKeywordsLoader() {
    }

    @Override
    public void appendKeywords(JsonValueWithLocation schemaJson, JsonSchemaBuilder schemaBuilder, JsonSchemaFactory factory) {
        checkNotNull(schemaBuilder, "schemaBuilder must not be null");
        checkNotNull(schemaJson, "schemaJson must not be null");

        schemaJson.findInt(MIN_LENGTH).ifPresent(schemaBuilder::minLength);
        schemaJson.findInt(MAX_LENGTH).ifPresent(schemaBuilder::maxLength);
        schemaJson.findString(PATTERN).ifPresent(schemaBuilder::pattern);
        schemaJson.findString(FORMAT).ifPresent(schemaBuilder::format);
    }
}
