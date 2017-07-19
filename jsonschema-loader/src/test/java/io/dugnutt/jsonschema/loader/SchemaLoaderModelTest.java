package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.six.SchemaLocation;
import org.junit.Test;

import java.net.URI;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PROPERTIES;
import static io.dugnutt.jsonschema.utils.JsonUtils.blankJsonObject;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonArrayBuilder;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonObjectBuilder;
import static org.assertj.core.api.Assertions.assertThat;
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
        assertThat(actual.getLocation().getPath())
                .containsExactly("properties", "0");
    }

    @Test
    public void childForSecond() {
        SchemaLoaderModel ls = emptySubject();
        SchemaLoaderModel actual = ls.childModel("hello").childModel("world");
        assertThat(actual.getLocation().getPath())
                .containsExactly("hello", "world");
    }

    @Test
    public void childForString() {
        SchemaLoaderModel ls = emptySubject();
        SchemaLoaderModel actual = ls.childModel("hello");
        assertThat(actual.getLocation().getPath())
                .containsExactly("hello");
    }

    @Test
    public void testCreateSchemaException() {
        SchemaLoaderModel subject = SchemaLoaderModel.createModelFor(blankJsonObject());
        SchemaException actual = subject.createSchemaException("message");
        assertEquals("#: message", actual.getMessage());
    }

    @Test
    public void testCreateSchemaExceptionWithPath() {
        SchemaLoaderModel subject = SchemaLoaderModel.builder()
                .schemaJson(jsonObjectBuilder().build())
                .location(
                        SchemaLocation.rootSchemaLocation(URI.create("http://mysite.com#/foo/bob"))
                                .withChildPath("from", "the", "base", "of", "bob")
                ).build();

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
