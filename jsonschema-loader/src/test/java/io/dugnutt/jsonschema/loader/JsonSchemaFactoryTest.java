package io.dugnutt.jsonschema.loader;

import com.google.common.collect.ImmutableSet;
import io.dugnutt.jsonschema.loader.reference.DefaultSchemaClient;
import io.dugnutt.jsonschema.loader.reference.SchemaClient;
import io.dugnutt.jsonschema.six.JsonSchema;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.NumberKeywords;
import io.dugnutt.jsonschema.six.ObjectKeywords;
import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.six.StringKeywords;
import io.dugnutt.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.function.Supplier;

import static io.dugnutt.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static javax.json.spi.JsonProvider.provider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.Assert.assertEquals;

public class JsonSchemaFactoryTest extends BaseLoaderTest {

    public JsonSchemaFactoryTest() {
        super("testschemas.json");
    }

    @Test
    public void booleanSchema() {
        JsonSchema actual = getSchemaForKey("booleanSchema");
        Assert.assertNotNull(actual);
    }

    @Test
    public void emptyPatternProperties() {
        JsonSchema actual = getSchemaForKey("emptyPatternProperties");
        assertSoftly(a -> {
            a.assertThat(actual).isNotNull();
            a.assertThat(actual.getObjectKeywords())
                    .isPresent()
                    .hasValueSatisfying(keywords -> {
                        a.assertThat(keywords.getPatternProperties())
                                .hasSize(0);
                    });
        });
    }

    @Test
    public void emptySchema() {
        final JsonSchema emptySchema = getSchemaForKey("emptySchema");
        assertSoftly(a -> {
            a.assertThat(emptySchema)
                    .isNotNull();
            a.assertThat(emptySchema.toString())
                    .isEqualTo("{}");
        });
    }

    @Test
    public void emptySchemaWithDefault() {
        final JsonSchema emptySchema = getSchemaForKey("emptySchemaWithDefault");
        assertSoftly(a -> {
            a.assertThat(emptySchema)
                    .isNotNull();
            a.assertThat(emptySchema.toString())
                    .isNotEqualTo("{}");
        });
    }

    @Test
    public void enumSchema() {
        JsonSchema actual = getSchemaForKey("enumSchema");
        assertSoftly(a -> {
            a.assertThat(actual).isNotNull();
            a.assertThat(actual.getEnumValues().orElse(null))
                    .isNotNull()
                    .hasSize(4);
        });
    }

    @Test
    public void genericProperties() {
        JsonSchema actual = getSchemaForKey("genericProperties");
        assertEquals("myId", actual.getId().toString());
        assertEquals("my title", actual.getTitle());
        assertEquals("my description", actual.getDescription());
    }

    @Test
    public void implicitAnyOfLoadsTypeProps() {
        JsonSchema schema = getSchemaForKey("multipleTypesWithProps");
        assertSoftly(a -> {
            a.assertThat(schema.getStringKeywords().orElse(null))
                    .isNotNull()
                    .extracting(StringKeywords::getMinLength)
                    .containsExactly(3);

            a.assertThat(schema.getNumberKeywords().orElse(null))
                    .isNotNull()
                    .extracting(NumberKeywords::getMinimum)
                    .containsExactly(5);
        });
    }

    @Test(expected = SchemaException.class)
    public void invalidExclusiveMinimum() {
        getSchemaForKey("invalidExclusiveMinimum");
    }

    @Test(expected = SchemaException.class)
    public void invalidNumberSchema() {
        JsonObject input = getJsonObjectForKey("invalidNumberSchema");
        schemaFactory().load(input);
    }

    @Test(expected = SchemaException.class)
    public void invalidStringSchema() {
        getSchemaForKey("invalidStringSchema");
    }

    @Test(expected = SchemaException.class)
    public void invalidType() {
        getSchemaForKey("invalidType");
    }

    @Test
    public void jsonPointerInArray() {
        final JsonSchema jsonSchema = getSchemaForKey("jsonPointerInArray");
        assertThat(jsonSchema.getArrayKeywords()).isPresent();
    }

