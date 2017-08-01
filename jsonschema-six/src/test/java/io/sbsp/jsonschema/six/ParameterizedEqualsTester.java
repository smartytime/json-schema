package io.sbsp.jsonschema.six;

import io.sbsp.jsonschema.six.enums.JsonSchemaType;
import io.sbsp.jsonschema.six.keywords.ArrayKeywords;
import io.sbsp.jsonschema.six.keywords.NumberKeywords;
import io.sbsp.jsonschema.six.keywords.ObjectKeywords;
import io.sbsp.jsonschema.six.keywords.StringKeywords;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RunWith(Parameterized.class)
public class ParameterizedEqualsTester {

    public static final Schema NUMBER_SCHEMA_A = Schema.jsonSchemaBuilder()
            .multipleOf(2)
            .exclusiveMaximum(33)
            .type(JsonSchemaType.NUMBER)
            .build();

    public static final Schema NUMBER_SCHEMA_B = Schema.jsonSchemaBuilder()
            .minimum(33)
            .type(JsonSchemaType.INTEGER)
            .build();


    public static final Schema STRING_SCHEMA_A = Schema.jsonSchemaBuilder()
            .format("uri")
            .maxLength(32)
            .type(JsonSchemaType.STRING)
            .build();

    public static final Schema STRING_SCHEMA_B = Schema.jsonSchemaBuilder()
            .maxLength(32)
            .minLength(3)
            .pattern("^[a-z]+$")
            .build();


    private final Class<Schema> testClass;

    public ParameterizedEqualsTester(Class<Schema> testClass, Set<String> ignores) {
        this.testClass = testClass;
    }

    @Parameters(name = "{0}")
    public static List<Object[]> params() {
        return Arrays.asList(
                new Object[] {SchemaLocation.class},
                new Object[] {JsonValueWithLocation.class},
                new Object[] {ReferenceSchema.class},
                new Object[] {JsonSchema.class},
                new Object[] {JsonPath.class},
                new Object[] {StringKeywords.class},
                new Object[] {StringKeywords.StringKeywordsBuilder.class},
                new Object[] {NumberKeywords.class},
                new Object[] {NumberKeywords.NumberKeywordsBuilder.class},
                new Object[] {ArrayKeywords.class},
                new Object[] {ArrayKeywords.ArrayKeywordsBuilder.class},
                new Object[] {ObjectKeywords.class},
                new Object[] {ObjectKeywords.ObjectKeywordsBuilder.class},
                new Object[] {Schema.JsonSchemaBuilder.class}
        );
    }

    @Test
    public void equalsVerifier() {
        final String[] ignores;
        if (testClass.equals(JsonSchema.class)) {
            ignores = new String[]{"location"};
        } else {
            ignores = new String[0];
        }

        EqualsVerifier.forClass(testClass)
                .withRedefinedSuperclass()
                .withIgnoredFields(ignores)
                .withPrefabValues(Schema.class, NUMBER_SCHEMA_B, STRING_SCHEMA_A)
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }
}
