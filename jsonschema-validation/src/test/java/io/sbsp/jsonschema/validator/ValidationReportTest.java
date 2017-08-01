package io.sbsp.jsonschema.validator;

import io.sbsp.jsonschema.six.JsonValueWithLocation;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.enums.JsonSchemaType;
import io.sbsp.jsonschema.utils.JsonUtils;
import org.junit.Test;

import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;
import static org.assertj.core.api.Assertions.assertThat;

public class ValidationReportTest {

    @Test
    public void toStringTest() {
        final ValidationReport report = new ValidationReport();
        final JsonValueWithLocation testSubject = JsonValueWithLocation.fromJsonValue(JsonUtils.blankJsonObject());
        report.addError(buildKeywordFailure(testSubject, Schema.jsonSchemaBuilder()
                .pattern("[a-z]+")
                .minLength(12)
                .type(JsonSchemaType.STRING)
                .build(), JsonSchemaKeyword.PATTERN)
                .build());

        final String reportString = report.toString();
        assertThat(reportString).isNotNull();
    }
}