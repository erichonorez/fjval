package org.h5z.jval;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import org.h5z.jval.Core.Validator;

import lombok.Value;

import static org.h5z.jval.DefaultErrors.ValidationError.error;
import static org.h5z.jval.Core.*;

public final class DefaultErrors {

    public static final String VALUE_REQUIRED = "ValueRequired";
    public static final String STRING_LENGTH_NOT_BETWEEN_BOUNDS = "StringLengthNotBetweenBounds";
    public static final String COLLECTION_SIZE_NOT_BETWEEN_BOUNDS = "CollectionSizeNotBetweenBounds";
    public static final String VALUE_NOT_EQUAL_TO = "ValueNotEqualTo";
    public static final String VAULE_NOT_IN_SET = "VauleNotInSet";
    public static final String STRING_DONT_CONTAIN = "StringDontContain";
    public static final String STRING_DONT_MATCH = "StringDontMatch";
    public static final String COMPARABLE_NOT_BETWEEN_BOUNDS = "ComparableNotBetweenBounds";
    public static final String COMPARABLE_NOT_LOWER_THAN_OR_EQUAL_TO = "ComparableNotLowerThanOrEqualTo";
    public static final String COMPARABLE_NOT_LOWER_THAN = "ComparableNotLowerThan";
    public static final String COMPARABLE_NOT_GREATER_THAN_OR_EQUAL_TO = "ComparableNotGreaterThanOrEqualTo";
    public static final String COMPARABLE_NOT_EQUAL_TO = "ComparableNotEqualTo";
    public static final String COMPARABLE_NOT_GREATER_THAN = "ComparableNotGreaterThan";

    private DefaultErrors() {
        throw new IllegalAccessError("Cannot be instanciated");
    }

    public static <T extends Comparable<T>> Validator<T, ValidationError> gt(T b) { 
        return Validators.gt(b, v -> error(COMPARABLE_NOT_GREATER_THAN, b, v));
    }

    public static <T extends Comparable<T>> Validator<T, ValidationError> eq(T b) {
        return Validators.eq(b, v -> error(COMPARABLE_NOT_EQUAL_TO, b, v));
      }

    public static <T extends Comparable<T>> Validator<T, ValidationError> gte(T b) {
        return Validators.gt(b, v -> error(COMPARABLE_NOT_GREATER_THAN_OR_EQUAL_TO, b, v));
    }

    public static <T extends Comparable<T>> Validator<T, ValidationError> lt(T b) {
        return Validators.gt(b, v -> error(COMPARABLE_NOT_LOWER_THAN, b, v));
     }

    public static <T extends Comparable<T>> Validator<T, ValidationError> lte(T b) { 
        return Validators.gt(b, v -> error(COMPARABLE_NOT_LOWER_THAN_OR_EQUAL_TO, b, v));
    }

    public static <T extends Comparable<T>> Validator<T, ValidationError> between(T a, T b) {
        return Validators.gt(b, v -> error(COMPARABLE_NOT_BETWEEN_BOUNDS, a, b, v));
     }

    public static <E> Validator<String, ValidationError> matches(String regex) {
        return Validators.matches(regex, v -> error(STRING_DONT_MATCH, regex, v));
     }

    public static <E> Validator<String, ValidationError> contains(CharSequence b) {
        return Validators.contains(b, v -> error(STRING_DONT_CONTAIN, b, v));
     }

    public static <T, E> Validator<T, ValidationError> in(Set<T> xs) {
        return Validators.in(xs, v -> error(VAULE_NOT_IN_SET, xs, v));
     }

    public static <T, E> Validator<T, ValidationError> equalsTo(T b) { 
        return Validators.equals(b, v -> error(VALUE_NOT_EQUAL_TO, b, v));
    }

    public static <E> Validator<Collection<?>, ValidationError> sizeBetween(int inclMin, int inclMax) {
        return Validators.sizeBetween(inclMin, inclMax, v -> error(COLLECTION_SIZE_NOT_BETWEEN_BOUNDS, inclMax, inclMax, v.size()));
     }

    public static Validator<String, ValidationError> lengthBetween(int inclMin, int inclMax) {
        return Validators.lengthBetween(inclMin, inclMax, v -> error(STRING_LENGTH_NOT_BETWEEN_BOUNDS, inclMin, inclMax, v.length()));
    }

    public static <T, E> Validator<T, ValidationError> required() {
        return Validators.required(() -> error(VALUE_REQUIRED));
    }

    public static <T, E> Validator<T, ValidationError> required(Validator<T, E> validator) {
        return Core.required(validator, () -> error(VALUE_REQUIRED));
    }

    public static <O, T> Validator<O, ValidationError> required(String key, 
                                                                     Function<O, T> fn, 
                                                                     Validator<T, ValidationError> validator) {
        return Core.required(key, fn, validator, () -> error(VALUE_REQUIRED));
    }

    public static <O, T, E> Validator<O, ValidationError> required(String key, 
                                                                        Function<O, T> fn) {
        return keyed(key, Core.required(fn, () -> error(VALUE_REQUIRED)));
    }

    @Value
    public static class ValidationError {
        private final String identifier;
        private final Object[] args;

        public static ValidationError error(String identifier, Object... args) {
            return new ValidationError(identifier, args);
        }

        public static ValidationError error(String identifier) {
            return new ValidationError(identifier, new Object[] {});
        }
    }
    
}
