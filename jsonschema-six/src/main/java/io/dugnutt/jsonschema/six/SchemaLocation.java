package io.dugnutt.jsonschema.six;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@Builder(toBuilder = true)
public class SchemaLocation {

    public static final String DUGNUTT_UUID_SCHEME = "dugnutt";

    @Nullable
    private final URI id;

    /**
     * Path within the containing document
     */
    @NonNull
    private final JsonPath jsonPath;

    /**
     * Resolution scope for this location
     */
    @NonNull
    private final URI resolutionScope;

    @NonNull
    private final URI documentURI;

    @NonNull
    private final URI absoluteURI;

    //Just for lombok... Grrrr
    public SchemaLocation(URI id, JsonPath jsonPath, URI resolutionScope, URI documentURI) {
        this(id, jsonPath, resolutionScope, documentURI, null);
    }

    private SchemaLocation(URI id, JsonPath jsonPath, URI resolutionScope, URI documentURI, URI absoluteURI) {
        this.id = id;
        this.jsonPath = jsonPath;
        this.resolutionScope = resolutionScope;
        this.documentURI = documentURI;
        if (id != null) {
            this.absoluteURI = resolutionScope.resolve(this.id);
        } else {
            this.absoluteURI = resolutionScope.resolve(getJsonPointerFragment());
        }
    }

    public static SchemaLocation schemaLocation() {
        // If there's not ID for a base schema, assign something unique to avoid false-positive cache-hites
        final URI baseURN = URI.create(DUGNUTT_UUID_SCHEME + "://autoassign-" + UUID.randomUUID().toString() + "/schema");
        return SchemaLocation.builder()
                .documentURI(baseURN)
                .jsonPath(JsonPath.rootPath())
                .resolutionScope(baseURN)
                .build();
    }

    public static SchemaLocation schemaLocation(URI rootDocument) {
        checkNotNull(rootDocument, "rootDocument must not be null");
        return new SchemaLocation(rootDocument, JsonPath.rootPath(), rootDocument, rootDocument);
    }

    public static SchemaLocation schemaLocation(String rootDocumentVal) {
        checkNotNull(rootDocumentVal, "rootDocument must not be null");
        JsonPath path;
        if (rootDocumentVal.startsWith("#/")) {
            path = JsonPath.parseFromURIFragment(rootDocumentVal);
        } else {
            path = JsonPath.rootPath();
        }
        URI rootDocument = URI.create(rootDocumentVal);
        return new SchemaLocation(rootDocument, path, rootDocument, rootDocument);
    }

    public URI getFullJsonPathURI() {
        return documentURI.resolve(getJsonPointerFragment());
    }

    public URI getJsonPointerFragment() {
        return jsonPath.toURIFragment();
    }

    public List<String> getPath() {
        return Arrays.stream(jsonPath.toArray()).map(Object::toString).collect(Collectors.toList());
    }

    public SchemaLocation withChildPath(String... jsonPath) {
        return builderWithResolutionScope()
                .id(null)
                .jsonPath(jsonPath)
                .build();
    }

    public SchemaLocation withChildPath(String key1, int key2) {
        return builderWithResolutionScope()
                .jsonPath(jsonPath.child(key1).child(key2))
                .build();
    }

    public SchemaLocation withChildPath(URI canonicalId, String... jsonPath) {
        checkNotNull(canonicalId, "canonicalId must not be null.  Use forChild(String... jsonPath)");
        checkNotNull(jsonPath, "jsonPath must not be null");

        JsonPath newPath = this.jsonPath;
        for (String pathPart : jsonPath) {
            newPath = newPath.child(pathPart);
        }

        return builderWithResolutionScope()
                .id(canonicalId)
                .jsonPath(jsonPath)
                .build();
    }

    public SchemaLocation withChildPath(JsonSchemaKeyword keyword) {
        return withChildPath(keyword.key());
    }

    private SchemaLocationBuilder builderWithResolutionScope() {
        final URI newResolutionPath;
        if (this.id != null) {
            newResolutionPath = this.resolutionScope.resolve(this.id);
        } else {
            newResolutionPath = this.resolutionScope;
        }
        return this.toBuilder()
                .id(null)
                .resolutionScope(newResolutionPath);
    }

    public static class SchemaLocationBuilder {
        public SchemaLocationBuilder jsonPath(JsonPath path) {
            this.jsonPath = path;
            return this;
        }

        public SchemaLocationBuilder jsonPath(String... pathParts) {
            checkNotNull(pathParts, "pathParts must not be null");

            JsonPath newPath = this.jsonPath;
            for (String pathPart : pathParts) {
                newPath = newPath.child(pathPart);
            }
            this.jsonPath = newPath;
            return this;
        }
    }
}
