package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.enums.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.enums.JsonSchemaType;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.SchemaLocation;
import io.dugnutt.jsonschema.utils.JsonUtils;
import org.junit.Test;

import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;
import static org.assertj.core.api.Assertions.assertThat;

public class ValidationReportTest {

    @Test
    public void toStringTest() {
        final ValidationReport report = new ValidationReport();
        final JsonValueWithLocation testSubject = JsonValueWithLocation.fromJsonValue(JsonUtils.blankJsonObject(),
                SchemaLocation.anonymousRoot());
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