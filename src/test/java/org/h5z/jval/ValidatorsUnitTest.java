package org.h5z.jval;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.jval.Core.failed;
import static org.h5z.jval.Validators.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.h5z.jval.Core.Validator;

import java.util.List;

public class ValidatorsUnitTest {

    @Test
    public void gtShoultReturnAnEmptyList() {
        Validator<Integer, String> gtThan5 = gt(5, () -> "The value should be greater than 5");
        assertEquals(0, gtThan5.apply(6).size());
    }

    @Test
    public void gtShouldReturnANonEmptyList() {
        Validator<Integer, String> gtThan5 = gt(5, () -> "The value should be greater than 5");
        assertEquals(1, gtThan5.apply(4).size());
    }

    @Nested
    @DisplayName("Required")
    class Required {

        @Test
        @DisplayName("Should fail if the value is null")
        public void test() {
            List<String> result = notNull(() -> "Required").apply(null);

            assertAll(
                () -> assertThat(failed(result)).isTrue(),
                () -> assertThat(result.size()).isEqualTo(1),
                () -> assertThat(result.get(0)).isEqualTo("Required")
            );
        }
    }

}
