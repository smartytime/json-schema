package io.dugnutt.jsonschema.six;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@Builder(toBuilder = true, builderMethodName = "locationBuilder")
@Getter
@EqualsAndHashCode
public class SchemaLocation {

    public static final JsonPath ROOT_PATH = JsonPath.rootPath();
    public static final URI ROOT_URI = URI.create("#");
    public static final String DUGNUTT_UUID_SCHEME = "dugnutt";

    private final URI absoluteURI;
    private final URI documentURI;

    /**
     * Path within the containing document
     */
    private final JsonPath jsonPath;

    /**
     * Resolution scope for this location
     */
    private final URI resolutionScope;

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

    public List<String> getJsonPathTokens() {
        return jsonPath.toStringPath();
    }

    public URI getJsonPointerFragment() {
        return jsonPath.toURIFragment();
    }

    public boolean isAutoAssign() {
        return DUGNUTT_UUID_SCHEME.equals(absoluteURI.getScheme());
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

        private URI id;

        public SchemaLocationBuilder() {
            jsonPath(ROOT_PATH);
            resolutionScope(ROOT_URI);
            documentURI(ROOT_URI);
        }

        public SchemaLocationBuilder absoluteURI(URI uri) {
            //Noop
            return this;
        }

        public SchemaLocation build() {
            if (id != null) {
                if (jsonPath == ROOT_PATH && SchemaUtils.isJsonPointer(id)) {
                    jsonPath(JsonPath.parseFromURIFragment(id));
                }
                if (documentURI == ROOT_URI) {
                    documentURI(id);
                }
                if (resolutionScope == ROOT_URI) {
                    resolutionScope(id);
                } else {
                    resolutionScope = resolutionScope.resolve(id);
                }
                this.absoluteURI = this.resolutionScope;
            } else {
                final URI jsonPathFragment = this.jsonPath.toURIFragment();
                this.absoluteURI = resolutionScope.resolve(jsonPathFragment);
            }

            return new SchemaLocation(this.absoluteURI, this.documentURI, this.jsonPath,
                    this.resolutionScope);
        }

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
