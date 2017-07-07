package org.everit.json;

import org.everit.jsonschema.api.JsonSchemaType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;

public class MultiplexingLoader<X> {

    private final JsonElement<X> element;

    private Set<JsonSchemaType> types = new LinkedHashSet<>();
    private List<Consumer<JsonElement<X>>> consumers = new ArrayList<>();
    private List<Function<JsonElement<X>, ?>> functions = new ArrayList<>();

    public MultiplexingLoader(JsonElement<X> element) {
        this.element = element;
    }

    public FluentOperation ifTypeIs(JsonSchemaType type) {
        checkNotNull(type, "type must not be null");
        types.add(type);
        return new FluentOperation(type == element.type());
    }

    public FluentOperation orIfTypeIs(JsonSchemaType type) {
        checkNotNull(type, "type must not be null");
        types.add(type);
        return new FluentOperation(type == element.type());
    }

    public <R> R executeAndExpect(Class<R> expectedOutput) {
        if (functions.isEmpty() && consumers.isEmpty()) {
            throw new MultiplexingFailure("No valid types were found.  Wanted one of %s but was %s",
                    types, element.type());
        } else if(!consumers.isEmpty()) {
            throw new MultiplexingFailure("You are requesting a return value but didn't provide any functions, only consumers");
        }

        Function<JsonElement<X>, ?> function = functions.stream().findFirst().get();
        return (R) function.apply(element);
    }

    public void execute() throws Exception {
        if (functions.isEmpty() && consumers.isEmpty()) {
            throw new MultiplexingFailure("No valid types were found.  Wanted one of %s but was %s",
                    types, element.type());
        }

        Consumer<JsonElement<X>> callable = consumers.stream().findFirst().orElse(null);
        if (callable != null) {
            callable.accept(element);
            return;
        } else {
            functions.stream().findFirst().get().apply(element);
        }
    }

    public class FluentOperation {

        private final boolean matches;

        FluentOperation(boolean matches) {
            this.matches = matches;
        }

        public MultiplexingLoader<X> then(Function<JsonElement<X>, ?> function) {
            if (matches) {
                functions.add(function);
            }
            return MultiplexingLoader.this;
        }

        public MultiplexingLoader<X> thenDo(Consumer<JsonElement<X>> function) {
            if (matches) {
                consumers.add(function);
            }
            return MultiplexingLoader.this;
        }
    }



}
