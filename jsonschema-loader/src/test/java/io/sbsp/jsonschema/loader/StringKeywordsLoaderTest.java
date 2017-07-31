package io.sbsp.jsonschema.loader;

import io.sbsp.jsonschema.six.enums.JsonSchemaType;
import io.sbsp.jsonschema.six.Schema;
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
            a.assertThat(actual.getStringKeywords().getPattern()).isNotNull();
        });
    }
}
