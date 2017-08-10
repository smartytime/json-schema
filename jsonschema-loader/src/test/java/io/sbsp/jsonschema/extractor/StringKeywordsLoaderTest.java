package io.sbsp.jsonschema.extractor;

import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import org.junit.Test;

import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchema;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

/**
 * @author erosb
 */
public class StringKeywordsLoaderTest extends BaseLoaderTest {

    public static final Schema NULL_SCHEMA = jsonSchema().type(JsonSchemaType.NULL).build();

    public StringKeywordsLoaderTest() {
        super("stringtestschemas.json");
    }

    @Test
    public void patternSchema() {
        Draft6Schema actual = getSchemaForKey("patternSchema");
        assertSoftly(a -> {
            a.assertThat(actual.getPattern()).isNotNull();
        });
    }
}
