package org.h5z.fval4j.validators;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.fval4j.DefaultErrors.*;
import static org.h5z.fval4j.Core.*;
import static org.h5z.fval4j.validators.ObjectValidators.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.h5z.fval4j.defaults.ObjectValidators.*;


import java.util.Map;

import static org.h5z.fval4j.validators.MapValidators.*;

import org.h5z.fval4j.Trie;
import org.h5z.fval4j.Core.Validator;
import org.h5z.fval4j.DefaultErrors.ValidationError;

public final class MapValidatorsExampleUnitTest {
    
    @Test
    public void example1() {
        Validator<Map<String, Object>, ValidationError> mapValidator = every(
            value(
                "un", 
                integer(gt(1), _i -> ValidationError.error("NotAnInteger")), 
                Object::toString),
            value(
                "deux",
                string(lengthBetween(5, 10), _s -> ValidationError.error("NotAString")),
                Object::toString)
        );

        mapValidator.validate(Map.of("un", 0, "deux", "a"));
    }

    @Test
    public void example2() {
        Validator<Map<String, Object>, ValidationError> mapValidator = 
            every(
                kv(
                    "un", 
                    required(
                        integer(
                            gt(1)))),
                kv(
                    "deux",
                    required(
                        string(
                            lengthBetween(5, 10))))
            );

        Trie<ValidationError> result = mapValidator.validate(Map.of("un", 2, "deu", "abf"));
        assertAll(
            () -> assertThat(result.isInvalid()).isTrue(),
            () -> assertThat(result.hasErrors("un")).isFalse(),
            () -> assertThat(result.hasErrors("deux")).isTrue(),
            () -> assertThat(result.getErrors("deux")).containsExactly(
                ValidationError.error("ValueRequired")
            )
        );
    }

    @Test
    public void example3() {
        Validator<Map<String, Object>, ValidationError> mapValidator = 
            every(
                kv(
                    "un", 
                    required(
                        integer(
                            gt(1)))),
                kv(
                    "deux",
                    optional(
                        string(
                            lengthBetween(5, 10))))
            );

        Trie<ValidationError> result = mapValidator.validate(Map.of("un", 2, "deu", "abf"));
        assertAll(
            () -> assertThat(result.isValid()).isTrue()
        );
    }

    @Test
    public void example4() {
        Validator<Map<String, Object>, ValidationError> mapValidator = 
            every(
                kv("firstName", required(
                                    integer(
                                        gt(1)))),
                kv("lastName",  optional(
                                    string(
                                        lengthBetween(5, 10)))),
                kv("address", required(map(every(
                    
                    kv("city",  required(
                                    string(lengthBetween(2, 128)))),

                    kv("postalCode", optional(
                                        integer(gt(1))))))))
            );

        Trie<ValidationError> result = mapValidator.validate(Map.of("un", 2, "deu", "abf"));
        assertAll(
            () -> assertThat(result.isValid()).isFalse()
        );
    }

    record Address(String city) { }
    record Person(String firtName, Address address) { }
    @Test
    public void example5() {
        
        var personValidator = every(
            required("firstName",
                     Person::firtName,
                     lengthBetween(2, 10)),
            required("address",
                     Person::address,
                     every(
                         required("city",
                                  Address::city,
                                  lengthBetween(2, 10)))));
        
        var result = personValidator.validate(new Person("John", new Address(null)));
        assertAll(
            () -> assertThat(result.isInvalid()).isTrue(),
            () -> assertThat(result.hasErrors("address", "city")).isTrue()
        );

    }

}
