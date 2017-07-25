package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.JsonSchema;
import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
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
        JsonSchema actual = getSchemaForKey("combinedSchema");
        Assert.assertNotNull(actual);
    }

    @Test
    public void combinedSchemaWithBaseSchema() {
        JsonSchema actual = getSchemaForKey("combinedSchemaWithBaseSchema");
        assertEquals(2, actual.getAnyOfSchemas().stream()
                .filter(JsonSchema::hasStringKeywords)
                .count());
    }

    @Test
    public void combinedSchemaWithExplicitBaseSchema() {
        JsonSchema actual = getSchemaForKey("combinedSchemaWithExplicitBaseSchema");
        assertEquals(2, actual.getAnyOfSchemas().stream()
                .filter(JsonSchema::hasStringKeywords).count());
    }

    @Test
    public void combinedSchemaWithMultipleBaseSchemas() {
        JsonSchema actual = getSchemaForKey("combinedSchemaWithMultipleBaseSchemas");
        assertSoftly(a -> {
            a.assertThat(actual.getAnyOfSchemas())
                    .filteredOn(JsonSchema::hasStringKeywords)
                    .isNotNull();
            a.assertThat(actual.getAnyOfSchemas())
                    .filteredOn(JsonSchema::hasNumberKeywords)
                    .isNotNull();
        });
    }
}
