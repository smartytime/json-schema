package io.sbsp.jsonschema.validation;

import io.sbsp.jsonschema.Schema;
import org.junit.Test;

import javax.json.JsonValue;
import java.util.Optional;

import static io.sbsp.jsonschema.JsonSchemaProvider.schemaBuilder;
import static io.sbsp.jsonschema.validation.ValidationMocks.createTestValidator;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaValidatorTest {

    @Test
    public void validate_WhenValueIsNull_AppliesNullValidators() {
        final Schema constSchema = schemaBuilder()
                .constValueDouble(3)
                .build();

        final Optional<ValidationError> results = createTestValidator(constSchema).validate(JsonValue.NULL);
        assertThat(results).isPresent();
    }

}