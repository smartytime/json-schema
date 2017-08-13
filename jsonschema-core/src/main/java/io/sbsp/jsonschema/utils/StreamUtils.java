package io.sbsp.jsonschema.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class StreamUtils {
    public static <t> Collector<t, List<t>, List<t>> toImmutableList() {
        return Collector.of(ArrayList::new, List::add, (left, right) -> {
            left.addAll(right);
            return left;
        }, Collections::unmodifiableList);
    }

    public static <F, T> List<T> safeTransform(Collection<F> input, Function<F, T> fn) {
        if (input != null) {
            return input.stream()
                    .map(fn)
                    .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    public static <X> X supplyIfNull(X nullable, Supplier<X> supplier) {
        if (nullable != null) {
            return nullable;
        }
        return supplier.get();
    }
}
