package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.loader.internal.DefaultSchemaClient;
import io.dugnutt.jsonschema.six.ArraySchema;
import io.dugnutt.jsonschema.six.BooleanSchema;
import io.dugnutt.jsonschema.six.CombinedSchema;
import io.dugnutt.jsonschema.six.EmptySchema;
import io.dugnutt.jsonschema.six.EnumSchema;
import io.dugnutt.jsonschema.six.NotSchema;
import io.dugnutt.jsonschema.six.NullSchema;
import io.dugnutt.jsonschema.six.NumberSchema;
import io.dugnutt.jsonschema.six.ObjectSchema;
import io.dugnutt.jsonschema.six.ReferenceSchema;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.six.StringSchema;
import io.dugnutt.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static io.dugnutt.jsonschema.loader.SchemaFactory.schemaFactory;
import static java.util.Arrays.asList;
import static javax.json.spi.JsonProvider.provider;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SchemaLoaderTest extends BaseLoaderTest {

    public SchemaLoaderTest() {
        super("testschemas.json");
    }

    @Test
    public void booleanSchema() {
        BooleanSchema actual = (BooleanSchema) getSchemaForKey("booleanSchema");
        Assert.assertNotNull(actual);
    }

    @Test
    public void builderUsesDefaultSchemaClient() {
        SchemaLoader.SchemaLoaderBuilder actual = SchemaLoader.builder();
        Assert.assertNotNull(actual);
        assertTrue(actual.httpClient instanceof DefaultSchemaClient);
    }

    @Test
    public void builderhasDefaultFormatValidators() {
        Assert.fail("Verify enum validator");
    }

    @Test
    public void customFormat() {
        Assert.fail("Support extra validators");
    }

    @Test
    public void emptyPatternProperties() {
        ObjectSchema actual = (ObjectSchema) getSchemaForKey("emptyPatternProperties");
        Assert.assertNotNull(actual);
        assertEquals(0, actual.getPatternProperties().size());
    }

    @Test
    public void emptySchema() {
        assertTrue(getSchemaForKey("emptySchema") instanceof EmptySchema);
    }

    @Test
    public void emptySchemaWithDefault() {
        EmptySchema actual = (EmptySchema) getSchemaForKey("emptySchemaWithDefault");
        Assert.assertNotNull(actual);
    }

    @Test
    public void enumSchema() {
        EnumSchema actual = (EnumSchema) getSchemaForKey("enumSchema");
        Assert.assertNotNull(actual);
        assertEquals(4, actual.getPossibleValues().size());
    }

    @Test
    public void genericProperties() {
        Schema actual = getSchemaForKey("genericProperties");
        assertEquals("myId", actual.getId());
        assertEquals("my title", actual.getTitle());
        assertEquals("my description", actual.getDescription());
    }

    @Test
    public void implicitAnyOfLoadsTypeProps() {
        CombinedSchema schema = (CombinedSchema) getSchemaForKey("multipleTypesWithProps");
        StringSchema stringSchema = schema.getSubSchemas().stream()
                .filter(sub -> sub instanceof StringSchema)
                .map(sub -> (StringSchema) sub)
                .findFirst().orElseThrow(() -> new AssertionError("no StringSchema"));
        NumberSchema numSchema = schema.getSubSchemas().stream()
                .filter(sub -> sub instanceof NumberSchema)
                .map(sub -> (NumberSchema) sub)
                .findFirst()
                .orElseThrow(() -> new AssertionError("no NumberSchema"));
        assertEquals(3, stringSchema.getMinLength().intValue());
        assertEquals(5, numSchema.getMinimum().intValue());
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
        assertTrue(getSchemaForKey("jsonPointerInArray") instanceof ArraySchema);
    }

    @Test
    public void multipleTypes() {
        assertTrue(getSchemaForKey("multipleTypes") instanceof CombinedSchema);
    }

    @Test
    public void neverMatchingAnyOf() {
        assertTrue(getSchemaForKey("anyOfNeverMatches") instanceof CombinedSchema);
    }

    @Test
    public void noExplicitObject() {
        ObjectSchema actual = (ObjectSchema) getSchemaForKey("noExplicitObject");
        Assert.assertFalse(actual.requiresObject());
    }

    @Test
    public void notSchema() {
        NotSchema actual = (NotSchema) getSchemaForKey("notSchema");
        Assert.assertNotNull(actual);
    }

    @Test
    public void nullSchema() {
        NullSchema actual = (NullSchema) getSchemaForKey("nullSchema");
        Assert.assertNotNull(actual);
    }

    @Test
    public void numberSchema() {
        NumberSchema actual = (NumberSchema) getSchemaForKey("numberSchema");
        assertEquals(10, actual.getMinimum().intValue());
        assertEquals(20, actual.getMaximum().intValue());
        assertEquals(5, actual.getMultipleOf().intValue());
        assertTrue(actual.isExclusiveMinimum());
        assertTrue(actual.isExclusiveMaximum());
        assertTrue(actual.isRequiresNumber());
    }

    @Test
    public void pointerResolution() {
        ObjectSchema actual = (ObjectSchema) getSchemaForKey("pointerResolution");
        ObjectSchema rectangleSchema = (ObjectSchema) ((ReferenceSchema) actual.getPropertySchemas()
                .get("rectangle"))
                .getReferredSchema();
        Assert.assertNotNull(rectangleSchema);
        ReferenceSchema aRef = (ReferenceSchema) rectangleSchema.getPropertySchemas().get("a");
        assertTrue(aRef.getReferredSchema() instanceof NumberSchema);
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
    public void propsAroundRefExtendTheReferredSchema() {
        ObjectSchema actual = (ObjectSchema) schemaFactory().load(getJsonObjectForKey("propsAroundRefExtendTheReferredSchema"));
        ObjectSchema prop = (ObjectSchema) ((ReferenceSchema) actual.getPropertySchemas().get("prop"))
                .getReferredSchema();
        assertTrue(prop.requiresObject());
        assertEquals(1, prop.getMinProperties().intValue());
    }

    @Test
    public void recursiveSchema() {
        getSchemaForKey("recursiveSchema");
    }

    @Test
    public void refWithType() {
        ObjectSchema actualRoot = (ObjectSchema) getSchemaForKey("refWithType");
        ReferenceSchema actual = (ReferenceSchema) actualRoot.getPropertySchemas().get("prop");
        ObjectSchema propSchema = (ObjectSchema) actual.getReferredSchema();
        assertEquals(propSchema.getRequiredProperties(), asList("a", "b"));
    }

    @Test
    public void remotePointerResulion() {
        SchemaClient httpClient = Mockito.mock(SchemaClient.class);
        Mockito.when(httpClient.get("http://example.org/asd")).thenReturn(asStream("{}"));
        Mockito.when(httpClient.get("http://example.org/otherschema.json")).thenReturn(asStream("{}"));
        Mockito.when(httpClient.get("http://example.org/folder/subschemaInFolder.json")).thenReturn(
                asStream("{}"));
        schemaFactory()
                .withHttpClient(httpClient)
                .load(getJsonObjectForKey("remotePointerResolution"));
    }

    @Test
    public void resolutionScopeTest() {
        SchemaClient schemaClient = url -> {
            System.out.println("GET " + url);
            return new DefaultSchemaClient().get(url);
        };
        schemaFactory()
                .withHttpClient(schemaClient)
                .load(getJsonObjectForKey("resolutionScopeTest"));
    }

    @Test
    public void schemaJsonIdIsRecognized() {
        SchemaClient client = Mockito.mock(SchemaClient.class);
        ByteArrayInputStream retval = new ByteArrayInputStream("{}".getBytes());
        Mockito.when(client.get("http://example.org/schema/schema.json")).thenReturn(retval);
        SchemaLoader.builder().schemaJson(getJsonObjectForKey("schemaWithId"))
                .httpClient(client)
                .build().load();
    }

    @Test
    public void schemaPointerIsPopulated() {
        JsonObject rawSchema = JsonUtils.readResource("objecttestschemas.json", JsonObject.class)
                .getJsonObject("objectWithSchemaDep");
        ObjectSchema schema = (ObjectSchema) schemaFactory().load(rawSchema);

        String actualSchemaPointer = schema.getSchemaDependencies().get("a").getSchemaLocation();
        Assert.fail("Not sure how to validate pointers to uri fragments");
        // String expectedSchemaPointer = new JSONPointer(asList("dependencies", "a")).toURIFragment();
        // assertEquals(expectedSchemaPointer, actualSchemaPointer);
    }

    @Test
    public void selfRecursiveSchema() {
        getSchemaForKey("selfRecursiveSchema");
    }

    @Test
    public void sniffByFormat() {
        JsonObject schema = provider().createObjectBuilder().add("format", "hostname").build();
        Schema actual = SchemaLoader.builder().schemaJson(schema).build().load().build();
        assertTrue(actual instanceof StringSchema);
    }

    @Test
    public void stringSchema() {
        StringSchema actual = (StringSchema) getSchemaForKey("stringSchema");
        assertEquals(2, actual.getMinLength().intValue());
        assertEquals(3, actual.getMaxLength().intValue());
    }

    @Test
    public void stringSchemaWithFormat() {
        StringSchema subject = (StringSchema) getSchemaForKey("stringSchemaWithFormat");
        LoadingTestSupport.expectFailure(subject, "asd");
    }

    @Test
    public void tupleSchema() {
        ArraySchema actual = (ArraySchema) getSchemaForKey("tupleSchema");
        Assert.assertFalse(actual.isPermitsAdditionalItems());
        Assert.assertNull(actual.getAllItemSchema());
        assertEquals(2, actual.getItemSchemas().size());
        assertEquals(BooleanSchema.INSTANCE, actual.getItemSchemas().get(0));
        assertEquals(NullSchema.INSTANCE, actual.getItemSchemas().get(1));
    }

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
        SchemaLoader.builder().schemaJson(schema).build().load();
    }

    @Test
    public void withoutFragment() {
        String actual = ReferenceLookup.withoutFragment("http://example.com#frag").toString();
        assertEquals("http://example.com", actual);
    }

    @Test
    public void withoutFragmentNoFragment() {
        String actual = ReferenceLookup.withoutFragment("http://example.com").toString();
        assertEquals("http://example.com", actual);
    }

    private InputStream asStream(final String string) {
        return new ByteArrayInputStream(string.getBytes());
    }
}
