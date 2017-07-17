package io.dugnutt.jsonschema.loader;

import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Fluent interface for attempting to produce a type of object base on an input.
 */
public class OptionalChainingLoader<INPUT, OUTPUT> {
    private final OUTPUT output;
    private final INPUT input;

    private OptionalChainingLoader(INPUT input, OUTPUT output) {
        checkNotNull(input, "input must not be null");
        this.output = output;
        this.input = input;
    }

    public static <I, O> OptionalChainingLoader<I, O> tryToCreate(I input, Function<I, O> createFn) {
        checkNotNull(createFn, "createFn must not be null");
        final O output = createFn.apply(input);
        return new OptionalChainingLoader<>(input, output);
    }

    public OptionalChainingLoader<INPUT, OUTPUT> orElseTry(Function<INPUT, OUTPUT> loader) {
        if (output == null) {
            return new OptionalChainingLoader<>(input, loader.apply(input));
        } else {
            return this;
        }
    }

    public Optional<OUTPUT> getResult() {
        return Optional.ofNullable(output);
    }

}
