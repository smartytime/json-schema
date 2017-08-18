package io.sbsp.jsonschema.loading.keyword;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaBuilder;
import io.sbsp.jsonschema.builder.DependenciesKeywordBuilder;
import io.sbsp.jsonschema.keyword.DependenciesKeyword;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.loading.KeywordDigest;
import io.sbsp.jsonschema.loading.KeywordDigester;
import io.sbsp.jsonschema.loading.LoadingReport;
import io.sbsp.jsonschema.loading.SchemaLoader;
import lombok.Getter;

import javax.json.JsonString;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.sbsp.jsonschema.loading.LoadingIssues.typeMismatch;

@Getter
public class DependenciesKeywordDigester implements KeywordDigester<DependenciesKeyword> {

    @Override
    public List<KeywordInfo<DependenciesKeyword>> getIncludedKeywords() {
        return Collections.singletonList(Keywords.DEPENDENCIES);
    }

    @Override
    public Optional<KeywordDigest<DependenciesKeyword>> extractKeyword(JsonValueWithPath jsonObject, SchemaBuilder builder, SchemaLoader schemaLoader, LoadingReport report) {

        final JsonValueWithPath dependencies = jsonObject.path(Keywords.DEPENDENCIES);
        DependenciesKeywordBuilder depsBuilder = new DependenciesKeywordBuilder();

        dependencies.forEachKey((key, pathValue) -> {
            switch (pathValue.getValueType()) {
                    case OBJECT:
                        final Schema dependencySchema = schemaLoader.loadSubSchema(pathValue, pathValue.getRoot(), report);
                        depsBuilder.addDependencySchema(key, dependencySchema);
                        break;
                    case ARRAY:
                        pathValue.asJsonArray()
                                .getValuesAs(JsonString::getString)
                                .forEach(arrayItem -> depsBuilder.propertyDependency(key, arrayItem));
                        break;
                    default:
                        report.error(typeMismatch(Keywords.DEPENDENCIES, pathValue, pathValue.getLocation()));
            }
        });

        return KeywordDigest.ofOptional(Keywords.DEPENDENCIES, depsBuilder.build());
    }
}
