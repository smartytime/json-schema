package io.sbsp.jsonschema.utils;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

@Value
@AllArgsConstructor
public class Pair<A,B> {
    private final A a;
    private final B b;

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

}
