package org.h5z.fval4j.defaults;

import java.util.Map;

import org.h5z.fval4j.Core.Validator;
import org.h5z.fval4j.DefaultErrors.ValidationError;

public final class ObjectValidators {
    
    private ObjectValidators() {
        throw new IllegalAccessError("Cannot be instanciated");
    }

    public static <T, E> Validator<Object, ValidationError> a(Class<?> clazz, Validator<T, ValidationError> validator) {
        return org.h5z.fval4j.validators.ObjectValidators.a(clazz, validator, _v -> ValidationError.error("NotInstanceOf"));
    }

    public static <E> Validator<Object, ValidationError> string(Validator<String, ValidationError> validator) {
        return org.h5z.fval4j.validators.ObjectValidators.string(validator, _v -> ValidationError.error("NotAString"));
    }

    public static <E> Validator<Object, ValidationError> integer(Validator<Integer, ValidationError> validator) {
        return org.h5z.fval4j.validators.ObjectValidators.integer(validator, _v -> ValidationError.error("NotAnInteger"));
    }

    public static <K, T, E> Validator<Object, ValidationError> map(Validator<Map<K, T>, ValidationError> validator) {
        return org.h5z.fval4j.validators.ObjectValidators.map(validator, _v -> ValidationError.error("NotAMap"));
    }

}
