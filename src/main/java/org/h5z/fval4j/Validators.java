package org.h5z.fval4j;

import static org.h5z.fval4j.Core.any;
import static org.h5z.fval4j.Core.not;
import static org.h5z.fval4j.Core.and;
import static org.h5z.fval4j.data.ValidationResult.invalid;
import static org.h5z.fval4j.data.ValidationResult.valid;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.h5z.fval4j.Core.Validator;
import org.h5z.fval4j.data.ValidationResult;

public final class Validators {

    private Validators() {
        throw new IllegalAccessError("Cannot be instantiated");
    }
   
    public static <T extends Comparable<T>, E> Validator<T, T, E> gt(T b, Function<T, E> errorFn) {
        return v -> v.compareTo(b) < 1 
                ? invalid(v, errorFn.apply(v)) 
                : valid(v, v);
    }
    
    public static <T extends Comparable<T>, E> Validator<T, T, E> gt(T b, Supplier<E> lazyE) {
        return gt(b, _v -> lazyE.get());
    }

    public static <T extends Comparable<T>, E> Validator<T, T, E> eq(T b, Function<T, E> errorFn) { 
        return v -> v.compareTo(b) == 0
                        ? invalid(v, errorFn.apply(v))
                        : valid(v, v);
    }

    public static <T extends Comparable<T>, E> Validator<T, T, E> eq(T b, Supplier<E> lazyE) {
        return eq(b, _v -> lazyE.get());
    }

    public static <T extends Comparable<T>, E> Validator<T, T, E> gte(T b, Function<T, E> errorFn) {
        return any(gt(b, errorFn), eq(b, errorFn));
    }

    public static <T extends Comparable<T>, E> Validator<T, T, E> gte(T b, Supplier<E> lazyE) {
        return gt(b, _v -> lazyE.get());
    }

    public static <T extends Comparable<T>, E> Validator<T, T, E> lt(T b, Function<T, E> errorFn) {
        return and(
                    not(eq(b, errorFn), errorFn), 
                    not(gt(b, errorFn), errorFn));
    }

    public static <T extends Comparable<T>, E> Validator<T, T, E> lt(T b, Supplier<E> lazyE) {
        return lt(b, _v -> lazyE.get());
    }

    public static <T extends Comparable<T>, E> Validator<T, T, E> lte(T b, Function<T, E> errorFn) {
        return not(gt(b, errorFn), errorFn);
    }

    public static <T extends Comparable<T>, E> Validator<T, T, E> lte(T b, Supplier<E> lazyE) {
        return lte(b, _v -> lazyE.get());
    }

    public static <T extends Comparable<T>, E> Validator<T, T, E> between(T a, T b, Function<T, E> errorFn) {
        return and(
                    gte(a, errorFn), 
                    lte(b, errorFn));
    }

    public static <T extends Comparable<T>, E> Validator<T, T, E> between(T a, T b, Supplier<E> lazyE) {
        return and(
                    gte(a, lazyE), 
                    lte(b, lazyE));
    }

    public static <E> Validator<String, String, E> matches(String regex, Function<String, E> errorFn) {
        return v -> !Pattern.compile(regex).matcher(v).find()
                        ? invalid(v, errorFn.apply(v))
                        : valid(v, v);
    }

    public static <E> Validator<String, String, E> matches(String regex, Supplier<E> lazyE) {
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
    public static <E> Validator<String, String, E> lengthBetween(int inclMin, int inclMax, Supplier<E> errorSupplier) {
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
    public static <E> Validator<String, String, E> lengthBetween(int inclMin, int inclMax, Function<String, E> errorFn) {
        return v -> {
            if (v.length() >= inclMin && v.length() <= inclMax) {
                return valid(v, v);
            }
            return invalid(v, errorFn.apply(v));
        };
    }

    public static <E> Validator<String, String, E> contains(CharSequence b, Function<String, E> errorFn) {
        return v -> !v.contains(b) 
                        ? invalid(v, errorFn.apply(v))
                        : valid(v, v);
    }

    public static <E> Validator<String, String, E> contains(CharSequence b, Supplier<E> lazyE) {
        return contains(b, _v -> lazyE.get());
    }

    public static <E> Validator<String, String, E> notBlank(Function<String, E> errorFn) {
        return v -> v.isBlank() 
                        ? invalid(v, errorFn.apply(v))
                        : valid(v, v);
    }

    public static <E> Validator<String, String, E> notBlank(Supplier<E> lazyE) {
        return notBlank(_v -> lazyE.get());
    }

    public static <T, E> Validator<T, T, E> in(Set<T> xs, Function<T, E> errorFn) {
        return v -> !xs.contains(v) 
                        ? invalid(v, errorFn.apply(v))
                        : valid(v, v);
    }

    // Object validation

    public static <T, E> Validator<T, T, E> cond(Function<T, Boolean> s, Function<T, E> errorFn) {
        return v -> !s.apply(v) 
                        ? invalid(v, errorFn.apply(v))
                        : valid(v, v);
    }

    public static <T, E> Validator<T, T, E> cond(Function<T, Boolean> s, Supplier<E> lazyE) {
        return cond(s, _v -> lazyE.get());
    }

    public static <T, E> Validator<T, T, E> required(Supplier<E> supplier) {
        return v -> null == v 
                        ? invalid(v, supplier.get())
                        : valid(v, v);
    }

    public static <T, E> Validator<T, T, E> in(Set<T> xs, Supplier<E> lazyE) {
        return in(xs, _v -> lazyE.get());
    }

    public static <T, E> Validator<T, T, E> equals(T b, Function<T, E> errorFn) {
        return v -> !v.equals(b)
                        ? invalid(v, errorFn.apply(v))
                        : valid(v, v);
     }

    public static <T, E> Validator<T, T, E> equals(T b, Supplier<E> lazyE) {
        return equals(b, _v -> lazyE.get());
    }

    // Collections validation

    public static <T, E> Validator<List<T>, List<T>, E> sizeBetween(int inclMin, int inclMax, Function<List<T>, E> errorFn) {
        return v -> {
            if (v.size() >= inclMin && v.size() <= inclMax) {
                return valid(v, v);
            }
            return invalid(v, errorFn.apply(v));
        };
    }

    public static <T, E> Validator<List<T>, List<T>, E> sizeBetween(int inclMin, int inclMax, Supplier<E> lazyE) {
        return sizeBetween(inclMin, inclMax, _v -> lazyE.get());
    }

    public static <T, E> Validator<T, T, E> identity() {
        return v -> ValidationResult.<E, T, T> valid(v, v);
    }

}
