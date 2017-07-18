package io.dugnutt.jsonschema.validator;

import com.google.common.base.Preconditions;

import javax.json.JsonValue;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ChainedValidator<X extends JsonValue> {

    private final ValidationError validationError;
    private final boolean shouldContinue;
    private final X input;

    private ChainedValidator(X input, ValidationError validationError, boolean shouldContinue, Function<X, Optional<ValidationError>> nextCheck) {
        Preconditions.checkNotNull(nextCheck, "nextCheck must not be null");
        Preconditions.checkNotNull(input, "input must not be null");
        if (validationError == null && shouldContinue) {
            this.validationError = nextCheck.apply(input).orElse(null);
        } else {
            this.validationError = validationError;
        }
        this.shouldContinue = true;
        this.input = input;
    }

    private ChainedValidator(X input, ValidationError validationError, boolean shouldContinue, Predicate<X> shouldContinueIf) {
        Preconditions.checkNotNull(shouldContinueIf, "nextCheck must not be null");
        Preconditions.checkNotNull(input, "input must not be null");
        if (validationError == null && shouldContinue) {
            this.shouldContinue = shouldContinueIf.test(input);
        } else {
            this.shouldContinue = shouldContinue;
        }
        this.validationError = validationError;
        this.input = input;
    }

    public static <V extends JsonValue> ChainedValidator<V> firstCheck(V v, Function<V, Optional<ValidationError>> check) {
        return new ChainedValidator<V>(v, null, true, check);
    }

    public Optional<ValidationError> getError() {
        return Optional.ofNullable(validationError);
    }

    public <Y extends JsonValue> ChainedValidator<Y> thenCheckAs(Class<Y> clazz, Function<Y, Optional<ValidationError>> nextCheck) {
        return new ChainedValidator<Y>((Y) input, validationError, shouldContinue, nextCheck);
    }

    public ChainedValidator<X> thenCheck(Function<X, Optional<ValidationError>> nextCheck) {
        return new ChainedValidator<X>(input, validationError, shouldContinue, nextCheck);
    }

    public ChainedValidator<X> thenIf(Predicate<X> shouldContinueIf) {
        return new ChainedValidator<X>(input, validationError, shouldContinue, shouldContinueIf);
    }


}
