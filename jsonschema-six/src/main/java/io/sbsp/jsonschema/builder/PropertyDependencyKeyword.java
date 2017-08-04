package io.sbsp.jsonschema.builder;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.enums.JsonSchemaKeywordType;
import io.sbsp.jsonschema.keyword.SchemaKeyword;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class PropertyDependencyKeyword implements SchemaKeyword {

    private final SetMultimap<String, String> keywordValue;
    private final Set<JsonSchemaKeywordType> keywords;

    public PropertyDependencyKeyword(String key, String keywordValue, JsonSchemaKeywordType keyword) {
        checkNotNull(key, "key must not be null");
        checkNotNull(keywordValue, "keywordValue must not be null");
        checkNotNull(keyword, "keyword must not be null");
        this.keywords = Collections.unmodifiableSet(EnumSet.of(keyword));
        this.keywordValue = ImmutableSetMultimap.of(key, keywordValue);
    }

    private PropertyDependencyKeyword(ImmutableSetMultimap<String, String> keywordValue, Set<JsonSchemaKeywordType> keywords) {
        checkNotNull(keywordValue, "keywordValue must not be null");
        checkNotNull(keywords, "keywords must not be null");
        this.keywords = keywords;
        this.keywordValue = keywordValue;
    }

    public SetMultimap<String, String> getPropertyDependencies() {
        return keywordValue;
    }

    public PropertyDependencyKeyword propertyDependency(String ifThisProperty, String thenExpectThisProperty) {
        checkNotNull(thenExpectThisProperty, "thenExpectThisProperty must not be null");

        final ImmutableSetMultimap<String, String> withNewValue = ImmutableSetMultimap.<String, String>builder()
                .putAll(keywordValue)
                .put(ifThisProperty, thenExpectThisProperty).build();
        return new PropertyDependencyKeyword(withNewValue, keywords);
    }


}
