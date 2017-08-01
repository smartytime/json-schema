package io.sbsp.jsonschema.six;

import com.google.common.base.MoreObjects;
import io.sbsp.jsonschema.six.enums.JsonSchemaKeyword;
import io.sbsp.jsonschema.utils.URIUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.annotation.Nullable;
import java.net.URI;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.sbsp.jsonschema.utils.URIUtils.generateUniqueURI;
import static io.sbsp.jsonschema.utils.URIUtils.resolve;

/**
 * Provides location information for any given schema or validation context, including:
 * <p>
 * documentURI: The path to the containing document<br/>
 * jsonPath: Path within the containing document to the current location<br/>
 * resolutionScope: The resolution scope for the current location<br/>
 * canonicalURI: Either an absolute json-pointer from the document base, or a resolution scope (if this location has $id)<br/>
 * uniqueURI: A potentially autogenerated unique URI that can be used to cache or look up a schema.
 */
@EqualsAndHashCode(doNotUseGetters = true, of = {"documentURI", "jsonPath", "resolutionScope"})
public class SchemaLocation {

    public static final JsonPath ROOT_PATH = JsonPath.rootPath();
    public static final URI ROOT_URI = URI.create("#");
    public static final URI BLANK_URI = URI.create("");

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

    /**
     * If this location has an $id, this canonicalURI represents $id resolved against any prior resolution scope.
     * Otherwise, this represents an absolute json-pointer URI resolved against documentURI
     * <p>
     * This URI does not need to be absolute
     */
    private URI canonicalURI;

    /**
     * A potentially autogenerated unique URI that can be used to cache or look up a schema.
     */
    private URI uniqueURI;

    private SchemaLocation(@Nullable URI canonicalURI, URI documentURI, URI resolutionScope, JsonPath jsonPath) {
        checkArgument(documentURI.isAbsolute(), "documentURI should be absolute");
        checkArgument(resolutionScope.isAbsolute(), "resolutionScope should be absolute");
        this.documentURI = checkNotNull(documentURI);
        this.resolutionScope = checkNotNull(resolutionScope);
        this.jsonPath = checkNotNull(jsonPath);
        this.canonicalURI = canonicalURI;
        if (!isGenerated()) {
            this.uniqueURI = this.canonicalURI;
        }
    }

    /**
     * @see #canonicalURI
     */
    public URI getCanonicalURI() {
        if (canonicalURI == null) {
            if (!isGenerated()) {
                this.canonicalURI = getUniqueURI();
            } else {
                this.canonicalURI = jsonPath.toURIFragment();
            }
        }
        return canonicalURI;
    }

    /**
     * @see #uniqueURI
     */
    public URI getUniqueURI() {
        if (this.uniqueURI == null) {
            this.uniqueURI = getAbsoluteJsonPointerURI();
        }
        return this.uniqueURI;
    }

    /**
     * @return The absolute json-pointer for this location, resolved against the documentURI
     */
    public URI getAbsoluteJsonPointerURI() {
        return resolve(this.documentURI, getJsonPointerFragment());
    }

    public URI getJsonPointerFragment() {
        return jsonPath.toURIFragment();
    }

    /**
     * @return Whether this location has an auto-generated root URI.
     */
    private boolean isGenerated() {
        return URIUtils.isGeneratedURI(this.documentURI);
    }

    public SchemaLocation child(String... jsonPath) {
        return new SchemaLocation(null, this.documentURI,
                resolutionScope, this.jsonPath.child(jsonPath));
    }

    public SchemaLocation child(String jsonPath) {
        return new SchemaLocation(null, this.documentURI,
                resolutionScope, this.jsonPath.child(jsonPath));
    }

    public SchemaLocation child(int key2) {
        return new SchemaLocation(null, this.documentURI,
                resolutionScope, this.jsonPath.child(key2));
    }

    public SchemaLocation child(URI canonicalId, String... jsonPath) {
        checkNotNull(canonicalId, "canonicalId must not be null.  Use forChild(String... jsonPath)");
        checkNotNull(jsonPath, "jsonPath must not be null");

        return toBuilder()
                .id(canonicalId)
                .jsonPath(this.jsonPath.child(jsonPath))
                .build();
    }

    public SchemaLocation child(JsonSchemaKeyword keyword) {
        return child(keyword.key());
    }

