package org.h5z.jval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.h5z.jval.Core.*;

import fj.data.vector.V;

import static org.h5z.jval.Core.*;

public final class Validators {

    /** The class cannot be instanciated */
    private Validators() {
    }

    /**
     * Create a validator that validates if a value is greater than another one.
     * If the value is lower than the reference value then call the provider
     * supplier to get an instance of the error and return it in a list.
     */
    public static <T extends Comparable<T>, E> Validator<T, E> gt(T b, Supplier<E> s) {
        return v -> {
            List<E> result = new ArrayList<>();
            if (v.compareTo(b) < 1) {
                result.add(s.get());
                return result;
            }
            return result;
        };
    }

    public static <T extends Comparable<T>, E> Validator<T, E> gte(T b, Function<T, E> fn) {
        return v -> v.compareTo(b) < 0
                ? invalid(fn.apply(v))
                : valid(v);
    }

    public static <T extends Comparable<T>, E> Validator<T, E> eq(T b, Supplier<E> s) {
        return v -> {
            List<E> result = new ArrayList<>();
            if (v.compareTo(b) != 0) {
                result.add(s.get());
                return result;
            }
            return result;
        };
    }

    public static <T extends Comparable<T>, E> Validator<T, E> lt(T b, Supplier<E> s) {
        return sequentially(eq(b, s), gt(b, s));
    }

    public static <T, E> Validator<T, E> cond(Function<T, Boolean> s, Supplier<E> e) {
        return v -> {
            if (!s.apply(v)) {
                return invalid(e.get());
            }
            return valid(v);
        };
    }

    public static <T, E> Validator<T, E> required(Supplier<E> supplier) {
        return v -> {
            if (null != v) {
                return valid(v);
            }
            return invalid(supplier.get());
        };
    }

    public static <E> Validator<String, E> matches(String regex, Supplier<E> supplier) {
        return v -> {
            boolean match = Pattern.compile(regex).matcher(v).find();
            if (match) {
                return valid(v);
            }
            return invalid(supplier.get());
        };
    }

    // @TODO improve it
    public static <E> Validator<String, E> integer(Supplier<E> supplier) {
        return matches("\\d+", supplier);
    }

    // write in

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
    public static <E> Validator<String, E> hasLengthBetween(int inclMin, int inclMax, Supplier<E> errorSupplier) {
        return v -> {
            if (v.length() >= inclMin && v.length() <= inclMax) {
                return valid(v);
            }
            return invalid(errorSupplier.get());
        };
    }

    /**
     * @see {@link #hasLengthBetween(int, int, Function)}.
     * 
     * @param errorFn A function to customize the error messsage with the validated
     *                value.
     * 
     * @return an empty list if the length of the validated value is between the
     *         given bounds. A list with containing the supplier error otherwise.
     */
    public static <E> Validator<String, E> hasLengthBetween(int inclMin, int inclMax, Function<String, E> errorFn) {
        return v -> hasLengthBetween(inclMin, inclMax, () -> errorFn.apply(v)).apply(v);
    }

}
