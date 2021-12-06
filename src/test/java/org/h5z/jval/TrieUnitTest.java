package org.h5z.jval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.jval.TreeModule.get;
import static org.h5z.jval.TreeModule.isValid;
import static org.h5z.jval.TreeModule.trie;
import static org.organicdesign.fp.StaticImports.map;
import static org.organicdesign.fp.StaticImports.tup;
import static org.organicdesign.fp.StaticImports.vec;

import org.h5z.jval.TreeModule.Trie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.organicdesign.fp.oneOf.Option;

public class TrieUnitTest {

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
        @DisplayName("Returns the node at given key in the given trie if it exists")
        void t0() {
            Trie<String> root = trie(
                    vec(),
                    map(tup("x", trie(
                            vec(),
                            map(tup("1", trie(
                                    vec(),
                                    map()))))),
                            tup("y", trie(
                                    vec(),
                                    map(tup("2", trie(
                                            vec(),
                                            map()))))),
                            tup("z", trie(
                                    vec(),
                                    map(tup("3", trie(
                                            vec(),
                                            map())))))));

            Option<Trie<Object>> expected = Option.some(trie(
                    vec(),
                    map()));

            assertThat(get(vec("y", "2"), root)).isEqualTo(expected);
        }

        @Test
        @DisplayName("Returns an Option.none if no node exists in the trie with the given key")
        void t1() {
            Trie<Object> root = trie(
                vec(),
                map(tup("x", trie(
                            vec(), map()))));

            assertThat(get(vec("x", "y"), root)).isEqualTo(Option.none());
                            
        }

    }

    /*
     * @Nested
     * 
     * @DisplayName("append")
     * static class Append {
     * 
     * private enum Error {
     * errorA,
     * errorB,
     * errorC,
     * errorD
     * };
     * 
     * @Test
     * 
     * @DisplayName("append example 1")
     * void t0() {
     * Trie<Error> a = trie(
     * vec(Error.errorA),
     * map());
     * 
     * Trie<Error> b = trie(
     * vec(Error.errorB),
     * map());
     * 
     * append(a, b);
     * }
     * 
     * @Test
     * 
     * @DisplayName("append example 2")
     * void t1() {
     * Trie<Error> a = trie(
     * vec(Error.errorA),
     * map(tup("x", trie(
     * vec(Error.errorC),
     * map()))));
     * 
     * Trie<Error> b = trie(
     * vec(Error.errorB),
     * map());
     * 
     * append(a, b);
     * }
     * 
     * @Test
     * 
     * @DisplayName("append example 3")
     * void t2() {
     * Trie<Error> a = trie(
     * vec(Error.errorA),
     * map(tup("x", trie(
     * vec(Error.errorC),
     * map()))));
     * 
     * Trie<Error> b = trie(
     * vec(Error.errorB),
     * map(tup("x", trie(
     * vec(Error.errorD),
     * map()))));
     * 
     * append(a, b);
     * }
     * }
     */
    /*
     * @Nested
     * 
     * @DisplayName("toMap")
     * static class ToMap {
     * 
     * @Test
     * 
     * @DisplayName("Some stuff")
     * void t0() {
     * Trie<Append.Error> a = trie(
     * Arrays.asList(Append.Error.errorA),
     * map(
     * "x",
     * trie(
     * Arrays.asList(Append.Error.errorC),
     * map(
     * "y",
     * trie(
     * Arrays.asList(Append.Error.errorB),
     * map(
     * "z",
     * trie(
     * vec(Append.Error.errorD),
     * map())))))));
     * 
     * Trie<Append.Error> b = trie(
     * Arrays.asList(Append.Error.errorB),
     * Maps.newHashMap(
     * "x",
     * trie(
     * vec(Append.Error.errorD),
     * map())));
     * 
     * toMap(append(a, b));
     * }
     * 
     * }
     */

}
