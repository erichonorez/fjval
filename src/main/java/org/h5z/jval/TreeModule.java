package org.h5z.jval;

import static org.organicdesign.fp.StaticImports.map;
import static org.organicdesign.fp.StaticImports.vec;
import static org.organicdesign.fp.StaticImports.xform;

import java.util.List;

import org.organicdesign.fp.collections.ImList;
import org.organicdesign.fp.collections.ImMap;
import org.organicdesign.fp.collections.ImSet;
import org.organicdesign.fp.function.Fn1;
import org.organicdesign.fp.oneOf.Option;

public final class TreeModule {

    public static class Trie<E> {
        private final ImList<E> errors;
        private final ImMap<String, Trie<E>> children;

        public Trie(ImList<E> errors, ImMap<String, Trie<E>> children) {
            this.errors = errors;
            this.children = children;
        }

        public ImList<E> getErrors() {
            return this.errors;
        }

        public ImMap<String, Trie<E>> getChildren() {
            return this.children;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Trie))
                return false;

            Trie<?> trie = (Trie<?>) o;

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

    public static <E> Trie<E> trie(ImList<E> errors, ImMap<String, Trie<E>> children) {
        return new Trie<>(errors, children);
    }

    /**
     * Tests if a given trie is valid.
     * 
     * @return <code>true</code> if the trie root node has no error and all of its
     *         children are valid. <code>false</code> otherwise.
     */
    public static <K, E> boolean isValid(Trie<E> trie) {
        return trie.getErrors().isEmpty() &&
                trie.getChildren().entrySet()
                        .stream()
                        .map(kv -> isValid(kv.getValue()))
                        .reduce(true, (a, b) -> a && b);
    }

    public static <K, E> boolean isValid(Path path, Trie<E> trie) {
        return isValid(get(path, trie));
    }

    /**
     * Test if a given trie is invalid.
     * 
     * @return <code>true</code> if the trie root node has an error or if one of its
     *         children is invalid. <code>false</code> otherwise.
     */
    public static <E> boolean isInvalid(Trie<E> trie) {
        return !isValid(trie);
    }

    public static <E> boolean isInvalid(Path path, Trie<E> root) {
        return !isValid(path, root);
    }

    public static <E> boolean hasErrors(Trie<E> root) {
        return root.getErrors().isEmpty();
    }

    public static <E> boolean hasErrors(Path path, Trie<E> root) {
        return get(path, root).getErrors().isEmpty();
    }

    /**
     * @TODO implement a safer get returning Option<Trie<E>>
     */
    public static <E> Trie<E> get(Path path, Trie<E> root) {
        String current = path.current();
        Path next = path.next();
        if ("/".equals(current)
                && next.isNil()) {
            return root;
        }

        if (!root.getChildren().containsKey(current)) {
            return null;
        }

        if (next.isNil()) {
            return root.getChildren().get(current);
        }

        return get(next, root.getChildren().get(current));
    }

    /**
     * Returns maybe the node at given key in the given trie if it exists.
     * 
     * @param path the sequence of keys to access the node.
     * @return {@link Option.Some} containing the node with specified key in the
     *         given trie. {@link Option.none} otherwise.
     */
    public static <E> Option<Trie<E>> get(List<String> path, Trie<E> root) {
        ImList<String> pathImList = xform(path).toImList();
        String current = pathImList.head().getOrElse("");
        ImList<String> tail = pathImList.drop(1).toImList();

        return recurGet(current, tail, root);
    }

    private static <E> Option<Trie<E>> recurGet(String head, ImList<String> tail, Trie<E> root) {
        if ("".equals(head)) {
            return Option.some(root);
        }

        if (!root.getChildren().containsKey(head)) {
            return Option.none();
        }

        Trie<E> child = root.getChildren().get(head);
        String next = tail.head().getOrElse("");
        return recurGet(next, tail.drop(1).toImList(), child);
    }

    /**
     * Merges two tries together.
     * 
     * @return the merged trie.
     */
    public static <E> Trie<E> merge(Trie<E> a, Trie<E> b) {
        ImList<E> mergedErrors = a.errors.concat(b.errors);
        if (a.getChildren().isEmpty()
                && b.getChildren().isEmpty()) {
            return new Trie<>(mergedErrors, map());
        }

        ImSet<String> aKeys = a.children.keySet();
        ImSet<String> bKeys = b.children.keySet();
        ImSet<String> intersection = aKeys.filter(x -> bKeys.contains(x)).toImSet();

        ImMap<String, Trie<E>> outer = a.children.concat(b.children)
                .filter(kv -> !intersection.contains(kv.getKey()))
                .toImMap(Fn1.identity());

        Trie<E> zero = trie(mergedErrors, map());
        Trie<E> withOuter = outer.fold(zero, (t, kv) -> {
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
    public static <E> ImMap<String, ImList<E>> toMap(Trie<E> root) {
        return recurToMap("", root, map());
    }

    private static <E> ImMap<String, ImList<E>> recurToMap(String currentPath, Trie<E> root,
            ImMap<String, ImList<E>> acc) {
        ImMap<String, ImList<E>> withErrors = acc.assoc(currentPath, root.errors);

        if (root.getChildren().isEmpty()) {
            return withErrors;
        }

        return root.children
                .fold(withErrors, (newAcc, child) -> {
                    String nextPath = currentPath + "." + child.getKey();
                    if ("".equals(currentPath)) {
                        nextPath = child.getKey();
                    }
                    return newAcc.concat(
                            recurToMap(nextPath, child.getValue(), map())).toImMap(Fn1.identity());
                });
    }

}
