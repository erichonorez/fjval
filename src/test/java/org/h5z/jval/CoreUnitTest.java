package org.h5z.jval;

import org.h5z.jval.Core.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.jval.Core.*;
import static org.h5z.jval.Validators.*;
import static org.junit.jupiter.api.Assertions.*;

public class CoreUnitTest {

    @Test
    public void anonymous() {
        Validator<Coord, String> xGtY = c -> {
            if (c.x <= c.y) {
                return Core.fail("Error");
            }
            return success(c);
        };
    }

    @Test
    public void not_shouldReturnTheNegation() {
        Validator<Integer, String> one = eq(1, () -> "shoult eq to 1");
        Validator<Integer, String> notOne = not(one, () -> "should not be 1");
        assertTrue(failed(notOne.apply(1)));
    }

    @Test
    public void prop1() {
        Validator<Coord, String> prop = prop(Coord::x, gt(2, () -> "X should be greater than 2"));
        assertFalse(succeed(prop.apply(new Coord(1, 0))));

    }

    @Test
    public void prop2() {
        Validator<Coord, String> prop = every(
            prop(Coord::x, gt(2, () -> "X should be greater than 2")),
            prop(Coord::y, eq(0, () -> "Y should be equal to 0")),
            cond((c) -> c.x > c.y, () -> "X should be greater than Y"));

        assertFalse(succeed(prop.apply(new Coord(1, 0))));

    }

    @Nested
    @DisplayName("Required")
    class Required {

        @Test
        @DisplayName("Should fail if the value is null")
        public void test() {
            Validator<Integer, String> validator = required(
                gt(1, () -> "Should be greater than 1"),
                () -> "Required"
            );
            List<String> result = validator.apply(null);

            assertAll(
                () -> assertThat(failed(result)).isTrue(),
                () -> assertThat(result.size()).isEqualTo(1),
                () -> assertThat(result.get(0)).isEqualTo("Required")
            );
        }
    }

    @Nested
    @DisplayName("Optional")
    class Optional {

        @Test
        @DisplayName("Should pass if the valud is null")
        public void test() {
            Validator<Integer, String> optional = optional(
                gt(1, () -> "Should be greater than 1")
            );

            List<String> result = optional.validate(null);

            assertThat(failed(result)).isFalse();
        }

        @Test
        @DisplayName("Should execute other validator if value is not null")
        public void test2() {
            Validator<Integer, String> optional = optional(
                gt(1, () -> "Should be greater than 1")
            );

            List<String> result = optional.apply(0);

            assertThat(failed(result)).isTrue();
        }

    }

    private static class Coord {
        public final int x;
        public final int y;

        public Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int x() {
            return this.x;
        }

        public int y() {
            return this.y;
        }
    }
}
