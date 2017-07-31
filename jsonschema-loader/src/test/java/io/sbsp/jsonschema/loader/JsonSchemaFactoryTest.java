package io.sbsp.jsonschema.loader;

import com.google.common.collect.ImmutableSet;
import io.sbsp.jsonschema.loader.reference.DefaultSchemaClient;
import io.sbsp.jsonschema.loader.reference.SchemaClient;
import io.sbsp.jsonschema.six.enums.JsonSchemaType;
import io.sbsp.jsonschema.six.keywords.NumberKeywords;
import io.sbsp.jsonschema.six.keywords.ObjectKeywords;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.six.SchemaException;
import io.sbsp.jsonschema.six.keywords.StringKeywords;
import io.sbsp.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Supplier;

import static io.sbsp.jsonschema.loader.JsonSchemaFactory.schemaFactory;
import static javax.json.spi.JsonProvider.provider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonSchemaFactoryTest extends BaseLoaderTest {

    public JsonSchemaFactoryTest() {
        super("testschemas.json");
    }

    @Test
    public void booleanSchema() {
        Schema actual = getSchemaForKey("booleanSchema");
        Assert.assertNotNull(actual);
    }

    @Test
    public void emptyPatternProperties() {
        Schema actual = getSchemaForKey("emptyPatternProperties");
        assertSoftly(a -> {
            a.assertThat(actual).isNotNull();
            a.assertThat(actual.hasObjectKeywords()).isFalse();
        });
    }

    @Test
    public void emptySchema() {
        final Schema emptySchema = getSchemaForKey("emptySchema");
        assertSoftly(a -> {
            a.assertThat(emptySchema).isNotNull();
            a.assertThat(emptySchema.toString()).isEqualToIgnoringWhitespace("{}");
        });
    }

    @Test
    public void emptySchemaWithDefault() {
        final Schema emptySchema = getSchemaForKey("emptySchemaWithDefault");
        assertSoftly(a -> {
            a.assertThat(emptySchema)
                    .isNotNull();
            a.assertThat(emptySchema.toString())
                    .isNotEqualTo("{}");
        });
    }

    @Test
    public void enumSchema() {
        Schema actual = getSchemaForKey("enumSchema");
        assertSoftly(a -> {
            a.assertThat(actual).isNotNull();
            a.assertThat(actual.getEnumValues().orElse(null))
                    .isNotNull()
                    .hasSize(4);
        });
    }

    @Test
    public void genericProperties() {
        Schema actual = getSchemaForKey("genericProperties");
        assertEquals("myId", actual.getId().toString());
        assertEquals("my title", actual.getTitle());
        assertEquals("my description", actual.getDescription());
    }

    @Test
    public void implicitAnyOfLoadsTypeProps() {
        Schema schema = getSchemaForKey("multipleTypesWithProps");
        assertSoftly(a -> {
            a.assertThat(schema.getStringKeywords())
                    .isNotSameAs(StringKeywords.blankStringKeywords())
                    .extracting(StringKeywords::getMinLength)
                    .containsExactly(3);

            a.assertThat(schema.getNumberKeywords())
                    .isNotSameAs(NumberKeywords.blankNumberKeywords())
                    .extracting(k->k.getMinimum().intValue())
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
        final Schema jsonSchema = getSchemaForKey("jsonPointerInArray");
        assertThat(jsonSchema.hasArrayKeywords()).isTrue();
    }

    @Test
    public void multipleTypes() {
        final Schema multipleTypes = getSchemaForKey("multipleTypes");
        assertThat(multipleTypes)
                .isNotNull()
                .extracting(Schema::getTypes)
                .contains(ImmutableSet.of(JsonSchemaType.STRING, JsonSchemaType.BOOLEAN));
    }

    @Test
    public void neverMatchingAnyOf() {
        final Schema anyOfNeverMatches = getSchemaForKey("anyOfNeverMatches");
        assertThat(anyOfNeverMatches.getTypes())
                .isEqualTo(ImmutableSet.of(JsonSchemaType.STRING));
    }

    @Test
    public void noExplicitObject() {
        Schema actual = getSchemaForKey("noExplicitObject");
        assertThat(actual.getTypes()).isEmpty();
    }

    @Test
    public void notSchema() {
        Schema actual = getSchemaForKey("notSchema");
        assertThat(actual.getNotSchema()).isPresent();
    }

    @Test
    public void nullSchema() {
        Schema actual = getSchemaForKey("nullSchema");
        assertThat(actual).isNotNull();
    }

    @Test
    public void numberSchema() {
        Schema actual = getSchemaForKey("numberSchema");
        assertThat(actual.hasNumberKeywords()).isTrue();
        assertSoftly(a -> {
            a.assertThat(actual.getTypes()).containsExactly(JsonSchemaType.NUMBER);
            final NumberKeywords keywords = actual.getNumberKeywords();
            a.assertThat(keywords.getMinimum().intValue()).isEqualTo(10);
            a.assertThat(keywords.getMaximum().intValue()).isEqualTo(20);
            a.assertThat(keywords.getExclusiveMaximum().intValue()).isEqualTo(21);
            a.assertThat(keywords.getExclusiveMinimum().intValue()).isEqualTo(11);
            a.assertThat(keywords.getMultipleOf().intValue()).isEqualTo(5);
        });
    }

    private Supplier<AssertionError> missingReference() {
        return () -> new AssertionError("Missing reference schema");
    }

    @Test
    public void pointerResolution() {
        Schema actual = getSchemaForKey("pointerResolution");

        assertThat(actual.hasObjectKeywords()).isTrue();
        assertSoftly(a -> {
            final ObjectKeywords objectKeywords = actual.getObjectKeywords();
            final Schema rectangleSchema = objectKeywords.getPropertySchemas().get("rectangle");
            a.assertThat(rectangleSchema).isNotNull();
            a.assertThat(rectangleSchema.hasObjectKeywords()).isTrue();

            assertSoftly(ref -> {
                final ObjectKeywords refSchemaKeywords = rectangleSchema.getObjectKeywords();
                final Schema schemaA = refSchemaKeywords.getPropertySchemas().get("a");
                ref.assertThat(schemaA).isNotNull();
                ref.assertThat(schemaA.hasNumberKeywords());
                ref.assertThat(schemaA.getNumberKeywords().getMinimum().intValue()).isEqualTo(0);
            });
        });
    }

    @Test(expected = SchemaException.class)
    public void pointerResolutionFailure() {
        final Schema pointerResolutionFailure = getSchemaForKey("pointerResolutionFailure");
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
        Schema actualRoot = getSchemaForKey("refWithType");
        assertThat(actualRoot).isNotNull();
        assertThat(actualRoot.hasObjectKeywords()).isTrue();
        assertSoftly(a -> {
            final ObjectKeywords keywords = actualRoot.getObjectKeywords();
            final Schema prop = keywords.getPropertySchemas().get("prop");
            a.assertThat(prop).isNotNull();
            a.assertThat(prop.hasObjectKeywords()).isTrue();
            a.assertThat(prop.getObjectKeywords().getRequiredProperties())
                    .containsExactly("a", "b");
        });
    }

    @Test
    public void remotePointerResulion() {
        SchemaClient httpClient = mock(SchemaClient.class);
        when(httpClient.fetchSchema(URI.create("http://example.org/asd"))).thenReturn(asStream("{}"));
        when(httpClient.fetchSchema(URI.create("http://example.org/otherschema.json"))).thenReturn(asStream("{}"));
        when(httpClient.fetchSchema(URI.create("http://example.org/folder/subschemaInFolder.json"))).thenReturn(
                asStream("{}"));
        JsonSchemaFactory factory = JsonSchemaFactory.builder().httpClient(httpClient).build();
        factory.load(getJsonObjectForKey("remotePointerResolution"));
    }

    @Test
    public void resolutionScopeTest() {
        SchemaClient schemaClient = url -> {
            System.out.println("GET " + url);
            return new DefaultSchemaClient().fetchSchema(url);
        };
        JsonSchemaFactory factory = JsonSchemaFactory.builder().httpClient(schemaClient).build();
        factory.load(getJsonObjectForKey("resolutionScopeTest"));
    }

    @Test
    public void schemaJsonIdIsRecognized() {
        SchemaClient client = mock(SchemaClient.class);
        ByteArrayInputStream retval = new ByteArrayInputStream("{}".getBytes());
        when(client.fetchSchema("http://example.org/schema/schema.json")).thenReturn(retval);
        when(client.fetchSchema(URI.create("http://example.org/schema/schema.json"))).thenReturn(retval);
        final JsonObject schemaWithId = getJsonObjectForKey("schemaWithId");
        JsonSchemaFactory factory = JsonSchemaFactory.builder().httpClient(client).build();
        factory.load(schemaWithId);
    }

    @Test
    public void schemaPointerIsPopulated() {
        JsonObject rawSchema = JsonUtils.readResourceAsJson("/tests/objecttestschemas.json", JsonObject.class)
                .getJsonObject("objectWithSchemaDep");
        Schema schema = schemaFactory().load(rawSchema);

        assertThat(schema).isNotNull();
        assertThat(schema.hasObjectKeywords()).isTrue();
        assertSoftly(a -> {
            final ObjectKeywords keywords = schema.getObjectKeywords();
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
        Schema actual = schemaFactory().load(schema);
        assertThat(actual.hasStringKeywords()).isTrue();
        assertThat(actual.getStringKeywords().getFormat()).isEqualTo("hostname");
    }

    @Test
    public void stringSchema() {
        Schema actual =  getSchemaForKey("stringSchema");
        assertThat(actual.hasStringKeywords()).isTrue();
        assertThat(actual.getStringKeywords().getMinLength()).isEqualTo(2);
        assertThat(actual.getStringKeywords().getMaxLength()).isEqualTo(3);
    }

    @Test
    public void tupleSchema() {
        Schema actual =  getSchemaForKey("tupleSchema");
        assertThat(actual.hasArrayKeywords()).isTrue();
        assertThat(actual.getArrayKeywords().findAllItemSchema()).isNotPresent();
        assertThat(actual.getArrayKeywords().getItemSchemas()).hasSize(2);
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
        schemaFactory().load(schema);
    }

    private InputStream asStream(final String string) {
        return new ByteArrayInputStream(string.getBytes());
    }
}