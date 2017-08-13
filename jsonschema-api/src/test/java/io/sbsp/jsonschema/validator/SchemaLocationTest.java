package io.sbsp.jsonschema.validator;

import io.sbsp.jsonschema.SchemaLocation;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.Test;

import java.net.URI;

import static io.sbsp.jsonschema.SchemaLocation.documentRoot;
import static org.junit.Assert.assertEquals;

public class SchemaLocationTest {

    @Test
    public void testLocation() {

        SchemaLocation schemaLocation = documentRoot("http://mywebsite.com/schemas/core/primitives.json");
        SchemaLocation intermediateLocation = schemaLocation.child(URI.create("entities"), "definitions", "core", "platformEntity");
        SchemaLocation childLocation = intermediateLocation.child(URI.create("#platformIdentifier"), "properties", "id");

        assertEquals("JSON Pointer Correct", "/definitions/core/platformEntity/properties/id", childLocation.getJsonPath().toJsonPointer());
        assertEquals("Child Canonical URL", "http://mywebsite.com/schemas/core/entities#platformIdentifier", childLocation.getUniqueURI().toString());
        assertEquals("Child Resolution Scope", "http://mywebsite.com/schemas/core/entities#platformIdentifier", childLocation.getResolutionScope().toString());
        assertEquals("Child Relative URL", "#/definitions/core/platformEntity/properties/id", childLocation.getJsonPointerFragment().toString());
    }

    @Test
    public void testEquals() {
        EqualsVerifier.forClass(SchemaLocation.class)
                .withOnlyTheseFields("documentURI", "jsonPath", "resolutionScope")
                .suppress(Warning.STRICT_INHERITANCE)
                .verify();
    }

}