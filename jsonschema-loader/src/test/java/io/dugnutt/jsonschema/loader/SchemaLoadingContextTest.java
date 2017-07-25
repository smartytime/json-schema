package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.six.SchemaLocation;
import org.junit.Test;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.PROPERTIES;
import static io.dugnutt.jsonschema.utils.JsonUtils.blankJsonObject;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonArrayBuilder;
import static io.dugnutt.jsonschema.utils.JsonUtils.jsonObjectBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author erosb
 */
public class SchemaLoadingContextTest {

    @Test
    public void childForArrayIndex() {
        SchemaLoadingContext ls = SchemaLoadingContext.createModelFor(
                jsonObjectBuilder()
                        .add(PROPERTIES.key(), jsonArrayBuilder()
                                .add(jsonObjectBuilder()))
                        .build()
        );
        SchemaLoadingContext actual = ls.childModel(PROPERTIES, 0);
        assertThat(actual.getLocation().getJsonPathTokens())
                .containsExactly("properties", "0");
    }

    @Test
    public void childForSecond() {
        SchemaLoadingContext ls = emptySubject();
        SchemaLoadingContext actual = ls.childModel("hello").childModel("world");
        assertThat(actual.getLocation().getJsonPathTokens())
                .containsExactly("hello", "world");
    }

    @Test
    public void childForString() {
        SchemaLoadingContext ls = emptySubject();
        SchemaLoadingContext actual = ls.childModel("hello");
        assertThat(actual.getLocation().getJsonPathTokens())
                .containsExactly("hello");
    }

    @Test
    public void testCreateSchemaException() {
        SchemaLoadingContext subject = SchemaLoadingContext.createModelFor(blankJsonObject());
        SchemaException actual = subject.createSchemaException("message");
        assertEquals("#: message", actual.getMessage());
    }

    @Test
    public void testCreateSchemaExceptionWithPath() {
        SchemaLoadingContext subject = SchemaLoadingContext.schemaContextBuilder()
                .schemaJson(jsonObjectBuilder().build())
                .location(
                        SchemaLocation.schemaLocation("http://mysite.com#/foo/bob")
                                .withChildPath("from", "the", "base", "of", "bob")
                ).build();

        SchemaException actual = subject.createSchemaException("message");
        assertEquals("#/from/the/base/of/bob: message", actual.getMessage());
    }

    private SchemaLoadingContext emptySubject() {
        return SchemaLoadingContext.createModelFor(
                jsonObjectBuilder()
                        .add("hello", jsonObjectBuilder()
                                .add("world", jsonObjectBuilder()))
                        .build());
    }
}
