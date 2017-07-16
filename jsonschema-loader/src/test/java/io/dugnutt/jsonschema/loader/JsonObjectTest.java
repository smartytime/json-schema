package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author erosb
 */
public class JsonObjectTest extends BaseLoaderTest {

    private JsonObject testSchemas;

    public JsonObjectTest() {
        super("objecttestcases.json");
        testSchemas = JsonUtils.readResource("testschemas.json", JsonObject.class);
    }

    @SuppressWarnings("unchecked")
    static <R> Consumer<R> mockConsumer() {
        return (Consumer<R>) mock(Consumer.class);
    }

    private Map<String, Object> storage() {
        Map<String, Object> rval = new HashMap<>();
        rval.put("a", true);
        rval.put("b", JsonUtils.blankObject());
        return rval;
    }

    // private static final LoadingState emptyLs = JsonValueTest.emptyLs;

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

    // @Test
    // public void idHandling() {
    //     Schema schema = getSchemaForKey("idInRoot");
    //     URI actual = JsonValue.of(schema, emptyLs).ls.id;
    //     assertEquals(schema.getId(), actual.toString());
    // }
    //
    // @Test
    // public void nullId() {
    //     JsonObject schema = JsonUtils.blankObject();
    //     URI actual = JsonValue.of(schema, emptyLs).ls.id;
    //     assertNull(actual);
    // }

    @Test
    public void nestedId() {
        JsonObject schema = getJsonObjectForKey("nestedId");
        // URI actual = schema.getJsonObject("properties")
        //         .getJsonObject("prop");
        Assert.fail("No LS to test");
        // assertEquals("http://x.y/z#zzz", actual.toString());
    }

    @Test
    public void childForConsidersIdAttr() {
        JsonObject input = testSchemas.getJsonObject("remotePointerResolution");
        JsonObject fc = input.getJsonObject("properties").getJsonObject("folderChange");
        JsonObject sIF = fc.getJsonObject("properties").getJsonObject("schemaInFolder");
        //What's the test?
        Assert.fail("No assertion");

    }

}
