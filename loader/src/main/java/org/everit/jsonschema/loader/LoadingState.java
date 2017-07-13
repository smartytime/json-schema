package org.everit.jsonschema.loader;

import org.everit.json.JsonApi;
import org.everit.json.JsonObject;
import org.everit.json.JsonPath;
import org.everit.jsonschema.api.ReferenceSchema;
import org.everit.jsonschema.api.SchemaException;
import org.everit.jsonschema.loader.internal.ReferenceResolver;

import java.net.URI;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
public class LoadingState {

    public static final Comparator<Class<?>> CLASS_COMPARATOR = (cl1, cl2) -> cl1.getSimpleName().compareTo(cl2.getSimpleName());
    final SchemaClient httpClient;

    URI id = null;

    final JsonPath pointerToCurrentObj;

    final Map<String, ReferenceSchema.Builder> pointerSchemas;

    final JsonObject<?> rootSchemaJson;

    final JsonObject<?> schemaJson;

    final JsonApi jsonApi;

    LoadingState(SchemaClient httpClient,
            Map<String, ReferenceSchema.Builder> pointerSchemas,
            JsonApi jsonApi,
            JsonObject rootSchemaJson,
            JsonObject schemaJson,
            URI id,
            JsonPath pointerToCurrentObj) {
        this.httpClient = requireNonNull(httpClient, "httpClient cannot be null");
        this.pointerSchemas = requireNonNull(pointerSchemas, "pointerSchemas cannot be null");
        this.rootSchemaJson = requireNonNull(rootSchemaJson, "rootSchemaJson cannot be null");
        this.jsonApi = checkNotNull(jsonApi);
        this.schemaJson = requireNonNull(schemaJson, "schemaJson cannot be null");
        this.id = id;
        this.pointerToCurrentObj = pointerToCurrentObj;
    }

    LoadingState(SchemaLoader.SchemaLoaderBuilder builder) {
        this(builder.httpClient,
                builder.pointerSchemas,
                builder.jsonApi,
                builder.rootSchemaJson == null ? builder.schemaJson : builder.rootSchemaJson,
                builder.schemaJson,
                builder.id,
                builder.pointerToCurrentObj);
    }

    SchemaLoader.SchemaLoaderBuilder initChildLoader() {
//        System.out.println("initChildLoader() " + pointerToCurrentObj.stream().collect(joining(", ")));
        return SchemaLoader.builder()
                .resolutionScope(id)
                .jsonApi(jsonApi)
                .schemaJson(schemaJson)
                .rootSchemaJson(rootSchemaJson)
                .pointerSchemas(pointerSchemas)
                .httpClient(httpClient)
                .pointerToCurrentObj(pointerToCurrentObj);
    }

    public LoadingState childFor(String key) {
        return new LoadingState(httpClient, pointerSchemas, jsonApi, rootSchemaJson, schemaJson, id, pointerToCurrentObj.child(key));
    }

    public LoadingState childFor(int arrayIndex) {
        return childFor(String.valueOf(arrayIndex));
    }

    public LoadingState childForId(Object idAttr) {
        URI childId = idAttr == null || !(idAttr instanceof String)
                ? this.id
                : ReferenceResolver.resolve(this.id, (String) idAttr);
        return new LoadingState(initChildLoader().resolutionScope(childId));
    }

    String locationOfCurrentObj() {
        return jsonApi.pointer(pointerToCurrentObj).toURIFragment();
    }

    public SchemaException createSchemaException(String message) {
        return new SchemaException(locationOfCurrentObj(), message);
    }

    public SchemaException createSchemaException(Class<?> actualType, Class<?> expectedType, Class<?>... furtherExpectedTypes) {
        return new SchemaException(locationOfCurrentObj(), actualType, expectedType, furtherExpectedTypes);
    }

    public SchemaException createSchemaException(Class<?> actualType, Collection<Class<?>> expectedTypes) {
        ArrayList<Class<?>> sortedTypes = new ArrayList<>(expectedTypes);
        Collections.sort(sortedTypes, CLASS_COMPARATOR);
        return new SchemaException(locationOfCurrentObj(), actualType, sortedTypes);
    }

}
