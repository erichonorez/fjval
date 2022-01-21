package org.h5z.fval4j;

import static org.h5z.fval4j.Trie.*;
import static org.organicdesign.fp.StaticImports.map;
import static org.organicdesign.fp.StaticImports.tup;
import static org.organicdesign.fp.StaticImports.vec;
import static org.organicdesign.fp.StaticImports.xform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.h5z.fval4j.data.ValidationResult;
import org.h5z.fval4j.data.Prelude.Fn0;
import org.h5z.fval4j.data.Prelude.Fn1;
import org.h5z.fval4j.data.Prelude.Fn2;
import org.h5z.fval4j.data.Prelude.F3;
import org.h5z.fval4j.data.Prelude.Fn4;
import org.h5z.fval4j.data.Prelude.Fn5;
import org.organicdesign.fp.collections.ImList;

public final class Core {

    private Core() {
        throw new IllegalAccessError("Cannot be instanciated");
    }

    @FunctionalInterface
    public interface Validator<T, U, E> extends Fn1<T, ValidationResult<E, T, U>> {

        /**
         * Alias for {@link Function#apply(Object)}
         */
        default ValidationResult<E, T, U> validate(T t) {
            return this.apply(t);
        }

    }

    /**
     * Creates a {@link Validator} given a key and another {@link Validator}.
     * The given key will be used to access the result of the given valitors in the
     * resulting trie.
     * 
     * Example: If the result of given {@link Validator} is keyed on a key 'x'
     * and the given key is 'y' then the result of new validator will be accessible
     * with the key at `x -> y` in the resulting trie.
     * 
     * @param <T>       the type of values validated
     * @param <E>       the type of errors returned by the validator
     * @param key       the key to index the result of the validator
     * @param validator the validator to apply
     * 
     * @return
     */
    public static <T, U, E> Validator<T, U, E> keyed(String key, Validator<T, U, E> validator) {
        return v -> { 
            ValidationResult<E, T, U> apply = validator.apply(v);
            return new ValidationResult<E, T, U>(
                trie(vec(), map(tup(key, apply._1()))),
                apply._2(),
                apply._3()); 
        };
    }

    /**
     * Creates a {@link Validator} reporting the errors of the supplied {@link Validator} at the root of the trie.
     * 
     * 
     * @param <T>   the type of value being validated
     * @param <E>   type type of error returned by the validator
     * @param validator the validator to execute
     * @return          a keyed validator.
     */
    public static <T, U, E> Validator<T, U, E> globally(Validator<T, U, E> validator) { 
        return keyed(Trie.ROOT_KEY, validator);
    }

    public static <T, U, E> Validator<T, U, E> sequentially(List<Validator<T, U, E>> validators) {
        return v -> {
            return recurSequentially(v, xform(validators).toImList(), ValidationResult.identity());
        };
    }

    /**
     * <b>Combinator</b> - Creates a validator that will execute sequentially the
     * given validators and return the errors of the first failed validator. The
     * execution of the validators stops at the first failed validators (fail-fast).
     * 
     * @param <T>        the type of values validated
     * @param <E>        the type of errors returned by the validator
     * @param validators the sequence of validator to evaluate.
     * 
     * @return a valid trie if all validators succeed. A trie with only the errors
     *         of the first failed validator otherwise.
     */
    @SafeVarargs
    public static <T, U, E> Validator<T, U, E> sequentially(Validator<T, U, E>... validators) {
        return sequentially(Arrays.asList(validators));
    }

    private static <T, U, E> ValidationResult<E, T, U> recurSequentially(T value, ImList<Validator<T, U, E>> validators, ValidationResult<E, T, U> acc) {
        return validators.head()
                .match(v -> {
                    ValidationResult<E, T, U> validated = v.validate(value);
                    ValidationResult<E, T, U> newAcc = new ValidationResult<>(
                        acc._1().merge(validated._1()),
                        validated._2(),
                        validated._3());
                    
                    if (validated.isInvalid()) {
                        return newAcc;
                    }
                    return recurSequentially(value, validators.drop(1).toImList(), newAcc);
                }, () -> acc);

    }

    /**
     * Creates a validator from other validatos. The resulting validator will pass
     * if all the given validators passes.
     *
     * All of the validators are evaluated even if some of the fail.
     *
     * The errors returned by each validator are collected and returned.
     */

    /**
     * <b>Combinator</b> - Creates a validator that will execute all the
     * given validators and returns the aggregated results. This validator does not
     * stop at the first failed validator.
     * 
     * @param <T>        the type of values validated
     * @param <E>        the type of errors returned by the validator
     * @param validators the sequence of validator to evaluate
     * 
     * @return a valid trie if all validators succeeded. A trie with the errors of
     *         all failed validators otherwise.
     */
    @SafeVarargs
    public static <T, U, E> Validator<T, U, E> every(Validator<T, U, E>... validators) {
        return every(vec(validators));
    }

