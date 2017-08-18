package io.sbsp.jsonschema.keyword;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import org.junit.Test;

import static io.sbsp.jsonschema.JsonSchemaProvider.schemaBuilder;
import static org.assertj.core.api.Assertions.assertThat;

public class ItemsKeywordTest {

    @Test
    public void testToString_WhenFalseAdditionalItemsSchema_ThenWritesFalse() {
        final Schema noAdditionalItemsSchema = schemaBuilder()
                .noAdditionalItems()
                .build();

        final String nullAdditionalItems = noAdditionalItemsSchema.toString(false, JsonSchemaVersion.Draft3);
        assertThat(nullAdditionalItems).isEqualTo("{\"additionalItems\":false}");
    }

    @Test
    public void testToString_WhenDalseAdditionalItemsSchema_AndVersionIsDraft6_ThenWritesFalseSchema() {
        final Schema noAdditionalItemsSchema = schemaBuilder()
                .noAdditionalItems()
                .build();

        final String nullAdditionalItems = noAdditionalItemsSchema.toString(false, JsonSchemaVersion.Draft6);
        assertThat(nullAdditionalItems).isEqualTo("{\"additionalItems\":{\"not\":{}}}");
    }

}