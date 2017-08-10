package io.sbsp.jsonschema.builder;

import io.sbsp.jsonschema.SchemaFactory;
import io.sbsp.jsonschema.SchemaLocation;
import io.sbsp.jsonschema.keyword.SchemaKeyword;

import javax.annotation.Nullable;
import javax.json.JsonObject;

public interface SchemaKeywordBuilder<K extends SchemaKeyword> {
    K build(SchemaLocation location, @Nullable SchemaFactory factory, @Nullable JsonObject rootDocument);
}
