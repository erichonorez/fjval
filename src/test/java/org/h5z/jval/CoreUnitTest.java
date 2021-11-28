package org.h5z.jval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.jval.Core.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.*;

public class CoreUnitTest {

    @Nested
    @DisplayName("valid")
    class Valid {

        @Test
        @DisplayName("Returns an empty list for a given value") void t0() {
            List<?> result = valid("this string is valid");
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Returns an empty list even when the given value is null") void t1() {
            List<?> result = valid(null);
            assertThat(result).isEmpty();
        }

    }

    @Nested
    @DisplayName("invalid")
    class Invalid {

        @Test
        @DisplayName("Returns a list with the given error") void t0() {
            List<String> result = invalid("This is an error");
            assertAll(
                      () -> assertThat(result).hasSize(1),
                      () -> assertThat(result).contains("This is an error")
            );
        }

        @Test
        @DisplayName("Returns a list with an null value if the error is null") void t1() {
            List<?> result = invalid(null);
            assertAll(
                      () -> assertThat(result).hasSize(1),
                      () -> assertThat(result.get(0)).isNull()
            );

        }

        @Nested
        class WithErrorHierarchy {

            @Test
            @DisplayName("Returns a list with a child class error") void t0() {
                SpecificError e = new SpecificError();
                List<Error> result = invalid(e);
                assertThat(result).containsExactly(e);
            }

            private abstract class Error { }
            private class SpecificError extends Error { }

        }

    }

    @Nested
    @DisplayName("isValid")
    class IsValid {

        @Test
        @DisplayName("Returns true if the given list is empty") void t0() {
            assertThat(isValid(valid("A valid value"))).isTrue();
        }

        @Test
        @DisplayName("Returns false if the given value is null") void t1() {
            assertThat(isValid(null)).isFalse();
        }

        @Test
        @DisplayName("Returns false if the given list is not empty") void t2() {
            assertThat(isValid(invalid("An error"))).isFalse();
        }

    }

    @Nested
    @DisplayName("isInvalid")
    class IsInvalid {

        @Test
        @DisplayName("Returns true if the given list is not empty") void t0() {
            assertThat(isInvalid(invalid("This is an error"))).isTrue();
        }

        @Test
        @DisplayName("Returns false if the given list is null") void t1() {
            assertThat(isInvalid(null)).isFalse();
        }

        @Test
        @DisplayName("Returns false if the given list is empty") void t2() {
            assertThat(isInvalid(valid("A valid value"))).isFalse();
        }

    }

    @Nested
    @DisplayName("sequentially")
    class Sequentially {

        Validator<Integer, String> gt0
            = v -> v > 0 ? valid(v) : invalid("Should be greater than 0");
        Validator<Integer, String> lt10
            = v -> v < 10 ? valid(v) : invalid("Should be lower than 10");
        Validator<Integer, String> between1And9 = sequentially(gt0, lt10);

        @TestFactory
        @DisplayName("Returns an empty list if all validators succeeded") List<DynamicTest> t0() {
            return IntStream.range(1, 10)
                .mapToObj(i -> dynamicTest(String.format("Value [%d]", i), () -> {
                            List<String> result = between1And9.apply(i);
                            assertThat(isValid(result)).isTrue();
                        }))
                .collect(Collectors.toList());
        }

        @TestFactory
        @DisplayName("Returns an the error of the first failed validator") List<DynamicTest> t1() {
            return Arrays.asList(
                dynamicTest("First validator should fail with [0]", () -> {
                    List<String> result = between1And9.apply(0);
                    assertThat(result).contains("Should be greater than 0");
                }),
                dynamicTest("Second validator should fail with [10]", () -> {
                    List<String> result = between1And9.apply(10);
                    assertThat(result).contains("Should be lower than 10");
                })
            );
        }

    }

    @Nested
    @DisplayName("every")
    class Every {

        Validator<Integer, String> gt0
            = v -> v > 0 ? valid(v) : invalid("gt0");
        Validator<Integer, String> gt5
            = v -> v > 5 ? valid(v) : invalid("gt5");
        Validator<Integer, String> gt0And5 = every(gt0, gt5);

