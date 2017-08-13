package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.Schema;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author erosb
 */
public class CombinedSchemaFactoryTest extends BaseLoaderTest {

    public CombinedSchemaFactoryTest() {
        super("combinedtestschemas.json");
    }

    @Test
    public void combinedSchemaLoading() {
        Schema actual = getSchemaForKey("combinedSchema");
        Assert.assertNotNull(actual);
    }

    @Test
    public void combinedSchemaWithBaseSchema() {
        Draft6Schema actual = getSchemaForKey("combinedSchemaWithBaseSchema");
        assertEquals(2, (long) actual.getAnyOfSchemas().size());
    }

    @Test
    public void combinedSchemaWithExplicitBaseSchema() {
        Draft6Schema actual = getSchemaForKey("combinedSchemaWithExplicitBaseSchema");
        assertThat(actual.getAnyOfSchemas()).hasSize(2);
    }

    @Test
    public void combinedSchemaWithMultipleBaseSchemas() {
        Draft6Schema actual = getSchemaForKey("combinedSchemaWithMultipleBaseSchemas");
        assertThat(actual.getAnyOfSchemas()).hasSize(2);
    }
}
