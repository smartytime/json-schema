package org.everit.jsonschema.loader;

import org.everit.json.JsonApi;
import org.everit.json.JsonObject;
import org.everit.jsonschema.api.ReferenceSchema;
import org.everit.jsonschema.api.SchemaException;
import org.everit.jsonschema.loader.internal.ReferenceResolver;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections.ListUtils.unmodifiableList;

/**
 * @author erosb
 */
public class LoadingState {

    public static final Comparator<Class<?>> CLASS_COMPARATOR = (cl1, cl2) -> cl1.getSimpleName().compareTo(cl2.getSimpleName());
    final SchemaClient httpClient;

    URI id = null;

    final List<String> pointerToCurrentObj;

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
            List<String> pointerToCurrentObj) {
        this.httpClient = requireNonNull(httpClient, "httpClient cannot be null");
        this.pointerSchemas = requireNonNull(pointerSchemas, "pointerSchemas cannot be null");
        this.rootSchemaJson = requireNonNull(rootSchemaJson, "rootSchemaJson cannot be null");
        this.jsonApi = checkNotNull(jsonApi);
        this.schemaJson = requireNonNull(schemaJson, "schemaJson cannot be null");
        this.id = id;
        this.pointerToCurrentObj = unmodifiableList(new ArrayList<>(
                requireNonNull(pointerToCurrentObj, "pointerToCurrentObj cannot be null")));
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
        List<String> newPtr = new ArrayList<>(pointerToCurrentObj.size() + 1);
        newPtr.addAll(pointerToCurrentObj);
        newPtr.add(key);
        return new LoadingState(httpClient, pointerSchemas, jsonApi, rootSchemaJson, schemaJson, id, newPtr);
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
