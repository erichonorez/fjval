package org.h5z.jval;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.jval.TreeModule.Path.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@DisplayName("TreeModule")
class TreeModuleUnitTest {

    @Nested
    @DisplayName("Path")
    class PathUnitTest {

        @Nested
        @DisplayName("ancestor")
        class Compare {
            String empty = "";

            BinaryOperator<String> concatFn = (acc, b) -> acc + "." + b;

            Function<String, BiFunction<TreeModule.Path, TreeModule.Path, String>> fmtMessageFn =
                m -> (a, b) -> {
                    String foldA = fold(a, empty, concatFn);
                    String foldB = fold(b, empty, concatFn);
                    return foldA + " has no ancestors with " + foldB;
            };

            @DisplayName("it should return an empty list if paths have no ancestors")
            @TestFactory
            Collection<DynamicTest> noAncestor() {
                BiFunction<TreeModule.Path, TreeModule.Path, String> messageFmt =
                    fmtMessageFn.apply("has no ancestors with");
                return Arrays.asList(
                    new TreeModule.Path[] {
                        path("org", "h5z", "jval"),
                        path("io", "spring", "starter")
                    },
                    new TreeModule.Path[] {
                        path("org", "h5z", "jval"),
                        path("io", "h5z", "jval")
                    }
                ).stream().map(paths ->
                    dynamicTest(
                        messageFmt.apply(paths[0], paths[1]),
                        () -> assertThat(ancestors(paths[0], paths[1]).getParts()).isEmpty())
                ).collect(Collectors.toList());
            }

            @DisplayName("it should return an list of ancestors if paths have some")
            @TestFactory
            Collection<DynamicTest> withAncestor() {
                BiFunction<TreeModule.Path, TreeModule.Path, String> messageFmt =
                    fmtMessageFn.apply("has ancestors with");
                return Arrays.asList(
                    new TreeModule.Path[] {
                        path("org", "h5z", "jval"),
                        path("org", "h5z", "starter"),
                        path("org", "h5z")
                    },
                    new TreeModule.Path[] {
                        path("org", "h5z", "jval"),
                        path("org", "h5z", "jval"),
                        path("org")
                    },
                    new TreeModule.Path[] {
                        path("org", "h5z", "jval"),
                        path("org", "h5z", "jval"),
                        path("org", "h5z", "jval")
                    }
                ).stream().map(paths ->
                    dynamicTest(messageFmt.apply(paths[0], paths[1]), () ->
                        assertThat(ancestors(paths[0], paths[1]).getParts())
                            .containsAll(paths[2].getParts()))
                ).collect(Collectors.toList());
            }
        }

    }

}