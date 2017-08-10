package io.sbsp.jsonschema.validator;

import io.sbsp.jsonschema.Schema;
import org.junit.Test;

import javax.json.JsonValue;
import java.util.Optional;

import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchema;
import static io.sbsp.jsonschema.validator.ValidationMocks.createTestValidator;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaValidatorTest {

    @Test
    public void validate_WhenValueIsNull_AppliesNullValidators() {
        final Schema constSchema = jsonSchema()
                .constValueDouble(3)
                .build();

        final Optional<ValidationError> results = createTestValidator(constSchema).validate(JsonValue.NULL);
        assertThat(results).isPresent();
    }

}