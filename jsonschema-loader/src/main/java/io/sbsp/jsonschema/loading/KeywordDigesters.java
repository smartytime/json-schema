package io.sbsp.jsonschema.loading;

import com.google.common.collect.Lists;
import io.sbsp.jsonschema.loading.keyword.BooleanKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.ItemsKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.JsonArrayKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.JsonValueKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.ListKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.MapKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.NumberKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.DependenciesKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.SingleSchemaKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.StringKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.StringSetKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.TypeKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.URIKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.versions.flex.AdditionalItemsBooleanKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.versions.flex.AdditionalItemsKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.versions.flex.AdditionalPropertiesBooleanKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.versions.flex.AdditionalPropertiesKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.versions.flex.LimitBooleanKeywordDigester;
import io.sbsp.jsonschema.loading.keyword.versions.flex.LimitKeywordDigester;

import javax.json.JsonValue;
import java.util.List;

import static io.sbsp.jsonschema.keyword.Keywords.*;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.FALSE;
import static javax.json.JsonValue.ValueType.NULL;
import static javax.json.JsonValue.ValueType.NUMBER;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.STRING;
import static javax.json.JsonValue.ValueType.TRUE;

public interface KeywordDigesters {
    static List<KeywordDigester<?>> defaultKeywordLoaders() {
        return Lists.newArrayList(
                new URIKeywordDigester($SCHEMA),
                new URIKeywordDigester($REF),
                new URIKeywordDigester($ID),
                new StringKeywordDigester(TITLE),
                new StringKeywordDigester(DESCRIPTION),
                new MapKeywordDigester(DEFINITIONS),
                new JsonValueKeywordDigester(DEFAULT, ARRAY, OBJECT, STRING, FALSE, TRUE, NUMBER, NULL, TRUE, FALSE),
                new MapKeywordDigester(PROPERTIES),
                new NumberKeywordDigester(MAX_PROPERTIES),
                new StringSetKeywordDigester(REQUIRED),
                new NumberKeywordDigester(MIN_PROPERTIES),
                new DependenciesKeywordDigester(),
                new MapKeywordDigester(PATTERN_PROPERTIES),
                new SingleSchemaKeywordDigester(PROPERTY_NAMES),
                new TypeKeywordDigester(),
                new NumberKeywordDigester(MULTIPLE_OF),
                LimitBooleanKeywordDigester.maximumExtractor(),
                LimitBooleanKeywordDigester.minimumExtractor(),
                LimitKeywordDigester.minimumExtractor(),
                LimitKeywordDigester.maximumExtractor(),
                new AdditionalPropertiesBooleanKeywordDigester(),
                new AdditionalPropertiesKeywordDigester(),
                new StringKeywordDigester(FORMAT),
                new NumberKeywordDigester(MAX_LENGTH),
                new NumberKeywordDigester(MIN_LENGTH),
                new StringKeywordDigester(PATTERN),
                new ItemsKeywordDigester(),
                new AdditionalItemsBooleanKeywordDigester(),
                new AdditionalItemsKeywordDigester(),
                new NumberKeywordDigester(MAX_ITEMS),
                new NumberKeywordDigester(MIN_ITEMS),
                new BooleanKeywordDigester(UNIQUE_ITEMS),
                new SingleSchemaKeywordDigester(CONTAINS),
                new JsonArrayKeywordDigester(ENUM),
                new JsonArrayKeywordDigester(EXAMPLES),
                new JsonValueKeywordDigester(CONST, ARRAY, OBJECT, STRING, FALSE, TRUE, NUMBER, NULL, TRUE, FALSE),
                new SingleSchemaKeywordDigester(NOT),
                new ListKeywordDigester(ALL_OF),
                new ListKeywordDigester(ANY_OF),
                new ListKeywordDigester(ONE_OF));
    }
}
