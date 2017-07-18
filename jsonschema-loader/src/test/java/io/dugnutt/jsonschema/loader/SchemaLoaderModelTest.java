package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.JsonPath;
import io.dugnutt.jsonschema.six.JsonPointerPath;
import io.dugnutt.jsonschema.six.SchemaException;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PROPERTIES;
import static io.dugnutt.jsonschema.utils.JsonUtils.blankJsonObject;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonArrayBuilder;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonObjectBuilder;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

/**
 * @author erosb
 */
public class SchemaLoaderModelTest {

    @Test
    public void childForArrayIndex() {
        SchemaLoaderModel ls = SchemaLoaderModel.createModelFor(
                jsonObjectBuilder()
                        .add(PROPERTIES.key(), jsonArrayBuilder()
                                .add(jsonObjectBuilder()))
                        .build()
        );
        SchemaLoaderModel actual = ls.childModel(PROPERTIES, 0);
        Assert.assertEquals(asList("properties", "0"), actual.currentJsonPath.jsonPathParts());
    }

    @Test
    public void childForSecond() {
        SchemaLoaderModel ls = emptySubject();
        SchemaLoaderModel actual = ls.childModel("hello").childModel("world");
        Assert.assertEquals(asList("hello", "world"), actual.currentJsonPath.jsonPathParts());
    }

    @Test
    public void childForString() {
        SchemaLoaderModel ls = emptySubject();
        SchemaLoaderModel actual = ls.childModel("hello");
        Assert.assertEquals(asList("hello"), actual.currentJsonPath.jsonPathParts());
    }

    @Test
    public void testCreateSchemaException() {
        SchemaLoaderModel subject = SchemaLoaderModel.createModelFor(blankJsonObject());
        SchemaException actual = subject.createSchemaException("message");
        assertEquals("#: message", actual.getMessage());
    }

    @Test
    public void testCreateSchemaExceptionWithPath() {
        SchemaLoaderModel subject = SchemaLoaderModel.createModelFor(jsonObjectBuilder().build())
                .withId(URI.create("http://mysite.com#/foo/bob"))
                .withCurrentJsonPath(new JsonPointerPath(JsonPath.jsonPath("from", "the", "base", "of", "bob")));

        SchemaException actual = subject.createSchemaException("message");
        assertEquals("#/from/the/base/of/bob: message", actual.getMessage());
    }

    private SchemaLoaderModel emptySubject() {
        return SchemaLoaderModel.createModelFor(
                jsonObjectBuilder()
                        .add("hello", jsonObjectBuilder()
                                .add("world", jsonObjectBuilder()))
                        .build());
    }
}
