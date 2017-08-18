package io.sbsp.jsonschema.validation.keywords.array;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.validation.SchemaValidator;
import io.sbsp.jsonschema.validation.ValidationError;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.spi.JsonProvider;
import java.util.Optional;

import static io.sbsp.jsonschema.JsonSchemaProvider.schemaBuilder;
import static io.sbsp.jsonschema.validation.ValidationMocks.createTestValidator;
import static org.assertj.core.api.Assertions.assertThat;

public class ArrayContainsValidatorTest {
    @Test
    public void validate_DoesntContains() throws Exception {
        Schema containsSchema = schemaBuilder()
                .type(JsonSchemaType.ARRAY)
                .containsSchema(schemaBuilder()
                        .anyOfSchema(schemaBuilder().constValueInteger(3))
                        .anyOfSchema(schemaBuilder().constValueDouble(4))
                        .anyOfSchema(schemaBuilder().constValueString("5")))
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
        assertThat(error.getKeyword()).isEqualTo(Keywords.CONTAINS);
    }

    @Test
    public void validate_Contains() throws Exception {
        Schema containsSchema = schemaBuilder()
                .type(JsonSchemaType.ARRAY)
                .containsSchema(schemaBuilder()
                        .anyOfSchema(schemaBuilder().constValueDouble(3))
                        .anyOfSchema(schemaBuilder().constValueDouble(4))
                        .anyOfSchema(schemaBuilder().constValueString("5")))
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