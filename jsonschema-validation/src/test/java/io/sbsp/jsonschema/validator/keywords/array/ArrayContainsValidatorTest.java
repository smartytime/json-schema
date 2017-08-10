package io.sbsp.jsonschema.validator.keywords.array;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.ValidationError;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.spi.JsonProvider;
import java.util.Optional;

import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchema;
import static io.sbsp.jsonschema.validator.ValidationMocks.createTestValidator;
import static org.assertj.core.api.Assertions.assertThat;

public class ArrayContainsValidatorTest {
    @Test
    public void validate_DoesntContains() throws Exception {
        Schema containsSchema = jsonSchema()
                .type(JsonSchemaType.ARRAY)
                .containsSchema(jsonSchema()
                        .anyOfSchema(jsonSchema().constValueInteger(3))
                        .anyOfSchema(jsonSchema().constValueDouble(4))
                        .anyOfSchema(jsonSchema().constValueString("5")))
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
        assertThat(error.getKeyword()).isEqualTo(JsonSchemaKeywordType.CONTAINS);
    }

    @Test
    public void validate_Contains() throws Exception {
        Schema containsSchema = jsonSchema()
                .type(JsonSchemaType.ARRAY)
                .containsSchema(jsonSchema()
                        .anyOfSchema(jsonSchema().constValueDouble(3))
                        .anyOfSchema(jsonSchema().constValueDouble(4))
                        .anyOfSchema(jsonSchema().constValueString("5")))
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