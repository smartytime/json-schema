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

import static java.util.Objects.requireNonNull;
import static org.everit.jsonschema.api.JsonSchemaProperty.$REF;

/**
 * @author erosb
 */
class ReferenceLookup {

    static JsonObject<?> extend(JsonObject<?> additional, JsonObject<?> original) {
        JsonApi api = additional.api();
        if (additional.properties().isEmpty()) {
            return original;
        }
        if (original.properties().isEmpty()) {
            return additional;
        }
        Map<String, Object> rawObj = new HashMap<>();
        original.properties().forEach(name -> rawObj.put(name, original.git(name)));
        additional.properties().forEach(name -> rawObj.put(name, additional.git(name)));
        return api.fromMap(rawObj);
    }

    private LoadingState ls;
    private JsonApi jsonApi;

    public ReferenceLookup(LoadingState ls) {
        this.ls = requireNonNull(ls, "ls cannot eb null");
        this.jsonApi = ls.jsonApi;
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
        return jsonApi.fromMap(rawObj);
    }

    /**
     * Returns a schema builder instance after looking up the JSON pointer.
     */
    Schema.Builder<?> lookup(String relPointerString, JsonObject ctx) {
        String absPointerString = ReferenceResolver.resolve(ls.id, relPointerString).toString();
        if (ls.pointerSchemas.containsKey(absPointerString)) {
            return ls.pointerSchemas.get(absPointerString);
        }
        boolean isExternal = !absPointerString.startsWith("#");
        JsonPointerEvaluator pointer = isExternal
                ? JsonPointerEvaluator.forURL(ls.httpClient, absPointerString, jsonApi)
                : JsonPointerEvaluator.forDocument(ls.rootSchemaJson, absPointerString, jsonApi);
        ReferenceSchema.Builder refBuilder = ReferenceSchema.builder()
                .refValue(relPointerString);
        ls.pointerSchemas.put(absPointerString, refBuilder);
        JsonPointerEvaluator.QueryResult result = pointer.query();
        JsonObject resultObject = extend(withoutRef(ctx), result.getQueryResult());
        SchemaLoader childLoader = ls.initChildLoader()
                        .resolutionScope(isExternal ? withoutFragment(absPointerString) : ls.id)
                        .schemaJson(resultObject)
                        .rootSchemaJson(result.getContainingDocument()).build();
        Schema referredSchema = childLoader.load().build();
        refBuilder.build().setReferredSchema(referredSchema);
        return refBuilder;
    }

}
