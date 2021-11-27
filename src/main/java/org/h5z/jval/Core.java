package org.h5z.jval;

import fj.data.Validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Core {

    /** The class cannot be instanciated */
    private Core() {}

    /**
     * A validator is simply a function take a value and returning a list of errors.
     * If the resulting list contains at least one element the validator failed to validate the value.
     * Otherwise the value passed the validator and is valid.
     */
    @FunctionalInterface
    public interface Validator<T, E> extends Function<T, List<E>> {

        @Override
        List<E> apply(T t);

        /**
         * Alias for {@link Validator#apply(Object)}
         */
        default List<E> validate(T t) { return this.apply(t); }

    }

    public static <E> boolean succeed(List<E> es) {
        return es.isEmpty();
    }

    public static <E> boolean failed(List<E> es) {
        return !succeed(es);
    }

    public static <E> List<E> fail(E e) {
        ArrayList<E> result = new ArrayList<>();
        result.add(e);
        return result;
    }

    public static <T, E> List<E> success(T v) {
        return new ArrayList<>();
    }

    // Combinator
    public static <T, E> Validator<T, E> not(Validator<T, E> v, Supplier<E> s) {
        return x -> {
            List<E> validated = v.apply(x);
            if (succeed(validated)) {
                return fail(s.get());
            }
            return success(x);
        };
    }

    // Combinator
    public static <T, E> Validator<T, E> sequentially(Validator<T, E>... validators) {
        return x -> {
            for (Validator<T, E> v : validators) {
                List<E> validated = v.apply(x);
                if (failed(validated)) {
                    return validated;
                }
            }
            return Collections.emptyList();
        };
    }

    /**
     * Creates a validator from other validatos.
     *
     * The resulting validator will pass if any of the given validators passes.
     *
     * All the validators are evaluated and if all fail the result is the collection of the errors returned by the validators.
     */
    // Combinator
    public static <T, E> Validator<T, E> any(Validator<T, E>... validators) {
        return x -> {
            Stream<List<E>> vs = Arrays.stream(validators)
                .map(v -> v.apply(x));

            Optional<List<E>> maybeOneValid = vs.filter(Core::succeed)
                .findFirst();

            if (!maybeOneValid.isPresent()) {
                List<E> collected = vs.reduce(new ArrayList<>(), (acc, b) -> {
                        acc.addAll(b);
                        return acc;
                    });
                return collected;
            }
            return success(x);

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
    // Combinator
    public static <T, E> Validator<T, E> every(Validator<T, E> ... validators) {
        return x -> Arrays.stream(validators)
            .map(v -> v.apply(x))
            .reduce(new ArrayList<>(),
                    (acc, ls) -> {
                        acc.addAll(ls);
                        return acc;
                    });
    }

    /**
     * Apply the validator to a list of elements
     *
     * @param validator
     * @param <V>
     * @param <T>
     * @param <E>
     * @return
     */
    public static <V, T extends List<V>, E> Validator<T, List<E>> list(Validator<V, E> validator) {
        return xs -> xs.stream()
            .map(x -> validator.apply(x))
            .collect(Collectors.toList());
    }

    public static <O, T, E> Validator<O, E> prop(Function<O, T> property, Validator<T, E> validator) {
        return obj -> validator.apply(property.apply(obj));
    }

    // Combinator
    public static <T, E> Validator<T, E> required(Validator<T, E> validator, Supplier<E> supplier) {
        return v -> {
            if (null != v) {
                return validator.apply(v);
            }
            return fail(supplier.get());
        };
    }

    // Combinator
    public static <T, E> Validator<T, E> optional(Validator<T, E> validator) {
        return v -> {
            if (null == v) {
                return success(v);
            }
            return validator.apply(v);
        };
    }

}
