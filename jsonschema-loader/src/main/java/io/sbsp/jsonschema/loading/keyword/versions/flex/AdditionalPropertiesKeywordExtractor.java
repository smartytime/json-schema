package io.sbsp.jsonschema.loading.keyword.versions.flex;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.keyword.SchemaKeywordExtractor;
import io.sbsp.jsonschema.keyword.KeywordMetadata;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.json.JsonValue;

import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchema;
import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.ADDITIONAL_PROPERTIES;
import static io.sbsp.jsonschema.loading.LoadingIssues.typeMismatch;
import static javax.json.JsonValue.ValueType.OBJECT;

@Getter
@EqualsAndHashCode
public class AdditionalPropertiesKeywordExtractor implements SchemaKeywordExtractor {

    private final boolean allowBooleanValue;

    private AdditionalPropertiesKeywordExtractor(boolean allowBooleanValue) {
        this.allowBooleanValue = allowBooleanValue;
    }

    @Override
    public KeywordMetadata<?> getKeyword() {
        return Keywords.additionalProperties;
    }

    @Override
    public LoadingReport extractKeyword(JsonValueWithLocation jsonObject, JsonSchemaBuilder builder, SchemaFactory schemaFactory, LoadingReport report) {
        final JsonValueWithLocation additionalProperties = jsonObject.getPathAwareObject(ADDITIONAL_PROPERTIES);
        if (additionalProperties.isBoolean() && allowBooleanValue) {
            if (additionalProperties == JsonValue.FALSE) {
                builder.schemaOfAdditionalProperties(jsonSchema().type(JsonSchemaType.NULL));
            }
        } else if (additionalProperties.is(OBJECT)) {
            final SchemaBuilder addtlPropertiesBuilder = schemaFactory.createSchemaBuilder(additionalProperties, report);
            builder.schemaOfAdditionalProperties(addtlPropertiesBuilder);
        } else if(additionalProperties.isNotNull()) {
            report.error(typeMismatch(Keywords.additionalProperties, additionalProperties));
        }

        return report;
    }

    public static AdditionalPropertiesKeywordExtractor flexible() {
        return new AdditionalPropertiesKeywordExtractor(true);
    }

    public static AdditionalPropertiesKeywordExtractor draft6() {
        return new AdditionalPropertiesKeywordExtractor(false);
    }
}