package org.everit.jsonschema.loader;

import org.everit.jsonschema.api.JsonPointerPath;
import org.everit.jsonschema.api.ReferenceSchema;
import org.everit.jsonschema.api.Schema;
import org.everit.jsonschema.loader.internal.ReferenceResolver;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.spi.JsonProvider;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;
import static org.everit.jsonschema.api.JsonSchemaProperty.$REF;

/**
 * @author erosb
 */
class ReferenceLookup {

    private static JsonObject combineWithRefSchema(JsonProvider provider, JsonObject additional, JsonObject original) {
        if (additional.keySet().isEmpty()) {
            return original;
        }
        if (original.keySet().isEmpty()) {
            return additional;
        }
        JsonObjectBuilder b = provider.createObjectBuilder();

        original.forEach(b::add);
        additional.forEach(b::add);
        return b.build();
    }

    private LoadingState loadingState;
    private JsonProvider provider;

    public ReferenceLookup(LoadingState loadingState) {
        this.loadingState = requireNonNull(loadingState, "ls cannot eb null");
        this.provider = checkNotNull(loadingState.provider);
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

    JsonObject withoutRef(JsonObject original) {
        JsonObjectBuilder b = provider.createObjectBuilder(original);
        b.remove($REF.key());

        //todo:ericm Need a path here??
        return b.build();
    }

    /**
     * Returns a schema builder instance after looking up the JSON pointer.
     */
    Schema.Builder<?> lookup(String jsonPointerVal, JsonObject document) {

        String absPointerString = ReferenceResolver.resolve(loadingState.id, jsonPointerVal).toString();
        if (loadingState.pointerSchemas.containsKey(absPointerString)) {
            return loadingState.pointerSchemas.get(absPointerString);
        }
        boolean isExternal = !absPointerString.startsWith("#");
        JsonPointerEvaluator pointer = isExternal
                ? JsonPointerEvaluator.forURL(loadingState.httpClient, absPointerString, provider)
                : JsonPointerEvaluator.forDocument(loadingState.rootSchemaJson, absPointerString, provider);
        ReferenceSchema.Builder refBuilder = ReferenceSchema.builder()
                .refValue(jsonPointerVal);
        loadingState.pointerSchemas.put(absPointerString, refBuilder);

        JsonPointerEvaluator.QueryResult queryResult = pointer.query();
        JsonObject resultObject = combineWithRefSchema(provider, withoutRef(document), queryResult.getQueryResult());
        JsonPointerPath schemaPath = new JsonPointerPath(absPointerString);
        SchemaLoader childLoader = loadingState.initChildLoader()
                .resolutionScope(isExternal ? withoutFragment(absPointerString) : loadingState.id)
                .schemaJson(new SchemaJsonWrapper(resultObject, schemaPath))
                .rootSchemaJson(queryResult.getContainingDocument()).build();
        Schema referredSchema = childLoader.load().build();
        refBuilder.build().setReferredSchema(referredSchema);
        return refBuilder;
    }

}
