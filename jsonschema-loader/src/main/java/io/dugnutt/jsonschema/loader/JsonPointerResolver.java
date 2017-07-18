package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.loader.reference.SchemaClient;
import io.dugnutt.jsonschema.six.UnexpectedValueException;
import lombok.SneakyThrows;
import io.dugnutt.jsonschema.six.SchemaException;

import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonPointer;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.function.Supplier;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Objects.requireNonNull;

/**
 * Used to find subschemas either in an existing document, or to fetch a remote document and find a schema within that document.
 *
 * @author erosb
 */
public class JsonPointerResolver {

    /**
     * Document could come from URL or memory
     */
    private final Supplier<JsonObject> documentProvider;

    /**
     * The json pointer represented by the fragment;
     */
    private final JsonPointer jsonPointer;

    /**
     * The fragment used to create the pointer;
     */
    private final String uriFragment;

    JsonPointerResolver(Supplier<JsonObject> documentProvider, String uriFragment, JsonProvider provider) {
        this.documentProvider = checkNotNull(documentProvider);
        checkArgument(uriFragment.startsWith("#"), "Fragment must start with a hash");
        checkArgument(uriFragment.length() == 1 || uriFragment.startsWith("#/"), "If fragment is longer than 1 character, hash " +
                "must be followed by a forward slash");

        try {
            this.uriFragment = URLDecoder.decode(uriFragment, UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new JsonException("Unable to decode pointer uri fragment", e);
        }

        //Trim the hash to create a json-pointer
        String jsonPointerValue = this.uriFragment.substring(1);

        jsonPointer = provider.createPointer(jsonPointerValue);
    }

    /**
     * Queries from {@code document} based on this pointer.
     *
     * @return a DTO containing the query result and the root document containing the query result.
     * @throws IllegalArgumentException if the pointer does not start with {@code '#'}.
     */
    public QueryResult query() {
        JsonObject document = documentProvider.get();

        if (!jsonPointer.containsValue(document)) {
            throw new SchemaException(uriFragment, "Couldn't load fragment");
        } else {
            JsonValue pointerValue = jsonPointer.getValue(document);
            if (!(pointerValue instanceof JsonObject)) {
                throw new UnexpectedValueException(jsonPointer, pointerValue, JsonValue.ValueType.OBJECT);
            }
            return new QueryResult(document, pointerValue.asJsonObject());
        }
    }

    static final JsonPointerResolver forDocument(JsonObject document, String fragment, JsonProvider provider) {
        return new JsonPointerResolver(() -> document, fragment, provider);
    }

    static final JsonPointerResolver forURL(SchemaClient schemaClient, String url, JsonProvider provider) {
        int poundIdx = url.indexOf('#');
        String uriFragment;
        String toFetch;
        if (poundIdx == -1) {
            toFetch = url;
            uriFragment = "#";
        } else {
            uriFragment = url.substring(poundIdx);
            toFetch = url.substring(0, poundIdx);
        }
        return new JsonPointerResolver(() -> fetchDocument(schemaClient, toFetch, provider), uriFragment, provider);
    }

    @SneakyThrows
    private static JsonObject fetchDocument(final SchemaClient client, final String url, final JsonProvider provider) {
        try (InputStream responseStream = client.get(url)) {
            return provider.createReader(responseStream).readObject();
        }
    }

    /**
     * Data-transfer object for holding the result of a JSON pointer query.
     */
    static class QueryResult {

        private final JsonObject containingDocument;
        private final JsonObject queryResult;

        /**
         * Constructor.
         *
         * @param containingDocument the JSON document which contains the query result.
         * @param queryResult        the JSON object being the result of the query execution.
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
}
