package io.sbsp.jsonschema.loader;

import io.sbsp.jsonschema.Schema.JsonSchemaBuilder;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonObject;
import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JsonSchemaFactoryResolutionTest {

    private JsonSchemaFactory jsonSchemaFactory;
    private JsonObject accountProfileJson;
    private URI documentURI;

    @Before
    public void before() {
        jsonSchemaFactory = JsonSchemaFactory.schemaFactory();
        accountProfileJson = readResource("sbsp-account-profile.json");
        documentURI = URI.create("http://schema.sbsp.io/sbsp-account-profile.json");
    }

    @Test
    public void resolve_WhenReferenceIsInsideDocument_AndRefHasAbsolutePathPlusSchemeWithLocalId_ThenReturnSomething() {
        URI relativeURI = URI.create("http://schema.sbsp.io/sbsp-account-profile.json#platformName");

        final URI absoluteURI = documentURI.resolve(relativeURI);

        Optional<JsonSchemaBuilder> schema = jsonSchemaFactory.findRefInDocument(documentURI, absoluteURI, accountProfileJson);
        assertThat(schema).isPresent();
    }

    @Test
    public void resolve_WhenReferenceIsInsideDocument_AndRefHasAbsolutePathWithLocalId_ThenReturnSomething() {
        URI relativeURI = URI.create("/sbsp-account-profile.json#platformName");

        final URI absoluteURI = documentURI.resolve(relativeURI);

        Optional<JsonSchemaBuilder> schema = jsonSchemaFactory.findRefInDocument(documentURI, absoluteURI, accountProfileJson);
        assertThat(schema).isPresent();
    }

    @Test
    public void resolve_WhenReferenceIsInsideDocument_AndRefHasLocalIdFragment_ThenReturnSomething() {
        URI relativeURI = URI.create("#platformName");

        final URI absoluteURI = documentURI.resolve(relativeURI);

        Optional<JsonSchemaBuilder> schema = jsonSchemaFactory.findRefInDocument(documentURI, absoluteURI, accountProfileJson);
        assertThat(schema).isPresent();
    }

    @Test
    public void resolve_WhenReferenceIsInsideDocument_AndRefHasRelativePathWithLocalId_ThenReturnSomething() {
        URI relativeURI = URI.create("sbsp-account-profile.json#platformName");

        final URI absoluteURI = documentURI.resolve(relativeURI);

        Optional<JsonSchemaBuilder> schema = jsonSchemaFactory.findRefInDocument(documentURI, absoluteURI, accountProfileJson);
        assertThat(schema).isPresent();
    }

    @Test
    public void resolve_WhenReferenceIsOutsideDocument_ThenReturnEmpty() {
        URI relativeURI = URI.create("/primitives.json#/definitions/color");

        final URI absoluteURI = documentURI.resolve(relativeURI);

        Optional<JsonSchemaBuilder> schema = jsonSchemaFactory.findRefInDocument(documentURI, absoluteURI, accountProfileJson);
        assertThat(schema).isNotPresent();
    }

    private JsonObject readResource(String relativePath) {
        return ResourceLoader.DEFAULT.readObj(relativePath);
    }
}
