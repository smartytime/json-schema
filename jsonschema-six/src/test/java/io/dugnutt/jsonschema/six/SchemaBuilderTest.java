package io.dugnutt.jsonschema.six;

import io.dugnutt.jsonschema.TestUtils;
import io.dugnutt.jsonschema.six.keywords.ArrayKeywords;
import io.dugnutt.jsonschema.six.keywords.NumberKeywords;
import io.dugnutt.jsonschema.six.keywords.ObjectKeywords;
import io.dugnutt.jsonschema.six.keywords.StringKeywords;
import io.dugnutt.jsonschema.utils.JsonUtils;
import org.junit.Test;

import java.net.URI;
import java.util.regex.Pattern;

import static io.dugnutt.jsonschema.six.Schema.jsonSchemaBuilder;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaType.ARRAY;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaType.BOOLEAN;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaType.INTEGER;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaType.NULL;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaType.NUMBER;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaType.OBJECT;
import static io.dugnutt.jsonschema.six.enums.JsonSchemaType.STRING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class SchemaBuilderTest {

    @Test
    public void buildAllStringKeywords() {
        final Schema stringSchema = jsonSchemaBuilder()
                .type(STRING)
                .minLength(3)
                .maxLength(6)
                .pattern("[a-z]*")
                .format("uri-template")
                .build();

        assertThat(stringSchema.hasStringKeywords())
                .isTrue();
        assertThat(stringSchema.getTypes())
                .hasSize(1)
                .containsExactly(STRING);

        final StringKeywords keywords = stringSchema.getStringKeywords();
        assertSoftly(a -> {
            a.assertThat(keywords.getMinLength())
                    .isNotNull()
                    .isEqualTo(3);
            a.assertThat(keywords.getMaxLength())
                    .isNotNull()
                    .isEqualTo(6);
            a.assertThat(keywords.getFormat())
                    .isNotEmpty().isNotNull()
                    .isEqualTo("uri-template");
            a.assertThat(keywords.getPattern())
                    .isNotNull();
            a.assertThat(keywords.getPattern().pattern())
                    .isEqualTo("[a-z]*");
        });
    }

    @Test
    public void buildAllNumberKeywords() {
        final Schema numberSchema = jsonSchemaBuilder()
                .type(NUMBER)
                .minimum(1)
                .maximum(9)
                .exclusiveMinimum(0)
                .exclusiveMaximum(10)
                .multipleOf(3.4d)
                .build();

        assertThat(numberSchema.hasNumberKeywords()).isTrue();
        final NumberKeywords keywords = numberSchema.getNumberKeywords();
        assertSoftly(a -> {
            a.assertThat(keywords.getMaximum())
                    .isNotNull()
                    .isEqualTo(9);
            a.assertThat(keywords.getMinimum())
                    .isNotNull()
                    .isEqualTo(1);
            a.assertThat(keywords.getExclusiveMaximum())
                    .isNotNull()
                    .isEqualTo(10);
            a.assertThat(keywords.getExclusiveMinimum())
                    .isNotNull()
                    .isEqualTo(0);

            a.assertThat(keywords.getMultipleOf())
                    .isNotNull()
                    .isEqualTo(3.4d);
        });
    }

    @Test
    public void buildArrayKeywords_AllItemSchema() {
        final Schema arraySchema = jsonSchemaBuilder()
                .type(ARRAY)
                .minItems(1)
                .maxItems(10)
                .needsUniqueItems(true)
                .allItemSchema(jsonSchemaBuilder()
                        .notSchema(
                                jsonSchemaBuilder().type(NULL)
                        )
                )
                .containsSchema(jsonSchemaBuilder()
                        .type(STRING)
                )
                .build();

        assertThat(arraySchema.hasArrayKeywords()).isTrue();
        final ArrayKeywords keywords = arraySchema.getArrayKeywords();
        assertSoftly(a -> {
            a.assertThat(keywords.getMinItems())
                    .isNotNull()
                    .isEqualTo(1);
            a.assertThat(keywords.getMaxItems())
                    .isNotNull()
                    .isEqualTo(10);
            a.assertThat(keywords.findAllItemSchema())
                    .isPresent()
                    .hasValue(jsonSchemaBuilder().notSchema(
                            jsonSchemaBuilder().type(NULL)
                    ).build());
            final Schema allItemSchema = keywords.getAllItemSchema();
            a.assertThat(allItemSchema.getLocation().getJsonPointerFragment())
                    .isEqualTo(URI.create("#/items"));

            a.assertThat(keywords.findContainsSchema())
                    .isPresent()
                    .hasValue(jsonSchemaBuilder().type(STRING).build());
            final Schema containschema = keywords.getContainsSchema();
            a.assertThat(containschema.getLocation().getJsonPointerFragment())
                    .isEqualTo(URI.create("#/contains"));
        });
    }

    @Test
    public void buildArrayKeywords_IndexedItemSchema() {
        final Schema arraySchema = jsonSchemaBuilder()
                .type(ARRAY)
                .itemSchema(jsonSchemaBuilder().type(STRING))
                .itemSchema(jsonSchemaBuilder().type(NUMBER))
                .itemSchema(jsonSchemaBuilder().type(BOOLEAN))
                .schemaOfAdditionalItems(jsonSchemaBuilder().type(STRING))
                .build();

        assertThat(arraySchema.hasArrayKeywords()).isTrue();
        final ArrayKeywords keywords = arraySchema.getArrayKeywords();
        assertSoftly(a -> {
            a.assertThat(keywords.getItemSchemas())
                    .isNotNull()
                    .hasSize(3)
                    .element(0)
                    .isEqualTo(jsonSchemaBuilder().type(STRING).build());
            final URI fragmentIdx0 = keywords.getItemSchemas().get(0).getLocation().getJsonPointerFragment();
            assertThat(fragmentIdx0)
                    .isNotNull()
                    .hasToString("#/items/0");
            a.assertThat(keywords.findSchemaOfAdditionalItems())
                    .isPresent()
                    .hasValue(jsonSchemaBuilder().type(STRING).build());
            a.assertThat(keywords.getSchemaOfAdditionalItems().getLocation().getJsonPointerFragment())
                    .hasToString("#/additionalItems");
        });
    }

    @Test
    public void buildObjectKeywords_AllKeywords() {
        final Pattern pattern = Pattern.compile("[A-Z]*");

        final Schema objectSchema = jsonSchemaBuilder()
                .type(OBJECT)
                .propertySchema("name", jsonSchemaBuilder().type(STRING))
                .propertySchema("age", jsonSchemaBuilder().type(NUMBER))
                .patternProperty(pattern, jsonSchemaBuilder().type(OBJECT))
                .schemaOfAdditionalProperties(jsonSchemaBuilder().type(ARRAY))
                .requiredProperty("name")
                .requiredProperty("another")
                .minProperties(1)
                .maxProperties(10)
                .schemaDependency("email", jsonSchemaBuilder().maxProperties(4))
                .propertyDependency("age", "email")
                .build();

        assertThat(objectSchema.hasObjectKeywords()).isTrue();
        final ObjectKeywords keywords = objectSchema.getObjectKeywords();
        assertSoftly(a -> {
            a.assertThat(keywords.getMinProperties()).isNotNull().isEqualTo(1);
            a.assertThat(keywords.getMaxProperties()).isNotNull().isEqualTo(10);
            a.assertThat(keywords.getRequiredProperties()).contains("name", "another");
            a.assertThat(keywords.getPropertyDependencies().keySet()).hasSize(1);
            a.assertThat(keywords.getPropertyDependencies().entries()).hasSize(1);
            a.assertThat(keywords.getPatternProperties())
                    .isNotNull()
                    .hasSize(1)
                    .containsEntry(pattern, jsonSchemaBuilder().type(OBJECT).build());

            a.assertThat(keywords.getSchemaDependencies())
                    .isNotNull()
                    .hasSize(1)
                    .containsEntry("email", jsonSchemaBuilder().maxProperties(4).build());
        });
    }

    @Test
    public void buildSharedKeywords_AllKeywords() {
        final Schema objectSchema = Schema.jsonSchemaBuilderWithId("#doopy")
                .title("My Title")
                .description("A description")
                .defaultValue(TestUtils.jsonString("A default value"))
                .allOfSchema(jsonSchemaBuilder().enumValues(JsonUtils.jsonArray(1, 2, 3, 4)))
                .allOfSchema(jsonSchemaBuilder().enumValues(JsonUtils.jsonArray(3, 4, 5, 6)))
                .anyOfSchema(jsonSchemaBuilder().constValueDouble(4))
                .anyOfSchema(jsonSchemaBuilder().constValueDouble(46))
                .oneOfSchema(jsonSchemaBuilder().minimum(2))
                .oneOfSchema(jsonSchemaBuilder().maximum(50))
                .type(NUMBER)
                .type(INTEGER)
                .notSchema(jsonSchemaBuilder().constValueDouble(21))
                .enumValues(JsonUtils.jsonArray(1, 2, 3, 4, 5, 6, 7, 8, 9))
                .build();

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
                    .hasValue(jsonSchemaBuilder().constValueDouble(21).build());
            a.assertThat(objectSchema.getEnumValues())
                    .isPresent()
                    .hasValue(JsonUtils.jsonArray(1, 2, 3, 4, 5, 6, 7, 8, 9));

        });
    }
}