package org.everit.jsonschema.loader;

import org.everit.jsonschema.api.SchemaException;
import org.everit.json.JsonApi;
import org.everit.json.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
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

    private static JsonObject executeWith(final SchemaClient client, final String url, final JsonApi jsonApi) {
        String resp = null;
        BufferedReader buffReader = null;
        InputStreamReader reader = null;
        try {
            InputStream responseStream = client.get(url);
            reader = new InputStreamReader(responseStream, Charset.defaultCharset());
            buffReader = new BufferedReader(reader);
            String line;
            StringBuilder strBuilder = new StringBuilder();
            while ((line = buffReader.readLine()) != null) {
                strBuilder.append(line);
            }
            resp = strBuilder.toString();
            return jsonApi.readJson(resp);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Exception e) {
            throw new SchemaException(e.getMessage(), e);
        } finally {
            try {
                if (buffReader != null) {
                    buffReader.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    static final JsonPointerEvaluator forDocument(JsonObject document, String fragment, JsonApi jsonApi) {
        return new JsonPointerEvaluator(() -> document, fragment, jsonApi);
    }

    static final JsonPointerEvaluator forURL(SchemaClient schemaClient, String url, JsonApi jsonApi) {
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
        return new JsonPointerEvaluator(() -> executeWith(schemaClient, toBeQueried, jsonApi), fragment, jsonApi);
    }

    private final Supplier<JsonObject> documentProvider;

    private final String fragment;

    private final JsonApi<?> jsonApi;

    JsonPointerEvaluator(Supplier<JsonObject> documentProvider, String fragment, JsonApi jsonApi) {
        this.documentProvider = documentProvider;
        this.fragment = fragment;
        this.jsonApi = jsonApi;
    }

    /**
     * Queries from {@code document} based on this pointer.
     *
     * @return a DTO containing the query result and the root document containing the query result.
     * @throws IllegalArgumentException
     *         if the pointer does not start with {@code '#'}.
     */
    public QueryResult query() {
        JsonObject<?> document = documentProvider.get();
        if (fragment.isEmpty()) {
            return new QueryResult(document, document);
        }
        String[] path = fragment.split("/");
        if ((path[0] == null) || !path[0].startsWith("#")) {
            throw new IllegalArgumentException("JSON pointers must start with a '#'");
        }
        String[] trimmedPath = Arrays.copyOfRange(path, 1, path.length);

        return queryFrom(document, trimmedPath)
                .map(object -> new QueryResult(document, object))
                .orElseThrow(() -> new SchemaException(fragment, "Couldn't load fragment"));
    }

    private Optional<JsonObject<?>> queryFrom(JsonObject<?> document, String... path) {
        return jsonApi.pointer(path).queryFrom(document);
    }

}
