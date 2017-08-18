package io.sbsp.jsonschema.six;

import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.TestUtils;
import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.utils.JsonUtils;
import io.sbsp.jsonschema.utils.Schemas;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import java.net.URI;
import java.util.regex.Pattern;

import static io.sbsp.jsonschema.JsonSchemaProvider.*;
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
        final Draft6Schema schema = schemaBuilder()
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
        final Draft6Schema schema = schemaBuilder()
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
        final Draft6Schema schema = schemaBuilder()
                .type(ARRAY)
                .minItems(1)
                .maxItems(10)
                .needsUniqueItems(true)
                .allItemSchema(schemaBuilder()
                        .notSchema(
                                schemaBuilder().type(NULL)
                        )
                )
                .containsSchema(schemaBuilder()
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
                    .hasValue(schemaBuilder().notSchema(schemaBuilder().type(NULL))
                            .build()
                            .asDraft6());
            final Schema allItemSchema = schema.getAllItemSchema().get();
            a.assertThat(allItemSchema.getLocation().getJsonPointerFragment())
                    .isEqualTo(URI.create("#/items"));

            a.assertThat(schema.getContainsSchema())
                    .isPresent()
                    .hasValue(schemaBuilder().type(STRING).build().asDraft6());

            final Schema containschema = schema.getContainsSchema().get();
            a.assertThat(containschema.getLocation().getJsonPointerFragment())
                    .isEqualTo(URI.create("#/contains"));
        });
    }

    @Test
    public void buildArrayKeywords_IndexedItemSchema() {
        final Draft6Schema draft6Schema = schemaBuilder()
                .type(ARRAY)
                .itemSchema(schemaBuilder().type(STRING))
                .itemSchema(schemaBuilder().type(NUMBER))
                .itemSchema(schemaBuilder().type(BOOLEAN))
                .schemaOfAdditionalItems(schemaBuilder().type(STRING))
                .build().asDraft6();

        assertSoftly(a -> {
            a.assertThat(draft6Schema.getItemSchemas())
                    .isNotNull()
                    .hasSize(3)
                    .element(0)
                    .isEqualTo(schemaBuilder().type(STRING).build());
            final URI fragmentIdx0 = draft6Schema.getItemSchemas().get(0).getLocation().getJsonPointerFragment();
            assertThat(fragmentIdx0)
                    .isNotNull()
                    .hasToString("#/items/0");
            a.assertThat(draft6Schema.getAdditionalItemsSchema())
                    .isPresent()
                    .hasValue(schemaBuilder().type(STRING).build().asDraft6());
            a.assertThat(draft6Schema.getAdditionalItemsSchema().get().getLocation().getJsonPointerFragment())
                    .hasToString("#/additionalItems");
        });
    }

    @Test
    public void buildArrayKeywords_NoAdditionalItems() {
        final Draft6Schema draft6Schema = schemaBuilder()
                .type(ARRAY)
                .noAdditionalItems()
                .build().asDraft6();

        assertSoftly(a -> {
            a.assertThat(draft6Schema.getAdditionalItemsSchema().get())
                    .isNotNull()
                    .isEqualTo(Schemas.falseSchema());
            a.assertThat(draft6Schema.getAdditionalItemsSchema().get().getLocation().getJsonPointerFragment())
                    .hasToString("#/additionalItems");
        });
    }

    @Test
    public void buildObjectKeywords_AllKeywords() {
        final Pattern pattern = Pattern.compile("[A-Z]*");

        final Draft6Schema schema = schemaBuilder()
                .type(OBJECT)
                .propertySchema("name", schemaBuilder().type(STRING))
                .propertySchema("age", schemaBuilder().type(NUMBER))
                .patternProperty(pattern, schemaBuilder().type(OBJECT))
                .schemaOfAdditionalProperties(schemaBuilder().type(ARRAY))
                .requiredProperty("name")
                .requiredProperty("another")
                .minProperties(1)
                .maxProperties(10)
                .schemaDependency("email", schemaBuilder().maxProperties(4))
                .build().asDraft6();

        assertSoftly(a -> {
            a.assertThat(schema.getMinProperties()).isNotNull().isEqualTo(1);
            a.assertThat(schema.getMaxProperties()).isNotNull().isEqualTo(10);
            a.assertThat(schema.getRequiredProperties()).contains("name", "another");
            a.assertThat(schema.getPatternProperties())
                    .isNotNull()
                    .hasSize(1)
                    .containsEntry(pattern.pattern(), schemaBuilder().type(OBJECT).build());

            a.assertThat(schema.getPropertySchemaDependencies())
                    .isNotNull()
                    .hasSize(1)
                    .containsEntry("email", schemaBuilder().maxProperties(4).build());
        });
    }

    @Test
    public void buildSharedKeywords_AllKeywords() {
        final Draft6Schema objectSchema = schemaBuilder("#doopy")
                .title("My Title")
                .description("A description")
                .defaultValue(TestUtils.jsonString("A default value"))
                .allOfSchema(schemaBuilder().enumValues(JsonUtils.jsonArray(1, 2, 3, 4)))
                .allOfSchema(schemaBuilder().enumValues(JsonUtils.jsonArray(3, 4, 5, 6)))
                .anyOfSchema(schemaBuilder().constValueDouble(4))
                .anyOfSchema(schemaBuilder().constValueDouble(46))
                .oneOfSchema(schemaBuilder().minimum(2))
                .oneOfSchema(schemaBuilder().maximum(50))
                .propertyDependency("age", "email")
                .type(NUMBER)
                .type(INTEGER)
                .notSchema(schemaBuilder().constValueDouble(21))
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

            a.assertThat(objectSchema.getPropertyDependencies().size()).isGreaterThan(0);
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
                    .hasValue(schemaBuilder().constValueDouble(21).build());
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
                .withPrefabValues(KeywordInfo.class, Keywords.$ID, Keywords.$SCHEMA)
                .withPrefabValues(SchemaBuilder.class,
                        schemaBuilder().type(STRING),
                        schemaBuilder().type(OBJECT))
                .withPrefabValues(Pattern.class, Pattern.compile("[a-z]*]"), Pattern.compile("[A-Z]*]"))
                .withOnlyTheseFields(
                        "keywords"
                )
                .verify();
    }

    @Test
    public void testBuilderEqualsWithPatternProperty() {
        SchemaBuilder a = schemaBuilder().patternProperty("[a-z]*", schemaBuilder());
        SchemaBuilder b = schemaBuilder().patternProperty("[a-z]*", schemaBuilder());
        assertThat(a).isEqualTo(b);
    }

    @Test
    public void testBuilderHashcodeMatchesWithPatternProperty() {
        SchemaBuilder a = schemaBuilder().patternProperty("[a-z]*", schemaBuilder());
        SchemaBuilder b = schemaBuilder().patternProperty("[a-z]*", schemaBuilder());
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    public void testBuilderEqualsWithPattern() {
        SchemaBuilder a = schemaBuilder().pattern("[a-z]*");
        SchemaBuilder b = schemaBuilder().pattern("[a-z]*");
        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(schemaBuilder().pattern("[A-Z]*"));
    }
}