package org.h5z.jval;

import org.h5z.jval.Core.Validator;
import org.junit.jupiter.api.Test;

import static org.h5z.jval.Core.*;
import static org.h5z.jval.Validators.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CoreUnitTest {

    @Test
    public void anonymous() {
        Validator<Coord, String> xGtY = c -> {
            if (c.x <= c.y) {
                return invalid("Error");
            }
            return valid(c);
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
        assertFalse(isValid(prop.apply(new Coord(1, 0))));

    }

    @Test
    public void prop2() {
        Validator<Coord, String> prop = every(
            prop(Coord::x, gt(2, () -> "X should be greater than 2")),
            prop(Coord::y, eq(0, () -> "Y should be equal to 0")),
            cond((c) -> c.x > c.y, () -> "X should be greater than Y"));

        assertFalse(isValid(prop.apply(new Coord(1, 0))));

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
