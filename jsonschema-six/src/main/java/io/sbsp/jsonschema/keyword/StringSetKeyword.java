package io.sbsp.jsonschema.keyword;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StringSetKeyword extends SchemaKeywordImpl<Set<String>> {

    public StringSetKeyword(String value) {
        super(Collections.singleton(value));
    }
    public StringSetKeyword(Set<String> keywordValue) {
        super(keywordValue);
    }

    public StringSetKeyword withAnotherValue(String anotherValue) {
        final Set<String> values = new HashSet<>(this.getKeywordValue());
        values.add(anotherValue);
        return new StringSetKeyword(values);
    }

    public Set<String> getStringSet() {
        return value();
    }
}
