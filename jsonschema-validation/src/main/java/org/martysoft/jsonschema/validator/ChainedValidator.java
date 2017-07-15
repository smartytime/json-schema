package org.martysoft.jsonschema.validator;

import com.google.common.base.Preconditions;

import javax.json.JsonValue;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class ChainedValidator<X extends JsonValue> {

    private final ValidationError validationError;
    private final boolean shouldContinue;
    private final X x;

    private ChainedValidator(X x, ValidationError validationError, boolean shouldContinue, Function<X, Optional<ValidationError>> nextCheck) {
        Preconditions.checkNotNull(nextCheck, "nextCheck must not be null");
        Preconditions.checkNotNull(x, "x must not be null");
        if (validationError == null && shouldContinue) {
            this.validationError = nextCheck.apply(x).orElse(null);
        } else {
            this.validationError = validationError;
        }
        this.shouldContinue = true;
        this.x = x;
    }

    private ChainedValidator(X x, ValidationError validationError, boolean shouldContinue,  Predicate<X> shouldContinueIf) {
        Preconditions.checkNotNull(shouldContinueIf, "nextCheck must not be null");
        Preconditions.checkNotNull(x, "x must not be null");
        if (validationError == null && shouldContinue) {
            this.shouldContinue = shouldContinueIf.test(x);
        } else {
            this.shouldContinue = shouldContinue;
        }
        this.validationError = validationError;
        this.x = x;
    }

    public static <V extends JsonValue> ChainedValidator<V> firstCheck(V v, Function<V, Optional<ValidationError>> check) {
        return new ChainedValidator<V>(v, null, true, check);
    }

    public Optional<ValidationError> getError() {
        return Optional.ofNullable(validationError);
    }

    public <Y extends JsonValue> ChainedValidator<Y> thenCheckAs(Class<Y> clazz, Function<Y, Optional<ValidationError>> nextCheck) {
        return new ChainedValidator<Y>((Y) x, validationError, shouldContinue, nextCheck);
    }

    public ChainedValidator<X> thenCheck(Function<X, Optional<ValidationError>> nextCheck) {
        return new ChainedValidator<X>(x, validationError, shouldContinue, nextCheck);
    }

    public ChainedValidator<X> thenIf(Predicate<X> shouldContinueIf) {
        return new ChainedValidator<X>(x, validationError, shouldContinue, shouldContinueIf);
    }


}
