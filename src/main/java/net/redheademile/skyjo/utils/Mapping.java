package net.redheademile.skyjo.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Mapping {
    public static <T, U> List<T> map(List<U> l, Function<U, T> converter) {
        List<T> results = new ArrayList<>();
        for (U u : l)
            results.add(converter.apply(u));
        return results;
    }
}
