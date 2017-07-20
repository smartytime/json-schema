package io.dugnutt.jsonschema.six;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.List;

import static io.dugnutt.jsonschema.six.SchemaLocation.schemaLocation;

@RunWith(Parameterized.class)
public class EqualsTester {

    public static final NumberSchema NUMBER_SCHEMA_A = NumberSchema.builder(schemaLocation())
            .multipleOf(2)
            .exclusiveMaximum(33)
            .requiresNumber(true)
            .build();

    public static final NumberSchema NUMBER_SCHEMA_B = NumberSchema.builder(schemaLocation())
            .minimum(33)
            .requiresInteger(true)
            .build();

    public static final StringSchema STRING_SCHEMA_A = StringSchema.builder(schemaLocation())
            .format("uri")
            .maxLength(32)
            .requiresString(true)
            .build();

    public static final StringSchema STRING_SCHEMA_B = StringSchema.builder(schemaLocation())
            .maxLength(32)
            .minLength(3)
            .pattern("^[a-z]+$")
            .build();

    public static final BooleanSchema BOOLEAN_SCHEMA_A = BooleanSchema.builder(schemaLocation())
            .title("Bool A")
            .build();

    public static final BooleanSchema BOOLEAN_SCHEMA_B = BooleanSchema.builder(schemaLocation())
            .title("Bool B")
            .build();

    public static final EmptySchema EMPTY_SCHEMA_A = EmptySchema.builder(schemaLocation())
            .title("Empty A")
            .build();

    public static final EmptySchema EMPTY_SCHEMA_B = EmptySchema.builder(schemaLocation())
            .title("Empty B")
            .build();

    public static final CombinedSchema COMBINED_SCHEMA_A = CombinedSchema.builder(schemaLocation())
            .subschema(STRING_SCHEMA_A)
            .subschema(NUMBER_SCHEMA_A)
            .combinedSchemaType(CombinedSchemaType.ANY_OF).build();

    public static final CombinedSchema COMBINED_SCHEMA_B = CombinedSchema.builder(schemaLocation())
            .subschema(STRING_SCHEMA_B)
            .subschema(NUMBER_SCHEMA_B)
            .combinedSchemaType(CombinedSchemaType.ANY_OF).build();
    private final Class<Schema> testClass;

    public EqualsTester(Class<Schema> testClass) {
        this.testClass = testClass;
    }

    @Parameters(name = "{0}")
    public static List<Object[]> params() {
        return Arrays.asList(
                new Object[] {Schema.class},
                new Object[] {BooleanSchema.class},
                new Object[] {CombinedSchema.class},
                new Object[] {EmptySchema.class},
                new Object[] {NullSchema.class},
                new Object[] {NumberSchema.class},
                new Object[] {StringSchema.class},
                new Object[] {ObjectSchema.class},
                new Object[] {ArraySchema.class}

        );
    }

    @Test
    public void equalsVerifier() {
        EqualsVerifier.forClass(testClass)
                .withRedefinedSuperclass()
                .withIgnoredFields("location")
                .withPrefabValues(Schema.class, NUMBER_SCHEMA_B, STRING_SCHEMA_A)
                .withPrefabValues(CombinedSchema.class, COMBINED_SCHEMA_A, COMBINED_SCHEMA_B)
                .withPrefabValues(BooleanSchema.class, BOOLEAN_SCHEMA_A, BOOLEAN_SCHEMA_B)
                .withPrefabValues(EmptySchema.class, EMPTY_SCHEMA_A, EMPTY_SCHEMA_B)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }
}
