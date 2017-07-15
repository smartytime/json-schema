package org.martysoft.jsonschema.loader;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.martysoft.jsonschema.utils.JsonUtils;
import org.martysoft.jsonschema.v6.Schema;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author erosb
 */
public class JsonObjectTest extends BaseLoaderTest {

    public JsonObjectTest() {
        super("objecttestcases.json");
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

    private static final LoadingState emptyLs = JsonValueTest.emptyLs;

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
    public void idHandling() {
        Schema schema = getSchemaForKey("idInRoot");
        URI actual = JsonValue.of(schema, emptyLs).ls.id;
        assertEquals(schema.getId(), actual.toString());
    }

    @Test
    public void nullId() {
        JsonObject schema = JsonUtils.blankObject();
        URI actual = JsonValue.of(schema, emptyLs).ls.id;
        assertNull(actual);
    }

    @Test
    public void nestedId() {
        JsonObject schema = RAW_OBJECTS.getJsonObject("nestedId");
        URI actual = JsonValue.of(schema, emptyLs).requireObject()
                .require("properties")
                .requireObject()
                .require("prop").ls.id;
        assertEquals("http://x.y/z#zzz", actual.toString());
    }

    @Test
    public void childForConsidersIdAttr() {
        JsonObject input = TESTSCHEMAS.getJsonObject("remotePointerResolution");
        JsonObject root = new JsonObject(input.toMap());
        System.out.println("root.ls.id = " +root.ls.id);
        JsonObject fc = root.require("properties").requireObject().require("folderChange").requireObject();
        System.out.println("fc.ls.id = " + fc.ls.id);
        JsonObject sIF = fc.require("properties").requireObject().require("schemaInFolder").requireObject();
        System.out.println("sIF.ls.id = " + sIF.ls.id);
    }

}
