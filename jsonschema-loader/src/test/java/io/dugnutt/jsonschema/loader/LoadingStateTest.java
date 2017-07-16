package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.utils.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author erosb
 */
public class LoadingStateTest {

    private LoadingState emptySubject() {
        return new LoadingState(SchemaLoader.builder()
                .rootSchemaJson(JsonUtils.blankJsonObject())
                .schemaJson(JsonUtils.blankJsonObject()));
    }

    @Test
    public void childForString() {
        LoadingState ls = emptySubject();
        LoadingState actual = ls.childFor("hello");
        Assert.assertEquals(asList("hello"), actual.currentJsonPath.jsonPathParts());
    }

    @Test
    public void childForSecond() {
        LoadingState ls = emptySubject();
        LoadingState actual = ls.childFor("hello").childFor("world");
        Assert.assertEquals(asList("hello", "world"), actual.currentJsonPath.jsonPathParts());
    }

    @Test
    public void childForArrayIndex() {
        LoadingState ls = emptySubject();
        LoadingState actual = ls.childFor(42);
        Assert.assertEquals(asList("42"), actual.currentJsonPath.jsonPathParts());
    }

    @Test
    public void testCreateSchemaException() {
        LoadingState subject = new LoadingState(SchemaLoader.builder().schemaJson(JsonUtils.blankJsonObject()));
        SchemaException actual = subject.createSchemaException("message");
        assertEquals("#: message", actual.getMessage());
        // assertEquals(JsonProvider.provider().createPointer("").toURIFragment(), actual.getSchemaLocation());
        Assert.fail("Not sure how to make this test work with jsr353");
    }

    @Test
    public void childForNotnullId() {
        LoadingState actual = emptySubject().childForId("http://x.y");
        assertEquals("http://x.y", actual.id.toString());
    }

    @Test
    public void childForNullId() {
        LoadingState actual = emptySubject().childForId(null);
        assertNull(actual.id);
    }
}
