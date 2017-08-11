package io.sbsp.jsonschema.six;

import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.TestUtils;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.utils.JsonUtils;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;

import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchema;
import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchemaBuilderWithId;
import static io.sbsp.jsonschema.enums.JsonSchemaType.ARRAY;
import static io.sbsp.jsonschema.enums.JsonSchemaType.BOOLEAN;
import static io.sbsp.jsonschema.enums.JsonSchemaType.INTEGER;
import static io.sbsp.jsonschema.enums.JsonSchemaType.NULL;
import static io.sbsp.jsonschema.enums.JsonSchemaType.NUMBER;
import static io.sbsp.jsonschema.enums.JsonSchemaType.OBJECT;
import static io.sbsp.jsonschema.enums.JsonSchemaType.STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class SchemaBuilderTest {

    

    @Test
    public void buildAllStringKeywords() {
        final Draft6Schema schema = jsonSchema()
                .type(STRING)
                .minLength(3)
                .maxLength(6)
                .pattern("[a-z]*")
                .format("uri-template")
                .build().asDraft6();

        assertThat(schema.getTypes())
                .hasSize(1)
                .containsExactly(STRING);

        assertSoftly(a -> {
            a.assertThat(schema.getMinLength())
                    .isNotNull()
                    .isEqualTo(3);
            a.assertThat(schema.getMaxLength())
                    .isNotNull()
                    .isEqualTo(6);
            a.assertThat(schema.getFormat())
                    .isNotEmpty().isNotNull()
                    .isEqualTo("uri-template");
            a.assertThat(schema.getPattern())
                    .isNotNull();
            a.assertThat(schema.getPattern())
                    .isEqualTo("[a-z]*");
        });
    }

    @Test
    public void buildAllNumberKeywords() {
        final Draft6Schema schema = jsonSchema()
                .type(NUMBER)
                .minimum(1)
                .maximum(9)
                .exclusiveMinimum(0)
                .exclusiveMaximum(10)
                .multipleOf(3.4d)
                .build().asDraft6();

        assertSoftly(a -> {
            a.assertThat(schema.getMaximum())
                    .isNotNull()
                    .isEqualTo(9);
            a.assertThat(schema.getMinimum())
                    .isNotNull()
                    .isEqualTo(1);
            a.assertThat(schema.getExclusiveMaximum())
                    .isNotNull()
                    .isEqualTo(10);
            a.assertThat(schema.getExclusiveMinimum())
                    .isNotNull()
                    .isEqualTo(0);

            a.assertThat(schema.getMultipleOf())
                    .isNotNull()
                    .isEqualTo(3.4d);
        });
    }

    @Test
    public void buildArrayKeywords_AllItemSchema() {
        final Draft6Schema schema = jsonSchema()
                .type(ARRAY)
                .minItems(1)
                .maxItems(10)
                .needsUniqueItems(true)
                .allItemSchema(jsonSchema()
                        .notSchema(
                                jsonSchema().type(NULL)
                        )
                )
                .containsSchema(jsonSchema()
                        .type(STRING)
                )
                .build()
                .asDraft6();

        assertSoftly(a -> {
            a.assertThat(schema.getMinItems())
                    .isNotNull()
                    .isEqualTo(1);
            a.assertThat(schema.getMaxItems())
                    .isNotNull()
                    .isEqualTo(10);
            a.assertThat(schema.getAllItemSchema())
                    .isPresent()
                    .hasValue(jsonSchema().notSchema(jsonSchema().type(NULL))
                            .build()
                            .asDraft6());
            final Schema allItemSchema = schema.getAllItemSchema().get();
            a.assertThat(allItemSchema.getLocation().getJsonPointerFragment())
                    .isEqualTo(URI.create("#/items"));

            a.assertThat(schema.getContainsSchema())
                    .isPresent()
                    .hasValue(jsonSchema().type(STRING).build().asDraft6());

            final Schema containschema = schema.getContainsSchema().get();
            a.assertThat(containschema.getLocation().getJsonPointerFragment())
                    .isEqualTo(URI.create("#/contains"));
        });
    }

    @Test
    public void buildArrayKeywords_IndexedItemSchema() {
        final Draft6Schema draft6Schema = jsonSchema()
                .type(ARRAY)
                .itemSchema(jsonSchema().type(STRING))
                .itemSchema(jsonSchema().type(NUMBER))
                .itemSchema(jsonSchema().type(BOOLEAN))
                .schemaOfAdditionalItems(jsonSchema().type(STRING))
                .build().asDraft6();

        assertSoftly(a -> {
            a.assertThat(draft6Schema.getItemSchemas())
                    .isNotNull()
                    .hasSize(3)
                    .element(0)
                    .isEqualTo(jsonSchema().type(STRING).build());
            final URI fragmentIdx0 = draft6Schema.getItemSchemas().get(0).getLocation().getJsonPointerFragment();
            assertThat(fragmentIdx0)
                    .isNotNull()
                    .hasToString("#/items/0");
            a.assertThat(draft6Schema.getAdditionalItemsSchema())
                    .isPresent()
                    .hasValue(jsonSchema().type(STRING).build().asDraft6());
            a.assertThat(draft6Schema.getAdditionalItemsSchema().get().getLocation().getJsonPointerFragment())
                    .hasToString("#/additionalItems");
        });
    }

    @Test
    public void buildObjectKeywords_AllKeywords() {
        final Pattern pattern = Pattern.compile("[A-Z]*");

        final Draft6Schema schema = jsonSchema()
                .type(OBJECT)
                .propertySchema("name", jsonSchema().type(STRING))
                .propertySchema("age", jsonSchema().type(NUMBER))
                .patternProperty(pattern, jsonSchema().type(OBJECT))
                .schemaOfAdditionalProperties(jsonSchema().type(ARRAY))
                .requiredProperty("name")
                .requiredProperty("another")
                .minProperties(1)
                .maxProperties(10)
                .schemaDependency("email", jsonSchema().maxProperties(4))
                .propertyDependency("age", "email")
                .build().asDraft6();

        assertSoftly(a -> {
            a.assertThat(schema.getMinProperties()).isNotNull().isEqualTo(1);
            a.assertThat(schema.getMaxProperties()).isNotNull().isEqualTo(10);
            a.assertThat(schema.getRequiredProperties()).contains("name", "another");
            a.assertThat(schema.getPropertyDependencies().keySet()).hasSize(1);
            a.assertThat(schema.getPropertyDependencies().entries()).hasSize(1);
            a.assertThat(schema.getPatternProperties())
                    .isNotNull()
                    .hasSize(1)
                    .containsEntry(pattern.pattern(), jsonSchema().type(OBJECT).build());

            a.assertThat(schema.getPropertySchemaDependencies())
                    .isNotNull()
                    .hasSize(1)
                    .containsEntry("email", jsonSchema().maxProperties(4).build());
        });
    }

    @Test
    public void buildSharedKeywords_AllKeywords() {
        final Draft6Schema objectSchema = jsonSchemaBuilderWithId("#doopy")
                .title("My Title")
                .description("A description")
                .defaultValue(TestUtils.jsonString("A default value"))
                .allOfSchema(jsonSchema().enumValues(JsonUtils.jsonArray(1, 2, 3, 4)))
                .allOfSchema(jsonSchema().enumValues(JsonUtils.jsonArray(3, 4, 5, 6)))
                .anyOfSchema(jsonSchema().constValueDouble(4))
                .anyOfSchema(jsonSchema().constValueDouble(46))
                .oneOfSchema(jsonSchema().minimum(2))
                .oneOfSchema(jsonSchema().maximum(50))
                .type(NUMBER)
                .type(INTEGER)
                .notSchema(jsonSchema().constValueDouble(21))
                .enumValues(JsonUtils.jsonArray(1, 2, 3, 4, 5, 6, 7, 8, 9))
                .build().asDraft6();

        assertSoftly(a -> {
            a.assertThat(objectSchema.getId()).isNotNull().hasToString("#doopy");
            a.assertThat(objectSchema.getTitle()).isNotNull().isEqualTo("My Title");
            a.assertThat(objectSchema.getDescription()).isNotNull().isEqualTo("A description");
            a.assertThat(objectSchema.getDefaultValue()).isPresent()
                    .hasValue(TestUtils.jsonString("A default value"));
            a.assertThat(objectSchema.getAllOfSchemas())
                    .isNotNull()
                    .hasSize(2);

            a.assertThat(objectSchema.getAnyOfSchemas())
                    .isNotNull()
                    .hasSize(2);

            a.assertThat(objectSchema.getOneOfSchemas())
                    .isNotNull()
                    .hasSize(2);

            a.assertThat(objectSchema.getTypes())
                    .contains(NUMBER, INTEGER);
            a.assertThat(objectSchema.getNotSchema())
                    .isPresent()
                    .hasValue(jsonSchema().constValueDouble(21).build());
            a.assertThat(objectSchema.getEnumValues())
                    .isPresent()
                    .hasValue(JsonUtils.jsonArray(1, 2, 3, 4, 5, 6, 7, 8, 9));
        });
    }

    @Test
    public void testBuilderEquals() {
        EqualsVerifier.forClass(JsonSchemaBuilder.class)
                .suppress(Warning.STRICT_INHERITANCE)
                .suppress(Warning.NONFINAL_FIELDS)
                .withPrefabValues(JsonSchemaBuilder.class,
                        jsonSchema().type(STRING),
                        jsonSchema().type(OBJECT))
                .withPrefabValues(Pattern.class, Pattern.compile("[a-z]*]"), Pattern.compile("[A-Z]*]"))
                .withOnlyTheseFields(
                        "ref",
                        "detailsBuilder",
                        "arrayKeywords",
                        "stringKeywords",
                        "objectKeywords",
                        "numberKeywords",
                        "combinedSchemas",
                        "notSchema",
                        "schemaOfAdditionalItems",
                        "itemSchemas",
                        "allItemSchema",
                        "containsSchema",
                        "patternProperties",
                        "propertySchemas",
                        "schemaDependencies",
                        "propertyNameSchema",
                        "schemaOfAdditionalProperties"
                )
                .verify();
    }

    @Test
    public void testBuilderEqualsWithPatternProperty() {
        JsonSchemaBuilder a = jsonSchema().patternProperty("[a-z]*", jsonSchema());
        JsonSchemaBuilder b = jsonSchema().patternProperty("[a-z]*", jsonSchema());
        assertThat(a).isEqualTo(b);
    }

    @Test
    public void testBuilderHashcodeMatchesWithPatternProperty() {
        JsonSchemaBuilder a = jsonSchema().patternProperty("[a-z]*", jsonSchema());
        JsonSchemaBuilder b = jsonSchema().patternProperty("[a-z]*", jsonSchema());
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    public void testBuilderEqualsWithPattern() {
        JsonSchemaBuilder a = jsonSchema().pattern("[a-z]*");
        JsonSchemaBuilder b = jsonSchema().pattern("[a-z]*");
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(jsonSchema().pattern("[A-Z]*"));
    }
}