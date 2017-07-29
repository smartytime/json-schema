package io.dugnutt.jsonschema.six;

import com.google.common.base.MoreObjects;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.URI;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@EqualsAndHashCode
public class SchemaLocation {

    public static final JsonPath ROOT_PATH = JsonPath.rootPath();
    public static final URI ROOT_URI = URI.create("#");
    public static final String DUGNUTT_UUID_SCHEME = "dugnutt";

    private URI absoluteURI;

    @Getter
    private final URI documentURI;

    /**
     * Path within the containing document
     */
    @Getter
    private final JsonPath jsonPath;

    /**
     * Resolution scope for this location
     */
    @Getter
    private final URI resolutionScope;

    private final boolean hasId;

    @Builder(builderMethodName = "locationBuilder")
    private SchemaLocation(URI id, URI documentURI, URI resolutionScope, JsonPath jsonPath) {
        if (id != null) {
            if (jsonPath == null && SchemaUtils.isJsonPointer(id)) {
                this.jsonPath = JsonPath.parseFromURIFragment(id);
            } else if(jsonPath != null) {
                this.jsonPath = jsonPath;
            } else {
                this.jsonPath = ROOT_PATH;
            }
            this.documentURI = MoreObjects.firstNonNull(documentURI, id);
            if (resolutionScope == null) {
                this.resolutionScope = id;
            } else {
                this.resolutionScope = resolutionScope.resolve(id);
            }
            this.hasId = true;
        } else {
            this.hasId = false;
            this.documentURI = MoreObjects.firstNonNull(documentURI, ROOT_URI);
            this.jsonPath = MoreObjects.firstNonNull(jsonPath, ROOT_PATH);
            this.resolutionScope = MoreObjects.firstNonNull(resolutionScope, ROOT_URI);
        }
    }

    public URI getAbsoluteURI() {
        if (absoluteURI == null) {
            if (hasId) {
                this.absoluteURI = resolutionScope;
            } else {
                this.absoluteURI= resolutionScope.resolve(jsonPath.toURIFragment());
            }
        }
        return absoluteURI;
    }

    // private SchemaLocation(URI absoluteURI, URI documentURI, JsonPath jsonPath, URI resolutionScope) {
    //     checkNotNull(absoluteURI, "absoluteURI must not be null");
    //     checkNotNull(documentURI, "documentURI must not be null");
    //     checkNotNull(jsonPath, "jsonPath must not be null");
    //     checkNotNull(resolutionScope, "resolutionScope must not be null");
    //
    //     this.absoluteURI = absoluteURI;
    //     this.documentURI = documentURI;
    //     this.jsonPath = jsonPath;
    //     this.resolutionScope = resolutionScope;
    // }

    public URI getAbsoluteJsonPointerURI() {
        return this.documentURI.resolve(getJsonPointerFragment());
    }

    public URI getJsonPointerFragment() {
        return jsonPath.toURIFragment();
    }

    public boolean isAutoAssign() {
        return DUGNUTT_UUID_SCHEME.equals(getAbsoluteURI().getScheme());
    }

    public SchemaLocation withChildPath(String... jsonPath) {
        return toBuilder()
                .appendJsonPath(jsonPath)
                .build();
    }


    public SchemaLocation withChildPath(String key1, int key2) {
        return toBuilder()
                .jsonPath(getJsonPath().child(key1).child(key2))
                .build();
    }

    public SchemaLocationBuilder toBuilder() {
        return new SchemaLocationBuilder()
                .jsonPath(jsonPath)
                .resolutionScope(resolutionScope)
                .documentURI(documentURI);

    }

    public SchemaLocation withChildPath(int key2) {
        return toBuilder()
                .jsonPath(getJsonPath().child(key2))
                .build();
    }

    public SchemaLocation withChildPath(URI canonicalId, String... jsonPath) {
        checkNotNull(canonicalId, "canonicalId must not be null.  Use forChild(String... jsonPath)");
        checkNotNull(jsonPath, "jsonPath must not be null");

        JsonPath newPath = this.getJsonPath();
        for (String pathPart : jsonPath) {
            newPath = newPath.child(pathPart);
        }

        return toBuilder()
                .id(canonicalId)
                .jsonPath(newPath)
                .build();
    }

    public SchemaLocation withChildPath(JsonSchemaKeyword keyword) {
        return withChildPath(keyword.key());
    }

    public SchemaLocation withId(URI id) {
        checkNotNull(id, "id must not be null");
        return toBuilder()
                .id(id)
                .build();
    }

    public static SchemaLocation anonymousRoot() {
        // If there's not ID for a base schema, assign something unique to avoid false-positive cache-hites
        final URI baseURN = URI.create(DUGNUTT_UUID_SCHEME + "://autoassign-" + UUID.randomUUID().toString() + "/schema");
        return locationBuilder()
                .documentURI(baseURN)
                .jsonPath(JsonPath.rootPath())
                .resolutionScope(baseURN)
                .build();
    }

    public static SchemaLocation schemaLocation(URI rootDocument) {
        checkNotNull(rootDocument, "rootDocument must not be null");
        final JsonPath path;
        if (SchemaUtils.isJsonPointer(rootDocument)) {
            path = JsonPath.parseFromURIFragment(rootDocument);
        } else {
            path = JsonPath.rootPath();
        }
        return locationBuilder()
                .id(rootDocument)
                .resolutionScope(rootDocument)
                .documentURI(rootDocument)
                .jsonPath(path)
                .build();
    }

    public static SchemaLocation schemaLocation(String rootDocumentVal) {
        checkNotNull(rootDocumentVal, "rootDocument must not be null");
        final URI rootAsURI = URI.create(rootDocumentVal);
        return schemaLocation(rootAsURI);
    }

    public static class SchemaLocationBuilder {

        public SchemaLocationBuilder id(String uri) {
            this.id = URI.create(uri);
            return this;
        }

        public SchemaLocationBuilder id(URI id) {
            this.id = id;
            return this;
        }

        private SchemaLocationBuilder appendJsonPath(String... pathParts) {
            checkNotNull(pathParts, "pathParts must not be null");

            JsonPath newPath = this.jsonPath;
            for (String pathPart : pathParts) {
                newPath = newPath.child(pathPart);
            }
            this.jsonPath(newPath);
            return this;
        }
    }
}
