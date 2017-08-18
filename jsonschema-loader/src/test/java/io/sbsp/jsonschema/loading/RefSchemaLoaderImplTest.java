package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.RefSchemaLoader;
import io.sbsp.jsonschema.loading.SchemaLoaderImpl;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonObject;
import java.net.URI;
import java.util.Optional;

import static io.sbsp.jsonschema.ResourceLoader.resourceLoaderForInstance;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RefSchemaLoaderImplTest {

    private RefSchemaLoader refSchemaLoader;
    private JsonObject accountProfileJson;
    private URI documentURI;

    @Before
    public void before() {
        final SchemaLoaderImpl schemaLoader = SchemaLoaderImpl.schemaLoader();
        this.refSchemaLoader = schemaLoader.getRefSchemaLoader();
        accountProfileJson = readResource("sbsp-account-profile.json");
        documentURI = URI.create("http://schema.sbsp.io/sbsp-account-profile.json");
    }

    @Test
    public void resolve_WhenReferenceIsInsideDocument_AndRefHasAbsolutePathPlusSchemeWithLocalId_ThenReturnSomething() {
        URI relativeURI = URI.create("http://schema.sbsp.io/sbsp-account-profile.json#platformName");

        final URI absoluteURI = documentURI.resolve(relativeURI);

        Optional<SchemaBuilder> schema = refSchemaLoader.findRefInDocument(documentURI, absoluteURI, accountProfileJson, new LoadingReport());
        assertThat(schema).isPresent();
    }

    @Test
    public void resolve_WhenReferenceIsInsideDocument_AndRefHasAbsolutePathWithLocalId_ThenReturnSomething() {
        URI relativeURI = URI.create("/sbsp-account-profile.json#platformName");

        final URI absoluteURI = documentURI.resolve(relativeURI);

        Optional<SchemaBuilder> schema = refSchemaLoader.findRefInDocument(documentURI, absoluteURI, accountProfileJson, new LoadingReport());
        assertThat(schema).isPresent();
    }

    @Test
    public void resolve_WhenReferenceIsInsideDocument_AndRefHasLocalIdFragment_ThenReturnSomething() {
        URI relativeURI = URI.create("#platformName");

        final URI absoluteURI = documentURI.resolve(relativeURI);

        Optional<SchemaBuilder> schema = refSchemaLoader.findRefInDocument(documentURI, absoluteURI, accountProfileJson, new LoadingReport());
        assertThat(schema).isPresent();
    }

    @Test
    public void resolve_WhenReferenceIsInsideDocument_AndRefHasRelativePathWithLocalId_ThenReturnSomething() {
        URI relativeURI = URI.create("sbsp-account-profile.json#platformName");

        final URI absoluteURI = documentURI.resolve(relativeURI);

        Optional<SchemaBuilder> schema = refSchemaLoader.findRefInDocument(documentURI, absoluteURI, accountProfileJson, new LoadingReport());
        assertThat(schema).isPresent();
    }

    @Test
    public void resolve_WhenReferenceIsOutsideDocument_ThenReturnEmpty() {
        URI relativeURI = URI.create("/primitives.json#/definitions/color");

        final URI absoluteURI = documentURI.resolve(relativeURI);

        Optional<SchemaBuilder> schema = refSchemaLoader.findRefInDocument(documentURI, absoluteURI, accountProfileJson, new LoadingReport());
        assertThat(schema).isNotPresent();
    }

    private JsonObject readResource(String relativePath) {
        return resourceLoaderForInstance(this).readJsonObject(relativePath);
    }
}
