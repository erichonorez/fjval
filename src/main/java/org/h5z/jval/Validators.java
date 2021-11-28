package org.h5z.jval;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.h5z.jval.Core.*;

import static org.h5z.jval.Core.*;

public final class Validators {

    /** The class cannot be instanciated */
    private Validators() {}

    /**
     * Create a validator that validates if a value is greater than another one.
     * If the value is lower than the reference value then call the provider supplier to get an instance of the error and return it in a list.
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

    public static <T, E> Validator<T, E> notNull(Supplier<E> supplier) {
        return v -> {
            if (null != v) {
                return valid(v);
            }
            return invalid(supplier.get());
        };
    }

    public static <T extends String, E> Validator<T, E> matches(String regex, Supplier<E> supplier) {
        return v -> {
            boolean match = Pattern.compile(regex).matcher(v).find();
            if (match) {
                return valid(v);
            }
            return invalid(supplier.get());
        };
    }

    // @TODO improve it
    public static <T extends String, E> Validator<T, E> integer(Supplier<E> supplier) {
        return matches("\\d+", supplier);
    }

    // write in

}
