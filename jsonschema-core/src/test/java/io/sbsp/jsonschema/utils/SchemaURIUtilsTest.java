package io.sbsp.jsonschema.utils;

import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import org.junit.Test;

import javax.json.JsonValue;
import java.net.URI;

import static io.sbsp.jsonschema.JsonSchemaProvider.schemaBuilder;
import static org.assertj.core.api.Assertions.assertThat;

public class SchemaURIUtilsTest {
    @Test
    public void generateUniqueURI_ForSameBuilder_ReturnsSameURI() {
        final SchemaBuilder aBuilder = schemaBuilder()
                .constValue(JsonValue.TRUE)
                .pattern("bob")
                .type(JsonSchemaType.STRING)
                .type(JsonSchemaType.NUMBER)
                .enumValues(JsonUtils.jsonArray("one", 2, "three"));

        final SchemaBuilder bBuilder = schemaBuilder()
                .constValue(JsonValue.TRUE)
                .pattern("bob")
                .type(JsonSchemaType.STRING)
                .type(JsonSchemaType.NUMBER)
                .enumValues(JsonUtils.jsonArray("one", 2, "three"));

        final URI uniqueA = URIUtils.generateUniqueURI(aBuilder);
        final URI uniqueB = URIUtils.generateUniqueURI(bBuilder);

        assertThat(uniqueA).isEqualTo(uniqueB);
    }
}