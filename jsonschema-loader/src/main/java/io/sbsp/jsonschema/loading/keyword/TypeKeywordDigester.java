package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.TypeKeyword;
import io.sbsp.jsonschema.loading.KeywordDigest;
import io.sbsp.jsonschema.loading.KeywordDigester;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.SchemaLoader;

import javax.json.JsonString;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static javax.json.JsonValue.ValueType.*;

public class TypeKeywordDigester implements KeywordDigester<TypeKeyword> {

    private final List<KeywordInfo<TypeKeyword>> keywords;

    public TypeKeywordDigester() {
        this.keywords = Keywords.TYPE.getTypeVariants(STRING, ARRAY);
    }

    @Override
    public List<KeywordInfo<TypeKeyword>> getIncludedKeywords() {
        return keywords;
    }

    @Override
    public Optional<KeywordDigest<TypeKeyword>> extractKeyword(JsonValueWithPath jsonObject, SchemaBuilder builder, SchemaLoader schemaLoader, LoadingReport report) {
        final JsonValueWithPath type = jsonObject.path(Keywords.TYPE);
        if (type.is(ARRAY)) {
            final Set<JsonSchemaType> typeArray = type.asJsonArray().getValuesAs(JsonString.class).stream()
                    .map(JsonString::getString)
                    .map(JsonSchemaType::fromString)
                    .collect(Collectors.toSet());
            return KeywordDigest.ofOptional(Keywords.TYPE, new TypeKeyword(typeArray));
        } else {
            final JsonSchemaType typeString = JsonSchemaType.fromString(type.asString());
            return KeywordDigest.ofOptional(Keywords.TYPE, new TypeKeyword(typeString));

        }
    }
}
