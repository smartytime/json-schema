package io.sbsp.jsonschema.utils;

import io.sbsp.jsonschema.builder.JsonSchemaBuilder;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import org.junit.Test;

import javax.json.JsonValue;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class SchemaURIUtilsTest {
    @Test
    public void generateUniqueURI_ForSameBuilder_ReturnsSameURI() {
        final JsonSchemaBuilder aBuilder = JsonSchemaBuilder.jsonSchema()
                .constValue(JsonValue.TRUE)
                .pattern("bob")
                .type(JsonSchemaType.STRING)
                .type(JsonSchemaType.NUMBER)
                .enumValues(JsonUtils.jsonArray("one", 2, "three"));

        final JsonSchemaBuilder bBuilder = JsonSchemaBuilder.jsonSchema()
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