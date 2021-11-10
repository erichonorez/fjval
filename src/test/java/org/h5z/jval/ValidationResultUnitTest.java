package org.h5z.jval;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;



@DisplayName("ValidationResult")
class ValidationResultUnitTest {

    @Nested
    @DisplayName("make")
    class Make {

        @Test
        @DisplayName("should create a single validation result")
        public void test() {
            ValidationResult<List<String>> result = ValidationResult.make(Arrays.asList(
                "This is an error A",
                "This is an error B"
            ));

            Boolean isNode = result.cases(
                n -> Boolean.TRUE,
                l -> Boolean.FALSE
            );

            assertThat(isNode).isTrue();
        }
    }

    @Nested
    @DisplayName("append")
    class Append {

        @Test
        @DisplayName("it should merge two singe validation result")
        public void test() {
            ValidationResult<List<String>> result = ValidationResult.make(Arrays.asList(
                "This is an error A",
                "This is an error B"
            ));

            ValidationResult<List<String>> appended = append(result, result);
            assertThat(result.cases(
                n -> Boolean.TRUE,
                l -> Boolean.FALSE
            )).isTrue();
        }

    }

}