        @Test
        @DisplayName("Returns an empty list of all validators succeeded") void t0() {
            assertThat(isValid(gt0And5.apply(6))).isTrue();
        }

        @Test
        @DisplayName("Returns collected errors of the failed validators") void t1() {
            assertThat(gt0And5.apply(0)).containsExactly(
                "gt0",
                "gt5"
            );
        }

        @Test
        @DisplayName("Executes all validators") void t2() {
            assertThat(gt0And5.apply(2)).containsExactly(
                "gt5"
            );
        }

    }

    @Nested
    @DisplayName("any")
    class Any {

        Validator<String, String> eqA = v -> "a".equals(v) ? valid(v) : invalid("not a");
        Validator<String, String> eqB = v -> "b".equals(v) ? valid(v) : invalid("not b");
        Validator<String, String> eqAOrB = any(eqA, eqB);

        @Test
        @DisplayName("Returns an empty list of all validators succeeded") List<DynamicTest> t0() {
            return Arrays.asList("a", "b")
                .stream()
                .map(v -> dynamicTest(String.format("Value: [%s]", v), () -> assertThat(isValid(eqAOrB.apply(v))).isTrue()))
                .collect(Collectors.toList());
        }

        @Test
        @DisplayName("Returns collected errors of the failed validators") void t1() {
            assertThat(eqAOrB.apply("c")).containsExactly(
                "not a",
                "not b"
            );
        }

    }

    @Nested
    @DisplayName("required")
    class Required {

        Validator<String, String> eqA = v -> "a".equals(v) ? valid(v) : invalid("not a");
        Validator<String, String> requiredEqA = required(eqA, () -> "required");

        @Test
        @DisplayName("Returns an empty list if the validated value is not null and pass the given validator") void t0() {
            assertThat(requiredEqA.apply("a")).satisfies(Core::isValid);
        }

        @Test
        @DisplayName("Returns the errors of the given validator if the value is not null but is not valid") void t1() {
            List<String> result = requiredEqA.apply("b");
            assertAll(
                () -> assertThat(result).satisfies(Core::isInvalid),
                () -> assertThat(result).containsExactly("not a")
            );
        }

        @Test
        @DisplayName("Returns the error provided by the given supplier if the validated value is null") void t2() {
            List<String> result = requiredEqA.apply(null);
            assertAll(
                () -> assertThat(result).satisfies(Core::isInvalid),
                () -> assertThat(result).containsExactly("required")
            );
        }

    }

    @Nested
    @DisplayName("optional")
    class Optional {

        Validator<String, String> eqA = v -> "a".equals(v) ? valid(v) : invalid("not a");
        Validator<String, String> optionalEqA = optional(eqA);

        @Test
        @DisplayName("Returns an empty list if the validated value is null") void t0() {
            assertThat(optionalEqA.apply(null)).satisfies(Core::isValid);
        }

        @Test
        @DisplayName("Returns an empty list if the validated value is valid") void t1() {
            assertThat(optionalEqA.apply("a")).satisfies(Core::isValid);
        }

        @Test
        @DisplayName("Returns the errors of the given validator if the value is not null but is not valid") void t2() {
            List<String> result = optionalEqA.apply("b");
            assertAll(
                () -> assertThat(result).satisfies(Core::isInvalid),
                () -> assertThat(result).containsExactly("not a")
            );
        }

    }

    @Nested
    @DisplayName("list")
    class CoreList {

        Validator<String, String> eqA =
            v -> "a".equals(v) ? valid(v) : invalid("not a");
        Validator<List<String>, String> listOfA = list(eqA);

        @Test
        @DisplayName("Returns a empty list if all the elements of the validated list pass the given validator") void t0() {
            assertThat(listOfA.apply(Arrays.asList("a", "a"))).satisfies(Core::isValid);
        }

        @Test
        @DisplayName("Returns the flattened collected errors") void t1() {
            List<String> result = listOfA.apply(Arrays.asList("b", "c"));
            assertThat(result).containsExactly(
                "not a",
                "not a"
            );
        }

    }

}
