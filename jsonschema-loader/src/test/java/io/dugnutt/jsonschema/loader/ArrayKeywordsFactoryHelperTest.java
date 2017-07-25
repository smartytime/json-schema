package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.ArrayKeywords;
import io.dugnutt.jsonschema.six.JsonSchema;
import io.dugnutt.jsonschema.six.JsonSchemaType;
import io.dugnutt.jsonschema.six.UnexpectedValueException;
import org.junit.Assert;
import org.junit.Test;

import javax.json.JsonObject;

import static io.dugnutt.jsonschema.loader.LoadingTestSupport.failWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author erosb
 */
public class ArrayKeywordsFactoryHelperTest extends BaseLoaderTest {

    public static final JsonSchema NULL_SCHEMA = JsonSchema.jsonSchemaBuilder().build();

    public ArrayKeywordsFactoryHelperTest() {
        super("arraytestschemas.json");
    }

    @Test
    public void additionalItemSchema() {
        assertTrue(getSchemaForKey("additionalItemSchema") instanceof JsonSchema);
    }

    @Test
    public void arrayByAdditionalItems() {
        JsonSchema actual = getSchemaForKey("arrayByAdditionalItems");
        assertSoftly(a -> {
            a.assertThat(actual.hasArrayKeywords()).isTrue();
            a.assertThat(actual.getTypes().contains(JsonSchemaType.ARRAY));
        });
    }

    @Test
    public void arrayByItems() {
        JsonSchema actual = getSchemaForKey("arrayByItems");
        assertNotNull(actual);
    }

    @Test
    public void arraySchema() {
        ArrayKeywords actual = arrayKeywords(getSchemaForKey("arraySchema"));
        assertSoftly(a -> {
            a.assertThat(actual.getMinItems())
                    .isEqualTo(2);
            a.assertThat(actual.getMaxItems())
                    .isEqualTo(3);
        });
        assertNotNull(actual);
        Assert.assertEquals(NULL_SCHEMA, actual.getAllItemSchema());
    }

    private ArrayKeywords arrayKeywords(JsonSchema schema) {
        assertThat(schema).isNotNull();
        assertThat(schema.getArrayKeywords()).isPresent();
        return schema.getArrayKeywords().get();
    }

    @Test
    public void invalidAdditionalItems() {
        failWith(UnexpectedValueException.class)
                .input(getJsonObjectForKey("invalidAdditionalItems"))
                .expectedSchemaLocation("#/additionalItems")
                .expect();
    }

    @Test
    public void invalidArrayItemSchema() {
        failWith(UnexpectedValueException.class)
                .input(getJsonObjectForKey("invalidArrayItemSchema"))
                .expectedSchemaLocation("#/items/0")
                .expected(err -> {
                    System.out.println();
                })
                .expect();
    }

    @Test
    public void invalidItemsJsonSchema() {
        final JsonObject schemaJson = getJsonObjectForKey("invalidItemsJsonSchema");
        failWith(UnexpectedValueException.class)
                .input(schemaJson)
                .expectedSchemaLocation("#/items")
                .expect();
    }
}
