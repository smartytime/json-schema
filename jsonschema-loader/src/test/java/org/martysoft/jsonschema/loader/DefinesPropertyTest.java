package org.martysoft.jsonschema.loader;

import org.junit.Assert;
import org.junit.Test;
import org.martysoft.jsonschema.utils.JsonUtils;
import org.martysoft.jsonschema.v6.BooleanSchema;
import org.martysoft.jsonschema.v6.CombinedSchema;
import org.martysoft.jsonschema.v6.CombinedSchemaType;
import org.martysoft.jsonschema.v6.ObjectSchema;
import org.martysoft.jsonschema.v6.Schema;

import javax.json.JsonObject;

import static org.junit.Assert.assertFalse;

public class DefinesPropertyTest extends BaseLoaderTest {

    public DefinesPropertyTest() {
        super("testschemas.json");
    }

    @Test
    public void objectSchemaHasField() {
        ObjectSchema actual = (ObjectSchema) getSchemaForKey("pointerResolution");
        Assert.assertTrue(actual.definesProperty("#/rectangle"));
        Assert.assertTrue(actual.definesProperty("#/rectangle/a"));
        Assert.assertTrue(actual.definesProperty("#/rectangle/b"));

        assertFalse(actual.definesProperty("#/rectangle/c"));
        assertFalse(actual.definesProperty("#/rectangle/"));
        assertFalse(actual.definesProperty("#/"));
        assertFalse(actual.definesProperty("#/a"));
        assertFalse(actual.definesProperty("#"));
        assertFalse(actual.definesProperty("#/rectangle/a/d"));
    }

    @Test
    public void recursiveSchemaHasField() {
        Schema recursiveSchema = getSchemaForKey("recursiveSchema");

        Assert.assertTrue(recursiveSchema.definesProperty("#/prop"));
        Assert.assertTrue(recursiveSchema.definesProperty("#/prop/subprop"));
        Assert.assertTrue(recursiveSchema.definesProperty("#/prop/subprop/subprop"));
        Assert.assertTrue(recursiveSchema.definesProperty("#/prop/subprop/subprop/subprop"));
    }

    @Test
    public void patternPropertiesHasField() {
        ObjectSchema actual = (ObjectSchema) getSchemaForKey("patternProperties");
        Assert.assertTrue(actual.definesProperty("#/a"));
        Assert.assertTrue(actual.definesProperty("#/aa"));
        Assert.assertTrue(actual.definesProperty("#/aaa"));
        Assert.assertTrue(actual.definesProperty("#/aaaa"));
        Assert.assertTrue(actual.definesProperty("#/aaaaa"));

        assertFalse(actual.definesProperty("b"));
    }

    @Test
    public void objectWithSchemaDep() {
        ObjectSchema actual = (ObjectSchema) getSchemaForKey("objectWithSchemaDep");
        Assert.assertTrue(actual.definesProperty("#/a"));
        Assert.assertTrue(actual.definesProperty("#/b"));

        assertFalse(actual.definesProperty("#/c"));
    }

    @Test
    public void objectWithSchemaRectangleDep() {
        ObjectSchema actual = (ObjectSchema) getSchemaForKey("objectWithSchemaRectangleDep");
        Assert.assertTrue(actual.definesProperty("#/d"));
        Assert.assertTrue(actual.definesProperty("#/rectangle/a"));
        Assert.assertTrue(actual.definesProperty("#/rectangle/b"));

        assertFalse(actual.definesProperty("#/c"));
        assertFalse(actual.definesProperty("#/d/c"));
        assertFalse(actual.definesProperty("#/rectangle/c"));
    }

    @Test
    public void objectEscape() {
        ObjectSchema actual = (ObjectSchema) getSchemaForKey("objectEscape");
        Assert.assertTrue(actual.definesProperty("#/a~0b"));
        Assert.assertTrue(actual.definesProperty("#/a~0b/c~1d"));

        assertFalse(actual.definesProperty("#/a~0b/c/d"));
    }

    @Test
    public void definesPropertyIfSubschemaMatchCountIsAcceptedByCriterion() {
        CombinedSchema subject = CombinedSchema.builder()
                .subschema(ObjectSchema.builder().addPropertySchema("a", BooleanSchema.INSTANCE).build())
                .subschema(ObjectSchema.builder().addPropertySchema("b", BooleanSchema.INSTANCE).build())
                .combinedSchemaType(CombinedSchemaType.AnyOf)
                .build();
        assertFalse(subject.definesProperty("a"));
    }

    @Test
    public void testOfTest() {
        ObjectSchema actual = (ObjectSchema) getSchemaForKey("patternPropsAndSchemaDeps");
        JsonObject input = JsonUtils.readResource("objecttestcases.json", JsonObject.class)
                .getJsonObject("validOfPatternPropsAndSchemaDeps");
        //todo:ericm Move to validator project
        // actual.validate(input);
    }

    @Test
    public void patternPropsAndSchemaDefs() {
        ObjectSchema actual = (ObjectSchema) getSchemaForKey("patternPropsAndSchemaDeps");
        // Assert.assertTrue(actual.definesProperty("#/1stLevel"));
        // Assert.assertTrue(actual.definesProperty("#/1stLevel/2ndLevel"));
        Assert.assertTrue(actual.definesProperty("#/1stLevel/2ndLevel/3rdLev"));
        // Assert.assertTrue(actual.definesProperty("#/1stLevel/2ndLevel/3rdLevel/4thLevel"));
    }

}
