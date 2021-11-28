package org.h5z.jval;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.jval.Core.isInvalid;
import static org.h5z.jval.Validators.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.h5z.jval.Core.Validator;

import java.util.List;

public class ValidatorsUnitTest {

    @Nested
    @DisplayName("Required")
    class Required {

        @Test
        @DisplayName("Should fail if the value is null")
        public void test() {
            List<String> result = notNull(() -> "Required").apply(null);

            assertAll(
                () -> assertThat(isInvalid(result)).isTrue(),
                () -> assertThat(result.size()).isEqualTo(1),
                () -> assertThat(result.get(0)).isEqualTo("Required")
            );
        }
    }

}
