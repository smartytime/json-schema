package io.sbsp.jsonschema.validator.keywords.array;

import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.six.enums.JsonSchemaType;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.ValidationError;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.spi.JsonProvider;

import java.util.Optional;

import static io.sbsp.jsonschema.six.Schema.jsonSchemaBuilder;
import static io.sbsp.jsonschema.validator.ValidationMocks.createTestValidator;
import static org.assertj.core.api.Assertions.assertThat;

public class ArrayContainsValidatorTest {
    @Test
    public void validate_DoesntContains() throws Exception {
        Schema containsSchema = jsonSchemaBuilder()
                .type(JsonSchemaType.ARRAY)
                .containsSchema(jsonSchemaBuilder()
                        .anyOfSchema(jsonSchemaBuilder().constValueInteger(3))
                        .anyOfSchema(jsonSchemaBuilder().constValueDouble(4))
                        .anyOfSchema(jsonSchemaBuilder().constValueString("5")))
                .build();

        final SchemaValidator testValidator = createTestValidator(containsSchema);
        final JsonArray invalidArray = JsonProvider.provider().createArrayBuilder()
                .add(24)
                .add("Bob")
                .add(5)
                .build();

        final Optional<ValidationError> validate = testValidator.validate(invalidArray);
        assertThat(validate).isPresent();
        final ValidationError error = validate.get();
        assertThat(error.getKeyword()).isEqualTo(JsonSchemaKeyword.CONTAINS);
    }

    @Test
    public void validate_Contains() throws Exception {
        Schema containsSchema = jsonSchemaBuilder()
                .type(JsonSchemaType.ARRAY)
                .containsSchema(jsonSchemaBuilder()
                        .anyOfSchema(jsonSchemaBuilder().constValueDouble(3))
                        .anyOfSchema(jsonSchemaBuilder().constValueDouble(4))
                        .anyOfSchema(jsonSchemaBuilder().constValueString("5")))
                .build();

        final SchemaValidator testValidator = createTestValidator(containsSchema);
        final JsonArray invalidArray = JsonProvider.provider().createArrayBuilder()
                .add(24)
                .add("Bob")
                .add("5")
                .add(3)
                .build();

        final Optional<ValidationError> validate = testValidator.validate(invalidArray);
        assertThat(validate).isNotPresent();
    }
}