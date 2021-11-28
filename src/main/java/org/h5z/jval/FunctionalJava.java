package org.h5z.jval;

import fj.data.Validation;

import java.util.List;
import java.util.function.Function;

public final class FunctionalJava {

    private FunctionalJava() { }


    // Move it to a Monad variation of the validator
    public static <T, E> Function<T, Validation<List<E>, T>> monadic(Core.Validator<T, E> validator) {
        return t -> {
            List<E> result = validator.apply(t);
            if (Core.isValid(result)) {
                return Validation.success(t);
            }
            return Validation.fail(result);
        };
    }
}
