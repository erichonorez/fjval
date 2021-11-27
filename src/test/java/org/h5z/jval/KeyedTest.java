package org.h5z.jval;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.jval.Core.*;
import static org.h5z.jval.Keyed.*;
import static org.h5z.jval.Validators.eq;
import static org.h5z.jval.Validators.gt;
import static org.h5z.jval.Validators.cond;
import static org.junit.jupiter.api.Assertions.*;

public class KeyedTest {

    @Test
    public void itShouldValidateAValue() {
        int value = 41;
        Keyed.KeyedValidator<String, Integer, String> validator =
            keyed("value", eq(42, () -> "Not equal to 42"));

        Map<String, List<String>> validated = validator.apply(value);
        assertAll(
            () -> assertTrue(validated.keySet().contains("value")),
            () -> assertEquals("Not equal to 42", validated.get("value"))
        );
    }

    @Test
    public void itShouldValidatedAnOjectWithOneProp() {
        keyed("x", prop(Coords::getX, eq(42, () -> "Should be equal to 42")));
    }

    @Test
    public void itShouldValidateAWholeObject() {
        Keyed.KeyedValidator<String, Coords, String> validator = Keyed.sequentially(
            every(
                keyed("x", prop(Coords::getX, eq(42, () -> "Should be equal to 42"))),
                keyed("y", prop(Coords::getY, gt(1, () -> "Should be equal to 42")))
            ),
            keyed("global", cond(c -> c.x > c.y, () -> "X should be greater than Y"))
        );

        Map<String, List<String>> result = validator.apply(new Coords(42, 43));
        assertAll(
            () -> assertTrue(failed(result)),
            () -> assertTrue(failed(result, "global")),
            () -> assertEquals("X should be greater than Y", failures(result, "global").get(0))
        );
    }

    @Test
    public void itShouldValidateNestedObject() {
        Core.Validator<String, String> stringGt2 = cond(s -> s.length() > 2, () -> "Error");
        KeyedValidator<String, Person, String> personValidator = every(
            keyed("firstName", prop(Person::getFirstName, stringGt2)),
            keyed("lastName", prop(Person::getLastName, stringGt2))
        );

        KeyedValidator<String, Household, String> householdValidator = sequentially(
            keyed("a", Keyed.prop(Household::getA, personValidator)),
            keyed("b", Keyed.prop(Household::getB, personValidator))
        );

        Household household = new Household(
            new Person("John", "Do"),
            new Person("Ambre", "Do")
        );

        Map<String, List<String>> result = householdValidator.apply(household);
    }

    @Test
    public void itShouldValidateAWholeObject_SequentiallyPropertyAndThenGlobal() {
        Keyed.KeyedValidator<String, Coords, String> validator = Keyed.sequentially(
            every(
                keyed("x", prop(Coords::getX, eq(42, () -> "Should be equal to 42"))),
                keyed("y", prop(Coords::getY, gt(1, () -> "Should be gt thane 1")))
            ),
            keyed("global", cond(c -> c.x > c.y, () -> "X should be greater than Y"))
        );

        Map<String, List<String>> result = validator.apply(new Coords(42, 0));
        failures(result, "y");
        assertAll(
            () -> assertTrue(failed(result)),
            () -> assertEquals(1, failures(result).size()),
            () -> assertTrue(failed(result, "y")),
            () -> assertEquals("Should be gt thane 1", failures(result, "y").get(0))
        );
    }

    @Test
    public void itShouldValidatedAMap() {
        KeyedValidator<String, Map<String, Integer>, String> validator = every(
            key("x", eq(42, () -> "Should be equal to 42")),
            key("y", gt(1, () -> "Should be gt thane 1"))
        );

        HashMap<String, Integer> map = new HashMap<String, Integer>() {{
            put("x", 42);
            put("y", 1);
        }};

        Map<String, List<String>> result = validator.apply(map);

        assertAll(
            () -> assertTrue(failed(result)),
            () -> assertEquals(1, failures(result).size()),
            () -> assertTrue(failed(result, "y")),
            () -> assertEquals("Should be gt thane 1", failures(result, "y").get(0))
        );
    }

    private static class Coords {
        private final int x;
        private final int y;

        private Coords(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    public static class Person {
        private final String firstName;
        private final String lastName;

        public Person(String firstName, String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getFirstName() {
            return firstName;
        }
    }

    public static class Household {
        private final Person a;
        private final Person b;

        public Household(Person a, Person b) {
            this.a = a;
            this.b = b;
        }

        public Person getA() {
            return a;
        }

        public Person getB() {
            return b;
        }
    }

}
