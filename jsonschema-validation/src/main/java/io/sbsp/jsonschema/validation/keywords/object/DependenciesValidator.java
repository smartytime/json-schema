package io.sbsp.jsonschema.validation.keywords.object;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.DependenciesKeyword;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.validation.SchemaValidator;
import io.sbsp.jsonschema.validation.SchemaValidatorFactory;
import io.sbsp.jsonschema.validation.ValidationReport;
import io.sbsp.jsonschema.validation.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import java.util.Map;

public class DependenciesValidator extends KeywordValidator<DependenciesKeyword> {

    @NonNull
    private final Map<String, SchemaValidator> dependencyValidators;

    @NonNull
    private final SetMultimap<String, String> propertyDependencies;

    @Builder
    public DependenciesValidator(DependenciesKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.DEPENDENCIES, schema);
        this.dependencyValidators = keyword.getDependencySchemas().getSchemas()
                .entrySet().stream()
                .collect(ImmutableMap.toImmutableMap(
                        entry -> entry.getKey(),
                        entry -> factory.createValidator(entry.getValue()))
                );
        this.propertyDependencies = ImmutableSetMultimap.copyOf(keyword.getPropertyDependencies());
    }

    @Override
    public boolean validate(JsonValueWithPath subject, ValidationReport report) {
        for (Map.Entry<String, SchemaValidator> dependencyValidatorEntries : dependencyValidators.entrySet()) {
            String propName = dependencyValidatorEntries.getKey();
            SchemaValidator dependencyValidator = dependencyValidatorEntries.getValue();
            if (subject.containsKey(propName)) {
                dependencyValidator.validate(subject, report);
            }
        }

        for (Map.Entry<String, String> dependency : propertyDependencies.entries()) {
            String ifThisPropertyExists = dependency.getKey();
            String thenThisMustAlsoExist = dependency.getValue();
            if (subject.containsKey(ifThisPropertyExists) && !subject.containsKey(thenThisMustAlsoExist)) {
                report.addError(buildKeywordFailure(subject)
                        .message("property [%s] is required because [%s] was present", thenThisMustAlsoExist, ifThisPropertyExists)
                        .build());
            }
        }
        return report.isValid();
    }
}
