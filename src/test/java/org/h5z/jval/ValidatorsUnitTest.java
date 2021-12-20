package org.h5z.jval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.jval.Core.isInvalid;
import static org.h5z.jval.Validators.required;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ValidatorsUnitTest {

    @Nested
    @DisplayName("Required")
    class Required {

        @Test
        @DisplayName("Should fail if the value is null")
        public void test() {
            List<String> result = required(() -> "Required").apply(null);

            assertAll(
                () -> assertThat(isInvalid(result)).isTrue(),
                () -> assertThat(result.size()).isEqualTo(1),
                () -> assertThat(result.get(0)).isEqualTo("Required")
            );
        }
    }

}
