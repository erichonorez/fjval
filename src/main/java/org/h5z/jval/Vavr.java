package org.h5z.jval;

import io.vavr.control.Validation;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class Vavr {

    private Vavr() { }

    public static <T, E> Function<T, Validation<List<E>, T>> monadic(Core.Validator<T, E> validator) {
        return v -> {
            List<E> result = validator.apply(v);
            if (Core.succeed(result)) {
                return Validation.valid(v);
            }
            return Validation.invalid(result);
        };
    }


    public static <K, T, E> Function<T, Validation<Map<K, List<E>>, T>> monadic(Keyed.KeyedValidator<K, T, E> validator) {
        return v -> {
            Map<K, List<E>> result = validator.apply(v);
            if (Keyed.succeed(result)) {
                return Validation.valid(v);
            }
            return Validation.invalid(result);
        };
    }

}
