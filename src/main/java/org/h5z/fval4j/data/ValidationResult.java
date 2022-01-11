package org.h5z.fval4j.data;

import org.h5z.fval4j.Trie;
import org.h5z.fval4j.data.Prelude.F1;
import org.h5z.fval4j.data.Prelude.Tuple3;

import static org.h5z.fval4j.Trie.*;

import static org.organicdesign.fp.StaticImports.map;
import static org.organicdesign.fp.StaticImports.vec;

import java.util.Arrays;
import java.util.List;

public final class ValidationResult<E, I, V> extends Tuple3<Trie<E>, I, V> {
    
    public ValidationResult(Trie<E> e, I i, V v) { 
        super(e, i, v);
    }

    public boolean isValid() {
        return this._1().isValid();
    }

    public boolean isInvalid() { return !this.isValid(); }

    public List<E> getErrors() { return this._1().getErrors(); }

    public List<E> getErrors(List<String> path) { return null; }

    public List<E> getErrors(String... path) {
        return this.getErrors(Arrays.asList(path));
    }

    public boolean hasErrors(String... path) { return false; }

    public <U> ValidationResult<E, I, U> mapValue(F1<V, U> fn) {
        return new ValidationResult<>(
            this._1(),
            this._2(),
            fn.apply(this._3())
        );
    }

    public static <E, I, V> ValidationResult<E, I, V> valid(V v, I i) {
        return new ValidationResult<>(Trie.valid(v), null, v);
    }

    public static <E, I, V> ValidationResult<E, I, V> valid(String key, V v, I i) {
        return new ValidationResult<>(Trie.valid(key, v), null, v);
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

}
