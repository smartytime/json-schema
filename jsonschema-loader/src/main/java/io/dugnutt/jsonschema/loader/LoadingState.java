package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.six.ReferenceSchema;
import io.dugnutt.jsonschema.six.JsonPointerPath;
import io.dugnutt.jsonschema.six.SchemaException;
import io.dugnutt.jsonschema.loader.internal.ReferenceResolver;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import java.net.URI;
import java.util.Comparator;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
public class LoadingState {

    public static final Comparator<Class<?>> CLASS_COMPARATOR = (cl1, cl2) -> cl1.getSimpleName().compareTo(cl2.getSimpleName());
    final SchemaClient httpClient;

    URI id = null;

    final JsonPointerPath pointerToCurrentObj;

    final Map<String, ReferenceSchema.Builder> pointerSchemas;

    final SchemaJsonWrapper rootSchemaJson;

    final SchemaJsonWrapper schemaJson;

    final JsonProvider provider;


    LoadingState(SchemaClient httpClient,
                 Map<String, ReferenceSchema.Builder> pointerSchemas,
                 JsonObject rootSchemaJson,
                 JsonObject schemaJson,
                 URI id,
                 JsonPointerPath pointerPath, JsonProvider provider) {
        this.httpClient = requireNonNull(httpClient, "httpClient cannot be null");
        this.pointerSchemas = requireNonNull(pointerSchemas, "pointerSchemas cannot be null");
        this.rootSchemaJson = new SchemaJsonWrapper(rootSchemaJson);
        this.schemaJson = new SchemaJsonWrapper(schemaJson);
        this.id = id;
        this.pointerToCurrentObj = pointerPath;
        this.provider = provider;
    }

    LoadingState(SchemaLoader.SchemaLoaderBuilder builder) {
        this(builder.httpClient,
                builder.pointerSchemas,
                builder.rootSchemaJson == null ? builder.schemaJson : builder.rootSchemaJson,
                builder.schemaJson,
                builder.id,
                builder.pointerToCurrentObj, builder.provider);
    }

    SchemaLoader.SchemaLoaderBuilder initChildLoader() {
        return SchemaLoader.builder()
                .resolutionScope(id)
                .schemaJson(schemaJson)
                .rootSchemaJson(rootSchemaJson)
                .pointerSchemas(pointerSchemas)
                .httpClient(httpClient)
                .pointerToCurrentObj(pointerToCurrentObj);
    }

    public LoadingState childFor(String key) {
        return new LoadingState(httpClient, pointerSchemas, rootSchemaJson, schemaJson, id, pointerToCurrentObj
                .child(key), provider);
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
        return pointerToCurrentObj.toURIFragment();
    }

    public SchemaException createSchemaException(String message) {
        return new SchemaException(locationOfCurrentObj(), message);
    }
}
