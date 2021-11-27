package org.h5z.jval;

import fj.data.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.jval.FunctionalJava.monadic;
import static org.h5z.jval.Validators.integer;
import static org.junit.jupiter.api.Assertions.*;

class FunctionalJavaUnitTest {

    @Nested
    @DisplayName("Monadic")
    class Monadic {

        @Test
        @DisplayName("Should return a succeed validation if valid")
        public void t() {
            String value = "1";
            Core.Validator<String, String> validator = integer(() -> "Not a valid value");

            Validation<List<String>, Integer> result = monadic(validator)
                .apply(value)
                .map(Integer::parseInt);

            assertAll(
                () -> result.isSuccess(),
                () -> assertThat(result.success()).isEqualTo(1)
            );
        }

        @Test
        @DisplayName("Should return a failed validation if valid")
        public void t2() {
            String value = "a";
            Core.Validator<String, String> validator = integer(() -> "Not a valid value");

            Validation<List<String>, Integer> result = monadic(validator)
                .apply(value)
                .map(Integer::parseInt);

            assertAll(
                () -> result.isFail(),
                () -> assertThat(result.fail()).contains("Not a valid value")
            );
        }

    }

}