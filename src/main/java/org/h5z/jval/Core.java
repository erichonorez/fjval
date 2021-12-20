package org.h5z.jval;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class Core {

    /**
     * The class cannot be instanciated
     */
    private Core() {
    }

    /**
     * A validator is a function accepting a value of type `T` and returning a list of errors of type `E`.
     * <p>
     * If the validated value is invalid then the validator returns a non-empty list of errors. Otherwise it returns an
     * empty list.
     * <p>
     * The validator must be a total function. It must return a value for any possible value of `T` and `null`.
     */
    @FunctionalInterface
    public interface Validator<T, E> extends Function<T, List<E>> {

        @Override
        List<E> apply(T t);

        /**
         * Alias for {@link Validator#apply(Object)}
         */
        default List<E> validate(T t) {
            return this.apply(t);
        }

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
     * @return <code>true</code> if the given list is empty and not <code>null</null>. It returns <code>false</false>
     * otherwise.
     */
    public static <E> boolean isValid(List<E> es) {
        return null != es && es.isEmpty();
    }

    /**
     * Checks if the validation result is invalid.
     *
     * @return <code>true</code> if the given list is not empty and not <code>null</null>. It returns <code>false</code>
     * otherwise.
     */
    public static <E> boolean isInvalid(List<E> es) {
        return null != es && !isValid(es);
    }

    /**
     * <b>Combinator</b> - Create a validator that will execute sequentially the given validators and return the errors
     * of the first failed validator. The execution of the validators stops at the first failed validators (fail-fast).
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
     * <b>Combinator</b> - Create a validator that will execute all the given validators and collect all the errors in a
     * single list.
     *
     * @return an empty list if all the validator succeeded. The errors of all failed validator otherwise.
     */
    public static <T, E> Validator<T, E> every(Validator<T, E>... validators) {
        return x -> Arrays.stream(validators)
            .map(v -> v.apply(x))
            .reduce(new ArrayList<>(),
                (acc, ls) -> {
                    acc.addAll(ls);
                    return acc;
                });
    }

    /**
     * <b>Combinator</b> - Creates a validator that will succeed if any on the given validators succeed.
     * All the validators are executed.
     *
     * @return an empty list if all the validator succeeded. The errors of all failed validator otherwise.
     */
    public static <T, E> Validator<T, E> any(Validator<T, E>... validators) {
        return x -> {
            List<List<E>> vs = Arrays.stream(validators)
                .map(v -> v.apply(x))
                .collect(Collectors.toList());

            Optional<List<E>> maybeOneValid = vs.stream().filter(Core::isValid)
                .findFirst();

            if (!maybeOneValid.isPresent()) {
                List<E> collected = vs.stream().reduce(new ArrayList<>(), (acc, b) -> {
                    acc.addAll(b);
                    return acc;
                });
                return collected;
            }
            return valid(x);

        };
    }

    /**
     * Creates a validator that will execute the given validator if the value to validate is not null. Returns the error
     * provided by the given supplier if the validated value is <code>null</code>.
     *
     * @return an empty list if the validated value is not null and pass the given validator. Returns the error provided
     * by the given supplier if the validated value is <code>null</code>. Returns the errors of the given validator if
     * the value is not <code>null</code> but is not valid.
     */
    public static <T, E> Validator<T, E> required(Validator<T, E> validator, Supplier<E> supplier) {
        return v -> {
            if (null != v) {
                return validator.apply(v);
            }
            return invalid(supplier.get());
        };
    }

    /**
     * Creates a validator that will execute the given validator if the value to validate is not null. Returns a valid
     * result if the validated value is <code>null</code>.
     *
     * @return an empty list if the validated value is null or pass the given validator. Returns the errors of the given
     * validator if the value is not <code>null</code> but is not valid.
     */
    public static <T, E> Validator<T, E> optional(Validator<T, E> validator) {
        return v -> {
            if (null == v) {
                return valid(v);
            }
            return validator.apply(v);
        };
    }

    /**
     * Creates a validator that will execute the given validator on all elements of a list. Returns an empty list if all
     * the elements of the validated list pass the given validator.
     *
     * @return Returns a empty list if all the elements of the validated list pass the given validator. Returns the
     * flattened collected errors otherwise.
     */
    public static <V, T extends List<V>, E> Validator<T, E> list(Validator<V, E> validator) {
        return xs -> xs.stream()
            .map(x -> validator.apply(x))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }

    /**
     * Creates a validator that will first get the value to validate by calling the given fn and then pass it to the
     * given validator.
     *
     * @return an empty list if the given validator succeeded. The errors return by the given validator otherwise.
     */
    public static <O, T, E> Validator<O, E> prop(Function<O, T> fn, Validator<T, E> validator) {
        return obj -> validator.apply(fn.apply(obj));
    }
    
    /**
     * Creates a validator for the mandatory value retrieved by calling fn.
     * 
     * If the value is absent (is equal to <code>null</code>) it returns the supplied error. Otherwise it execute 
     * the given validator and return its errors.
     * 
     * Example:
     * 
     * <pre>
     * required(MyDto::getX, someValidator, () -> new Error()))
     * </pre>
     * 
     * The code above is equivalent to :
     * 
     * <pre>
     * prop(MyDto::getX, required(someValidator, () -> new Error()))
     * </pre>
     * 
     * @param <O>   the type of object accepted by the given fn
     * @param <T>   the type of value being validated
     * @param <E>   type type of error returned by the validator
     * @param fn    the function extracting the value to validate from an instance of `O`
     * @param validator the validator to execute if the value to validate is not `null`
     * @param errorFn   the error to return in case of the value to validate is `null`
     * @return          If the value is absent (is equal to <code>null</code>) it returns the supplied error. 
     *                  Otherwise it execute the given validator and return its errors.
     */
    public static <O, T, E> Validator<O, E> required(Function<O, T> fn, Validator<T, E> validator, Supplier<E> errorFn) {
        return prop(fn, required(validator, errorFn));
    }

    /**
     * Creates a validator for the mandatory value retrieved by calling fn.
     * 
     * If the value is absent (is equal to <code>null</code>) it returns the supplied error. Otherwise it returns
     * a valid validation result.
     * 
     * Example:
     * 
     * <pre>
     * required(MyDto::getX, () -> new Error()))
     * </pre>
     * 
     * The code above is equivalent to :
     * 
     * <pre>
     * prop(MyDto::getX,  required(() -> new Error()))
     * </pre>
     * 
     * @param <O>   the type of object accepted by the given fn
     * @param <T>   the type of value being validated
     * @param <E>   type type of error returned by the validator
     * @param fn    the function extracting the value to validate from an instance of `O`
     * @param errorFn   the error to return in case of the value to validate is `null`
     * @return          If the value is absent (is equal to <code>null</code>) it returns the supplied error. 
     *                  Otherwise it returns a valid validation result.
     */
    public static <O, T, E> Validator<O, E> required(Function<O, T> fn, Supplier<E> errorFn) {
        return prop(fn, Validators.required(errorFn));
    }

    /**
     * Creates a validator for the optional value retrived by calling fn.
     * 
     * If the value is absent (is equal to <code>null</code>) it returns a valid validation result. 
     * Otherwise it execute the given validator and return its errors.
     * 
     * Example:
     * 
     * <pre>
     * optional(MyDto::getX, someValidator))
     * </pre>
     * 
     * The code above is equivalent to :
     * 
     * <pre>
     * prop(MyDto::getX, optional(someValidator))
     * </pre>
     * 
     * @param <O>   the type of object accepted by the given fn
     * @param <T>   the type of value being validated
     * @param <E>   type type of error returned by the validator
     * @param fn    the function extracting the value to validate from an instance of `O`
     * @param validator the validator to execute if the value to validate is not `null`
     * @return          If the value is absent (is equal to <code>null</code>) it returns a valid validation result. 
     *                  Otherwise it execute the given validator and return its errors.
     */
    public static <O, T, E> Validator<O, E> optional(Function<O, T> fn, Validator<T, E> validator) {
        return prop(fn, optional(validator));
    }

    /**
     * Creates a validator that will return the negation of the given validator. If the given validator returns a valid
     * result then this validator will return an invalid result with the error provided by the given supplier.
     *
     * @return an empty list if the given validator fails. A non-empty list containing the error provided by the
     * supplier otherwise.
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

}