    @Test
    public void multipleTypes() {
        final JsonSchema multipleTypes = getSchemaForKey("multipleTypes");
        assertThat(multipleTypes)
                .isNotNull()
                .extracting(JsonSchema::getTypes)
                .contains(ImmutableSet.of(JsonSchemaType.STRING, JsonSchemaType.BOOLEAN));
    }

    @Test
    public void neverMatchingAnyOf() {
        final JsonSchema anyOfNeverMatches = getSchemaForKey("anyOfNeverMatches");
        assertThat(anyOfNeverMatches.getTypes())
                .isEqualTo(ImmutableSet.of(JsonSchemaType.STRING));
    }

    @Test
    public void noExplicitObject() {
        JsonSchema actual = getSchemaForKey("noExplicitObject");
        assertThat(actual.getTypes()).isEmpty();
    }

    @Test
    public void notSchema() {
        JsonSchema actual = getSchemaForKey("notSchema");
        assertThat(actual.getNotSchema()).isPresent();
    }

    @Test
    public void nullSchema() {
        JsonSchema actual = getSchemaForKey("nullSchema");
        assertThat(actual).isNotNull();
    }

    @Test
    public void numberSchema() {
        JsonSchema actual = getSchemaForKey("numberSchema");
        assertThat(actual.getNumberKeywords()).isPresent();
        assertSoftly(a -> {
            a.assertThat(actual.getTypes()).containsExactly(JsonSchemaType.NUMBER);
            final NumberKeywords keywords = actual.getNumberKeywords().get();
            a.assertThat(keywords.getMinimum()).isEqualTo(10);
            a.assertThat(keywords.getMaximum()).isEqualTo(20);
            a.assertThat(keywords.getExclusiveMaximum()).isEqualTo(21);
            a.assertThat(keywords.getExclusiveMinimum()).isEqualTo(11);
            a.assertThat(keywords.getMultipleOf()).isEqualTo(5);
        });
    }

    private Supplier<AssertionError> missingReference() {
        return () -> new AssertionError("Missing reference schema");
    }

    @Test
    public void pointerResolution() {
        JsonSchema actual = getSchemaForKey("pointerResolution");

        assertThat(actual.getObjectKeywords()).isPresent();
        assertSoftly(a -> {
            final ObjectKeywords objectKeywords = actual.getObjectKeywords().get();
            final JsonSchema rectangleSchema = objectKeywords.getPropertySchemas().get("rectangle");
            a.assertThat(rectangleSchema).isNotNull();
            a.assertThat(rectangleSchema.getObjectKeywords()).isPresent();

            assertSoftly(ref -> {
                final ObjectKeywords refSchemaKeywords = rectangleSchema.getObjectKeywords().get();
                final JsonSchema schemaA = refSchemaKeywords.getPropertySchemas().get("a");
                ref.assertThat(schemaA).isNotNull();
                ref.assertThat(schemaA.getNumberKeywords()).isPresent();
                ref.assertThat(schemaA.getNumberKeywords().get().getMinimum()).isEqualTo(0);
            });
        });
    }

    @Test(expected = SchemaException.class)
    public void pointerResolutionFailure() {
        getSchemaForKey("pointerResolutionFailure");
    }

    @Test(expected = SchemaException.class)
    public void pointerResolutionQueryFailure() {
        getSchemaForKey("pointerResolutionQueryFailure");
    }

    @Test
    public void recursiveSchema() {
        getSchemaForKey("recursiveSchema");
    }

    @Test
    public void refWithType() {
        JsonSchema actualRoot = getSchemaForKey("refWithType");
        assertThat(actualRoot).isNotNull();
        assertThat(actualRoot.getObjectKeywords()).isPresent();
        assertSoftly(a -> {
            final ObjectKeywords keywords = actualRoot.getObjectKeywords().get();
            final JsonSchema prop = keywords.getPropertySchemas().get("prop");
            a.assertThat(prop).isNotNull();
            a.assertThat(prop.getObjectKeywords()).isPresent();
            a.assertThat(prop.getObjectKeywords().get().getRequiredProperties())
                    .containsExactly("a", "b");
        });
    }

