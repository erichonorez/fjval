package org.h5z.jval;

import org.junit.jupiter.api.Test;

import static org.h5z.jval.Validators.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.h5z.jval.JVal.Validator;

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

}
