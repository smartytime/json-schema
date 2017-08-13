package io.sbsp.jsonschema.loading;

import com.google.common.collect.Lists;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.loading.impl.SchemaExtractorImpl;
import io.sbsp.jsonschema.loading.keyword.BooleanKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.ItemsKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.JsonArrayKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.JsonValueKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.NumberKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.PropertyDependencyKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.SchemaKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.SchemaListKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.SchemaMapKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.SingleSchemaKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.StringKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.StringSetKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.TypeKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.URIKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.versions.flex.AdditionalItemsKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.versions.flex.AdditionalPropertiesKeywordExtractor;
import io.sbsp.jsonschema.loading.keyword.versions.flex.LimitKeywordExtractor;
import io.sbsp.jsonschema.keyword.SchemaKeyword;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface SchemaExtractors {
    static SchemaExtractor flexible() {
        final List<SchemaKeywordExtractor> extractors = Lists.newArrayList(
                new URIKeywordExtractor(Keywords.$schema),
                new URIKeywordExtractor(Keywords.$ref),
                new URIKeywordExtractor(Keywords.$id),
                new URIKeywordExtractor(Keywords.id),
                new StringKeywordExtractor(Keywords.title),
                new StringKeywordExtractor(Keywords.description),
                new SchemaMapKeywordExtractor(Keywords.definitions),
                new JsonValueKeywordExtractor(Keywords.$default),
                new SchemaMapKeywordExtractor(Keywords.properties),
                new NumberKeywordExtractor(Keywords.maxProperties),
                new StringSetKeywordExtractor(Keywords.required),
                new NumberKeywordExtractor(Keywords.minProperties),
                new PropertyDependencyKeywordExtractor(),
                new SchemaMapKeywordExtractor(Keywords.patternProperties),
                new SingleSchemaKeywordExtractor(Keywords.propertyNames),
                new TypeKeywordExtractor(),
                new NumberKeywordExtractor(Keywords.multipleOf),
                LimitKeywordExtractor.flexibleMinimumExtractor(),
                LimitKeywordExtractor.flexibleMaximumExtractor(),
                AdditionalPropertiesKeywordExtractor.flexible(),
                AdditionalItemsKeywordExtractor.flexible(),
                new StringKeywordExtractor(Keywords.format),
                new NumberKeywordExtractor(Keywords.maxLength),
                new NumberKeywordExtractor(Keywords.minLength),
                new StringKeywordExtractor(Keywords.pattern),
                new ItemsKeywordExtractor(),
                new NumberKeywordExtractor(Keywords.maxItems),
                new NumberKeywordExtractor(Keywords.minItems),
                new BooleanKeywordExtractor(Keywords.uniqueItems),
                new SingleSchemaKeywordExtractor(Keywords.contains),
                new JsonArrayKeywordExtractor(Keywords.$enum),
                new JsonArrayKeywordExtractor(Keywords.examples),
                new JsonValueKeywordExtractor(Keywords.$const),
                new SingleSchemaKeywordExtractor(Keywords.not),
                new SchemaListKeywordExtractor(Keywords.allOf),
                new SchemaListKeywordExtractor(Keywords.anyOf),
                new SchemaListKeywordExtractor(Keywords.oneOf));

        return new SchemaExtractorImpl(extractors);
    }

    static SchemaExtractor strict(JsonSchemaVersion version) {
        final List<SchemaKeywordExtractor> extractors =
                Stream.of(
                        new URIKeywordExtractor(Keywords.$schema),
                        new URIKeywordExtractor(Keywords.$ref),
                        new URIKeywordExtractor(Keywords.$id),
                        new URIKeywordExtractor(Keywords.id),
                        new StringKeywordExtractor(Keywords.title),
                        new SchemaMapKeywordExtractor(Keywords.definitions),
                        new JsonValueKeywordExtractor(Keywords.$default),
                        new SchemaMapKeywordExtractor(Keywords.properties),
                        new NumberKeywordExtractor(Keywords.maxProperties),
                        new StringSetKeywordExtractor(Keywords.required),
                        new NumberKeywordExtractor(Keywords.minProperties),
                        new PropertyDependencyKeywordExtractor(),
                        new SchemaMapKeywordExtractor(Keywords.patternProperties),
                        new SingleSchemaKeywordExtractor(Keywords.propertyNames),
                        new TypeKeywordExtractor(),
                        new NumberKeywordExtractor(Keywords.multipleOf),
                        LimitKeywordExtractor.minimumExtractor(version),
                        LimitKeywordExtractor.maximumExtractor(version),
                        new StringKeywordExtractor(Keywords.format),
                        new NumberKeywordExtractor(Keywords.maxLength),
                        new NumberKeywordExtractor(Keywords.minLength),
                        new StringKeywordExtractor(Keywords.pattern),
                        new ItemsKeywordExtractor(),
                        new NumberKeywordExtractor(Keywords.maxItems),
                        new NumberKeywordExtractor(Keywords.minItems),
                        new BooleanKeywordExtractor(Keywords.uniqueItems),
                        new SingleSchemaKeywordExtractor(Keywords.contains),
                        new JsonArrayKeywordExtractor(Keywords.$enum),
                        new JsonArrayKeywordExtractor(Keywords.examples),
                        new JsonValueKeywordExtractor(Keywords.$const),
                        new SingleSchemaKeywordExtractor(Keywords.not),
                        new SchemaListKeywordExtractor(Keywords.allOf),
                        new SchemaListKeywordExtractor(Keywords.anyOf),
                        new SchemaListKeywordExtractor(Keywords.oneOf))
                        .filter(k -> k.appliesTo(version))
                        .collect(Collectors.toList());

        return new SchemaExtractorImpl(extractors);
    }
}
