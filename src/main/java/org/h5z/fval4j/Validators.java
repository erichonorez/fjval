package org.h5z.fval4j;

import static org.h5z.fval4j.Core.any;
import static org.h5z.fval4j.Core.not;
import static org.h5z.fval4j.Core.sequentially;
import static org.h5z.fval4j.Trie.invalid;
import static org.h5z.fval4j.Trie.valid;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.h5z.fval4j.Core.Validator;

public final class Validators {

    private Validators() {
        throw new IllegalAccessError("Cannot be instantiated");
    }
   
    public static <T extends Comparable<T>, E> Validator<T, E> gt(T b, Function<T, E> errorFn) {
        return v -> v.compareTo(b) < 1 
                ? invalid(errorFn.apply(v)) 
                : valid(v);
    }
    
    public static <T extends Comparable<T>, E> Validator<T, E> gt(T b, Supplier<E> lazyE) {
        return gt(b, _v -> lazyE.get());
    }

    public static <T extends Comparable<T>, E> Validator<T, E> eq(T b, Function<T, E> errorFn) { 
        return v -> v.compareTo(b) == 0
                        ? invalid(errorFn.apply(v))
                        : valid(v);
    }

    public static <T extends Comparable<T>, E> Validator<T, E> eq(T b, Supplier<E> lazyE) {
        return eq(b, _v -> lazyE.get());
    }

    public static <T extends Comparable<T>, E> Validator<T, E> gte(T b, Function<T, E> errorFn) {
        return any(gt(b, errorFn), eq(b, errorFn));
    }

    public static <T extends Comparable<T>, E> Validator<T, E> gte(T b, Supplier<E> lazyE) {
        return gt(b, _v -> lazyE.get());
    }

    public static <T extends Comparable<T>, E> Validator<T, E> lt(T b, Function<T, E> errorFn) {
        return sequentially(
                    not(eq(b, errorFn), errorFn), 
                    not(gt(b, errorFn), errorFn));
    }

    public static <T extends Comparable<T>, E> Validator<T, E> lt(T b, Supplier<E> lazyE) {
        return lt(b, _v -> lazyE.get());
    }

    public static <T extends Comparable<T>, E> Validator<T, E> lte(T b, Function<T, E> errorFn) {
        return not(gt(b, errorFn), errorFn);
    }

    public static <T extends Comparable<T>, E> Validator<T, E> lte(T b, Supplier<E> lazyE) {
        return lte(b, _v -> lazyE.get());
    }

    public static <T extends Comparable<T>, E> Validator<T, E> between(T a, T b, Function<T, E> errorFn) {
        return sequentially(
                    gte(a, errorFn), 
                    lte(b, errorFn));
    }

    public static <T extends Comparable<T>, E> Validator<T, E> between(T a, T b, Supplier<E> lazyE) {
        return sequentially(
                    gte(a, lazyE), 
                    lte(b, lazyE));
    }

    public static <E> Validator<String, E> matches(String regex, Function<String, E> errorFn) {
        return v -> !Pattern.compile(regex).matcher(v).find()
                        ? invalid(errorFn.apply(v))
                        : valid(v);
    }

    public static <E> Validator<String, E> matches(String regex, Supplier<E> lazyE) {
        return matches(regex, _v -> lazyE.get());
    }

    /**
     * Creates a validator to assert the length of a string is between the given
     * bounds.
     * 
     * @param <E>           the type of error returned by the validator
     * @param inclMin       the inclusive lower bound
     * @param inclMax       the inclusive upper bound
     * @param errorSupplier the supplier for the error message
     * 
     * @return an empty list if the length of the validated value is between the
     *         given bounds. A list with containing the supplier error otherwise.
     */
    public static <E> Validator<String, E> lengthBetween(int inclMin, int inclMax, Supplier<E> errorSupplier) {
        return v -> lengthBetween(inclMin, inclMax, _v -> errorSupplier.get()).apply(v);
    }

    /**
     * @see {@link #lengthBetween(int, int, Function)}.
     * 
     * @param errorFn A function to customize the error messsage with the validated
     *                value.
     * 
     * @return an empty list if the length of the validated value is between the
     *         given bounds. A list with containing the supplier error otherwise.
     */
    public static <E> Validator<String, E> lengthBetween(int inclMin, int inclMax, Function<String, E> errorFn) {
        return v -> {
            if (v.length() >= inclMin && v.length() <= inclMax) {
                return valid(v);
            }
            return invalid(errorFn.apply(v));
        };
    }

    public static <E> Validator<String, E> contains(CharSequence b, Function<String, E> errorFn) {
        return v -> !v.contains(b) 
                        ? invalid(errorFn.apply(v))
                        : valid(v);
    }

    public static <E> Validator<String, E> contains(CharSequence b, Supplier<E> lazyE) {
        return contains(b, _v -> lazyE.get());
    }

    public static <E> Validator<String, E> notBlank(Function<String, E> errorFn) {
        return v -> v.isBlank() 
                        ? invalid(errorFn.apply(v))
                        : valid(v);
    }

    public static <E> Validator<String, E> notBlank(Supplier<E> lazyE) {
        return notBlank(_v -> lazyE.get());
    }

    public static <T, E> Validator<T, E> in(Set<T> xs, Function<T, E> errorFn) {
        return v -> !xs.contains(v) 
                        ? invalid(errorFn.apply(v))
                        : valid(v);
    }

    // Object validation

    public static <T, E> Validator<T, E> cond(Function<T, Boolean> s, Function<T, E> errorFn) {
        return v -> !s.apply(v) 
                        ? invalid(errorFn.apply(v))
                        : valid(v);
    }

    public static <T, E> Validator<T, E> cond(Function<T, Boolean> s, Supplier<E> lazyE) {
        return cond(s, _v -> lazyE.get());
    }

    public static <T, E> Validator<T, E> required(Supplier<E> supplier) {
        return v -> null == v 
                        ? invalid(supplier.get())
                        : valid(v);
    }

    public static <T, E> Validator<T, E> in(Set<T> xs, Supplier<E> lazyE) {
        return in(xs, _v -> lazyE.get());
    }

    public static <T, E> Validator<T, E> equals(T b, Function<T, E> errorFn) {
        return v -> !v.equals(b)
                        ? invalid(errorFn.apply(v))
                        : valid(v);
     }

    public static <T, E> Validator<T, E> equals(T b, Supplier<E> lazyE) {
        return equals(b, _v -> lazyE.get());
    }

    // Collections validation

    public static <T, E> Validator<List<T>, E> sizeBetween(int inclMin, int inclMax, Function<List<T>, E> errorFn) {
        return v -> {
            if (v.size() >= inclMin && v.size() <= inclMax) {
                return valid(v);
            }
            return invalid(errorFn.apply(v));
        };
    }

    public static <T, E> Validator<List<T>, E> sizeBetween(int inclMin, int inclMax, Supplier<E> lazyE) {
        return sizeBetween(inclMin, inclMax, _v -> lazyE.get());
    }

    public static <T, E> Validator<T, E> identity() {
        return v -> valid(v);
    }

}
