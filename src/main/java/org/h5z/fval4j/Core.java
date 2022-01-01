package org.h5z.fval4j;

import static org.h5z.fval4j.Trie.*;
import static org.organicdesign.fp.StaticImports.map;
import static org.organicdesign.fp.StaticImports.tup;
import static org.organicdesign.fp.StaticImports.vec;
import static org.organicdesign.fp.StaticImports.xform;
import static org.organicdesign.fp.StaticImports.xformArray;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.organicdesign.fp.collections.ImList;

public final class Core {

    private Core() {
        throw new IllegalAccessError("Cannot be instanciated");
    }

    @FunctionalInterface
    public interface Validator<T, E> extends Function<T, Trie<E>> {

        /**
         * Alias for {@link Function#apply(Object)}
         */
        default Trie<E> validate(T t) {
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
    public static <T, E> Validator<T, E> keyed(String key, Validator<T, E> validator) {
        return v -> trie(vec(), map(tup(key, validator.apply(v))));
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
    public static <T, E> Validator<T, E> globally(Validator<T, E> validator) { 
        return keyed(Trie.ROOT_KEY, validator);
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
    public static <T, E> Validator<T, E> sequentially(Validator<T, E>... validators) {
        return v -> {
            return recurSequentially(v, xformArray(validators).toImList(), trie(vec(), map()));
        };
    }

    private static <T, E> Trie<E> recurSequentially(T value, ImList<Validator<T, E>> validators, Trie<E> acc) {
        return validators.head()
                .match(v -> {
                    Trie<E> validated = v.validate(value);
                    Trie<E> newAcc = acc.merge(validated);
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
    public static <K, E> Validator<K, E> every(Validator<K, E>... validators) {
        return every(vec(validators));
    }

    /**
     * @see {@link Core#every(Validator...)}
     */
    public static <K, E> Validator<K, E> every(List<Validator<K, E>> validators) {
        return v -> xform(validators)
                .map(fn -> fn.apply(v))
                .fold(trie(vec(), map()), (a, b) -> a.merge(b));
    }

    
    public static <T, E> Validator<T, E> any(List<Validator<T, E>> validators) {
        return v -> {
            if (validators.size() == 0) {
                return valid(v);
            } else if (validators.size() == 1) {
                return validators.get(0).apply(v);
            } else {
                Trie<E> result = validators.get(0).apply(v);
                if (result.isInvalid()) {
                    return result;
                } else {
                    return any(validators.subList(1, validators.size())).apply(v);
                }
            }
        };
    }

    @SafeVarargs
    public static <T, E> Validator<T, E> any(Validator<T, E>... validators) {
        return any(Arrays.asList(validators));
    }

    public static <T, E> Validator<T, E> not(Validator<T, E> validator, Function<T, E> errorFn) {
        return v -> validator.apply(v).isValid() ? invalid(errorFn.apply(v)) : valid(v);
    }

    public static <T, E> Validator<T, E> not(Validator<T, E> validator, Supplier<E> lazyE) {
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
    public static <O, T, E> Validator<O, E> prop(Function<O, T> fn, Validator<T, E> validator) {
        return x -> validator.apply(fn.apply(x));
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
     * @param reducer   a function reducing a List<KeyedValidator<T, E>> to a
     *                  KeyedValidator<T, E>
     * 
     * @return a valid trie if the given validator succeeded. A trie containing the
     *         collected errors otherwise.
     */
    public static <V, T extends List<V>, E> Validator<T, E> list(
            Validator<V, E> validator,
            Function<List<Validator<T, E>>, Validator<T, E>> reducer) {
        return t -> {
            List<Validator<T, E>> validators = IntStream
                    .range(0, t.size())
                    .mapToObj(i -> keyed(String.valueOf(i), (T xs) -> validator.apply(xs.get(i))))
                    .toList();
            return reducer.apply(validators).apply(t);
        };
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
    public static <K, E> Validator<K, E> required(Validator<K, E> validator, Supplier<E> lazyE) {
        return x -> {
            if (null == x) {
                return trie(vec(lazyE.get()), map());
            }
            return validator.apply(x);
        };
    }

    public static <O, T, E> Validator<O, E> required(Function<O, T> fn, Validator<T, E> validator, Supplier<E> lazyE) {
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
    public static <O, T, E> Validator<O, E> required(String key, 
                                                     Function<O, T> fn, 
                                                     Validator<T, E> validator, 
                                                     Supplier<E> lazyE) {
        return keyed(key, required(fn, validator, lazyE));
    }

    public static <O, T, E> Validator<O, E> optional(Function<O, T> fn, Validator<T, E> validator) {
        return prop(fn, optional(validator));
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
    public static <O, T, E> Validator<O, E> optional(String key, 
                                                          Function<O, T> fn, 
                                                          Validator<T, E> validator) {
        return keyed(key, optional(fn, validator));

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
    public static <T, E> Validator<T, E> optional(Validator<T, E> validator) {
        return x -> {
            if (null == x) {
                return trie(vec(), map());
            }
            return validator.apply(x);
        };
    }

}
