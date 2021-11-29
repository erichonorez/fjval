package org.h5z.jval;

import java.util.*;
import java.util.stream.Stream;

public final class TreeModule {

    public static class Trie<E> {
        private final List<E> errors;
        private final Map<String, Trie<E>> children;

        public Trie(List<E> errors, Map<String, Trie<E>> children) {
            this.errors = errors;
            this.children = children;
        }

        public List<E> getErrors() {
            return this.errors;
        }

        public Map<String, Trie<E>> getChildren() {
            return this.children;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Trie)) return false;

            Trie<?> trie = (Trie<?>) o;

            if (!getErrors().equals(trie.getErrors())) return false;
            return getChildren().equals(trie.getChildren());
        }

        @Override
        public int hashCode() {
            int result = getErrors().hashCode();
            result = 31 * result + getChildren().hashCode();
            return result;
        }
    }

    public static <E> Trie<E> tree(List<E> errors, Map<String, Trie<E>> children) {
        return new Trie<>(errors, children);
    }

    public static <K, E> boolean isValid(Trie<E> trie) {
        return trie.getErrors().isEmpty() &&
            trie.getChildren().entrySet()
                .stream()
                .map(kv -> isValid(kv.getValue()))
                .reduce(true, (a, b) -> a && b);
    }

    public static <E> boolean isInvalid(Trie<E> trie) {
        return !isValid(trie);
    }

    public static <E> Map<String, List<E>> toMap(Trie<E> root) {
        Map<String, List<E>> map = recurToMap(root, "", new HashMap<>());
        map.put(".", root.getErrors());
        return map;
    }

    private static <E> Map<String, List<E>> recurToMap(Trie<E> root, String prefix, Map<String, List<E>> acc) {
        if (root.getChildren().isEmpty()) {
            return acc;
        }

        root.getChildren()
            .forEach((k, v) -> {
                String key = "".equals(prefix) ? k : prefix + "." + k;
                acc.put(key, v.getErrors());
                recurToMap(v, key, acc);
            });

        return acc;
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

    public static <E> Trie<E> append(Trie<E> a, Trie<E> b) {
        ArrayList<E> mergedErrors = new ArrayList<>(a.errors);
        mergedErrors.addAll(b.errors);
        Map<String, Trie<E>> mergedChildren = new HashMap<>();

        if (a.getChildren().isEmpty()
            && b.getChildren().isEmpty()) {
            return new Trie<>(mergedErrors, mergedChildren);
        }

        Set<String> intersection = new HashSet<>(a.getChildren().keySet());
        intersection.retainAll(b.getChildren().keySet());

        Stream<String> notInB = a.getChildren()
            .keySet()
            .stream()
            .filter(k -> !intersection.contains(k));

        Stream<String> notInA = b.getChildren()
            .keySet()
            .stream()
            .filter(k -> !intersection.contains(k));

        notInB.forEach(k -> mergedChildren.put(k, a.getChildren().get(k)));
        notInA.forEach(k -> mergedChildren.put(k, b.getChildren().get(k)));

        intersection.forEach(k -> mergedChildren.put(
                k,
                append(
                    a.getChildren().get(k),
                    b.getChildren().get(k)
                ))
        );

        return new Trie<>(mergedErrors, mergedChildren);
    }

}
