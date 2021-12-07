package org.h5z.jval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.jval.TreeModule.get;
import static org.h5z.jval.TreeModule.isValid;
import static org.h5z.jval.TreeModule.merge;
import static org.h5z.jval.TreeModule.toMap;
import static org.h5z.jval.TreeModule.trie;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.organicdesign.fp.StaticImports.map;
import static org.organicdesign.fp.StaticImports.tup;
import static org.organicdesign.fp.StaticImports.vec;

import java.util.List;

import org.h5z.jval.TreeModule.Trie;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.organicdesign.fp.collections.ImList;
import org.organicdesign.fp.collections.ImMap;
import org.organicdesign.fp.oneOf.Option;
import org.organicdesign.fp.tuple.Tuple2;
import org.organicdesign.fp.tuple.Tuple3;

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

    @Nested
    @DisplayName("merge")
    class Merge {

        @TestFactory
        @DisplayName("Returns the merged tries")
        List<DynamicTest> t0() {
            @NotNull
            ImList<Tuple2<String, Tuple3<Trie<String>, Trie<String>, Trie<String>>>> testCases = vec(
                    tup(
                            "Two tries with errors but without children", 
                            tup(
                                    trie(vec("error a", "error b"), map()), // a
                                    trie(vec("error d", "error e"), map()), // b
                                    trie(vec("error a", "error b", "error d", "error e"), map()))), // expected
                    tup(
                            "Two empty trie",
                            tup(
                                    trie(vec(), map()), // a
                                    trie(vec(), map()), // be
                                    trie(vec(), map()) // result
                            )),
                    tup(
                            "Two tries with children and errors without common keys", 
                            tup(
                                trie(vec(), map(
                                    tup("x", trie(vec("error a", "error b"), map()))
                                )), // a,
                                trie(vec(), map(
                                    tup("y", trie(vec("error c", "error d"), map()))
                                )), // b
                                trie(vec(), map(
                                    tup("x", trie(vec("error a", "error b"), map())),
                                    tup("y", trie(vec("error c", "error d"), map()))
                                )) // expected
                    )),
                    tup(
                        "Two tries with children and errors with common keys",
                        tup(
                            trie(vec(), map(
                                    tup("x", trie(vec("error a", "error b"), map()))
                                )), // a,
                                trie(vec(), map(
                                    tup("y", trie(vec("error c", "error d"), map())),
                                    tup("x", trie(vec("error e", "error f"), map()))
                                )), // b
                                trie(vec(), map(
                                    tup("x", trie(vec("error a", "error b", "error e", "error f"), map())),
                                    tup("y", trie(vec("error c", "error d"), map()))
                                )) // expected
                        )
                    ));

            return testCases.map(tc -> {
                String label = tc._1();
                Trie<String> a = tc._2()._1();
                Trie<String> b = tc._2()._2();
                Trie<String> expected = tc._2()._3();

                return dynamicTest(label, () -> assertThat(merge(a, b)).isEqualTo(expected));
            }).toImList();
        }

    }

    @Nested
    @DisplayName("toMap")
    class ToMap {

        @TestFactory
        @DisplayName("Returns a map representation of the trie")
        List<DynamicTest> t0() {
            @NotNull
            ImList<Tuple3<String, Trie<String>, ImMap<String, ImList<String>>>> testCases = vec(
                tup(
                    "a trie with error and children",
                    trie(
                        vec("error A", "error B"),
                        map(
                            tup("x", trie(
                                vec("error C", "error D"),
                                map(
                                    tup("1", trie(
                                        vec("error E"),
                                        map()))
                                )
                            )),
                            tup("y", trie(
                                vec("error F"),
                                map()))
                        )
                    ),
                    map(
                        tup("", vec("error A", "error B")),
                        tup("x", vec("error C", "error D")),
                        tup("x.1", vec("error E")),
                        tup("y", vec("error F"))
                    )
                ),
                tup(
                    "an empty trie",
                    trie(vec(), map()),
                    map(tup("", vec()))
                )
            );

            return testCases.map(tc -> {
                String label = tc._1();
                Trie<String> trie = tc._2();
                ImMap<String, ImList<String>> expected = tc._3();

                return dynamicTest(label, () -> assertThat(toMap(trie).equals(expected)).isTrue());
            }).toImList();
        }

    }

}
