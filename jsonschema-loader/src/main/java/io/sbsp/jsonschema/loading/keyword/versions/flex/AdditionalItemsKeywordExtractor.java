package io.sbsp.jsonschema.loading.keyword.versions.flex;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.loading.LoadingIssue.LoadingIssueBuilder;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.keyword.SchemaKeywordExtractor;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.json.JsonValue;

import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchema;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ADDITIONAL_ITEMS;
import static io.sbsp.jsonschema.loading.LoadingIssues.typeMismatch;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.TRUE;

@Getter
@EqualsAndHashCode
public class AdditionalItemsKeywordExtractor implements SchemaKeywordExtractor {

    private final boolean allowBooleanValue;

    private AdditionalItemsKeywordExtractor(boolean allowBooleanValue) {
        this.allowBooleanValue = allowBooleanValue;
    }

    @Override
    public KeywordMetadata<?> getKeyword() {
        return Keywords.additionalItems;
    }

    @Override
    public LoadingReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory schemaFactory, LoadingReport report) {
        final JsonValueWithLocation additionalItems = jsonObject.getPathAwareObject(ADDITIONAL_ITEMS);
        if (additionalItems.is(TRUE, FALSE) && allowBooleanValue) {
            if (additionalItems == JsonValue.FALSE) {
                builder.schemaOfAdditionalItems(jsonSchema().type(JsonSchemaType.NULL));
            }
        } else if (additionalItems.is(OBJECT)) {
            final SchemaBuilder addtlItemsBuilder = schemaFactory.createSchemaBuilder(additionalItems, report);
            builder.schemaOfAdditionalItems(addtlItemsBuilder);
        } else if (additionalItems.isNotNull()) {
            final LoadingIssueBuilder typeMismatchError = typeMismatch(Keywords.additionalItems, additionalItems)
                    .location(additionalItems.getLocation());
            report.error(typeMismatchError);
        }

        return report;
    }

    public static AdditionalItemsKeywordExtractor flexible() {
        return new AdditionalItemsKeywordExtractor(true);
    }

    public static AdditionalItemsKeywordExtractor draft6() {
        return new AdditionalItemsKeywordExtractor(false);
    }
}