package org.h5z.fval4j.data;

import org.h5z.fval4j.Trie;
import org.h5z.fval4j.data.Prelude.Fn1;
import org.h5z.fval4j.data.Prelude.Tuple3;

import static org.h5z.fval4j.Trie.*;

import static org.organicdesign.fp.StaticImports.map;
import static org.organicdesign.fp.StaticImports.vec;

import java.util.Arrays;
import java.util.List;

/**
 * A validation result is a tuple holding 3 values :
 * <ul>
 *  <li>A {@link Trie} of errors;</li>
 *  <li>The value being validated in input;<li>
 *  <li>The validated value.</li>
 * </ul>
 * 
 * The input value and the validated value might have different type. That might happen if the {@link Validator} transforms the input before validation.
 * 
 * The validation result is invalid if its {@link Trie} is not empty. If the validation result is invalid the validated value is {@code null}.
 * When the validation result is valid then the the validation result is not {@code null} and its {@link Trie} is empty.
 */
public final class ValidationResult<E, I, V> extends Tuple3<Trie<E>, I, V> {
    
    public ValidationResult(Trie<E> e, I i, V v) { 
        super(e, i, v);
    }

    public boolean isValid() {
        return this._1().isValid();
    }

    public boolean isInvalid() { return !this.isValid(); }

    public List<E> getErrors() { return this._1().getErrors(); }

    public List<E> getErrors(List<String> path) { return this._1().getErrors(path); }

    public List<E> getErrors(String... path) {
        return this.getErrors(Arrays.asList(path));
    }

    public boolean hasErrors(String... path) { return this._1().hasErrors(path); }

    public <U> ValidationResult<E, I, U> mapValue(Fn1<V, U> fn) {
        return new ValidationResult<>(
            this._1(),
            this._2(),
            fn.apply(this._3())
        );
    }

    @Override
    public String toString() {
        return new StringBuilder()
                        .append("{ trie: ")
                        .append(this._1() != null ? this._1().toString() : "null")
                        .append(", input: ")
                        .append(this._2()!= null ? this._2().toString() : "null")
                        .append(", value:")
                        .append(this._3()!= null ? this._3().toString() : "null")
                        .append(" }")
                        .toString();
    }

    public static <E, I, V> ValidationResult<E, I, V> valid(V v, I i) {
        return new ValidationResult<>(Trie.valid(v), i, v);
    }

    public static <E, I, V> ValidationResult<E, I, V> valid(String key, V v, I i) {
        return new ValidationResult<>(Trie.valid(key, v), i, v);
    }

    public static <E, I, V> ValidationResult<E, I, V> invalid(I i, E e) {
        return new ValidationResult<>(Trie.invalid(e), i, null);
    }

    public static <E, I, V> ValidationResult<E, I, V> invalid(I i, String key, E e) {
        return new ValidationResult<>(Trie.invalid(key, Arrays.asList(e)), i, null);
    }

    public static <E, I, V> ValidationResult<E, I, V> identity() {
        return new ValidationResult<>(trie(vec(), map()), null, null);
    }

    public static <E, I, V> ValidationResult<E, I, V> validationResult(Trie<E> e, I i, V v) {
        return new ValidationResult<E,I,V>(e, i, v);
    }

}
