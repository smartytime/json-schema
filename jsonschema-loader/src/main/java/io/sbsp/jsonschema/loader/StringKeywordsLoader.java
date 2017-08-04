package io.sbsp.jsonschema.loader;

import io.sbsp.jsonschema.JsonValueWithLocation;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.Schema.JsonSchemaBuilder;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.FORMAT;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MAX_LENGTH;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.MIN_LENGTH;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.PATTERN;

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
