package io.sbsp.jsonschema.extractor;

import com.google.common.collect.Lists;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.extractor.impl.SchemaExtractorImpl;
import io.sbsp.jsonschema.extractor.keyword.BooleanKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.ItemsKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.JsonArrayKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.JsonValueKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.NumberKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.PropertyDependencyKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.SchemaKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.SchemaListKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.SchemaMapKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.SingleSchemaKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.StringKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.StringSetKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.TypeKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.URIKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.versions.draft4.Draft4MaximumKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.versions.draft4.Draft4MinimumKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.versions.draft6.Draft6MaximumKeywordExtractor;
import io.sbsp.jsonschema.extractor.keyword.versions.draft6.Draft6MinimumKeywordExtractor;
import io.sbsp.jsonschema.keyword.SchemaKeyword;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface SchemaExtractors {
    static SchemaExtractor flexible() {
        final List<SchemaKeywordExtractor> extractors = Lists.newArrayList(
                new URIKeywordExtractor(SchemaKeyword.$schema),
                new URIKeywordExtractor(SchemaKeyword.$ref),
                new URIKeywordExtractor(SchemaKeyword.$id),
                new URIKeywordExtractor(SchemaKeyword.id),
                new StringKeywordExtractor(SchemaKeyword.title),
                new SchemaMapKeywordExtractor(SchemaKeyword.definitions),
                new JsonValueKeywordExtractor(SchemaKeyword.$default),
                new SchemaMapKeywordExtractor(SchemaKeyword.properties),
                new NumberKeywordExtractor(SchemaKeyword.maxProperties),
                new StringSetKeywordExtractor(SchemaKeyword.required),
                new SingleSchemaKeywordExtractor(SchemaKeyword.additionalProperties),
                new NumberKeywordExtractor(SchemaKeyword.minProperties),
                new PropertyDependencyKeywordExtractor(),
                new SchemaMapKeywordExtractor(SchemaKeyword.patternProperties),
                new SingleSchemaKeywordExtractor(SchemaKeyword.propertyNames),
                new TypeKeywordExtractor(),
                new NumberKeywordExtractor(SchemaKeyword.multipleOf),
                new Draft4MaximumKeywordExtractor(),
                new Draft4MinimumKeywordExtractor(),
                new Draft6MaximumKeywordExtractor(),
                new Draft6MinimumKeywordExtractor(),
                new StringKeywordExtractor(SchemaKeyword.format),
                new NumberKeywordExtractor(SchemaKeyword.maxLength),
                new NumberKeywordExtractor(SchemaKeyword.minLength),
                new StringKeywordExtractor(SchemaKeyword.pattern),
                new ItemsKeywordExtractor(forVersion),
                new SingleSchemaKeywordExtractor(SchemaKeyword.additionalItems),
                new NumberKeywordExtractor(SchemaKeyword.maxItems),
                new NumberKeywordExtractor(SchemaKeyword.minItems),
                new BooleanKeywordExtractor(SchemaKeyword.uniqueItems),
                new SingleSchemaKeywordExtractor(SchemaKeyword.contains),
                new JsonArrayKeywordExtractor(SchemaKeyword.$enum),
                new JsonArrayKeywordExtractor(SchemaKeyword.examples),
                new JsonValueKeywordExtractor(SchemaKeyword.$const),
                new SingleSchemaKeywordExtractor(SchemaKeyword.not),
                new SchemaListKeywordExtractor(SchemaKeyword.allOf),
                new SchemaListKeywordExtractor(SchemaKeyword.anyOf),
                new SchemaListKeywordExtractor(SchemaKeyword.oneOf));

        return new SchemaExtractorImpl(extractors);
    }

    static SchemaExtractor strict(JsonSchemaVersion version) {
        final List<SchemaKeywordExtractor> extractors =
                Stream.of(
                        new URIKeywordExtractor(SchemaKeyword.$schema),
                        new URIKeywordExtractor(SchemaKeyword.$ref),
                        new URIKeywordExtractor(SchemaKeyword.$id),
                        new URIKeywordExtractor(SchemaKeyword.id),
                        new StringKeywordExtractor(SchemaKeyword.title),
                        new SchemaMapKeywordExtractor(SchemaKeyword.definitions),
                        new JsonValueKeywordExtractor(SchemaKeyword.$default),
                        new SchemaMapKeywordExtractor(SchemaKeyword.properties),
                        new NumberKeywordExtractor(SchemaKeyword.maxProperties),
                        new StringSetKeywordExtractor(SchemaKeyword.required),
                        new SingleSchemaKeywordExtractor(SchemaKeyword.additionalProperties),
                        new NumberKeywordExtractor(SchemaKeyword.minProperties),
                        new PropertyDependencyKeywordExtractor(),
                        new SchemaMapKeywordExtractor(SchemaKeyword.patternProperties),
                        new SingleSchemaKeywordExtractor(SchemaKeyword.propertyNames),
                        new TypeKeywordExtractor(),
                        new NumberKeywordExtractor(SchemaKeyword.multipleOf),
                        new Draft4MaximumKeywordExtractor(),
                        new ItemsKeywordExtractor(forVersion),
                        new Draft4MinimumKeywordExtractor(),
                        new Draft6MaximumKeywordExtractor(),
                        new Draft6MinimumKeywordExtractor(),
                        new StringKeywordExtractor(SchemaKeyword.format),
                        new NumberKeywordExtractor(SchemaKeyword.maxLength),
                        new NumberKeywordExtractor(SchemaKeyword.minLength),
                        new StringKeywordExtractor(SchemaKeyword.pattern),
                        new SingleSchemaKeywordExtractor(SchemaKeyword.additionalItems),
                        new NumberKeywordExtractor(SchemaKeyword.maxItems),
                        new NumberKeywordExtractor(SchemaKeyword.minItems),
                        new BooleanKeywordExtractor(SchemaKeyword.uniqueItems),
                        new SingleSchemaKeywordExtractor(SchemaKeyword.contains),
                        new JsonArrayKeywordExtractor(SchemaKeyword.$enum),
                        new JsonArrayKeywordExtractor(SchemaKeyword.examples),
                        new JsonValueKeywordExtractor(SchemaKeyword.$const),
                        new SingleSchemaKeywordExtractor(SchemaKeyword.not),
                        new SchemaListKeywordExtractor(SchemaKeyword.allOf),
                        new SchemaListKeywordExtractor(SchemaKeyword.anyOf),
                        new SchemaListKeywordExtractor(SchemaKeyword.oneOf))
                        .filter(k -> k.getKeyword()
                                .getAppliesToVersions()
                                .contains(version))
                        .collect(Collectors.toList());

        return new SchemaExtractorImpl(extractors);
    }
}
