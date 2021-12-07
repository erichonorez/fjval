package org.h5z.jval;

import static org.organicdesign.fp.StaticImports.map;
import static org.organicdesign.fp.StaticImports.vec;
import static org.organicdesign.fp.StaticImports.xform;

import java.util.List;
import java.util.function.BiFunction;

import org.organicdesign.fp.collections.ImList;
import org.organicdesign.fp.collections.ImMap;
import org.organicdesign.fp.collections.ImSet;
import org.organicdesign.fp.function.Fn1;
import org.organicdesign.fp.oneOf.Option;

public final class TreeModule {

    public static class Trie<K, E> {
        private final ImList<E> errors;
        private final ImMap<K, Trie<K, E>> children;

        public Trie(ImList<E> errors, ImMap<K, Trie<K, E>> children) {
            this.errors = errors;
            this.children = children;
        }

        public ImList<E> getErrors() {
            return this.errors;
        }

        public ImMap<K, Trie<K, E>> getChildren() {
            return this.children;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Trie))
                return false;

            Trie<?, ?> trie = (Trie<?, ?>) o;

            if (!getErrors().equals(trie.getErrors()))
                return false;
            return getChildren().equals(trie.getChildren());
        }

        @Override
        public int hashCode() {
            int result = getErrors().hashCode();
            result = 31 * result + getChildren().hashCode();
            return result;
        }
    }

    public static <K, E> Trie<K, E> trie(ImList<E> errors, ImMap<K, Trie<K, E>> children) {
        return new Trie<>(errors, children);
    }

    /**
     * Tests if a given trie is valid.
     * 
     * @return <code>true</code> if the trie root node has no error and all of its
     *         children are valid. <code>false</code> otherwise.
     */
    public static <K, E> boolean isValid(Trie<K, E> trie) {
        return trie.getErrors().isEmpty() &&
                trie.getChildren().entrySet()
                        .stream()
                        .map(kv -> isValid(kv.getValue()))
                        .reduce(true, (a, b) -> a && b);
    }

    public static <K, E> boolean isValid(List<K> path, Trie<K, E> trie) {
        return get(path, trie)
                .match(
                        el -> isValid(el),
                        () -> false);
    }

    /**
     * Test if a given trie is invalid.
     * 
     * @return <code>true</code> if the trie root node has an error or if one of its
     *         children is invalid. <code>false</code> otherwise.
     */
    public static <K, E> boolean isInvalid(Trie<K, E> trie) {
        return !isValid(trie);
    }

    public static <K, E> boolean isInvalid(List<K> path, Trie<K, E> root) {
        return !isValid(path, root);
    }

    public static <K, E> boolean hasErrors(Trie<K, E> root) {
        return root.getErrors().isEmpty();
    }

    public static <K, E> boolean hasErrors(List<K> path, Trie<K, E> root) {
        return get(path, root)
                .match(
                        el -> el.getErrors().isEmpty(),
                        () -> false);
    }

    /**
     * Returns maybe the node at given key in the given trie if it exists.
     * 
     * @param path the sequence of keys to access the node.
     * @return {@link Option.Some} containing the node with specified key in the
     *         given trie. {@link Option.none} otherwise.
     */
    public static <K, E> Option<Trie<K, E>> get(List<K> path, Trie<K, E> root) {
        ImList<K> pathImList = xform(path).toImList();
        Option<K> current = pathImList.head();
        ImList<K> tail = pathImList.drop(1).toImList();

        return recurGet(current, tail, root);
    }

    private static <K, E> Option<Trie<K, E>> recurGet(Option<K> head, ImList<K> tail, Trie<K, E> root) {
        if (!head.isSome()) {
            return Option.some(root);
        }

        if (!root.getChildren().containsKey(head.get())) {
            return Option.none();
        }

        Trie<K, E> child = root.getChildren().get(head.get());
        Option<K> next = tail.head();
        return recurGet(next, tail.drop(1).toImList(), child);
    }

    /**
     * Merges two tries together.
     * 
     * @return the merged trie.
     */
    public static <K, E> Trie<K, E> merge(Trie<K, E> a, Trie<K, E> b) {
        ImList<E> mergedErrors = a.errors.concat(b.errors);
        if (a.getChildren().isEmpty()
                && b.getChildren().isEmpty()) {
            return new Trie<>(mergedErrors, map());
        }

        ImSet<K> aKeys = a.children.keySet();
        ImSet<K> bKeys = b.children.keySet();
        ImSet<K> intersection = aKeys.filter(x -> bKeys.contains(x)).toImSet();

        ImMap<K, Trie<K, E>> outer = a.children.concat(b.children)
                .filter(kv -> !intersection.contains(kv.getKey()))
                .toImMap(Fn1.identity());

        Trie<K, E> zero = trie(mergedErrors, map());
        Trie<K, E> withOuter = outer.fold(zero, (t, kv) -> {
            return trie(
                    t.errors,
                    t.children.assoc(kv));
        });

        return intersection.fold(withOuter, (t, k) -> {
            return trie(
                    t.errors,
                    t.children.assoc(k,
                            merge(
                                    get(vec(k), a).get(), // unsafeGet
                                    get(vec(k), b).get()) // unsafeGet
            ));
        });
    }

    /**
     * Transforms a trie to a map.
     * 
     * Keys of the map are the keys of the nodes in the trie and their associated
     * value is the list of errors of the node identified by the key in the trie.
     * 
     * @return a map
     */
    public static <K, E, B> ImMap<B, ImList<E>> toMap(Trie<K, E> root, B zero, BiFunction<B, K, B> reducer) {
        return recurToMap(zero, root, map(), reducer);
    }

    private static <K, E, B> ImMap<B, ImList<E>> recurToMap(
            B currentPath,
            Trie<K, E> root,
            ImMap<B, ImList<E>> acc,
            BiFunction<B, K, B> reducer) {

        ImMap<B, ImList<E>> withErrors = acc.assoc(currentPath, root.errors);

        if (root.getChildren().isEmpty()) {
            return withErrors;
        }

        return root.children
                .fold(withErrors, (newAcc, child) -> {
                    B nextPath = reducer.apply(currentPath, child.getKey());
                    return newAcc.concat(
                            recurToMap(nextPath, child.getValue(), map(), reducer)).toImMap(Fn1.identity());
                });
    }

}
