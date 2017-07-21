package io.dugnutt.jsonschema.six;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class StreamUtils {
    public static <t> Collector<t, List<t>, List<t>> toImmutableList() {
        return Collector.of(ArrayList::new, List::add, (left, right) -> {
            left.addAll(right);
            return left;
        }, Collections::unmodifiableList);
    }

    public static <X> Stream<X> safeStream(Collection<X> input) {
        if (input == null) {
            return Stream.empty();
        } else {
            return input.stream();
        }
    }
}
