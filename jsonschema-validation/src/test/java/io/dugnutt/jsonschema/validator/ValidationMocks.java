package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonPath;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonValue;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidationMocks {

    public static SchemaValidator<Schema> alwaysSuccessfulValidator() {
        SchemaValidator mockedValidator = mock(SchemaValidator.class);
        Optional<ValidationError> empty = Optional.empty();
        when(mockedValidator.validate(any(PathAwareJsonValue.class))).thenReturn(empty);
        when(mockedValidator.validate(any(JsonValue.class))).thenReturn(empty);

        return mockedValidator;
    }

    public static PathAwareJsonValue vsubject(JsonValue subject) {
        return new PathAwareJsonValue(subject, JsonPath.rootPath());
    }

}
