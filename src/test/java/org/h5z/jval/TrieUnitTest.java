package org.h5z.jval;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.jval.TreeModule.*;

public class TrieUnitTest {

    public static final List<String> NO_ERROR = Collections.emptyList();
    public static final Map<String, Trie<String>> NO_CHILD = Collections.emptyMap();

    @Nested
    @DisplayName("isValid")
    class IsValid{

        @Test
        @DisplayName("Returns true if tree has no error and all of its children are valid") void t0() {
            Trie<String> validation = tree(
                NO_ERROR,
                Maps.newHashMap(
                    "x",
                    tree(
                        NO_ERROR,
                        NO_CHILD
                    )
                ));

            assertThat(isValid(validation)).isTrue();
        }

        @Test
        @DisplayName("Returns false if tree has an error and all of its children are valid") void t1() {
            Trie<String> validation = tree(
                Arrays.asList("This is an error"),
                Maps.newHashMap(
                    "x",
                    tree(
                        NO_ERROR,
                        NO_CHILD
                    )
                ));

            assertThat(isInvalid(validation)).isTrue();
        }

    }

    @Nested
    @DisplayName("get")
    class Get {

        @Test
        @DisplayName("Should do something") void t0() {
            Trie<String> root = tree(
                Arrays.asList("This is an error"),
                Maps.newHashMap(
                    "x",
                    tree(
                        NO_ERROR,
                        Maps.newHashMap(
                            "1",
                            tree(
                                NO_ERROR,
                                NO_CHILD
                            )
                        )
                    )
                ));

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
        @DisplayName("append example 1") void t0() {
            Trie<Error> a = tree(
                Arrays.asList(Error.errorA),
                new HashMap<>()
            );

            Trie<Error> b = tree(
                Arrays.asList(Error.errorB),
                new HashMap<>()
            );

            append(a, b);
        }

        @Test
        @DisplayName("append example 2") void t1() {
            Trie<Error> a = tree(
                Arrays.asList(Error.errorA),
                Maps.newHashMap(
                    "x",
                    tree(
                        Arrays.asList(Error.errorC),
                        new HashMap<>()
                    )
                )
            );

            Trie<Error> b = tree(
                Arrays.asList(Error.errorB),
                new HashMap<>()
            );

            append(a, b);
        }

        @Test
        @DisplayName("append example 3") void t2() {
            Trie<Error> a = tree(
                Arrays.asList(Error.errorA),
                Maps.newHashMap(
                    "x",
                    tree(
                        Arrays.asList(Error.errorC),
                        new HashMap<>()
                    )
                )
            );

            Trie<Error> b = tree(
                Arrays.asList(Error.errorB),
                Maps.newHashMap(
                    "x",
                    tree(
                        Arrays.asList(Error.errorD),
                        new HashMap<>()
                    )
                )
            );

            append(a, b);
        }
    }

    @Nested
    @DisplayName("toMap")
    static class ToMap {

        @Test
        @DisplayName("Some stuff") void t0() {
            Trie<Append.Error> a = tree(
                Arrays.asList(Append.Error.errorA),
                Maps.newHashMap(
                    "x",
                    tree(
                        Arrays.asList(Append.Error.errorC),
                        Maps.newHashMap(
                            "y",
                            tree(
                                Arrays.asList(Append.Error.errorB),
                                Maps.newHashMap(
                                    "z",
                                    tree(
                                        Arrays.asList(Append.Error.errorD),
                                        new HashMap<>()
                                    )
                                )
                            )
                        )
                    )
                )
            );

            Trie<Append.Error> b = tree(
                Arrays.asList(Append.Error.errorB),
                Maps.newHashMap(
                    "x",
                    tree(
                        Arrays.asList(Append.Error.errorD),
                        new HashMap<>()
                    )
                )
            );

            toMap(append(a, b));
        }

    }

}
