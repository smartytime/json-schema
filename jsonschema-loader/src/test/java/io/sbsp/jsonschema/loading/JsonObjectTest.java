package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.utils.JsonUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import java.util.function.Consumer;

import static io.sbsp.jsonschema.ResourceLoader.resourceLoader;
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
        testSchemas = resourceLoader().readJsonObject("loading/testschemas.json");
    }

    @SuppressWarnings("unchecked")
    static <R> Consumer<R> mockConsumer() {
        return (Consumer<R>) mock(Consumer.class);
    }

    @Rule
    public ExpectedException expExc = ExpectedException.none();

    @Test
    public void testHasKey() {
        assertTrue(subject().containsKey("a"));
    }

    private JsonObject subject() {
        return JsonProvider.provider()
                .createObjectBuilder()
                .add("a", true)
                .add("b", JsonUtils.blankJsonObject())
                .build();
    }

    @Test
    public void nestedId() {
        JsonObject schema = getJsonObjectForKey("nestedId");
        JsonValueWithPath schemaJson = JsonValueWithPath.fromJsonValue(schema);

        JsonValueWithPath grandChild = schemaJson.path(Keywords.PROPERTIES).path("prop");
        assertEquals("http://x.y/z#zzz", grandChild.getLocation().getCanonicalURI().toString());
    }

    @Test
    public void childForConsidersIdAttr() {
        JsonObject input = testSchemas.getJsonObject("remotePointerResolution");
        JsonObject fc = input.getJsonObject("properties").getJsonObject("folderChange");
        JsonObject sIF = fc.getJsonObject("properties").getJsonObject("schemaInFolder");
    }

}
