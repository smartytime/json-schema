package io.dugnutt.jsonschema.validator;

import java.util.ArrayList;
import java.util.List;

public class ValidationReport {
    List<ValidationError> errors = new ArrayList<>();

    public void addError(ValidationError validationError) {
        errors.add(validationError);
    }
}
