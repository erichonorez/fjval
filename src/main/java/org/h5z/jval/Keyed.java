package org.h5z.jval;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.h5z.jval.Core.Validator;

public final class Keyed {

    private Keyed() {}

    @FunctionalInterface
    public interface KeyedValidator<K, T, E> extends Function<T, Map<K, List<E>>> {

        @Override
        Map<K, List<E>> apply(T t);

    }

    public static <K, T, E> KeyedValidator<K, T, E> keyed(K key, Validator<T, E> validator) {
        return o -> new HashMap<K, List<E>>() {
            {
                put(key, validator.apply(o));
            }
        };
    }

    public static <T, E> KeyedValidator<String, T, E> keyed(String key, KeyedValidator<String, T, E> validator) {
        return o -> validator.apply(o).entrySet()
            .stream()
            .map(kv -> new AbstractMap.SimpleEntry<>(key + "." + kv.getKey(), kv.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <K, E> boolean failed(Map<K, List<E>> validated) {
        for (K key : validated.keySet()) {
            List<E> es = validated.get(key);
            if (Core.failed(es)) {
                return true;
            }
        }
        return false;
    }

    public static <K, E> boolean succeed(Map<K, List<E>> validated) {
        return !failed(validated);
    }

    public static <K, E> boolean failed(Map<K, List<E>> validated, K k) {
        if (!validated.containsKey(k)) {
            return false;
        }

        return Core.failed(validated.get(k));
    }

    public static <K, E> Map<K, List<E>> failures(Map<K, List<E>> validated) {
        return validated.entrySet()
            .stream()
            .filter(e -> Core.failed(e.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <K, E> List<E> failures(Map<K, List<E>> validated, K k) {
        return validated.entrySet()
            .stream()
            .filter(e -> k.equals(e.getKey()) && Core.failed(e.getValue()))
            .map(Map.Entry::getValue)
            .reduce(new ArrayList<>(), (acc, b) -> {
                acc.addAll(b);
                return acc;
            });
    }

    public static <K, T, E> KeyedValidator<K, T, E> sequentially(KeyedValidator<K, T, E>... validators) {
        return x -> {
            for (KeyedValidator<K, T, E> v : validators) {
                Map<K, List<E>> validated = v.apply(x);
                if (failed(validated)) {
                    return validated;
                }
            }
            return new HashMap<>();
        };
    }

    /**
     * Creates a validator from other validatos. The resulting validator will pass
     * if all the given validators passes.
     *
     * All of the validators are evaluated even if some of the fail.
     *
     * The errors returned by each validator are collected and returned.
     */
    public static <K, T, E> KeyedValidator<K, T, E> every(KeyedValidator<K, T, E> ... validators) {
        return x -> Arrays.stream(validators)
            .map(v -> v.apply(x))
            .reduce(new HashMap<>(),
                    (acc, ls) -> {
                        acc.putAll(ls);
                        return acc;
                    });
    }

    public static <K, V, T extends Map<K, V>, E> KeyedValidator<K, T, E> key(K key, Validator<V, E> validator) {
        return map -> new HashMap<K, List<E>>() {{
            put(key, validator.apply(map.get(key)));
        }};
    }

    public static <K, O, T, E> KeyedValidator<K, O, E> prop(Function<O, T> property, KeyedValidator<K, T, E> validator) {
        return x -> validator.apply(property.apply(x));
    }

    // write val + keyed validators to validated map of objects

}
