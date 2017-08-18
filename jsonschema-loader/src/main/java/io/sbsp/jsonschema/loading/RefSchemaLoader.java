package io.sbsp.jsonschema.loading;

import com.google.common.base.Strings;
import io.sbsp.jsonschema.JsonPath;
import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.SchemaException;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.SchemaLoader;
import io.sbsp.jsonschema.loading.reference.JsonDocumentClient;
import io.sbsp.jsonschema.utils.JsonUtils;
import io.sbsp.jsonschema.utils.URIUtils;

import javax.annotation.Nullable;
import javax.json.JsonObject;
import javax.json.JsonPointer;
import javax.json.spi.JsonProvider;
import java.net.URI;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.JsonValueWithPath.*;
import static io.sbsp.jsonschema.SchemaLocation.BLANK_URI;
import static io.sbsp.jsonschema.SchemaLocation.ROOT_URI;

public class RefSchemaLoader  {

    private final JsonProvider provider;
    private final JsonDocumentClient documentClient;
    private final SchemaLoader schemaLoader;

    public RefSchemaLoader(JsonProvider provider, JsonDocumentClient documentClient, SchemaLoader schemaLoader) {
        this.provider = checkNotNull(provider, "Must have a provider");
        this.documentClient = checkNotNull(documentClient);
        this.schemaLoader = checkNotNull(schemaLoader, "schemaLoader must not be null");
    }

    public Schema loadRefSchema(Schema referencedFrom, URI refURI, @Nullable JsonObject currentDocument, LoadingReport report) {
        // Cache ahead to deal with any infinite recursion.
        final SchemaLocation currentLocation = referencedFrom.getLocation();
        schemaLoader.registerLoadedSchema(referencedFrom);

        // Make sure we're dealing with an absolute URI
        final URI absoluteReferenceURI = currentLocation.getResolutionScope().resolve(refURI);
        final URI documentURI = currentLocation.getDocumentURI();

        // Look for a cache schema at this URI
        final Optional<Schema> cachedSchema = schemaLoader.findLoadedSchema(absoluteReferenceURI);
        if (cachedSchema.isPresent()) {
            return cachedSchema.get();
        }

        final SchemaBuilder schemaBuilder = findRefInDocument(documentURI, absoluteReferenceURI, currentDocument, report)
                .orElseGet(() -> findRefInRemoteDocument(absoluteReferenceURI, report));
        final Schema refSchema = schemaBuilder.build();
        schemaLoader.registerLoadedSchema(refSchema);
        return refSchema;
    }

    JsonObject loadDocument(URI referenceURI) {
        final URI remoteDocumentURI = URIUtils.withoutFragment(referenceURI);
        final JsonObject targetDocument;

        targetDocument = documentClient.findLoadedDocument(remoteDocumentURI)
                .orElseGet(() -> {
                    String scheme = Strings.nullToEmpty(referenceURI.getScheme()).toLowerCase();
                    if (!scheme.startsWith("http")) {
                        throw new SchemaException(referenceURI, "Couldn't resolve ref within document, but can't readSchema non-http scheme: %s", scheme);
                    }

                    // Load document remotely
                    final JsonObject document = documentClient.fetchDocument(remoteDocumentURI);
                    documentClient.registerLoadedDocument(remoteDocumentURI, document);
                    return document;
                });

        if (targetDocument == null) {
            throw new SchemaException(referenceURI, "Unable to get document: " + referenceURI);
        }

        return targetDocument;
    }

    SchemaBuilder findRefInRemoteDocument(URI referenceURI, LoadingReport report) {
        checkNotNull(referenceURI, "referenceURI must not be null");
        URI remoteDocumentURI = referenceURI.resolve("#");
        final JsonObject remoteDocument = loadDocument(remoteDocumentURI);
        return findRefInDocument(remoteDocumentURI, referenceURI, remoteDocument, report)
                .orElseThrow(() -> new SchemaException(referenceURI, "Unable to locate fragment: \n\tFragment: '#%s' in document\n\tDocument:'%s'", referenceURI.getFragment(), remoteDocument));
    }

    Optional<SchemaBuilder> findRefInDocument(URI documentURI, URI referenceURI, JsonObject parentDocument, LoadingReport report) {
        if (parentDocument == null) {
            parentDocument = loadDocument(referenceURI);
        }

        //Remove any fragments from the parentDocument URI
        documentURI = URIUtils.withoutFragment(documentURI);

        // Relativizing strips the path down to only the difference between the documentURI and referenceURI.
        // This will tell us whether the referenceURI is naturally scoped within the parentDocument.
        URI relativeURL = documentURI.relativize(referenceURI);

        final JsonPath pathWithinDocument;
        if (relativeURL.equals(ROOT_URI) || relativeURL.equals(BLANK_URI)) {
            // The parentDocument is the target
            pathWithinDocument = JsonPath.rootPath();
        } else if (URIUtils.isJsonPointer(relativeURL)) {
            //This is a json fragment
            pathWithinDocument = JsonPath.parseFromURIFragment(relativeURL);
        } else {
            //This must be a reference $id somewhere in the parentDocument.
            pathWithinDocument = documentClient.resolveSchemaWithinDocument(documentURI, referenceURI, parentDocument)
                    .orElse(null);
        }
        if (pathWithinDocument != null) {
            final JsonPointer pointer = provider.createPointer(pathWithinDocument.toJsonPointer());
            final JsonObject schemaObject;
            if (!pointer.containsValue(parentDocument)) {
                throw new SchemaException(referenceURI, "Unable to resolve '#" + relativeURL + "' as JSON Pointer within '" + documentURI + "'");
            } else {
                schemaObject = pointer.getValue(parentDocument).asJsonObject();
            }

            final URI found$ID = JsonUtils.extract$IdFromObject(schemaObject).orElse(null);
            SchemaLocation fetchedDocumentLocation = SchemaLocation.refLocation(documentURI, found$ID, pathWithinDocument);
            final JsonValueWithPath schemaJson = fromJsonValue(parentDocument, schemaObject, fetchedDocumentLocation);
            return Optional.of(schemaLoader.subSchemaBuilder(schemaJson, parentDocument, report));
        }
        return Optional.empty();
    }
}
