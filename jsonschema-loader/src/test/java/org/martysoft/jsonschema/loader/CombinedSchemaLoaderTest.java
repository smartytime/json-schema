package org.martysoft.jsonschema.loader;

import org.junit.Assert;
import org.junit.Test;
import org.martysoft.jsonschema.v6.CombinedSchema;
import org.martysoft.jsonschema.v6.Schema;
import org.martysoft.jsonschema.v6.SchemaException;
import org.martysoft.jsonschema.v6.StringSchema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author erosb
 */
public class CombinedSchemaLoaderTest extends BaseLoaderTest {

    public CombinedSchemaLoaderTest() {
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
        try {
            getSchemaForKey("multipleKeywordsFailure");
        } catch (SchemaException e) {
            assertEquals("#/properties/wrapper/items", e.getSchemaLocation());
        }
    }

}
