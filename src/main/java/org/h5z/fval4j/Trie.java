package org.h5z.fval4j;

import static org.organicdesign.fp.StaticImports.map;
import static org.organicdesign.fp.StaticImports.tup;
import static org.organicdesign.fp.StaticImports.vec;
import static org.organicdesign.fp.StaticImports.xform;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.organicdesign.fp.collections.ImList;
import org.organicdesign.fp.collections.ImMap;
import org.organicdesign.fp.collections.ImSet;
import org.organicdesign.fp.function.Fn1;
import org.organicdesign.fp.oneOf.Option;

public class Trie<E> {

    static final String ROOT_KEY = "";
    
    final ImList<E> errors;
    final ImMap<String, Trie<E>> children;

    /**
     * 
     * @param errors
     * @param children
     * 
     * @see {@link Trie#invalid(Object)} to create a trie with an error at the root
     * @see {@link Trie#invalid(String, List)} to create a trie with the given errors with the given path
     * @see {@link Trie#valid(Object)} to create a valid trie for the given value
     * @see {@link Trie#valid(String, Object)} to create a valid trie with the given path
     * @see {@link Trie#trie(List, Map)} to create a trie on your own
     * 
     * @author Eric Honorez
     */
    private Trie(ImList<E> errors, ImMap<String, Trie<E>> children) {
        this.errors = errors;
        this.children = children;
    }

    public List<E> getErrors() {
        return this.errors;
    }

    public List<E> getErrors(List<String> path) {
        return this.get(path)
            .match(n -> n.getErrors(),
                   () -> vec());
    }

    public List<E> getErrors(String... path) {
        return this.getErrors(Arrays.asList(path));
    }

    public Map<String, Trie<E>> getChildren() {
        return this.children;
    }

    /**
     * Tests if a given trie is valid.
     * 
     * @return <code>true</code> if the this trie has no error and all of its
     *         children are valid. <code>false</code> otherwise.
     */
    public boolean isValid() {
        return this.getErrors().isEmpty() &&
                this.getChildren().entrySet()
                        .stream()
                        .map(kv -> kv.getValue().isValid())
                        .reduce(true, (a, b) -> a && b);
    }

    public boolean isValid(List<String> path) {
        return this.get(path)
                .match(
                        el -> el.isValid(),
                        () -> false);
    }

    /**
     * Test if a given trie is invalid.
     * 
     * @return <code>true</code> if this trie has an error or if one of its
     *         children is invalid. <code>false</code> otherwise.
     */
    public boolean isInvalid() {
        return !this.isValid();
    }

    public boolean isInvalid(List<String> path) {
        return !this.isValid(path);
    }

    public boolean hasErrors() {
        return !this.getErrors().isEmpty();
    }

    public boolean hasErrors(List<String> path) {
        return this.get(path)
                .match(
                        el -> !el.getErrors().isEmpty(),
                        () -> false);
    }

    public boolean hasErrors(String... path) {
        return this.hasErrors(Arrays.asList(path));
    }

    /**
     * Returns maybe the node at given key in the given trie if it exists.
     * 
     * @param path the sequence of keys to access the node.
     * @return {@link Option.Some} containing the node with specified key in the
     *         given trie. {@link Option.none} otherwise.
     */
    public Option<Trie<E>> get(List<String> path) {
        ImList<String> pathImList = xform(path).toImList();
        String current = pathImList.head().getOrElse("");
        ImList<String> tail = pathImList.drop(1).toImList();

        return recurGet(current, tail, this);
    }

    public Option<Trie<E>> get(String... path) {
        return get(Arrays.asList(path));
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
    public Trie<E> merge(Trie<E> b) {
        ImList<E> mergedErrors = this.errors.concat(b.errors);
        if (this.getChildren().isEmpty()
                && b.getChildren().isEmpty()) {
            return new Trie<>(mergedErrors, map());
        }

        ImSet<String> aKeys = this.children.keySet();
        ImSet<String> bKeys = b.children.keySet();
        ImSet<String> intersection = aKeys.filter(x -> bKeys.contains(x)).toImSet();

        ImMap<String, Trie<E>> outer = this.children.concat(b.children)
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
                    t.children.assoc(k, this.get(vec(k)).get().merge(b.get(vec(k)).get()) // unsafeGet
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
    public Map<String, ImList<E>> toMap() {
        return recurToMap("", this, map());
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

    @Override
    public String toString() {
        return "{ errors: [%s], children: [%s] }"
                .formatted(
                    String.join(", ", this.errors.map(Object::toString)),
                    String.join(", \n", this.children.map(kv -> "{ %s: %s }".formatted(kv.getKey(), kv.getValue().toString())))
                );
    }

    public static <E, T> Trie<E> valid(T v) {
        return trie(vec(), map());
    }

    public static <E, T> Trie<E> valid(String key, T v) {
        if (ROOT_KEY.equals(key)) {
            return Trie.valid(v);
        }
        return trie(vec(), map(tup(key, Trie.valid(v))));
    }

    public static <E> Trie<E> invalid(List<E> es) {
        return trie(es, map());
    }

    public static <E> Trie<E> invalid(E e) {
        return invalid(vec(e));
    }

    public static <E> Trie<E> invalid(String key, List<E> es) {
        if (ROOT_KEY.equals(key)) {
            return invalid(es);
        }
        return trie(vec(), map(tup(key, invalid(es))));
    }

    public static <E> Trie<E> trie(List<E> errors, Map<String, Trie<E>> children) {
        return new Trie<>(xform(errors).toImList(), xform(children.entrySet()).toImMap(kv -> kv));
    }
}