    /**
     * @see {@link Core#every(Validator...)}
     */
    public static <T, U, E> Validator<T, U, E> every(List<Validator<T, U, E>> validators) {
        return v -> xform(validators)
                .map(fn -> fn.apply(v))
                .fold(
                    ValidationResult.identity(),
                    (a, b) -> new ValidationResult<>(
                        a._1().merge(b._1()),
                        b._2(),
                        b._3()));
    }

    public static <T, U1, U2, E, X> Validator<T, X, E> every(Validator<T, U1, E> v0, Validator<T, U2, E> v1, Fn2<U1, U2, X> fn) {
        return t -> { 
            ValidationResult<E, T, U1> apply0 = v0.apply(t);
            ValidationResult<E, T, U2> apply1 = v1.apply(t);
            if (apply0.isValid() && apply1.isValid()) {
                return new ValidationResult<E, T, X>(
                    apply0._1().merge(apply1._1()),
                    t,
                    fn.apply(apply0._3(), apply1._3())
                );
            }
            return new ValidationResult<E, T, X>(
                apply0._1().merge(apply1._1()),
                t,
                null);
        };
    }

    public static <T, U1, U2, E> Validator<T, T, E> every(Validator<T, U1, E> v0, Validator<T, U2, E> v1) {
        return t -> every(v0, v1, (_a, _b) -> t).apply(t);
    }

    public static <T, U1, U2, U3, E, X> Validator<T, X, E> every(Validator<T, U1, E> v0, Validator<T, U2, E> v1, Validator<T, U3, E> v2, F3<U1, U2, U3, X> fn) {
        return t -> {
            ValidationResult<E, T, U1> apply0 = v0.apply(t);
            ValidationResult<E, T, U2> apply1 = v1.apply(t);
            ValidationResult<E, T, U3> apply2 = v2.apply(t);

            if (apply0.isValid() && apply1.isValid() && apply2.isValid()) {
                return new ValidationResult<E, T, X>(
                    apply0._1().merge(apply1._1()).merge(apply2._1()),
                    t,
                    fn.apply(apply0._3(), apply1._3(), apply2._3())
                );
            }
            return new ValidationResult<E, T, X>(apply0._1().merge(apply1._1()), t, null);
        };
    }

    public static <T, U1, U2, U3, U4, E, X> Validator<T, X, E> every(Validator<T, U1, E> v0, Validator<T, U2, E> v1, Validator<T, U3, E> v2, Validator<T, U4, E> v3, Fn4<U1, U2, U3, U4, X> fn) {
        return t -> { 
            ValidationResult<E, T, U1> apply0 = v0.apply(t);
            ValidationResult<E, T, U2> apply1 = v1.apply(t);
            ValidationResult<E, T, U3> apply2 = v2.apply(t);
            ValidationResult<E, T, U4> apply3 = v3.apply(t);

            if (apply0.isValid() && apply1.isValid() && apply2.isValid() && apply3.isValid()) {
                return new ValidationResult<E, T, X>(
                    apply0._1().merge(apply1._1()),
                    t,
                    fn.apply(apply0._3(), apply1._3(), apply2._3(), apply3._3())
                );
            }
            return new ValidationResult<E, T, X>(
                apply0._1().merge(apply1._1()).merge(apply2._1()).merge(apply3._1()),
                t,
                null);
        };
    }

    public static <T, U1, U2, U3, U4, U5, E, X> Validator<T, X, E> every(Validator<T, U1, E> v0, Validator<T, U2, E> v1, Validator<T, U3, E> v2, Validator<T, U4, E> v3, Validator<T, U5, E> v4, Fn5<U1, U2, U3, U4, U5, X> fn) {
        return t -> { 
            ValidationResult<E, T, U1> apply0 = v0.apply(t);
            ValidationResult<E, T, U2> apply1 = v1.apply(t);
            ValidationResult<E, T, U3> apply2 = v2.apply(t);
            ValidationResult<E, T, U4> apply3 = v3.apply(t);
            ValidationResult<E, T, U5> apply4 = v4.apply(t);

            if (apply0.isValid() && apply1.isValid() && apply2.isValid() && apply3.isValid() && apply4.isValid()) {
                return new ValidationResult<E, T, X>(
                    apply0._1().merge(apply1._1()),
                    t,
                    fn.apply(apply0._3(), apply1._3(), apply2._3(), apply3._3(), apply4._3())
                );
            }
            return new ValidationResult<E, T, X>(
                apply0._1().merge(apply1._1()).merge(apply2._1()).merge(apply3._1()).merge(apply4._1()),
                t,
                null);
        };
    }

