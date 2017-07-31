package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.Schema;
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
        Schema actual = getSchemaForKey("combinedSchema");
        Assert.assertNotNull(actual);
    }

    @Test
    public void combinedSchemaWithBaseSchema() {
        Schema actual = getSchemaForKey("combinedSchemaWithBaseSchema");
        assertEquals(2, actual.getAnyOfSchemas().stream()
                .filter(Schema::hasStringKeywords)
                .count());
    }

    @Test
    public void combinedSchemaWithExplicitBaseSchema() {
        Schema actual = getSchemaForKey("combinedSchemaWithExplicitBaseSchema");
        assertEquals(2, actual.getAnyOfSchemas().stream()
                .filter(Schema::hasStringKeywords).count());
    }

    @Test
    public void combinedSchemaWithMultipleBaseSchemas() {
        Schema actual = getSchemaForKey("combinedSchemaWithMultipleBaseSchemas");
        assertSoftly(a -> {
            a.assertThat(actual.getAnyOfSchemas())
                    .filteredOn(Schema::hasStringKeywords)
                    .isNotNull();
            a.assertThat(actual.getAnyOfSchemas())
                    .filteredOn(Schema::hasNumberKeywords)
                    .isNotNull();
        });
    }
}
