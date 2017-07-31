package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.Schema;
import org.junit.Test;

import javax.json.JsonValue;
import java.util.Optional;

import static io.dugnutt.jsonschema.validator.ValidationMocks.createTestValidator;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonSchemaValidatorTest {

    @Test
    public void validate_WhenValueIsNull_AppliesNullValidators() {
        final Schema constSchema = Schema.jsonSchemaBuilder()
                .constValueDouble(3)
                .build();

        final Optional<ValidationError> results = createTestValidator(constSchema).validate(JsonValue.NULL);
        assertThat(results).isPresent();
    }

}