    public SchemaLocation child(String key1, int key2) {
        return new SchemaLocation(null, this.documentURI,
                resolutionScope, this.jsonPath.child(key1, String.valueOf(key2)));
    }

    @Override
    public String toString() {
        return this.getAbsoluteJsonPointerURI().toString();
    }

    public SchemaLocation withId(URI id) {
        checkNotNull(id, "id must not be null");
        return toBuilder()
                .id(id)
                .build();
    }

    private SchemaLocationBuilder toBuilder() {
        return new SchemaLocationBuilder(documentURI, resolutionScope, jsonPath);
    }

    public static SchemaLocation hashedRoot(Object builder, URI id) {
        checkNotNull(id, "id must not be null");
        final URI uniqueURI = generateUniqueURI(builder);
        final URI resolvedFromUnique = resolve(uniqueURI, id);
        final JsonPath path;
        if (URIUtils.isJsonPointer(id)) {
            return new SchemaLocation(resolvedFromUnique, resolvedFromUnique, resolvedFromUnique, JsonPath.parseFromURIFragment(id));
        } else {
            return new SchemaLocation(resolvedFromUnique, resolvedFromUnique, resolvedFromUnique, ROOT_PATH);
        }
    }

    public static SchemaLocation hashedRoot(Object builder) {
        // If there's not ID for a base schema, assign something unique to avoid false-positive cache-hites
        return new SchemaLocationBuilder(generateUniqueURI(builder)).build();
    }

    public static SchemaLocation documentRoot(URI $id) {
        checkNotNull($id, "$id must not be null");
        final JsonPath path;
        if (URIUtils.isJsonPointer($id)) {
            path = JsonPath.parseFromURIFragment($id);
        } else {
            path = ROOT_PATH;
        }
        return new SchemaLocationBuilder($id).jsonPath(path).build();
    }

    public static SchemaLocation documentRoot(String $id) {
        checkNotNull($id, "rootDocument must not be null");
        final URI rootAsURI = URI.create($id);
        return documentRoot(rootAsURI);
    }

    public static SchemaLocation refLocation(URI documentURI, @Nullable URI $id, JsonPath refPath) {
        checkNotNull(documentURI, "documentURI must not be null");
        checkNotNull(refPath, "refPath must not be null");
        URI resolutionScope;
        if ($id != null) {
            resolutionScope = resolve(documentURI, $id);
        } else {
            resolutionScope = documentURI;
        }
        return new SchemaLocationBuilder(documentURI, resolutionScope, refPath).build();
    }

    public static class SchemaLocationBuilder {
        private final URI documentURI;
        private final URI resolutionScope;
        private URI id;
        private JsonPath jsonPath;

        private SchemaLocationBuilder(URI id) {
            this.id = id;
            this.documentURI = null;
            this.resolutionScope = null;
            this.jsonPath = null;
        }

        private SchemaLocationBuilder(URI documentURI, URI resolutionScope, JsonPath jsonPath) {
            this.documentURI = documentURI;
            this.resolutionScope = resolutionScope;
            this.jsonPath = jsonPath;
        }

        public SchemaLocation build() {
            // Initialize everything from the id
            if (this.documentURI == null && this.id != null) {
                checkState(id.isAbsolute(), "ID must be absolute");
                final URI baseURI = id;
                return new SchemaLocation(baseURI, baseURI, baseURI, MoreObjects.firstNonNull(jsonPath, ROOT_PATH));
            }

            // Change resolution scope
            if (id != null) {
                checkState(resolutionScope != null, "Should have provided a resolution scope");
                final URI resolvedURI = resolve(resolutionScope, id);
                return new SchemaLocation(resolvedURI, documentURI, resolvedURI, jsonPath);
            }

            // Just changing jsonPath
            return new SchemaLocation(null, documentURI, resolutionScope, jsonPath);
        }

        public SchemaLocationBuilder id(String uri) {
            return this.id(URI.create(uri));
        }

        public SchemaLocationBuilder id(URI id) {
            this.id = id;
            return this;
        }

        public SchemaLocationBuilder jsonPath(JsonPath jsonPath) {
            this.jsonPath = jsonPath;
            return this;
        }

        private SchemaLocationBuilder appendJsonPath(String... pathParts) {
            checkNotNull(pathParts, "pathParts must not be null");
            return this.jsonPath(this.jsonPath.child(pathParts));
        }
    }
}
