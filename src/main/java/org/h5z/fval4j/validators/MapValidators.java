package org.h5z.fval4j.validators;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.h5z.fval4j.Core.Validator;

import static org.h5z.fval4j.Core.*;
import static org.h5z.fval4j.Trie.*;

public final class MapValidators {
    
    private MapValidators() {
        throw new IllegalAccessError("Cannot be instanciated");
    }

    public static <K, T, E> Validator<Map<K, T>, E> key(K key, Validator<T, E> validator) {
        return kvs -> validator.apply(kvs.getOrDefault(key, null));
    }

    public static <K, T, E> Validator<Map<K, T>, E> hasKey(K key, Function<K, E> errorFn) {
        return kvs -> {
            if (kvs.containsKey(key)) {
                return valid(kvs);
            }
            return invalid(errorFn.apply(key));
        }; 
    }

    public static <K, T, E> Validator<Map<K, T>, E> hasKey(K key, Supplier<E> lazyE) {
        return hasKey(key, _k -> lazyE.get());
    }

    public static <K, T, E> Validator<Map<K, T>, E> value(
        K key, Validator<T, E> validator, Function<K, String> keyFn) {
        return key(key, keyed(keyFn.apply(key), validator));
    }

    public static <K, T, E> Validator<Map<K, T>, E> kv(K key, Validator<T, E> validator) {
        return value(key, validator, Object::toString);
    }
}
