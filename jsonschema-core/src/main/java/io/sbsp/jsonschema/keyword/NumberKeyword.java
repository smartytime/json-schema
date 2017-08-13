package io.sbsp.jsonschema.keyword;

public class NumberKeyword extends SchemaKeywordImpl<Number> {

    public NumberKeyword(Number keywordValue) {
        super(keywordValue);
    }

    public double getDouble() {
        return getKeywordValue().doubleValue();
    }

    public int getInteger() {
        return getKeywordValue().intValue();
    }
}
