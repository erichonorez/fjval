package org.h5z.jval;

import org.h5z.jval.Core.Validator;
import org.h5z.jval.KeyedTrie.KeyedValidator;
import org.h5z.jval.TreeModule.Trie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.h5z.jval.TreeModule.*;
import static org.h5z.jval.Validators.*;
import static org.h5z.jval.KeyedTrie.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.organicdesign.fp.StaticImports.*;

public class KeyedTrieUnitTest {

    @Nested
    @DisplayName("keyed")
    class Keyed {

        Validator<String, String> containsA = s -> s.contains("a")
                ? Core.valid(s)
                : Core.invalid("Does not contains a");

        @Nested
        @DisplayName("With simple validator")
        class WithValidator {

            @Test
            @DisplayName("Returns a keyed validator")
            void t0() {
                KeyedValidator<String, String> keyedContainsA = keyed("x", containsA);
                Trie<String> validated = keyedContainsA.validate("");

                assertThat(validated)
                        .isEqualTo(
                                trie(vec(),
                                        map(tup("x", trie(vec("Does not contains a"),
                                                map())))));
            }

        }

        @Nested
        @DisplayName("With keyed validator")
        class WithKeyedValidator {

            @Test
            @DisplayName("Returns a keyed validator")
            void t0() {
                var yxContainsA = keyed("y", keyed("x", containsA));
                Trie<String> validated = yxContainsA.validate("");

                assertThat(validated)
                        .isEqualTo(
                                trie(vec(),
                                        map(tup("y",
                                                trie(vec(),
                                                        map(tup("x",
                                                                trie(vec("Does not contains a"), map()))))))));
            }

        }

    }

    @Nested
    @DisplayName("sequentially")
    class Sequentially {

        @Test
        @DisplayName("Returns a valid trie if all validators succeed")
        void t0() {
            KeyedValidator<Integer, String> sequentially = sequentially(
                    keyed("x", gt(0, () -> "Should be gt 0")),
                    keyed("y", gt(0, () -> "Should be gt 0")));

            Trie<String> result = sequentially.apply(1);
            assertThat(result)
                    .isEqualTo(trie(vec(), map(
                            tup("x", trie(vec(), map())),
                            tup("y", trie(vec(), map())))));
        }

        @Test
        @DisplayName("Returns a trie with only the errors of the first failed validator")
        void t1() {
            KeyedValidator<Integer, String> sequentially = sequentially(
                    keyed("x", gt(2, () -> "Should be gt 2")),
                    keyed("y", gt(0, () -> "Should be gt 0")));

            Trie<String> result = sequentially.apply(1);
            assertThat(result)
                    .isEqualTo(trie(vec(), map(
                            tup("x", trie(vec("Should be gt 2"), map())))));
        }

    }

    @Nested
    @DisplayName("every")
    class Every {

        @Test
        @DisplayName("Returns a valid trie if all validators succeeded")
        void t0() {
            KeyedValidator<Integer, String> every = every(
                    keyed("x", gt(0, () -> "Should be gt 0")),
                    keyed("y", gt(0, () -> "Should be gt 0")));

            Trie<String> result = every.apply(1);
            assertThat(result)
                    .isEqualTo(trie(vec(), map(
                            tup("x", trie(vec(), map())),
                            tup("y", trie(vec(), map())))));
        }

        @Test
        @DisplayName("Returns a trie with the errors of all failed validators otherwise.")
        void t1() {
            KeyedValidator<Integer, String> every = every(
                    keyed("a", gt(-1, () -> "Should be gt -1")),
                    keyed("b", gt(1, () -> "Should be gt 1")),
                    keyed("c", gt(2, () -> "Should be gt 2")),
                    keyed("d", gt(3, () -> "Should be gt 3")),
                    keyed("e", gt(-1, () -> "Should be gt -1")));

            Trie<String> result = every.apply(0);
            assertThat(result)
                    .isEqualTo(trie(vec(), map(
                            tup("a", trie(vec(), map())),
                            tup("b", trie(vec("Should be gt 1"), map())),
                            tup("c", trie(vec("Should be gt 2"), map())),
                            tup("d", trie(vec("Should be gt 3"), map())),
                            tup("e", trie(vec(), map())))));
        }

    }

}
