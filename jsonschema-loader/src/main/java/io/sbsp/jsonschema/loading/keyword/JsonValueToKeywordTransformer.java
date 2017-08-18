package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.keyword.SchemaKeyword;

import javax.json.JsonValue;
import java.util.Optional;

/**
 * This class can be used to quickly create a keyword loader that takes in a JsonValue and outputs the appropriate
 * keyword.  It assumes that someone else has already determined the correct key and validated that the incoming type
 * is one of the accepted types.
 */
@FunctionalInterface
public interface JsonValueToKeywordTransformer<K extends SchemaKeyword> {

    Optional<K> loadKeywordFromJsonValue(JsonValue jsonValue);

}