    @Test
    public void remotePointerResulion() {
        SchemaClient httpClient = Mockito.mock(SchemaClient.class);
        Mockito.when(httpClient.fetchSchema("http://example.org/asd")).thenReturn(asStream("{}"));
        Mockito.when(httpClient.fetchSchema("http://example.org/otherschema.json")).thenReturn(asStream("{}"));
        Mockito.when(httpClient.fetchSchema("http://example.org/folder/subschemaInFolder.json")).thenReturn(
                asStream("{}"));
        schemaFactory()
                .withHttpClient(httpClient)
                .load(getJsonObjectForKey("remotePointerResolution"));
    }

    @Test
    public void resolutionScopeTest() {
        SchemaClient schemaClient = url -> {
            System.out.println("GET " + url);
            return new DefaultSchemaClient().fetchSchema(url);
        };
        schemaFactory()
                .withHttpClient(schemaClient)
                .load(getJsonObjectForKey("resolutionScopeTest"));
    }

    @Test
    public void schemaJsonIdIsRecognized() {
        SchemaClient client = Mockito.mock(SchemaClient.class);
        ByteArrayInputStream retval = new ByteArrayInputStream("{}".getBytes());
        Mockito.when(client.fetchSchema("http://example.org/schema/schema.json")).thenReturn(retval);
        JsonSchemaFactory.schemaFactory()
                .withHttpClient(client)
                .load(getJsonObjectForKey("schemaWithId"));
    }

    @Test
    public void schemaPointerIsPopulated() {
        JsonObject rawSchema = JsonUtils.readResourceAsJson("/tests/objecttestschemas.json", JsonObject.class)
                .getJsonObject("objectWithSchemaDep");
        JsonSchema schema = schemaFactory().load(rawSchema);

        assertThat(schema).isNotNull();
        assertThat(schema.getObjectKeywords()).isPresent();
        assertSoftly(a -> {
            final ObjectKeywords keywords = schema.getObjectKeywords().get();
            a.assertThat(keywords.getSchemaDependencies())
                    .isNotNull()
                    .isNotEmpty();
            final String actualSchemaPointer = keywords.getSchemaDependencies().get("a").getLocation().getJsonPointerFragment().toString();
            assertThat(actualSchemaPointer).isEqualTo("#/dependencies/a");
        });
    }

    @Test
    public void selfRecursiveSchema() {
        getSchemaForKey("selfRecursiveSchema");
    }

    @Test
    public void sniffByFormat() {
        JsonObject schema = provider().createObjectBuilder().add("format", "hostname").build();
        JsonSchema actual = JsonSchemaFactory.schemaFactory().load(schema);
        assertThat(actual.getStringKeywords()).isPresent();
        assertThat(actual.getStringKeywords().get().getFormat()).isEqualTo("hostname");
    }

    @Test
    public void stringSchema() {
        JsonSchema actual =  getSchemaForKey("stringSchema");
        assertThat(actual.getStringKeywords()).isPresent();
        assertThat(actual.getStringKeywords().get().getMinLength()).isEqualTo(2);
        assertThat(actual.getStringKeywords().get().getMaxLength()).isEqualTo(3);
    }

    @Test
    public void tupleSchema() {
        JsonSchema actual =  getSchemaForKey("tupleSchema");
        assertThat(actual.getArrayKeywords()).isPresent();
        assertThat(actual.getArrayKeywords().get().getAllItemSchema()).isNull();
        assertThat(actual.getArrayKeywords().get().getItemSchemas()).hasSize(2);
    }

    //todo:ericm Test nulls everywhere
    @Test(expected = SchemaException.class)
    public void unknownSchema() {
        getSchemaForKey("unknown");
    }

    @Test
    public void unsupportedFormat() {
        JsonObject schema = provider().createObjectBuilder()
                .add("type", "string")
                .add("format", "unknown")
                .build();
        JsonSchemaFactory.schemaFactory().load(schema);
    }

    private InputStream asStream(final String string) {
        return new ByteArrayInputStream(string.getBytes());
    }
}
