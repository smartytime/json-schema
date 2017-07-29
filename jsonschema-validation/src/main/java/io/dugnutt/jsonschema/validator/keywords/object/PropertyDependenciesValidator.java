package io.dugnutt.jsonschema.validator.keywords.object;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;
import io.dugnutt.jsonschema.validator.ValidationReport;
import io.dugnutt.jsonschema.validator.keywords.KeywordValidator;
import lombok.Builder;
import lombok.NonNull;

import java.util.Map;

import static io.dugnutt.jsonschema.six.JsonSchemaKeyword.DEPENDENCIES;
import static io.dugnutt.jsonschema.validator.ValidationErrorHelper.buildKeywordFailure;

public class PropertyDependenciesValidator extends KeywordValidator {

    @NonNull
    private final ImmutableSetMultimap<String, String> propertyDependencies;

    @Builder
    public PropertyDependenciesValidator(Schema schema, SetMultimap<String, String> propertyDependencies) {
        super(DEPENDENCIES, schema);
        this.propertyDependencies = ImmutableSetMultimap.copyOf(propertyDependencies);
    }

    @Override
    public boolean validate(JsonValueWithLocation subject, ValidationReport report) {
        boolean success = true;
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
