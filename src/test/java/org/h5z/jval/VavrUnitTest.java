package org.h5z.jval;

import io.vavr.control.Validation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.jval.Keyed.every;
import static org.h5z.jval.Keyed.keyed;
import static org.h5z.jval.Vavr.monadic;
import static org.h5z.jval.Validators.integer;
import static org.junit.jupiter.api.Assertions.*;

class VavrUnitTest {

    @Nested
    @DisplayName("Monadic")
    class Monadic {

        Core.Validator<String, String> validator = integer(() -> "Not a valid value");

        @Nested
        @DisplayName("With Core.Validator")
        class WithCoreValidator {

            @Test
            @DisplayName("Should return a succeed validation if valid")
            public void t() {
                String value = "1";

                Validation<List<String>, Integer> result = monadic(validator)
                    .apply(value)
                    .map(Integer::parseInt);

                assertAll(
                    () -> result.isValid(),
                    () -> assertThat(result.get()).isEqualTo(1)
                );
            }

            @Test
            @DisplayName("Should return a failed validation if valid")
            public void t2() {
                String value = "a";

                Validation<List<String>, Integer> result = monadic(validator)
                    .apply(value)
                    .map(Integer::parseInt);

                assertAll(
                    () -> result.isInvalid(),
                    () -> assertThat(result.getError()).contains("Not a valid value")
                );
            }

        }

        @Nested
        @DisplayName("With Keyed.KeyedValidator")
        class WithKeyedValidator {

            Keyed.KeyedValidator<String, String, String> everyInteger = every(
                keyed("x", integer(() -> "Not a valid value")),
                keyed("y", integer(() -> "Not a valid value"))
            );

            @Test
            @DisplayName("Should return a succeed validation if valid")
            public void t() {
                String value = "1";

                Validation<Map<String, List<String>>, Integer> result = monadic(everyInteger)
                    .apply(value)
                    .map(Integer::parseInt);

                assertAll(
                    () -> result.isValid(),
                    () -> assertThat(result.get()).isEqualTo(1)
                );
            }

            @Test
            @DisplayName("Should return a failed validation if valid")
            public void t2() {
                String value = "a";
                Validation<Map<String, List<String>>, Integer> result = monadic(everyInteger)
                    .apply(value)
                    .map(Integer::parseInt);

                assertAll(
                    () -> result.isInvalid(),
                    () -> assertThat(result.getError()).containsKeys("x", "y")
                );
            }

        }
    }

}