    public static <T, U1, U2, U3, U4, U5, E> Validator<T, T, E> every(Validator<T, U1, E> v0, Validator<T, U2, E> v1, Validator<T, U3, E> v2, Validator<T, U4, E> v3, Validator<T, U5, E> v4) {
        return t -> every(v0, v1, v2, v3, v4, (_1, _2, _3, _4, _5) -> t).apply(t);
    }

    
    public static <T, U, E> Validator<T, U, E> any(List<Validator<T, U, E>> validators) {
        return v -> {
            if (validators.size() == 0) {
                return ValidationResult.identity();
            } else if (validators.size() == 1) {
                return validators.get(0).apply(v);
            } else {
                ValidationResult<E, T, U> result = validators.get(0).apply(v);
                if (result.isInvalid()) {
                    return result;
                } else {
                    return any(validators.subList(1, validators.size())).apply(v);
                }
            }
        };
    }

    @SafeVarargs
    public static <T, U, E> Validator<T, U, E> any(Validator<T, U, E>... validators) {
        return any(Arrays.asList(validators));
    }

    public static <T, U, E> Validator<T, U, E> not(Validator<T, U, E> validator, Function<T, E> errorFn) {
        return v -> {
            ValidationResult<E, T, U> result = validator.apply(v);
            return result.isValid() 
                ? ValidationResult.invalid(result._2(), errorFn.apply(v)) 
                : ValidationResult.<E, T, U> valid(result._3(), result._2());
        };
    }

    public static <T, U, E> Validator<T, U, E> not(Validator<T, U, E> validator, Supplier<E> lazyE) {
        return not(validator, _v -> lazyE.get());
    }

    /**
     * <b>Combinator</b> - Creates a validator that will first extract the value to
     * validate by calling the given fn on the provided value and then pass it to
     * the
     * given validator.
     * 
     * @param <O>       the type of object accepted by the given fn
     * @param <T>       the type of values validated
     * @param <E>       the type of errors returned by the validator
     * @param fn        a function returning an instance of type T given an instance
     *                  of type O
     * @param validator the validator to apply on the extracted value
     * 
     * @return a valid trie if the given validator succeeded. A trie containing the
     *         collected errors otherwise.
     */
    public static <O, T, U, E> Validator<O, U, E> prop(Function<O, T> fn, Validator<T, U, E> validator) {
        return x -> { 
            ValidationResult<E, T, U> result = validator.apply(fn.apply(x));
            return new ValidationResult<E,O,U>(result._1(), x, result._3());
        };
    }

    /**
     * <b>Combinator</b> - Creates a validator that will validate all the elements
     * of a list with the given validator. The validator will validates all the
     * elements of the list and return the collected errors.
     * It has the same behavior than {@link Core#every(Validator...)}
     * 
     * @see {@link Core#every(Validator...)}
     * 
     * @param <V>       the type of the values in the list to validate
     * @param <T>       the type of the list
     * @param <E>       the type of errors returned by the validator
     * @param validator the validator to apply to all elements of a list
     * @param reducer   a function reducing a List<KeyedValidator<T, U, E>> to a
     *                  KeyedValidator<T, U, E>
     * 
     * @return a valid trie if the given validator succeeded. A trie containing the
     *         collected errors otherwise.
     */
    public static <V, T extends List<V>, U, E> Validator<T, List<U>, E> list(
            Validator<V, U, E> validator,
            Fn1<List<Validator<T, U, E>>, Validator<T, List<U>, E>> reducer) {
        return t -> {
            List<Validator<T, U, E>> validators = IntStream
                    .range(0, t.size())
                    .mapToObj(i -> keyed(String.valueOf(i), (T xs) -> {
                        ValidationResult<E, V, U> result = validator.apply(xs.get(i));
                        return new ValidationResult<>(
                            result._1(),
                            xs,
                            result._3());
                    }))
                    .toList();
            return reducer.apply(validators).apply(t);
        };
    }

    public static <V, T extends List<V>, U, E> Validator<T, List<U>, E> everyEl(List<Validator<T, U, E>> validators) {
        return xs -> {
            return validators.stream()
                .map(v -> v.apply(xs))
                .map(r -> r.mapValue(v -> vec(v)))
                .reduce(new ValidationResult<>(Trie.identity(), xs, vec()), (a, b) -> {
                    return new ValidationResult<>(
                        a._1().merge(b._1()),
                        a._2(),
                        a._3().concat(b._3())
                    );
                }).mapValue(v -> new ArrayList<>(v));
        };
    }

    public static <T, U, V, E> Validator<T, U, E> mapValue(Validator<T, V, E> validator, Fn1<V, U> fn) {
        return t -> validator.apply(t).mapValue(fn);
    }

