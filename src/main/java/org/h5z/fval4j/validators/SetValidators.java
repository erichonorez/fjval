package org.h5z.fval4j.validators;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.h5z.fval4j.Core.Validator;

import static org.h5z.fval4j.Core.*;

public final class SetValidators {

    private SetValidators() {
        throw new IllegalAccessError("Cannot be instantiaed");
    }

    public static <V, T extends Set<V>, E> Validator<T, E> set(
            Validator<V, E> validator,
            Function<V, String> keyFn,
            Function<List<Validator<V, E>>, Validator<T, E>> reducer) {
        return s -> {
            List<Validator<V, E>> validators = s.stream()
                .map(v -> keyed(keyFn.apply(v), validator))
                .toList();
            return reducer.apply(validators).apply(s);
        };
    }
    
}
