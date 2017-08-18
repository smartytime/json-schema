package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The result of a {@link KeywordDigester} processing a json document to extract keywords.
 *
 * Returns a keyword value that was retrieved from the document, along with the keyword metadata that sourced it.
 */
@Getter
@EqualsAndHashCode
@ToString
public class KeywordDigest<K extends SchemaKeyword> {
    private final KeywordInfo<K> keyword;
    private final K keywordValue;

    private KeywordDigest(KeywordInfo<K> keyword, K keywordValue) {
        this.keyword = checkNotNull(keyword, "keyword must not be null");
        this.keywordValue = checkNotNull(keywordValue, "keywordValue must not be null");
    }

    public static <K extends SchemaKeyword> KeywordDigest<K> of(KeywordInfo<K> keyword, K value) {
        return new KeywordDigest<>(keyword, value);
    }

    public static <K extends SchemaKeyword> Optional<KeywordDigest<K>> ofOptional(KeywordInfo<K> keyword, K value) {
        return Optional.of(new KeywordDigest<>(keyword, value));
    }
}
