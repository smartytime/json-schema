package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.Schema;
import org.junit.Test;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * @author erosb
 */
public class StringKeywordsLoaderTest extends BaseLoaderTest {

    public static final Schema NULL_SCHEMA = Schema.jsonSchemaBuilder().type(JsonSchemaType.NULL).build();

    public StringKeywordsLoaderTest() {
        super("stringtestschemas.json");
    }

    @Test
    public void patternSchema() {
        Schema actual = getSchemaForKey("patternSchema");
        assertSoftly(a -> {
            a.assertThat(actual.hasStringKeywords()).isTrue();
            a.assertThat(actual.getStringKeywords().get().getPattern()).isNotNull();
        });
    }
}
