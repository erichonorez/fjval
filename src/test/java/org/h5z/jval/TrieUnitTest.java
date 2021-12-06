package org.h5z.jval;

import org.assertj.core.util.Maps;
import org.h5z.jval.TreeModule.Trie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;
import static org.organicdesign.fp.StaticImports.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.jval.TreeModule.*;

public class TrieUnitTest {

    public static final List<String> NO_ERROR = Collections.emptyList();
    public static final Map<String, Trie<String>> NO_CHILD = Collections.emptyMap();

    @Nested
    @DisplayName("isValid")
    class IsValid {

        @Test
        @DisplayName("Returns true if the trie root node has no error and all of its children are valid")
        void t0() {
            Trie<String> validation = trie(
                    vec(), // no error at the root
                    map(tup("x", trie(
                            vec(), // no error for "x"
                            map())))); // no child

            assertThat(isValid(validation)).isTrue();
        }

        @Test
        @DisplayName("Returns false if the root node has an error and all of its children are valid")
        void t1() {
            Trie<String> validation = trie(
                    vec("this is an error"), // an error at the root
                    map(tup("x", trie(
                            vec(), // no error for x
                            map())))); // no child

            assertThat(isValid(validation)).isFalse();
        }

        @Test
        @DisplayName("Returns false if the root node has no error but a child is invalid")
        void t2() {
            Trie<String> validation = trie(
                    vec(), // no errors at the root
                    map(tup("x", trie(
                            vec(), // no error for "x"
                            map(tup("y", trie(
                                    vec("This is an error"), // an error for x.y
                                    map()))))))); // no child

            assertThat(isValid(validation)).isFalse();
        }

    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("Should do something")
        void t0() {
            Trie<String> root = trie(
                    Arrays.asList("This is an error"),
                    Maps.newHashMap(
                            "x",
                            trie(
                                    NO_ERROR,
                                    Maps.newHashMap(
                                            "1",
                                            trie(
                                                    NO_ERROR,
                                                    NO_CHILD)))));

            Trie<String> node = get(Path.parse("/"), root);
            assertThat(node.getErrors()).containsExactly("This is an error");
        }

    }

    @Nested
    @DisplayName("append")
    static class Append {

        private enum Error {
            errorA,
            errorB,
            errorC,
            errorD
        };

        @Test
        @DisplayName("append example 1")
        void t0() {
            Trie<Error> a = trie(
                    Arrays.asList(Error.errorA),
                    new HashMap<>());

            Trie<Error> b = trie(
                    Arrays.asList(Error.errorB),
                    new HashMap<>());

            append(a, b);
        }

        @Test
        @DisplayName("append example 2")
        void t1() {
            Trie<Error> a = trie(
                    Arrays.asList(Error.errorA),
                    Maps.newHashMap(
                            "x",
                            trie(
                                    Arrays.asList(Error.errorC),
                                    new HashMap<>())));

            Trie<Error> b = trie(
                    Arrays.asList(Error.errorB),
                    new HashMap<>());

            append(a, b);
        }

        @Test
        @DisplayName("append example 3")
        void t2() {
            Trie<Error> a = trie(
                    Arrays.asList(Error.errorA),
                    Maps.newHashMap(
                            "x",
                            trie(
                                    Arrays.asList(Error.errorC),
                                    new HashMap<>())));

            Trie<Error> b = trie(
                    Arrays.asList(Error.errorB),
                    Maps.newHashMap(
                            "x",
                            trie(
                                    Arrays.asList(Error.errorD),
                                    new HashMap<>())));

            append(a, b);
        }
    }

    @Nested
    @DisplayName("toMap")
    static class ToMap {

        @Test
        @DisplayName("Some stuff")
        void t0() {
            Trie<Append.Error> a = trie(
                    Arrays.asList(Append.Error.errorA),
                    Maps.newHashMap(
                            "x",
                            trie(
                                    Arrays.asList(Append.Error.errorC),
                                    Maps.newHashMap(
                                            "y",
                                            trie(
                                                    Arrays.asList(Append.Error.errorB),
                                                    Maps.newHashMap(
                                                            "z",
                                                            trie(
                                                                    Arrays.asList(Append.Error.errorD),
                                                                    new HashMap<>())))))));

            Trie<Append.Error> b = trie(
                    Arrays.asList(Append.Error.errorB),
                    Maps.newHashMap(
                            "x",
                            trie(
                                    Arrays.asList(Append.Error.errorD),
                                    new HashMap<>())));

            toMap(append(a, b));
        }

    }

}
