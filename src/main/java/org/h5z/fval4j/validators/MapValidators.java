package org.h5z.fval4j.validators;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.h5z.fval4j.Core.Validator;
import org.h5z.fval4j.data.ValidationResult;

import static org.h5z.fval4j.Core.*;
import static org.h5z.fval4j.data.ValidationResult.*;

public final class MapValidators {
    
    private MapValidators() {
        throw new IllegalAccessError("Cannot be instanciated");
    }

    /**
     * Validates the value with the specified key.
     * 
     * @param <K>
     * @param <T>
     * @param <U>
     * @param <E>
     * @param key
     * @param validator
     * @return
     */
    public static <K, T, U, E> Validator<Map<K, T>, U, E> key(K key, Validator<T, U, E> validator) {
        return kvs -> {
            ValidationResult<E, T, U> result = validator.apply(kvs.getOrDefault(key, null));
            return new ValidationResult<E,Map<K,T>,U>(
                result._1(),
                kvs,
                result._3());
        };
    }

    public static <K, T, E> Validator<Map<K, T>, Map<K, T>, E> hasKey(K key, Function<K, E> errorFn) {
        return kvs -> {
            if (kvs.containsKey(key)) {
                return valid(kvs, kvs);
            }
            return invalid(kvs, errorFn.apply(key));
        }; 
    }

    public static <K, T, E> Validator<Map<K, T>, Map<K, T>, E> hasKey(K key, Supplier<E> lazyE) {
        return hasKey(key, _k -> lazyE.get());
    }

    public static <K, T, U, E> Validator<Map<K, T>, U, E> value(
        K key, Validator<T, U, E> validator, Function<K, String> keyFn) {
        return key(key, keyed(keyFn.apply(key), validator));
    }

    public static <K, T, U, E> Validator<Map<K, T>, U, E> kv(K key, Validator<T, U, E> validator) {
        return value(key, validator, Object::toString);
    }
}
