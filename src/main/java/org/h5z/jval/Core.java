package org.h5z.jval;

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
     * A validator is a function accepting a value of type `T` and returning a list of errors of type `E`.
     *
     * If the validated value is invalid then the validator returns a non-empty list of errors.
     * Otherwise it returns an empty list.
     *
     * The validator must be a total function. It must return a value for any possible value of `T` and `null`.
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

    /**
     * Creates a valid validation result for the given value.
     *
     * @return an empty list.
     */
    public static <T, E> List<E> valid(T v) {
        return new ArrayList<>();
    }

    /**
     * Creates an invalid validation result with the given error.
     *
     * @return a list with the given error.
     */
    public static <E> List<E> invalid(E e) {
        ArrayList<E> result = new ArrayList<>();
        result.add(e);
        return result;
    }

    /**
     * Checks if the validation result is valid.
     *
     * @return <code>true</code> if the given list is empty and not <code>null</null>. It returns <code>false</false> otherwise.
     */
    public static <E> boolean isValid(List<E> es) {
        return null != es && es.isEmpty();
    }

    /**
     * Checks if the validation result is invalid.
     *
     * @return <code>true</code> if the given list is not empty and not <code>null</null>. It returns <code>false</code> otherwise.
     */
    public static <E> boolean isInvalid(List<E> es) {
        return null != es && !isValid(es);
    }

    /**
     * <b>Combinator</b> - Create a validator that will execute sequentially the given validators and return the errors of the first failed validator. The execution of the validators stops at the first failed validators (fail-fast).
     *
     * @return an empty list if all the validator succeeded. The errors of the first failed validator otherwise.
     */
    public static <T, E> Validator<T, E> sequentially(Validator<T, E>... validators) {
        return x -> {
            for (Validator<T, E> v : validators) {
                List<E> validated = v.apply(x);
                if (isInvalid(validated)) {
                    return validated;
                }
            }
            return Collections.emptyList();
        };
    }

    /**
     * <b>Combinator</b> - Create a validator that will execute all the given validators and collect all the errors in a single list.
     *
     * @return an empty list if all the validator succeeded. The errors of all failed validator otherwise.
     */
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
     * 
     */
    public static <T, E> Validator<T, E> not(Validator<T, E> v, Supplier<E> s) {
        return x -> {
            List<E> validated = v.apply(x);
            if (isValid(validated)) {
                return invalid(s.get());
            }
            return valid(x);
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

            Optional<List<E>> maybeOneValid = vs.filter(Core::isValid)
                .findFirst();

            if (!maybeOneValid.isPresent()) {
                List<E> collected = vs.reduce(new ArrayList<>(), (acc, b) -> {
                        acc.addAll(b);
                        return acc;
                    });
                return collected;
            }
            return valid(x);

        };
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
            return invalid(supplier.get());
        };
    }

    // Combinator
    public static <T, E> Validator<T, E> optional(Validator<T, E> validator) {
        return v -> {
            if (null == v) {
                return valid(v);
            }
            return validator.apply(v);
        };
    }

}
