package io.sbsp.jsonschema.loader;

import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema.JsonSchemaBuilder;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.EXCLUSIVE_MAXIMUM;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.EXCLUSIVE_MINIMUM;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.MAXIMUM;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.MINIMUM;
import static io.sbsp.jsonschema.six.enums.JsonSchemaKeyword.MULTIPLE_OF;

public class NumberKeywordsLoader implements KeywordsLoader {

    private NumberKeywordsLoader() {

    }

    @Override
    public void appendKeywords(JsonValueWithLocation schemaJson, JsonSchemaBuilder schemaBuilder, JsonSchemaFactory factory) {

        checkNotNull(schemaBuilder, "schemaBuilder must not be null");
        checkNotNull(schemaJson, "schemaJson must not be null");

        schemaJson.findNumber(MINIMUM).ifPresent(schemaBuilder::minimum);
        schemaJson.findNumber(MAXIMUM).ifPresent(schemaBuilder::maximum);
        schemaJson.findNumber(MULTIPLE_OF).ifPresent(schemaBuilder::multipleOf);
        schemaJson.findNumber(EXCLUSIVE_MINIMUM).ifPresent(schemaBuilder::exclusiveMinimum);
        schemaJson.findNumber(EXCLUSIVE_MAXIMUM).ifPresent(schemaBuilder::exclusiveMaximum);
    }

    public static NumberKeywordsLoader numberKeywordsLoader() {
        return new NumberKeywordsLoader();
    }
}
