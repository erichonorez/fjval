package org.h5z.jval;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;

import static  org.h5z.jval.ValidationResult.*;

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

        }
    }

    @Nested
    @DisplayName("append")
    class Append {

        @Test
        @DisplayName("it should merge two singe validation result")
        public void test() {

        }

    }

    @Nested
    @DisplayName("API")
    class API {

        @Test
        @DisplayName("Demo")
        public void test() {
            ValidationResult<String> result = root(
                Collections.emptyList(), // no root errors
                Arrays.asList(           // only property level errors
                    leaf("x", Arrays.asList("Should be greater than 1")),
                    leaf("y", Arrays.asList("Should be greater than 1"))
                )
            );
        }

    }

}