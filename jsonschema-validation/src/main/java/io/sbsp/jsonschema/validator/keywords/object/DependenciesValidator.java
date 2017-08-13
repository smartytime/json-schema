package io.sbsp.jsonschema.validator.keywords.object;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import io.sbsp.jsonschema.JsonValueWithLocation;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.keyword.DependenciesKeyword;
import io.sbsp.jsonschema.keyword.Keywords;
import io.sbsp.jsonschema.keyword.SchemaKeyword;
import io.sbsp.jsonschema.validator.SchemaValidator;
import io.sbsp.jsonschema.validator.SchemaValidatorFactory;
import io.sbsp.jsonschema.validator.ValidationReport;
import io.sbsp.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.sbsp.jsonschema.enums.JsonSchemaKeywordType.DEPENDENCIES;
import static io.sbsp.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class DependenciesValidator extends KeywordValidator<DependenciesKeyword> {

    @NonNull
    private final Map<String, SchemaValidator> dependencyValidators;

    @NonNull
    private final SetMultimap<String, String> propertyDependencies;

    @Builder
    public DependenciesValidator(DependenciesKeyword keyword, Schema schema, SchemaValidatorFactory factory) {
        super(Keywords.dependencies, schema);
        Map<String, SchemaValidator> tmp = new HashMap<>();
        keyword.getDependencySchemas().getSchemas()
                .forEach((key, depSchema) -> tmp.put(key, factory.createValidator(depSchema)));
        this.dependencyValidators = Collections.unmodifiableMap(tmp);
        this.propertyDependencies = ImmutableSetMultimap.copyOf(keyword.getPropertyDependencies());
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
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
                report.addError(buildKeywordFailure(subject, schema, DEPENDENCIES)
                        .message("property [%s] is required because [%s] was present", thenThisMustAlsoExist, ifThisPropertyExists)
                        .build());
            }
        }
        return report.isValid();
    }
}
