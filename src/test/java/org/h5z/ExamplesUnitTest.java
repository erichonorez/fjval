package org.h5z;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.jval.Validators.*;
import static org.h5z.jval.Core.*;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.h5z.jval.Core;
import org.h5z.jval.Core.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class ExamplesUnitTest {

    @Nested
    @DisplayName("Example of how to validate value")
    class SimpleValue {

        // We want to create a validator for a username
        // The rules are the following:
        // it must only contains alphanumeric characters and '_'
        // the length must be between 3 and 16
        // The validator must apply all the validators (no fail-fast).
        @Test
        void example0() {
            Validator<String, String> usernameValidator = every( // 'every' will execute all the validator even one of
                                                                 // them fails
                    matches("^[\\w]+$", () -> "It must only contain alphanumeric characters and '_'"),
                    hasLengthBetween(3, 16, () -> "The length must be between 3 and 16"));

            assertAll(

                    () -> assertThat(usernameValidator.validate(""))
                            .matches(Core::isInvalid)
                            .containsOnly(
                                    "It must only contain alphanumeric characters and '_'",
                                    "The length must be between 3 and 16"),

                    () -> assertThat(usernameValidator.validate("this is invalid"))
                            .matches(Core::isInvalid)
                            .containsOnly("It must only contain alphanumeric characters and '_'"),

                    () -> assertThat(usernameValidator.validate("myUserName_76"))
                            .matches(Core::isValid));
        }

        // We want to validate a password
        // Credits :
        // https://stackoverflow.com/questions/31539727/laravel-password-validation-rule
        // The validator must fail at the first failed validator (fail-fast).
        @Test
        void example1() {
            Validator<String, String> passwordValidator = sequentially( // 'sequentially' will execute all the validator
                                                                        // and stop at the first failed validator
                    matches("^.*(?=.{3,})(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!$#%]).*$",
                            () -> "Password should contains lower case letters, upper case letters, digits and special characters (!, $, #, or %)"),
                    hasLengthBetween(8, 42, () -> "The length must be between 8 and 42"));

            assertAll(

                    () -> assertThat(passwordValidator.validate(""))
                            .matches(Core::isInvalid)
                            .containsOnly(
                                    "Password should contains lower case letters, upper case letters, digits and special characters (!, $, #, or %)"),

                    () -> assertThat(passwordValidator.validate("Short!8"))
                            .matches(Core::isInvalid)
                            .containsOnly("The length must be between 8 and 42"),

                    () -> assertThat(passwordValidator.validate("P4ssW0rd!42"))
                            .matches(Core::isValid));
        }

        // Let's validate aggregated values
        static record SignUpForm(String firstName, String lastName, String userName, String password,
                String passwordConfirmation) {
        }

        @Test
        void example2() {
            Validator<String, String> firstNameValidator = hasLengthBetween(3, 42,
                    () -> "The length must be between 2 and 42");
            Validator<String, String> lastNameValidator = firstNameValidator; // Same validation for the first name and
                                                                              // the last name

            Validator<String, String> usernameValidator = every(
                    matches("^[\\w]+$", () -> "It must only contain alphanumeric characters and '_'"),
                    hasLengthBetween(3, 16, () -> "The length must be between 3 and 16"));

            Validator<String, String> passwordValidator = sequentially(
                    matches("^.*(?=.{3,})(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!$#%]).*$",
                            () -> "Password should contains lower case letters, upper case letters, digits and special characters (!, $, #, or %)"),
                    hasLengthBetween(8, 42, () -> "The length must be between 8 and 42"));

            Validator<SignUpForm, String> passwordsMatchValidator = cond(
                    form -> form.password().equals(form.passwordConfirmation()),
                    () -> "Passwords must match");

            Validator<SignUpForm, String> formValidator = sequentially( // stops after the first failed validator
                    every(
                            prop(SignUpForm::firstName, optional(firstNameValidator)), // first name may be null
                            prop(SignUpForm::lastName, optional(lastNameValidator)), // last name may be null
                            prop(SignUpForm::userName, required(usernameValidator, () -> "A username is mandatory")),
                            prop(SignUpForm::password, required(passwordValidator, () -> "A password is mandatory")),
                            prop(SignUpForm::passwordConfirmation, notNull(() -> "Confirm your password"))),
                    passwordsMatchValidator // then if previous validors succeeded then validate that both passwords
                                            // match
            );

            assertAll(
                    () -> assertThat(formValidator.validate(new SignUpForm(null, null, null, null, null)))
                            .containsExactly(
                                    "A username is mandatory",
                                    "A password is mandatory",
                                    "Confirm your password"),

                    () -> assertThat(
                            formValidator.validate(new SignUpForm("", "Doe", "username", "P4ssW0rd!42", "P4ssW0rd!42")))
                                    .containsExactly("The length must be between 2 and 42"), // The first name validator
                                                                                             // has failed

                    () -> assertThat(formValidator
                            .validate(new SignUpForm("John", "Doe", "userName", "P4ssW0rd!42", "password")))
                                    .containsExactly("Passwords must match"),

                    () -> assertThat(formValidator
                            .validate(new SignUpForm("John", "Doe", "userName", "P4ssW0rd!42", "P4ssW0rd!42")))
                                    .matches(Core::isValid));

        }

    }

}
