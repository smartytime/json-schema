package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.loader.internal.ReferenceResolver;
import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.ReferenceSchema;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.spi.JsonProvider;
import java.net.URI;
import java.net.URISyntaxException;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

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

    private SchemaLoaderModel schemaLoaderModel;
    private JsonProvider provider;

    public ReferenceLookup(SchemaLoaderModel schemaLoaderModel) {
        this.schemaLoaderModel = requireNonNull(schemaLoaderModel, "ls cannot eb null");
        this.provider = checkNotNull(schemaLoaderModel.provider);
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
        b.remove(JsonSchemaKeyword.$REF.key());

        //todo:ericm Need a path here??
        return b.build();
    }

    /**
     * Returns a schema builder instance after looking up the JSON pointer.
     */
    Schema.Builder<?> lookup(String jsonPointerVal, JsonObject document) {

        String absPointerString = ReferenceResolver.resolve(schemaLoaderModel.id, jsonPointerVal).toString();
        if (schemaLoaderModel.pointerSchemas.containsKey(absPointerString)) {
            return schemaLoaderModel.pointerSchemas.get(absPointerString);
        }
        boolean isExternal = !absPointerString.startsWith("#");
        JsonPointerResolver pointer = isExternal
                ? JsonPointerResolver.forURL(schemaLoaderModel.httpClient, absPointerString, provider)
                : JsonPointerResolver.forDocument(schemaLoaderModel.rootSchemaJson, absPointerString, provider);
        ReferenceSchema.Builder refBuilder = ReferenceSchema.builder()
                .refValue(jsonPointerVal);
        schemaLoaderModel.pointerSchemas.put(absPointerString, refBuilder);

        JsonPointerResolver.QueryResult queryResult = pointer.query();
        //We shouldn't do this...
        // JsonObject resultObject = combineWithRefSchema(provider, withoutRef(document), queryResult.getQueryResult());
        JsonObject resultObject = queryResult.getQueryResult();
        SchemaLoader childLoader = schemaLoaderModel.initChildLoader()
                .resolutionScope(isExternal ? withoutFragment(absPointerString) : schemaLoaderModel.id)
                .schemaJson(new SchemaJsonObject(resultObject, path))
                .rootSchemaJson(queryResult.getContainingDocument()).build();
        Schema referredSchema = childLoader.load().build();
        refBuilder.build().setReferredSchema(referredSchema);
        return refBuilder;
    }

}
