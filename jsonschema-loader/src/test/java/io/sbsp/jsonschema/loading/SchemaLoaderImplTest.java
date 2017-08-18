package io.sbsp.jsonschema.loading;

import com.google.common.collect.ImmutableSet;
import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.RefSchema;
import io.sbsp.jsonschema.ResourceLoader;
import io.sbsp.jsonschema.SchemaException;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.loading.reference.DefaultJsonDocumentClient;
import io.sbsp.jsonschema.loading.reference.JsonDocumentClient;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;
import java.net.URI;
import java.util.function.Supplier;

import static io.sbsp.jsonschema.loading.SchemaLoaderImpl.schemaLoader;
import static io.sbsp.jsonschema.utils.JsonUtils.blankJsonObject;
import static javax.json.spi.JsonProvider.provider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class SchemaLoaderImplTest extends BaseLoaderTest {

    public SchemaLoaderImplTest() {
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
        Draft6Schema schema = getSchemaForKey("multipleTypesWithProps");
        assertSoftly(a -> {
            a.assertThat(schema.getMinLength()).isEqualTo(3);
            a.assertThat(schema.getMinimum()).isEqualTo(5);
        });
    }

    @Test(expected = SchemaException.class)
    public void invalidExclusiveMinimum() {
        getSchemaForKey("invalidExclusiveMinimum");
    }

    @Test(expected = SchemaException.class)
    public void invalidNumberSchema() {
        JsonObject input = getJsonObjectForKey("invalidNumberSchema");
        SchemaLoaderImpl.schemaLoader().readSchema(input);
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
        assertThat(jsonSchema.getItemSchemas()).hasSize(2);
        assertThat(jsonSchema.getItemSchemas().get(1))
                .isInstanceOf(RefSchema.class);
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
            a.assertThat(schema.getMaximum()).isNotNull().isEqualTo(20);
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
        JsonDocumentClient documentClient = spy(DefaultJsonDocumentClient.getInstance());

        doReturn(blankJsonObject()).when(documentClient).fetchDocument(URI.create("http://example.org/asd"));
        doReturn(blankJsonObject()).when(documentClient).fetchDocument(URI.create("http://example.org/otherschema.json"));
        doReturn(blankJsonObject()).when(documentClient).fetchDocument(URI.create("http://example.org/folder/subschemaInFolder.json"));

        SchemaLoaderImpl factory = SchemaLoaderImpl.builder().documentClient(documentClient).build();
        factory.readSchema(getJsonObjectForKey("remotePointerResolution"));
    }

    @Test
    public void resolutionScopeTest() {
        JsonDocumentClient jsonDocumentClient = DefaultJsonDocumentClient.getInstance();
        SchemaLoaderImpl factory = SchemaLoaderImpl.builder().documentClient(jsonDocumentClient).build();
        factory.readSchema(getJsonObjectForKey("resolutionScopeTest"));
    }

    @Test
    public void schemaJsonIdIsRecognized() {
        JsonDocumentClient client = spy(DefaultJsonDocumentClient.getInstance());
        JsonObject retval = blankJsonObject();
        doReturn(retval).when(client).fetchDocument("http://example.org/schema/schema.json");
        doReturn(retval).when(client).fetchDocument(URI.create("http://example.org/schema/schema.json"));
        final JsonObject schemaWithId = getJsonObjectForKey("schemaWithId");
        SchemaLoaderImpl factory = SchemaLoaderImpl.builder().documentClient(client).build();
        factory.readSchema(schemaWithId);
    }

    @Test
    public void schemaPointerIsPopulated() {
        JsonObject rawSchema = ResourceLoader.resourceLoaderForInstance(this).readJsonObject("objecttestschemas.json")
                .getJsonObject("objectWithSchemaDep");
        Draft6Schema actual = SchemaLoaderImpl.schemaLoader().readSchema(rawSchema).asDraft6();

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
        Draft6Schema actual = SchemaLoaderImpl.schemaLoader().readSchema(schemaJson).asDraft6();
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
        JsonObject schema = provider().createObjectBuilder()
                .add("type", "string")
                .add("format", "unknown")
                .build();
        SchemaLoaderImpl.schemaLoader().readSchema(schema);
    }
}
