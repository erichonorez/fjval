package org.h5z.fval4j.defaults;

import java.util.Map;

import org.h5z.fval4j.Core.Validator;
import org.h5z.fval4j.DefaultErrors.ValidationError;


public final class MapValidators {
    
    private MapValidators() {
        throw new IllegalAccessError("Cannot be instanciated");
    }

    public static <K, T> Validator<Map<K, T>, Map<K, T>, ValidationError> hasKey(K key) {
        return org.h5z.fval4j.validators.MapValidators.hasKey(key, () -> ValidationError.error("KeyNotFound"));
    }

}
