package io.sbsp.jsonschema.six;

import com.google.common.collect.ImmutableList;
import io.sbsp.jsonschema.JsonSchema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keywords.ArrayKeywords;
import io.sbsp.jsonschema.keywords.ArrayKeywords.ArrayKeywordsBuilder;
import io.sbsp.jsonschema.keywords.NumberKeywords;
import io.sbsp.jsonschema.keywords.NumberKeywords.NumberKeywordsBuilder;
import io.sbsp.jsonschema.keywords.ObjectKeywords;
import io.sbsp.jsonschema.keywords.ObjectKeywords.ObjectKeywordsBuilder;
import io.sbsp.jsonschema.keywords.StringKeywords;
import io.sbsp.jsonschema.keywords.StringKeywords.StringKeywordsBuilder;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.stream.Collectors;

public class EqualsTester {

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

    @Test
    public void testEquals() {

    }

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> params() {

        return ImmutableList.builder()
                .add(EqualsVerifier
                        .forClass(SchemaLocation.class)
                        .withOnlyTheseFields("documentURI", "jsonPath", "resolutionScope"))
                .add(EqualsVerifier.forClass(StringKeywords.class))
                .add(EqualsVerifier.forClass(StringKeywordsBuilder.class))
                .add(EqualsVerifier.forClass(NumberKeywords.class))
                .add(EqualsVerifier.forClass(NumberKeywordsBuilder.class))
                .add(EqualsVerifier.forClass(ArrayKeywords.class))
                .add(EqualsVerifier.forClass(ArrayKeywordsBuilder.class))
                .add(EqualsVerifier.forClass(ObjectKeywords.class))
                .add(EqualsVerifier.forClass(ObjectKeywordsBuilder.class))
                .add(EqualsVerifier.forClass(JsonSchema.class))
                .add(EqualsVerifier.forClass(Schema.JsonSchemaBuilder.class))
                .build()
                .stream()
                .map(verifier -> new Object[] {verifier})
                .collect(Collectors.toList());
    }

    @Test
    public void equalsVerifier() {

        // equalsVerifier
        //         .withRedefinedSuperclass()
        //         .verify();

        // final EqualsVerifier<?> verifier = EqualsVerifier.forClass(testClass)
        //         .withRedefinedSuperclass();
        // verifier
        //         .withRedefinedSuperclass()
        //         .withIgnoredFields(this.ignoredFields)
        //         .withOnlyTheseFields(this.specificFields)
        //         .withPrefabValues(Schema.class, NUMBER_SCHEMA_B, STRING_SCHEMA_A)
        //         .verify();
    }
}
