package io.sbsp.jsonschema.validation;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.utils.JsonUtils;
import org.junit.Test;

import static io.sbsp.jsonschema.JsonSchemaProvider.schemaBuilder;
import static io.sbsp.jsonschema.validation.ValidationErrorHelper.buildKeywordFailure;
import static org.assertj.core.api.Assertions.assertThat;

public class ValidationReportTest {

    @Test
    public void toStringTest() {
        final ValidationReport report = new ValidationReport();
        final JsonValueWithPath testSubject = JsonValueWithPath.fromJsonValue(JsonUtils.blankJsonObject());
        final Schema stringSchema = schemaBuilder()
                .pattern("[a-z]+")
                .minLength(12)
                .type(JsonSchemaType.STRING)
                .build();
        report.addError(buildKeywordFailure(testSubject, stringSchema, Keywords.PATTERN));

        final String reportString = report.toString();
        assertThat(reportString).isNotNull();
    }
}