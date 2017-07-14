package org.everit.jsonschema.loader;

import lombok.SneakyThrows;
import org.everit.jsonschema.api.SchemaException;
import org.everit.jsonschema.api.UnexpectedValueException;

import javax.json.JsonObject;
import javax.json.JsonPointer;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import java.io.InputStream;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * @author erosb
 */
class JsonPointerEvaluator {


    /**
     * Data-transfer object for holding the result of a JSON pointer query.
     */
    static class QueryResult {

        private final JsonObject containingDocument;

        private final JsonObject queryResult;

        /**
         * Constructor.
         *
         * @param containingDocument
         *         the JSON document which contains the query result.
         * @param queryResult
         *         the JSON object being the result of the query execution.
         */
        QueryResult(JsonObject containingDocument, JsonObject queryResult) {
            this.containingDocument = requireNonNull(containingDocument, "containingDocument cannot be null");
            this.queryResult = requireNonNull(queryResult, "queryResult cannot be null");
        }

        /**
         * Getter for {@link #containingDocument}.
         *
         * @return the JSON document which contains the query result.
         */
        public JsonObject getContainingDocument() {
            return containingDocument;
        }

        /**
         * Getter for {@link #queryResult}.
         *
         * @return the JSON object being the result of the query execution.
         */
        public JsonObject getQueryResult() {
            return queryResult;
        }

    }

    @SneakyThrows
    private static JsonObject executeWith(final SchemaClient client, final String url, final JsonProvider provider) {
        try (InputStream responseStream = client.get(url)) {
            return provider.createReader(responseStream).readObject();
        }
    }

    static final JsonPointerEvaluator forDocument(JsonObject document, String fragment, JsonProvider provider) {
        return new JsonPointerEvaluator(() -> document, fragment, provider);
    }

    static final JsonPointerEvaluator forURL(SchemaClient schemaClient, String url, JsonProvider provider) {
        int poundIdx = url.indexOf('#');
        String fragment;
        String toBeQueried;
        if (poundIdx == -1) {
            toBeQueried = url;
            fragment = "";
        } else {
            fragment = url.substring(poundIdx);
            toBeQueried = url.substring(0, poundIdx);
        }
        return new JsonPointerEvaluator(() -> executeWith(schemaClient, toBeQueried, provider), fragment, provider);
    }

    private final Supplier<JsonObject> documentProvider;

    private final String fragment;

    private final JsonProvider provider;

    JsonPointerEvaluator(Supplier<JsonObject> documentProvider, String fragment, JsonProvider provider) {
        this.documentProvider = documentProvider;
        this.fragment = fragment;
        this.provider = provider;
    }

    /**
     * Queries from {@code document} based on this pointer.
     *
     * @return a DTO containing the query result and the root document containing the query result.
     * @throws IllegalArgumentException
     *         if the pointer does not start with {@code '#'}.
     */
    public QueryResult query() {
        JsonObject document = documentProvider.get();
        if (fragment.isEmpty()) {
            return new QueryResult(document, document);
        }
        JsonPointer pointer = provider.createPointer(fragment);
        if (!pointer.containsValue(document)) {
            throw new SchemaException(fragment, "Couldn't load fragment");
        } else {
            JsonValue pointerValue = pointer.getValue(document);
            if (pointerValue.getValueType() != JsonValue.ValueType.OBJECT) {
                throw new UnexpectedValueException(pointerValue, JsonValue.ValueType.OBJECT);
            }
            return new QueryResult(document, (JsonObject) pointerValue);
        }
    }
}
