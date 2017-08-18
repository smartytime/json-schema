package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.SchemaKeyword;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.loading.LoadingIssues.typeMismatch;

/**
 * Responsible for extracting a keyword or keywords from a json document and loading them into a {@link SchemaBuilder}
 *
 * These loaders specify the keywords they process via {@link #getIncludedKeywords()}, which allows for flexible or
 * strict loading
 */
public interface KeywordDigester<K extends SchemaKeyword> {

    /**
     * This list of keywords allows the processing system to handle most of the validation around "flexible" schema
     * parsing - knowing when to raise certain schema incompatibilities as errors or warnings.
     * @return
     */
    List<KeywordInfo<K>> getIncludedKeywords();

    /**
     * Extracts the specified keyword(s) from the source json document.  This method can either a) return the keywords
     * as a digest object (return value), or load them directly into the provided builder.
     *
     * It's recommended to return the values as a digest.
     *
     * @param jsonObject The source document to retrieve keyword for
     * @param builder The target schema builder
     * @param schemaLoader A loader that can be used to load subschemas or ref schemas
     * @param report A loading report object that stores any errors
     * @return Optionally, a keyword digest that contains the results of the processing.
     */
    Optional<KeywordDigest<K>> extractKeyword(JsonValueWithPath jsonObject, SchemaBuilder builder, SchemaLoader schemaLoader, LoadingReport report);
}