    public static <T, U, V, E> Validator<T, U, E> mapInput(Validator<T, V, E> validator, Fn1<T, U> fn) {
        return t -> validator.apply(t).mapValue(_v -> fn.apply(t));
    }

    /**
     * Creates a validator that will apply the given validator only if the validated
     * value is not null. If the validated value is null it returns a given error.
     * Otherwise it executes the validator and returns its result.
     * 
     * @param <T>       the type of values validated
     * @param <E>       the type of errors returned by the validator
     * @param validator the validator to apply if the validated value is not null
     * @param lazyE     the error to return in an trie if the validated value is
     *                  null
     * 
     * @return Returns an invalid trie with the given error if the validated value
     *         is null.
     *         Returns an invalid trie with the errors of the validators if the
     *         validated value is not null and invalid.
     *         Returns an valid trie if the validated value is not null and valid
     */
    public static <T, U, E> Validator<T, U, E> required(Validator<T, U, E> validator, Supplier<E> lazyE) {
        return x -> {
            if (null == x) {
                return new ValidationResult<>(trie(vec(lazyE.get()), map()), x, null);
            }
            return validator.apply(x);
        };
    }

    public static <O, T, U, E> Validator<O, U, E> required(Function<O, T> fn, Validator<T, U, E> validator, Supplier<E> lazyE) {
        return prop(fn, required(validator, lazyE));
    }

    /**
     * Creates a {@link Validator} by composing the given validator with 
     * {@link Core#required(Function, Supplier)}.
     * 
     * @param <O>       the type of object accepted by the given fn
     * @param <T>       the type of values validated
     * @param <E>       the type of errors returned by the validator
     * @param key       the key to index the result of the validator
     * @param fn        the function extracting the value to validate from an instance of `O`
     * @param validator the validator to apply on the extracted value
     * @param lazyE     the error to return if the value is <code>null</code>
     * @return          a {@link Validator} that returns the given error if the extracted value <code>T</code>
     *                  from <code>O</code> is null. Returns the result of the given validator otherwise.
     */
    public static <O, T, U, E> Validator<O, U, E> required(String key, 
                                                     Function<O, T> fn, 
                                                     Validator<T, U, E> validator, 
                                                     Supplier<E> lazyE) {
        return keyed(key, required(fn, validator, lazyE));
    }

    public static <O, T, U, E> Validator<O, U, E> optional(Function<O, T> fn, Validator<T, U, E> validator) {
        return prop(fn, optional(validator));
    }

    public static <O, T, U, E> Validator<O, U, E> optional(Function<O, T> fn, Validator<T, U, E> validator, Fn0<T> defaultValue) {
        return prop(fn, optional(validator, defaultValue));
    }

    /**
     * Creates a {@link Validator} by composing the given validator with 
     * {@link Core#optional(Function, Supplier)}.
     * 
     * @param <O>       the type of object accepted by the given fn
     * @param <T>       the type of values validated
     * @param <E>       the type of errors returned by the validator
     * @param key       the key to index the result of the validator
     * @param fn        the function extracting the value to validate from an instance of `O`
     * @param validator the validator to apply on the extracted value
     * @param lazyE     the error to return if the value is <code>null</code>
     * @return          a {@link Validator} that returns the given a valid result if the extracted 
     *                  value <code>T</code> from <code>O</code> is null.
     *                  Returns the result of the given validator otherwise.
     */
    public static <O, T, U, E> Validator<O, U, E> optional(String key, 
                                                          Function<O, T> fn, 
                                                          Validator<T, U, E> validator) {
        return keyed(key, optional(fn, validator));

    }

    public static <O, T, U, E> Validator<O, U, E> optional(String key, 
                                                          Function<O, T> fn, 
                                                          Validator<T, U, E> validator,
                                                          Fn0<T> defaultValue) {
        return keyed(key, optional(fn, validator, defaultValue));

    }

    /**
     * Creates a validator that will apply the given validator only if the validator
     * value is not null. If the validated value is null it return a valid trie.
     * Otherwise it executes the validator and returns its result.
     * 
     * @param <T>       the type of values validated
     * @param <E>       the type of errors returned by the validator
     * @param validator the validator to apply if the validated value is not null
     * 
     * @return           Returns a valid trie if the validated value is null.
     */
    public static <T, U, E> Validator<T, U, E> optional(Validator<T, U, E> validator) {
        return x -> {
            return x != null ? validator.apply(x) : ValidationResult.validationResult(Trie.valid(x), x, null);
        };
    }

    // F0<T> or F0<U> ???
    public static <T, U, E> Validator<T, U, E> optional(Validator<T, U, E> validator, Fn0<T> defaultValue) {
        return x -> {
            T value  = x != null 
                            ? x
                            : defaultValue.apply();
            return validator.apply(value);
        };
    }

    // implement alwaysValid()

}
