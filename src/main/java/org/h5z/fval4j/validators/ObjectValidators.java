package org.h5z.fval4j.validators;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.h5z.fval4j.Core.Validator;
import org.h5z.fval4j.data.ValidationResult;

import static org.h5z.fval4j.data.ValidationResult.invalid;

public final class ObjectValidators {
    
    private ObjectValidators() {
        throw new IllegalAccessError("Cannot be instanciated");
    }

    public static <T, U, E> Validator<Object, U, E> a(Class<?> clazz,
                                                      Validator<T, U, E> validator,
                                                      Function<Object, E> errorFn) {
        return o -> {
            if (clazz.isAssignableFrom(o.getClass())) {
                ValidationResult<E, T, U> apply = validator.apply((T) o);
                return new ValidationResult<E,Object,U>(apply._1(), o, apply._3());
            }
            return invalid(o, errorFn.apply(o));
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
    public static <E> Validator<Object, String, E> string(Validator<String, String, E> validator,
                                                  Function<Object, E> errorFn) {
        return a(String.class, validator, errorFn);
    }

    public static <E> Validator<Object, String, E> string(Validator<String, String, E> validator,
                                                  Supplier<E> lazyE) {
        return a(String.class, validator, _v -> lazyE.get());
    }

    public static <E> Validator<Object, Integer, E> integer(Validator<Integer, Integer, E> validator,
                                                   Function<Object, E> errorFn) {
        return a(Integer.class, validator, errorFn);
    }

    public static <E> Validator<Object, Integer, E> integer(Validator<Integer, Integer, E> validator,
                                                   Supplier<E> lazyE) {
        return a(Integer.class, validator, _v -> lazyE.get());
    }

    public static <K, T, U, E> Validator<Object, Map<K, U>, E> map(Validator<Map<K, T>, Map<K, U>, E> validator,
                                               Function<Object, E> errorFn) {
        return a(Map.class, validator, errorFn);
    }

    public static <K, T, U, E> Validator<Object, Map<K, U>, E> map(Validator<Map<K, T>, Map<K, U>, E> validator,
                                               Supplier<E> lazyE) {
        return a(Map.class, validator, _v -> lazyE.get());
    }

    public static <K, T, U, E> Validator<Object, U, E> mapXform(Validator<Map<K, T>, U, E> validator,
                                                    Function<Object, E> errorFn) {
        return a(Map.class, validator, errorFn);
    }

    public static <K, T, U, E> Validator<Object, U, E> mapXform(Validator<Map<K, T>, U, E> validator,
                                               Supplier<E> lazyE) {
        return a(Map.class, validator, _v -> lazyE.get());
    }

}
