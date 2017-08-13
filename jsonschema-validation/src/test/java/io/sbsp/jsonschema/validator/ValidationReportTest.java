package io.sbsp.jsonschema.validator;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.utils.JsonUtils;
import org.junit.Test;

import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchema;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;
import static org.assertj.core.api.Assertions.assertThat;

public class ValidationReportTest {

    @Test
    public void toStringTest() {
        final ValidationReport report = new ValidationReport();
        final JsonValueWithLocation testSubject = JsonValueWithLocation.fromJsonValue(JsonUtils.blankJsonObject());
        report.addError(buildKeywordFailure(testSubject, jsonSchema()
                .pattern("[a-z]+")
                .minLength(12)
                .type(JsonSchemaType.STRING)
                .build(), JsonSchemaKeywordType.PATTERN)
                .build());

        final String reportString = report.toString();
        assertThat(reportString).isNotNull();
    }
}