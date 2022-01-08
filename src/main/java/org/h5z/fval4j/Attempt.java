package org.h5z.fval4j;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.organicdesign.fp.function.Fn2;
import org.organicdesign.fp.tuple.Tuple2;

import static org.h5z.fval4j.Trie.*;

import static org.organicdesign.fp.StaticImports.map;
import static org.organicdesign.fp.StaticImports.vec;
import static org.organicdesign.fp.StaticImports.xform;
import static org.organicdesign.fp.StaticImports.tup;

public final class Attempt {
    
    interface Validator<T, U, E> extends Function<T, ValidationResult<E, U>> { }

    public static final class ValidationResult<E, T> extends Tuple2<Trie<E>, T> {
        protected ValidationResult(Trie<E> b, T a) { 
            super(b, a);
        }

        public boolean isValid() {
            return this._1().isValid();
        }

        public static <E, T> ValidationResult<E, T> ok(T t) {
            return new ValidationResult<>(Trie.valid(t), t);
        }

        public static <E, T> ValidationResult<E, T> ok(String key, T t) {
            return new ValidationResult<>(Trie.valid(key, t), t);
        }

        public static <E, T> ValidationResult<E, T> nok(E e) {
            return new ValidationResult<>(Trie.invalid(e), null);
        }

        public static <E, T> ValidationResult<E, T> nok(String key, E e) {
            return new ValidationResult<>(Trie.invalid(key, Arrays.asList(e)), null);
        }

        public static <E, T> ValidationResult<E, T> identity() {
            return new ValidationResult<>(trie(vec(), map()), null);
        }

        // implement applicative
    }

    // keep the last U
    public static <T, U, E> Validator<T, U, E> every(List<Validator<T, U, E>> validators) { 
        return v -> xform(validators)
            .map(fn -> fn.apply(v))
            .fold(ValidationResult.identity(), (a, b) -> new ValidationResult<>(a._1().merge(b._1()), b._2()));
    }

    public static <T, E, R0, R1, R2> Validator<T, R2, E> every(Validator<T, R0, E> v0, Validator<T, R1, E> v1, Fn2<R0, R1, R2> fn) {
        return t -> { 
            ValidationResult<E, R0> apply0 = v0.apply(t);
            ValidationResult<E, R1> apply1 = v1.apply(t);
            if (apply0.isValid() && apply1.isValid()) {
                return new ValidationResult<E,R2>(
                    apply0._1().merge(apply1._1()),
                    fn.apply(apply0._2(),
                    apply1._2())
                );
            }
            return new ValidationResult<E,R2>(apply0._1().merge(apply1._1()), null);
        };
    }

    public static <T, E, R0, R1, R2> Validator<T, R2, E> sequentially(Validator<T, R0, E> v0, Validator<T, R1, E> v1, Fn2<R0, R1, R2> fn) {
        return t -> { 
            ValidationResult<E, R0> apply0 = v0.apply(t);
            if (!apply0.isValid()) {
                return new ValidationResult<E, R2>(apply0._1(), null);
            }
            ValidationResult<E, R1> apply1 = v1.apply(t);
            if (!apply1.isValid()) {
                return new ValidationResult<E, R2>(apply1._1(), null);
            }
            return ValidationResult.ok(fn.apply(apply0._2(), apply1._2()));
        };
    }

    public static <T, E, R0, R1> Validator<T, R1, E> then(Validator<T, R0, E> v0, Validator<R0, R1, E> v1) {
        return t -> { 
            ValidationResult<E, R0> apply0 = v0.apply(t);
            if (!apply0.isValid()) {
                return new ValidationResult<E, R1>(apply0._1(), null);
            }
            return v1.apply(apply0._2());
        };
    }

    public static void example() { 
        Validator<Tuple2<Object, Object>, Tuple2<Integer, String>, String> validator = sequentially(
            prop(Tuple2::_1, isInteger),
            prop(Tuple2::_2, isString),
            (i, s) -> tup(i, s)
        );

        validator.apply(tup(1, "String"))._2();
    }

    public static final <R, S, T, E> Validator<T, S, E> prop(Function<T, R> fn, Validator<R, S, E> v) {
        return t -> v.apply(fn.apply(t));
    }

    public static final Validator<Object, Integer, String> isInteger = o -> {
        if (o instanceof Integer) {
            return ValidationResult.ok((Integer) o);
        }
        return ValidationResult.nok("not an integer");
    };

    public static final Validator<Object, String, String> isString = o -> {
        if (o instanceof String) {
            return ValidationResult.ok((String) o);
        }
        return ValidationResult.nok("no a string");
    };

}
