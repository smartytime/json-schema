package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;

import static io.sbsp.jsonschema.builder.JsonSchemaBuilder.jsonSchema;
import static io.sbsp.jsonschema.loading.LoadingTestSupport.failWith;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.Assert.assertNotNull;

/**
 * @author erosb
 */
public class ArrayKeywordsLoaderTest extends BaseLoaderTest {

    private static final Draft6Schema NULL_SCHEMA = jsonSchema()
            .type(JsonSchemaType.NULL)
            .build()
            .asDraft6();

    public ArrayKeywordsLoaderTest() {
        super("arraytestschemas.json");
    }

    @Test
    public void arrayByAdditionalItems() {
        Draft6Schema actual = getSchemaForKey("arrayByAdditionalItems");
        assertSoftly(a -> {
            a.assertThat(actual.getTypes().contains(JsonSchemaType.ARRAY));
        });
    }

    @Test
    public void arrayByItems() {
        Draft6Schema actual = getSchemaForKey("arrayByItems");
        assertNotNull(actual);
    }

    @Test
    public void arraySchema() {
        Draft6Schema actual = getSchemaForKey("arraySchema").asDraft6();
        assertSoftly(a -> {
            a.assertThat(actual.getMinItems()).isEqualTo(2);
            a.assertThat(actual.getMaxItems()).isEqualTo(3);
        });
        assertNotNull(actual);
        Assert.assertEquals(NULL_SCHEMA, actual.getAllItemSchema().orElse(null));
    }

    @Test
    public void invalidAdditionalItems() {
        failWith(SchemaLoadingException.class)
                .input(getJsonObjectForKey("invalidAdditionalItems"))
                .expectedSchemaLocation("#/additionalItems")
                .expect();
    }

    @Test
    public void invalidArrayItemSchema() {
        failWith(SchemaLoadingException.class)
                .input(getJsonObjectForKey("invalidArrayItemSchema"))
                .expectedSchemaLocation("#/items/0")
                .expected(err -> {
                    System.out.println();
                })
                .expect();
    }

    @Test
    public void invalidItemsJsonSchema() {
        final JsonObject schemaJson = getJsonObjectForKey("invalidItemsArraySchema");
        failWith(SchemaLoadingException.class)
                .input(schemaJson)
                .expectedSchemaLocation("#/items")
                .expect();
    }
}
