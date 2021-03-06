package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.utils.JsonUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author erosb
 */
public class JsonObjectTest extends BaseLoaderTest {

    private JsonObject testSchemas;

    public JsonObjectTest() {
        super("objecttestcases.json");
        testSchemas = JsonUtils.readResourceAsJson("/tests/testschemas.json", JsonObject.class);
    }

    @SuppressWarnings("unchecked")
    static <R> Consumer<R> mockConsumer() {
        return (Consumer<R>) mock(Consumer.class);
    }

    private Map<String, Object> storage() {
        Map<String, Object> rval = new HashMap<>();
        rval.put("a", true);
        rval.put("b", JsonUtils.blankJsonObject());
        return rval;
    }

    @Rule
    public ExpectedException expExc = ExpectedException.none();

    @Test
    public void testHasKey() {
        assertTrue(subject().containsKey("a"));
    }

    private JsonObject subject() {
        return JsonProvider.provider()
                .createObjectBuilder(storage())
                .build();
    }

    @Test
    public void nestedId() {
        JsonObject schema = getJsonObjectForKey("nestedId");
        JsonValueWithLocation schemaJson = JsonValueWithLocation.fromJsonValue(schema);

        JsonValueWithLocation grandChild = schemaJson.getPathAwareObject(JsonSchemaKeywordType.PROPERTIES).getPathAwareObject("prop");
        assertEquals("http://x.y/z#zzz", grandChild.getLocation().getCanonicalURI().toString());
    }

    @Test
    public void childForConsidersIdAttr() {
        JsonObject input = testSchemas.getJsonObject("remotePointerResolution");
        JsonObject fc = input.getJsonObject("properties").getJsonObject("folderChange");
        JsonObject sIF = fc.getJsonObject("properties").getJsonObject("schemaInFolder");
    }

}
