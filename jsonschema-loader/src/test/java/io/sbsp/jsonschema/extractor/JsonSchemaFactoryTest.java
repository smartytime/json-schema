package io.sbsp.jsonschema.extractor;

import com.google.common.collect.ImmutableSet;
import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.ReferenceSchema;
import io.sbsp.jsonschema.SchemaException;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.extractor.reference.DefaultSchemaClient;
import io.sbsp.jsonschema.extractor.reference.SchemaClient;
import io.sbsp.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Supplier;

import static io.sbsp.jsonschema.extractor.JsonSchemaFactory.schemaFactory;
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
        Draft6Schema actual = getSchemaForKey("booleanSchema");
        Assert.assertNotNull(actual);
    }

    @Test
    public void emptyPatternProperties() {
        Draft6Schema actual = getSchemaForKey("emptyPatternProperties");
        assertSoftly(a -> {
            a.assertThat(actual).isNotNull();
        });
    }

    @Test
    public void emptySchema() {
        final Draft6Schema emptySchema = getSchemaForKey("emptySchema");
        assertSoftly(a -> {
            a.assertThat(emptySchema).isNotNull();
            a.assertThat(emptySchema.toString()).isEqualToIgnoringWhitespace("{}");
        });
    }

    @Test
    public void emptySchemaWithDefault() {
        final Draft6Schema emptySchema = getSchemaForKey("emptySchemaWithDefault");
        assertSoftly(a -> {
            a.assertThat(emptySchema)
                    .isNotNull();
            a.assertThat(emptySchema.toString())
                    .isNotEqualTo("{}");
        });
    }

    @Test
    public void enumSchema() {
        Draft6Schema actual = getSchemaForKey("enumSchema");
        assertSoftly(a -> {
            a.assertThat(actual).isNotNull();
            a.assertThat(actual.getEnumValues().orElse(null))
                    .isNotNull()
                    .hasSize(4);
        });
    }

    @Test
    public void genericProperties() {
        Draft6Schema actual = getSchemaForKey("genericProperties");
        assertEquals("myId", actual.getId().toString());
        assertEquals("my title", actual.getTitle());
        assertEquals("my description", actual.getDescription());
    }

    @Test
    public void implicitAnyOfLoadsTypeProps() {
        Draft6Schema Draft6Schema = getSchemaForKey("multipleTypesWithProps");
        assertSoftly(a -> {
            a.assertThat(Draft6Schema.getMinLength()).isEqualTo(3);
            a.assertThat(Draft6Schema.getMinimum()).isEqualTo(5);
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
        final Draft6Schema jsonSchema = getSchemaForKey("jsonPointerInArray");
        assertThat(jsonSchema.getItemSchemas()).hasSize(1);
        assertThat(jsonSchema.getItemSchemas().get(0))
                .isInstanceOf(ReferenceSchema.class);
    }

    @Test
    public void multipleTypes() {
        final Draft6Schema multipleTypes = getSchemaForKey("multipleTypes");
        assertThat(multipleTypes).isNotNull();
        assertThat(multipleTypes.getTypes())
                .contains(JsonSchemaType.STRING, JsonSchemaType.BOOLEAN);
    }

    @Test
    public void neverMatchingAnyOf() {
        final Draft6Schema anyOfNeverMatches = getSchemaForKey("anyOfNeverMatches");
        assertThat(anyOfNeverMatches.getTypes())
                .isEqualTo(ImmutableSet.of(JsonSchemaType.STRING));
    }

    @Test
    public void noExplicitObject() {
        Draft6Schema actual = getSchemaForKey("noExplicitObject");
        assertThat(actual.getTypes()).isEmpty();
    }

    @Test
    public void notSchema() {
        Draft6Schema actual = getSchemaForKey("notSchema");
        assertThat(actual.getNotSchema()).isPresent();
    }

    @Test
    public void nullSchema() {
        Draft6Schema actual = getSchemaForKey("nullSchema");
        assertThat(actual).isNotNull();
    }

    @Test
    public void numberSchema() {
        Draft6Schema schema = getSchemaForKey("numberSchema");
        assertSoftly(a -> {
            a.assertThat(schema.getTypes()).containsExactly(JsonSchemaType.NUMBER);
            a.assertThat(schema.getMinimum().intValue()).isEqualTo(10);
            a.assertThat(schema.getMaximum().intValue()).isEqualTo(20);
            a.assertThat(schema.getExclusiveMaximum().intValue()).isEqualTo(21);
            a.assertThat(schema.getExclusiveMinimum().intValue()).isEqualTo(11);
            a.assertThat(schema.getMultipleOf().intValue()).isEqualTo(5);
        });
    }

    private Supplier<AssertionError> missingReference() {
        return () -> new AssertionError("Missing reference Draft6Schema");
    }

    @Test
    public void pointerResolution() {
        Draft6Schema actual = getSchemaForKey("pointerResolution");

        assertSoftly(a -> {
            final Draft6Schema rectangleSchema = actual.getProperties().get("rectangle").asDraft6();
            a.assertThat(rectangleSchema).isNotNull();

            final Draft6Schema schemaA = rectangleSchema.getProperties().get("a").asDraft6();
            a.assertThat(schemaA).isNotNull();
            a.assertThat(schemaA.getMinimum().intValue()).isEqualTo(0);

        });
    }

    @Test(expected = SchemaException.class)
    public void pointerResolutionFailure() {
        final Draft6Schema pointerResolutionFailure = getSchemaForKey("pointerResolutionFailure");
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
        Draft6Schema actualRoot = getSchemaForKey("refWithType");
        assertThat(actualRoot).isNotNull();
        assertSoftly(a -> {
            final Draft6Schema prop = actualRoot.getPropertySchema("prop");
            a.assertThat(prop).isNotNull();
            a.assertThat(prop.getRequiredProperties()).containsExactly("a", "b");
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
        when(client.fetchSchema("http://example.org/Draft6Schema/Draft6Schema.json")).thenReturn(retval);
        when(client.fetchSchema(URI.create("http://example.org/Draft6Schema/Draft6Schema.json"))).thenReturn(retval);
        final JsonObject schemaWithId = getJsonObjectForKey("schemaWithId");
        JsonSchemaFactory factory = JsonSchemaFactory.builder().httpClient(client).build();
        factory.load(schemaWithId);
    }

    @Test
    public void schemaPointerIsPopulated() {
        JsonObject rawSchema = JsonUtils.readResourceAsJson("/tests/objecttestschemas.json", JsonObject.class)
                .getJsonObject("objectWithSchemaDep");
        Draft6Schema actual = schemaFactory().load(rawSchema).asDraft6();

        assertThat(actual).isNotNull();
        assertSoftly(a -> {
            a.assertThat(actual.getPropertySchemaDependencies())
                    .isNotNull()
                    .isNotEmpty();
            final String actualSchemaPointer = actual.getPropertySchemaDependencies()
                    .get("a")
                    .getLocation()
                    .getJsonPointerFragment()
                    .toString();
            assertThat(actualSchemaPointer).isEqualTo("#/dependencies/a");
        });
    }

    @Test
    public void selfRecursiveSchema() {
        getSchemaForKey("selfRecursiveSchema");
    }

    @Test
    public void sniffByFormat() {
        JsonObject schemaJson = provider().createObjectBuilder().add("format", "hostname").build();
        Draft6Schema actual = schemaFactory().load(schemaJson).asDraft6();
        assertThat(actual.getFormat()).isEqualTo("hostname");
    }

    @Test
    public void stringSchema() {
        Draft6Schema actual = getSchemaForKey("stringSchema");
        assertThat(actual.getMinLength()).isEqualTo(2);
        assertThat(actual.getMaxLength()).isEqualTo(3);
    }

    @Test
    public void tupleSchema() {
        Draft6Schema actual = getSchemaForKey("tupleSchema");
        assertThat(actual.getAllItemSchema()).isNotPresent();
        assertThat(actual.getItemSchemas()).hasSize(2);
    }

    //todo:ericm Test nulls everywhere
    @Test(expected = SchemaException.class)
    public void unknownSchema() {
        getSchemaForKey("unknown");
    }

    @Test
    public void unsupportedFormat() {
        JsonObject Draft6Schema = provider().createObjectBuilder()
                .add("type", "string")
                .add("format", "unknown")
                .build();
        schemaFactory().load(Draft6Schema);
    }

    private InputStream asStream(final String string) {
        return new ByteArrayInputStream(string.getBytes());
    }
}
