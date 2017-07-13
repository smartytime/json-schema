package org.everit.jsonschema.loader;

import org.everit.jsonschema.api.ReferenceSchema;
import org.everit.jsonschema.api.Schema;
import org.everit.jsonschema.loader.internal.ReferenceResolver;
import org.everit.json.JsonApi;
import org.everit.json.JsonObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;
import static org.everit.jsonschema.api.JsonSchemaProperty.$REF;

/**
 * @author erosb
 */
class ReferenceLookup {

    static JsonObject<?> extend(JsonApi<?> jsonApi, JsonObject<?> additional, JsonObject<?> original) {
        if (additional.properties().isEmpty()) {
            return original;
        }
        if (original.properties().isEmpty()) {
            return additional;
        }
        Map<String, Object> rawObj = new HashMap<>();
        original.properties().forEach(name -> rawObj.put(name, original.git(name)));
        additional.properties().forEach(name -> rawObj.put(name, additional.git(name)));
        return jsonApi.fromMap(rawObj, original.path());
    }

    private LoadingState loadingState;
    private JsonApi jsonApi;

    public ReferenceLookup(LoadingState loadingState) {
        this.loadingState = requireNonNull(loadingState, "ls cannot eb null");
        this.jsonApi = checkNotNull(loadingState.jsonApi);
    }

    /**
     * Returns the absolute URI without its fragment part.
     *
     * @param fullUri the abslute URI
     * @return the URI without the fragment part
     */
    static URI withoutFragment(final String fullUri) {
        int hashmarkIdx = fullUri.indexOf('#');
        String rval;
        if (hashmarkIdx == -1) {
            rval = fullUri;
        } else {
            rval = fullUri.substring(0, hashmarkIdx);
        }
        try {
            return new URI(rval);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    JsonObject<?> withoutRef(JsonObject<?> original) {
        Map<String, Object> rawObj = new HashMap<>();
        original.properties().stream()
                .filter(name -> !$REF.getKey().equals(name))
                .forEach(name -> rawObj.put(name, original.git(name)));
        return jsonApi.fromMap(rawObj, original.path());
    }

    /**
     * Returns a schema builder instance after looking up the JSON pointer.
     */
    Schema.Builder<?> lookup(String relPointerString, JsonObject<?> ctx) {
        String absPointerString = ReferenceResolver.resolve(loadingState.id, relPointerString).toString();
        if (loadingState.pointerSchemas.containsKey(absPointerString)) {
            return loadingState.pointerSchemas.get(absPointerString);
        }
        boolean isExternal = !absPointerString.startsWith("#");
        JsonPointerEvaluator pointer = isExternal
                ? JsonPointerEvaluator.forURL(loadingState.httpClient, absPointerString, jsonApi)
                : JsonPointerEvaluator.forDocument(loadingState.rootSchemaJson, absPointerString, jsonApi);
        ReferenceSchema.Builder refBuilder = ReferenceSchema.builder()
                .refValue(relPointerString);
        loadingState.pointerSchemas.put(absPointerString, refBuilder);
        JsonPointerEvaluator.QueryResult result = pointer.query();
        JsonObject resultObject = extend(jsonApi, withoutRef(ctx), result.getQueryResult());
        SchemaLoader childLoader = loadingState.initChildLoader()
                        .resolutionScope(isExternal ? withoutFragment(absPointerString) : loadingState.id)
                        .schemaJson(resultObject)
                        .rootSchemaJson(result.getContainingDocument()).build();
        Schema referredSchema = childLoader.load().build();
        refBuilder.build().setReferredSchema(referredSchema);
        return refBuilder;
    }

}
