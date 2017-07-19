package io.dugnutt.jsonschema.six;

import lombok.Builder;
import lombok.Getter;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@Builder(toBuilder = true)
public class SchemaLocation {
    private final URI id;

    private final JsonPath jsonPath;

    private final URI priorResolutionScope;

    private final URI documentUri;

    private final URI absoluteLocation;

    //Just for lombok... Grrrr
    public SchemaLocation(URI id, JsonPath jsonPath, URI resolutionScope, URI documentUri) {
        this(id, jsonPath, resolutionScope, documentUri, null);
    }

    private SchemaLocation(URI id, JsonPath jsonPath, URI priorResolutionScope, URI documentUri, URI absoluteLocation) {
        this.id = id;
        this.jsonPath = jsonPath;
        this.priorResolutionScope = priorResolutionScope;
        this.documentUri = documentUri;
        if (id != null) {
            this.absoluteLocation = priorResolutionScope.resolve(this.id);
        } else {
            this.absoluteLocation = getRelativeURI();
        }
    }

    public static SchemaLocation rootSchemaLocation() {
        return rootSchemaLocation(URI.create("#"));
    }

    public static SchemaLocation rootSchemaLocation(URI rootDocument) {
        checkNotNull(rootDocument, "rootDocument must not be null");
        return new SchemaLocation(rootDocument, JsonPath.jsonPath(), rootDocument, rootDocument);
    }

    public List<String> getPath() {
        return Arrays.stream(jsonPath.toArray()).map(Object::toString).collect(Collectors.toList());
    }

    public static SchemaLocation rootSchemaLocation(String rootDocumentVal) {
        checkNotNull(rootDocumentVal, "rootDocument must not be null");
        URI rootDocument = URI.create(rootDocumentVal);
        return new SchemaLocation(rootDocument, JsonPath.jsonPath(), rootDocument, rootDocument);
    }

    public URI getFullJsonPathURI() {
        return documentUri.resolve(jsonPath.toJsonPointer());
    }

    public URI getRelativeURI() {
        return URI.create("#" + jsonPath.toJsonPointer());
    }

    public SchemaLocation withChildPath(String... jsonPath) {
        return this.toBuilder()
                .id(null)
                .jsonPath(jsonPath)
                .build();
    }

    public SchemaLocation withChildPath(String key1, int key2) {
        return this.toBuilder()
                .id(null)
                .jsonPath(jsonPath.child(key1).child(key2))
                .build();
    }

    public SchemaLocation withChildPath(URI canonicalId, String... jsonPath) {
        checkNotNull(canonicalId, "canonicalId must not be null.  Use forChild(String... jsonPath)");
        checkNotNull(jsonPath, "jsonPath must not be null");
        checkArgument(jsonPath.length > 0, "Must have jsonPath");

        JsonPath newPath = this.jsonPath;
        for (String pathPart : jsonPath) {
            newPath = newPath.child(pathPart);
        }

        SchemaLocationBuilder newLocation = this.toBuilder();
        if (this.id != null) {
            newLocation.priorResolutionScope(this.absoluteLocation);
        }

        return newLocation
                .id(canonicalId)
                .jsonPath(jsonPath)
                .build();
    }

    public SchemaLocation withChildPath(JsonSchemaKeyword keyword) {
        return withChildPath(keyword.key());
    }

    public static class SchemaLocationBuilder {
        public SchemaLocationBuilder jsonPath(JsonPath path) {
            this.jsonPath = path;
            return this;
        }

        public SchemaLocationBuilder jsonPath(String... pathParts) {
            checkNotNull(pathParts, "pathParts must not be null");
            checkArgument(pathParts.length > 0, "Must have pathParts");

            JsonPath newPath = this.jsonPath;
            for (String pathPart : pathParts) {
                newPath = newPath.child(pathPart);
            }
            this.jsonPath = newPath;
            return this;
        }
    }
}
