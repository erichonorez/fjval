package org.h5z.fval4j.validators;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.h5z.fval4j.Core.Validator;

import static org.h5z.fval4j.Trie.invalid;

public final class ObjectValidators {
    
    private ObjectValidators() {
        throw new IllegalAccessError("Cannot be instanciated");
    }

    public static <T, E> Validator<Object, E> a(Class<?> clazz,
                                           Validator<T, E> validator,
                                           Function<Object, E> errorFn) {
        return o -> {
            if (clazz.isAssignableFrom(o.getClass())) {
                return validator.apply((T) o);
            }
            return invalid(errorFn.apply(o));
        };
    }

    /**
     * Applies the given validator is the validated object is a string.
     * 
     * @param <T>
     * @param <E>
     * @param validator
     * @param errorFn
     * @return
     */
    public static <E> Validator<Object, E> string(Validator<String, E> validator,
                                                  Function<Object, E> errorFn) {
        return a(String.class, validator, errorFn);
    }

    public static <E> Validator<Object, E> string(Validator<String, E> validator,
                                                  Supplier<E> lazyE) {
        return a(String.class, validator, _v -> lazyE.get());
    }

    public static <E> Validator<Object, E> integer(Validator<Integer, E> validator,
                                                   Function<Object, E> errorFn) {
        return a(Integer.class, validator, errorFn);
    }

    public static <E> Validator<Object, E> integer(Validator<Integer, E> validator,
                                                   Supplier<E> lazyE) {
        return a(Integer.class, validator, _v -> lazyE.get());
    }

    public static <K, T, E> Validator<Object, E> map(Validator<Map<K, T>, E> validator,
                                               Function<Object, E> errorFn) {
        return a(Map.class, validator, errorFn);
    }

    public static <K, T, E> Validator<Object, E> map(Validator<Map<K, T>, E> validator,
                                               Supplier<E> lazyE) {
        return a(Map.class, validator, _v -> lazyE.get());
    }

}
