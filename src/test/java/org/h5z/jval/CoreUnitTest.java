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

}
