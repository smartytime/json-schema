package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.BooleanSchema;
import io.dugnutt.jsonschema.six.ObjectSchema;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.six.SchemaException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author erosb
 */
public class ObjectSchemaFactoryTest extends BaseLoaderTest {

    public ObjectSchemaFactoryTest() {
        super("objecttestschemas.json");
    }

    @Rule
    public ExpectedException expExc = ExpectedException.none();

    @Test
    public void objectSchema() {
        ObjectSchema actual = (ObjectSchema) getSchemaForKey("objectSchema");
        Assert.assertNotNull(actual);
        Map<String, Schema> propertySchemas = actual.getPropertySchemas();
        assertEquals(2, propertySchemas.size());
        assertEquals(BooleanSchema.BOOLEAN_SCHEMA, propertySchemas.get("boolProp"));
        Assert.assertFalse(actual.isPermitsAdditionalProperties());
        assertEquals(2, actual.getRequiredProperties().size());
        assertEquals(2, actual.getMinProperties().intValue());
        assertEquals(3, actual.getMaxProperties().intValue());
    }

    @Test(expected = SchemaException.class)
    public void objectInvalidAdditionalProperties() {
        getSchemaForKey("objectInvalidAdditionalProperties");
    }

    @Test
    public void objectWithAdditionalPropSchema() {
        ObjectSchema actual = (ObjectSchema) getSchemaForKey("objectWithAdditionalPropSchema");
        assertEquals(BooleanSchema.BOOLEAN_SCHEMA, actual.getSchemaOfAdditionalProperties());
    }

    @Test
    public void objectWithPropDep() {
        ObjectSchema actual = (ObjectSchema) getSchemaForKey("objectWithPropDep");
        assertEquals(1, actual.getPropertyDependencies().get("isIndividual").size());
    }

    @Test
    public void objectWithSchemaDep() {
        ObjectSchema actual = (ObjectSchema) getSchemaForKey("objectWithSchemaDep");
        assertEquals(1, actual.getSchemaDependencies().size());
    }

    @Test
    public void patternProperties() {
        ObjectSchema actual = (ObjectSchema) getSchemaForKey("patternProperties");
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

    @Test @Ignore
    public void invalidRequired() {
        expExc.expect(SchemaException.class);
        expExc.expectMessage("#/required/1: expected type: String, found: JsonArray");
        getSchemaForKey("invalidRequired");
    }

}
