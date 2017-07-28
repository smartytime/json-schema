package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.SchemaLocation;
import org.junit.Test;

import java.net.URI;

import static io.dugnutt.jsonschema.six.SchemaLocation.schemaLocation;
import static org.junit.Assert.assertEquals;

public class SchemaLocationTest {

    @Test
    public void testLocation() {

        SchemaLocation schemaLocation = schemaLocation("http://mywebsite.com/schemas/core/primitives.json");
        SchemaLocation intermediateLocation = schemaLocation.withChildPath(URI.create("entities"), "definitions", "core", "platformEntity");
        SchemaLocation childLocation = intermediateLocation.withChildPath(URI.create("#platformIdentifier"), "properties", "id");

        assertEquals("JSON Pointer Correct", "/definitions/core/platformEntity/properties/id", childLocation.getJsonPath().toJsonPointer());
        assertEquals("Child Canonical URL", "http://mywebsite.com/schemas/core/entities#platformIdentifier", childLocation.getAbsoluteURI().toString());
        assertEquals("Child Resolution Scope", "http://mywebsite.com/schemas/core/entities#platformIdentifier", childLocation.getResolutionScope().toString());
        assertEquals("Child Relative URL", "#/definitions/core/platformEntity/properties/id", childLocation.getJsonPointerFragment().toString());
    }

}