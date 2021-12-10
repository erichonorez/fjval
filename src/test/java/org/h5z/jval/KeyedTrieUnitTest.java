package org.h5z.jval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.jval.KeyedTrie.every;
import static org.h5z.jval.KeyedTrie.keyed;
import static org.h5z.jval.KeyedTrie.list;
import static org.h5z.jval.KeyedTrie.prop;
import static org.h5z.jval.KeyedTrie.sequentially;
import static org.h5z.jval.TreeModule.trie;
import static org.h5z.jval.Validators.gt;
import static org.organicdesign.fp.StaticImports.map;
import static org.organicdesign.fp.StaticImports.tup;
import static org.organicdesign.fp.StaticImports.vec;

import org.h5z.jval.Core.Validator;
import org.h5z.jval.KeyedTrie.KeyedValidator;
import org.h5z.jval.TreeModule.Trie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
                        .isEqualTo(trie(vec(), map(
                            tup("x", trie(vec("Does not contains a"), map())))));
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
                        .isEqualTo(trie(vec(), map(
                                    tup("y", trie(vec(), map(
                                        tup("x", trie(vec("Does not contains a"), map()))))))));
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

    @Nested
    @DisplayName("prop")
    static class Prop {

        Validator<Integer, String> gt0 = v -> v > 0 ? Core.valid(v) : Core.invalid("gt0");
        KeyedValidator<RootClass, String> rootValidator = keyed("x", 
                prop(RootClass::getX, keyed("y",
                    Core.prop(NestedClass::getY, gt0))));

        @Test
        @DisplayName("Returns a valid trie if the given validator succeeded")
        void t0() {
            assertThat( 
                rootValidator.apply(new RootClass(new NestedClass(1)))
            ).isEqualTo(trie(vec(), map(tup("x", trie(vec(), map(tup("y", trie(vec(), map()))))))));
        }

        @Test
        @DisplayName("Returns the errors return by the given validator otherwise")
        void t1() {
            assertThat(
                rootValidator.apply(new RootClass(new NestedClass(0)))
            ).isEqualTo(trie(vec(), map(
                tup("x", trie(vec(), map(
                    tup("y", trie(vec("gt0"), map()))))))));
                
        }

        static class RootClass {
            private final NestedClass x;

            RootClass(NestedClass x) {
                this.x = x;
            }

            public NestedClass getX() {
                return this.x;
            }
        }

        static class NestedClass {
            private final int y;

            NestedClass(int y) {
                this.y = y;
            }

            public int getY() {
                return this.y;
            }
        }

    }

    @Nested
    @DisplayName("list")
    class List {

        @Test
        @DisplayName("Retuns a valid trie if all the elements in the list are valid") void t0() {
            var listValidator = list(gt(0, () -> "Should be gt 0"));
            assertThat(listValidator.apply(vec(1, 2, 3, 4, 5)))
                .isEqualTo(trie(vec(), map(
                    tup("0", trie(vec(), map())),
                    tup("1", trie(vec(), map())),
                    tup("2", trie(vec(), map())),
                    tup("3", trie(vec(), map())),
                    tup("4", trie(vec(), map()))
                )));
        }

        @Test
        @DisplayName("Return a trie with the errors of all failed validators") void t1() {
            var listValidator = list(gt(0, () -> "Should be gt 0"));
            assertThat(listValidator.apply(vec(0, 0, 0, 0, 0)))
                .isEqualTo(trie(vec(), map(
                    tup("0", trie(vec("Should be gt 0"), map())),
                    tup("1", trie(vec("Should be gt 0"), map())),
                    tup("2", trie(vec("Should be gt 0"), map())),
                    tup("3", trie(vec("Should be gt 0"), map())),
                    tup("4", trie(vec("Should be gt 0"), map()))
                )));
        }

    }

}
