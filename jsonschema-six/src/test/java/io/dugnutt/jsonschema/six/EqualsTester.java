package io.dugnutt.jsonschema.six;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class EqualsTester {

    public static final JsonSchema NUMBER_SCHEMA_A = JsonSchema.jsonSchemaBuilder()
            .multipleOf(2)
            .exclusiveMaximum(33)
            .type(JsonSchemaType.NUMBER)
            .build();

    public static final JsonSchema NUMBER_SCHEMA_B = JsonSchema.jsonSchemaBuilder()
            .minimum(33)
            .type(JsonSchemaType.INTEGER)
            .build();


    public static final JsonSchema STRING_SCHEMA_A = JsonSchema.jsonSchemaBuilder()
            .format("uri")
            .maxLength(32)
            .type(JsonSchemaType.STRING)
            .build();

    public static final JsonSchema STRING_SCHEMA_B = JsonSchema.jsonSchemaBuilder()
            .maxLength(32)
            .minLength(3)
            .pattern("^[a-z]+$")
            .build();

    // public static final JsonSchema BOOLEAN_SCHEMA_A = JsonSchema.builder()
    //         .title("Bool A")
    //         .build();
    //
    // public static final JsonSchema BOOLEAN_SCHEMA_B = JsonSchema.builder()
    //         .title("Bool B")
    //         .build();
    //
    // public static final EmptySchema EMPTY_SCHEMA_A = EmptySchema.builder()
    //         .title("Empty A")
    //         .build();
    //
    // public static final EmptySchema EMPTY_SCHEMA_B = EmptySchema.builder()
    //         .title("Empty B")
    //         .build();

    // public static final JsonSchema COMBINED_SCHEMA_A = JsonSchema.builder()
    //         .subschema(STRING_SCHEMA_A)
    //         .subschema(NUMBER_SCHEMA_A)
    //         .JsonSchemaType(JsonSchemaType.ANY_OF).build();
    //
    // public static final JsonSchema COMBINED_SCHEMA_B = JsonSchema.builder()
    //         .subschema(STRING_SCHEMA_B)
    //         .subschema(NUMBER_SCHEMA_B)
    //         .JsonSchemaType(JsonSchemaType.ANY_OF).build();
    private final Class<Schema> testClass;

    public EqualsTester(Class<Schema> testClass) {
        this.testClass = testClass;
    }

    @Parameters(name = "{0}")
    public static List<Object[]> params() {
        return Arrays.asList(
                new Object[] {JsonSchema.class},
                new Object[] {StringKeywords.class},
                new Object[] {NumberKeywords.class},
                new Object[] {ArrayKeywords.class},
                new Object[] {ObjectKeywords.class}
        );
    }

    @Test
    public void equalsVerifier() {
        final String[] ignores;
        if (testClass.equals(JsonSchema.class)) {
            ignores = new String[]{"info"};
        } else {
            ignores = new String[0];
        }

        EqualsVerifier.forClass(testClass)
                .withRedefinedSuperclass()
                .withIgnoredFields(ignores)
                .withPrefabValues(JsonSchema.class, NUMBER_SCHEMA_B, STRING_SCHEMA_A)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }
}
