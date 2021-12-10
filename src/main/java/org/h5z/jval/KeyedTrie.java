package org.h5z.jval;

import static org.h5z.jval.TreeModule.trie;
import static org.h5z.jval.TreeModule.merge;
import static org.h5z.jval.TreeModule.isInvalid;

import static org.organicdesign.fp.StaticImports.map;
import static org.organicdesign.fp.StaticImports.tup;
import static org.organicdesign.fp.StaticImports.vec;
import static org.organicdesign.fp.StaticImports.xform;
import static org.organicdesign.fp.StaticImports.xformArray;

import java.util.List;
import java.util.function.Function;

import org.h5z.jval.Core.Validator;
import org.h5z.jval.TreeModule.Trie;
import org.organicdesign.fp.collections.ImList;

public final class KeyedTrie {

    private KeyedTrie() {
        throw new IllegalAccessError("Cannot be instanciated");
    }

    @FunctionalInterface
    public interface KeyedValidator<T, E> extends Function<T, Trie<E>> {

        /**
         * Alias for {@link Function#apply(Object)}
         */
        default Trie<E> validate(T t) {
            return this.apply(t);
        }

    }

    public static <E> Trie<E> invalid(String key, ImList<E> es) {
        return trie(vec(), map(tup(key, invalid(es))));
    }

    public static <E> Trie<E> invalid(ImList<E> es) {
        return trie(es, map());
    }

    public static <E, T> Trie<E> valid(String key, T v) {
        return trie(vec(), map(tup(key, valid(v))));
    }

    public static <E, T> Trie<E> valid(T v) {
        return trie(vec(), map());
    }

    /**
     * Creates a {@link KeyedValidator} given a key and a {@link Validator}.
     * 
     * Example: If the given key is `x` then the result of the validator will be
     * accessible with the key `x` in the resulting trie.
     * 
     * @param <T>       the type of values validated
     * @param <E>       the type of errors returned by the validator
     * @param key       the key to index the result of the validator
     * @param validator the validator to apply
     * 
     * @return a keyed validator.
     */
    public static <T, E> KeyedValidator<T, E> keyed(String key, Validator<T, E> validator) {
        return v -> {
            List<E> result = validator.apply(v);
            if (Core.isValid(result)) {
                return valid(key, v);
            }
            return invalid(key, xform(result).toImList());
        };
    }

    /**
     * Creates a {@link KeyedValidator} given a key and another {@KeyedValidator}.
     * The given key will be used to access the result of the given valitors in the
     * resulting trie.
     * 
     * Example: If the result of given {@link KeyedValidator} is keyed on a key 'x'
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
    public static <T, E> KeyedValidator<T, E> keyed(String key, KeyedValidator<T, E> validator) {
        return v -> trie(vec(), map(tup(key, validator.apply(v))));
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
    public static <T, E> KeyedValidator<T, E> sequentially(KeyedValidator<T, E>... validators) {
        return v -> {
            return recurSequentially(v, xformArray(validators).toImList(), trie(vec(), map()));
        };
    }

    private static <T, E> Trie<E> recurSequentially(T value, ImList<KeyedValidator<T, E>> validators, Trie<E> acc) {
        return validators.head()
                .match(v -> {
                    Trie<E> validated = v.validate(value);
                    Trie<E> newAcc = merge(acc, validated);
                    if (isInvalid(validated)) {
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
     * @return a valid trie if all validators succeeded. A trie with the errors of
     *         all failed validators otherwise.
     */
    public static <K, E> KeyedValidator<K, E> every(KeyedValidator<K, E>... validators) {
        return v -> vec(validators)
                .map(fn -> fn.apply(v))
                .fold(trie(vec(), map()), TreeModule::merge);
    }

    /**
     * <b>Combinator</b> - Creates a validator that will first extract the value to
     * validate by calling the given fn on the provided value and then pass it to the
     * given validator.
     * 
     * @param <O>        the type of object accepted by the given fn
     * @param <T>        the type of values validated
     * @param <E>        the type of errors returned by the validator
     * @param fn         a function returning an instance of type T given an instance of type O
     * @param validator  the validator to apply on the extracted value
     * 
     * @return an trie if the given validator succeeded. A trie containing the
     *         collected errors otherwise.
     */
    public static <O, T, E> KeyedValidator<O, E> prop(Function<O, T> fn, KeyedValidator<T, E> validator) {
        return x -> validator.apply(fn.apply(x));
    }

}
