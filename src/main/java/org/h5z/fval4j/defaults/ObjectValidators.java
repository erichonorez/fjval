package org.h5z.fval4j.defaults;

import java.util.Map;

import org.h5z.fval4j.Core.Validator;
import org.h5z.fval4j.DefaultErrors.ValidationError;

public final class ObjectValidators {
    
    private ObjectValidators() {
        throw new IllegalAccessError("Cannot be instanciated");
    }

    public static <T, U, E> Validator<Object, U, ValidationError> a(Class<?> clazz, Validator<T, U, ValidationError> validator) {
        return org.h5z.fval4j.validators.ObjectValidators.a(clazz, validator, _v -> ValidationError.error("NotInstanceOf"));
    }

    public static <E> Validator<Object, String, ValidationError> string(Validator<String, String, ValidationError> validator) {
        return org.h5z.fval4j.validators.ObjectValidators.string(validator, _v -> ValidationError.error("NotAString"));
    }

    public static <E> Validator<Object, Integer, ValidationError> integer(Validator<Integer, Integer, ValidationError> validator) {
        return org.h5z.fval4j.validators.ObjectValidators.integer(validator, _v -> ValidationError.error("NotAnInteger"));
    }

    public static <K, T, U, E> Validator<Object, Map<K, U>, ValidationError> map(Validator<Map<K, T>, Map<K, U>, ValidationError> validator) {
        return org.h5z.fval4j.validators.ObjectValidators.map(validator, _v -> ValidationError.error("NotAMap"));
    }

    public static <K, T, U, E> Validator<Object, U, ValidationError> nestedMap(Validator<Map<K, T>, U, ValidationError> validator) {
        return org.h5z.fval4j.validators.ObjectValidators.nestedMap(validator, _v -> ValidationError.error("NotAMap"));
    }

}
