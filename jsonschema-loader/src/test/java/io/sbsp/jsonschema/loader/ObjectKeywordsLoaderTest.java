package io.sbsp.jsonschema.loader;

import io.sbsp.jsonschema.six.enums.JsonSchemaType;
import io.sbsp.jsonschema.six.keywords.ObjectKeywords;
import io.sbsp.jsonschema.six.Schema;
import io.sbsp.jsonschema.six.SchemaException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author erosb
 */
public class ObjectKeywordsLoaderTest extends BaseLoaderTest {

    public static final Schema BOOLEAN_SCHEMA = Schema.jsonSchemaBuilder().type(JsonSchemaType.BOOLEAN).build();

    public ObjectKeywordsLoaderTest() {
        super("objecttestschemas.json");
    }

    @Rule
    public ExpectedException expExc = ExpectedException.none();

    @Test
    public void objectSchema() {
        Schema actual =  getSchemaForKey("objectSchema");
        assertThat(actual.hasObjectKeywords()).isTrue();
        final ObjectKeywords keywords = actual.getObjectKeywords();
        final Map<String, Schema> propertySchemas = keywords.getPropertySchemas();
        assertThat(propertySchemas).isNotNull();
        assertThat(propertySchemas).hasSize(2);
        final Schema boolProp = propertySchemas.get("boolProp");
        assertThat(boolProp).isNotNull();

        //todo:ericm Boolean?
        // assertEquals(BooleanSchema.BOOLEAN_SCHEMA, boolProp);

        assertThat(keywords.getRequiredProperties()).hasSize(2);
        assertThat(keywords.getMinProperties()).isEqualTo(2);
        assertThat(keywords.getMaxProperties()).isEqualTo(3);
    }

    @Test(expected = SchemaException.class)
    public void objectInvalidAdditionalProperties() {
        getSchemaForKey("objectInvalidAdditionalProperties");
    }

    @Test
    public void objectWithAdditionalPropSchema() {
        Schema actual = getSchemaForKey("objectWithAdditionalPropSchema");
        assertThat(actual.hasObjectKeywords());
        assertThat(actual.getObjectKeywords().getSchemaOfAdditionalProperties())
                .isPresent()
                .hasValue(BOOLEAN_SCHEMA);
    }

    private ObjectKeywords assertObjectKeywords(Schema actual) {
        assertThat(actual.hasObjectKeywords()).isTrue();
        return actual.getObjectKeywords();
    }

    @Test
    public void objectWithPropDep() {
        Schema actual =  getSchemaForKey("objectWithPropDep");
        assertEquals(1, assertObjectKeywords(actual).getPropertyDependencies().get("isIndividual").size());
    }

    @Test
    public void objectWithSchemaDep() {
        ObjectKeywords actual = assertObjectKeywords(getSchemaForKey("objectWithSchemaDep"));
        assertEquals(1, actual.getSchemaDependencies().size());
    }

    @Test
    public void patternProperties() {
        ObjectKeywords actual = assertObjectKeywords(getSchemaForKey("patternProperties"));
        Assert.assertNotNull(actual);
        assertEquals(2, actual.getPatternProperties().size());
    }

    @Test(expected = SchemaException.class)
    public void invalidDependency() {
        getSchemaForKey("invalidDependency");
    }

    @Test
    public void emptyDependencyList() {
        getSchemaForKey("emptyDependencyList");
    }
}
