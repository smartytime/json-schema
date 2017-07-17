package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.CombinedSchema;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.six.StringSchema;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;

import static io.dugnutt.jsonschema.loader.LoadingTestSupport.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author erosb
 */
public class CombinedSchemaFactoryTest extends BaseLoaderTest {

    public CombinedSchemaFactoryTest() {
        super("combinedtestschemas.json");
    }

    @Test
    public void combinedSchemaLoading() {
        CombinedSchema actual = (CombinedSchema) getSchemaForKey("combinedSchema");
        Assert.assertNotNull(actual);
    }

    @Test
    public void combinedSchemaWithBaseSchema() {
        CombinedSchema actual = (CombinedSchema) getSchemaForKey("combinedSchemaWithBaseSchema");
        assertEquals(1, actual.getSubSchemas().stream()
                .filter(schema -> schema instanceof StringSchema).count());
        assertEquals(1, actual.getSubSchemas().stream()
                .filter(schema -> schema instanceof CombinedSchema).count());
    }

    @Test
    public void combinedSchemaWithExplicitBaseSchema() {
        CombinedSchema actual = (CombinedSchema) getSchemaForKey("combinedSchemaWithExplicitBaseSchema");
        assertEquals(1, actual.getSubSchemas().stream()
                .filter(schema -> schema instanceof StringSchema).count());
        assertEquals(1, actual.getSubSchemas().stream()
                .filter(schema -> schema instanceof CombinedSchema).count());
    }

    @Test
    public void combinedSchemaWithMultipleBaseSchemas() {
        Schema actual = getSchemaForKey("combinedSchemaWithMultipleBaseSchemas");
        assertTrue(actual instanceof CombinedSchema);
    }

    @Test
    public void multipleKeywordsFailure() {
        final JsonObject schemaJson = getJsonObjectForKey("multipleKeywordsFailure");
        final Exception exception = failWith(SchemaException.class)
                .input(schemaJson)
                .expectedSchemaLocation("#/properties/wrapper/items")
                .expected(e->{
                    System.out.println(e);
                })
                .expect();
        System.out.println();
    }

}
