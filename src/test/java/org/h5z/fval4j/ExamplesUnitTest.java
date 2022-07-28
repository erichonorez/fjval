package org.h5z.fval4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.fval4j.Core.*;
import static org.h5z.fval4j.DefaultErrors.*;
import static org.h5z.fval4j.DefaultErrors.ValidationError.*;
import static org.h5z.fval4j.Validators.*;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.h5z.fval4j.data.ValidationResult;
import org.h5z.fval4j.Core.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import lombok.Value;

public class ExamplesUnitTest {

    @Nested
    @DisplayName("Examples of how to validate simple values")
    class SimpleValue {

        // We want to create a validator for a username
        // The rules are the following:
        // it must only contains alphanumeric characters and '_'
        // the length must be between 3 and 16
        // The validator must apply all the validators (no fail-fast).
        @Test
        void example0() {
            Validator<String, String, String> usernameValidator = all( // 'every' will execute all the validator even one of
                                                                 // them fails
                    matches("^[\\w]+$", () -> "It must only contain alphanumeric characters and '_'"),
                    lengthBetween(3, 16, () -> "The length must be between 3 and 16"));

            assertAll(

                    () -> {
                        ValidationResult<String, String, String> result = usernameValidator.validate("");
                        assertThat(result)
                            .matches(ValidationResult::isInvalid);
                        
                        assertThat(result.getErrors())
                            .containsOnly(
                                    "It must only contain alphanumeric characters and '_'",
                                    "The length must be between 3 and 16");
                    },

                    () -> {
                        ValidationResult<String, String, String> result = usernameValidator.validate("this is invalid");
                        assertThat(result)
                            .matches(ValidationResult::isInvalid);
                        
                        assertThat(result.getErrors())
                            .containsOnly("It must only contain alphanumeric characters and '_'");
                    },

                    () -> assertThat(usernameValidator.validate("myUserName_76"))
                            .matches(ValidationResult::isValid));
        }

        // We want to validate a password
        // Credits :
        // https://stackoverflow.com/questions/31539727/laravel-password-validation-rule
        // The validator must fail at the first failed validator (fail-fast).
        @Test
        void example1() {
            Validator<String, String, String> passwordValidator = and( // 'sequentially' will execute all the validator
                                                                        // and stop at the first failed validator
                    matches("^.*(?=.{3,})(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!$#%]).*$",
                            () -> "Password should contains lower case letters, upper case letters, digits and special characters (!, $, #, or %)"),
                    lengthBetween(8, 42, () -> "The length must be between 8 and 42"));

            assertAll(

                    () -> { 
                        ValidationResult<String, String, String> result = passwordValidator.validate("");
                        assertThat(result)
                            .matches(ValidationResult::isInvalid);
                        
                        assertThat(result.getErrors())
                            .containsOnly(
                                    "Password should contains lower case letters, upper case letters, digits and special characters (!, $, #, or %)");
                    },

                    () -> {
                        ValidationResult<String, String, String> result = passwordValidator.validate("Short!8");
                        assertThat(result)
                            .matches(ValidationResult::isInvalid);

                        assertThat(result.getErrors())
                            .containsOnly("The length must be between 8 and 42");
                    },

                    () -> { 
                        assertThat(passwordValidator.validate("P4ssW0rd!42"))
                            .matches(ValidationResult::isValid);
                    }
            );
        }

        @Nested
        @DisplayName("Examples of how to validate aggregated values like objects")
        class AggregatedValues {

            @Nested
            @DisplayName("Examples with error as string")
            class ErrorAsString {
        
                @Test
                void example2() {
                    Validator<String, String, String> firstNameValidator = lengthBetween(3, 42,
                            () -> "The length must be between 2 and 42");
                    Validator<String, String, String> lastNameValidator = firstNameValidator; // Same validation for the first name and
                                                                                    // the last name

                    Validator<String, String, String> usernameValidator = all(
                            matches("^[\\w]+$", () -> "It must only contain alphanumeric characters and '_'"),
                            lengthBetween(3, 16, () -> "The length must be between 3 and 16"));

                    Validator<String, String, String> passwordValidator = and(
                            matches("^.*(?=.{3,})(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!$#%]).*$",
                                    () -> "Password should contains lower case letters, upper case letters, digits and special characters (!, $, #, or %)"),
                            lengthBetween(8, 42, () -> "The length must be between 8 and 42"));

                    Validator<SignUpForm, SignUpForm, String> passwordsMatchValidator = cond(
                            form -> form.password().equals(form.passwordConfirmation()),
                            () -> "Passwords must match");

                    Validator<SignUpForm, SignUpForm, String> formValidator = and( // stops after the first failed validator
                            all(
                                    prop(SignUpForm::firstName, optional(firstNameValidator)), // first name may be null
                                    prop(SignUpForm::lastName, optional(lastNameValidator)), // last name may be null
                                    prop(SignUpForm::userName, required(usernameValidator, () -> "A username is mandatory")),
                                    prop(SignUpForm::password, required(passwordValidator, () -> "A password is mandatory")),
                                    prop(SignUpForm::passwordConfirmation, required(() -> "Confirm your password"))),
                            passwordsMatchValidator // then if previous validors succeeded then validate that both passwords
                                                    // match
                    );

                    assertAll(
                            () -> assertThat(formValidator.validate(new SignUpForm(null, null, null, null, null)).getErrors())
                                    .containsExactly(
                                            "A username is mandatory",
                                            "A password is mandatory",
                                            "Confirm your password"),

                            () -> assertThat(
                                    formValidator.validate(new SignUpForm("", "Doe", "username", "P4ssW0rd!42", "P4ssW0rd!42")).getErrors())
                                            .containsExactly("The length must be between 2 and 42"), // The first name validator
                                                                                                    // has failed

                            () -> assertThat(formValidator
                                    .validate(new SignUpForm("John", "Doe", "userName", "P4ssW0rd!42", "password")).getErrors())
                                            .containsExactly("Passwords must match"),

                            () ->
                                assertThat(formValidator
                                    .validate(new SignUpForm("John", "Doe", "userName", "P4ssW0rd!42", "P4ssW0rd!42")))
                                            .matches(ValidationResult::isValid)
                    );

                }
            } 

            @Nested
            @DisplayName("Examples with typed error for each validator")
            class TypedError {
                /**
                 * In this example we want to validate data from a sign up form : a first name, a last name,
                 * a username, a password and a password confirmation. 
                 * 
                 * These data are aggregated in an instance of a {@link SignUpForm}.
                 * 
                 * Validation rules for each of the {@link SignUpForm}'s properties are explained below. If a validator fails it 
                 * will return an instance of one the subclasses of {@link SignUpFormError}.
                 */
                @Test
                void example() {
                    Validator<SignUpForm, SignUpForm, ValidationError> formValidator =
                        and(
                                // The properties are all validated first
                                all(
                                        // The firstName property is optional
                                        // If present the length should be between 3 and 42
                                        prop(SignUpForm::firstName, 
                                            optional(
                                                lengthBetween(3, 42, () -> new FirstNameLengthError()))
                                            ),

                                        // The lastName property is optional
                                        // If present the length should be between 3 and 42
                                        prop(SignUpForm::lastName, 
                                            optional(
                                                lengthBetween(3, 42, () -> new LastNameLengthError()))
                                            ),

                                        // The userName property is required
                                        // It must contains only alphanumeric characters
                                        // It must have a length between 3 and 16
                                        prop(SignUpForm::userName, 
                                            required(
                                                all(
                                                    matches("^[\\w]+$", () -> new UserNameComplexityError()),
                                                    lengthBetween(3, 16, () -> new UserNameLengthError())
                                                ), 
                                                () -> new UserNameRequiredError())
                                            ),

                                        // The password property is required
                                        // It must contains upper case and lower case letter, numbers and special characters
                                        prop(SignUpForm::password, 
                                            required(
                                                and(
                                                    matches("^.*(?=.{3,})(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!$#%]).*$",
                                                        () -> new PasswordComplexityError()),
                                                    lengthBetween(8, 42, () -> new PasswordLengthError())
                                                ), 
                                                () -> new PasswordRequiredError())
                                            ),

                                        // The passwordConfirmation property is required
                                        prop(SignUpForm::passwordConfirmation, 
                                            required(() -> new ConfirmedPasswordRequiredError()))
                                ),
                                // If the properties are valid then apply cross properties validations
                                cond( // If the passwords don't match then return an error
                                    form -> form.password().equals(form.passwordConfirmation()),
                                    () -> new PasswordsDontMatchError())
                        );

                    // The validation result is a list of SignUpFormError.
                    assertAll(
                            () -> assertThat(formValidator.validate(new SignUpForm(null, null, null, null, null)).getErrors())
                                    .containsExactly(
                                            new UserNameRequiredError(),
                                            new PasswordRequiredError(),
                                            new ConfirmedPasswordRequiredError()),

                            () -> assertThat(
                                    formValidator.validate(new SignUpForm("", "Doe", "username", "P4ssW0rd!42", "P4ssW0rd!42")).getErrors())
                                            .containsExactly(new FirstNameLengthError()),

                            () -> assertThat(formValidator
                                    .validate(new SignUpForm("John", "Doe", "userName", "P4ssW0rd!42", "password")).getErrors())
                                            .containsExactly(new PasswordsDontMatchError()),

                            () -> assertThat(formValidator
                                    .validate(new SignUpForm("John", "Doe", "userName", "P4ssW0rd!42", "P4ssW0rd!42")))
                                            .matches(ValidationResult::isValid));
                }
            }
        }

    }

    /**
     * Until now we learned how to validate primitive types and objects.
     * 
     * We started to create small validators for each values of the form. Then we combined these validators to 
     * creates a more complex one validating the form as an object (as aggregated value).
     * 
     * The problem with the validators we created so far is that the errors are returned in the list. 
     * Sometimes a flat list of errors may not be practical to use and you want to reflect the structure of the object
     * beeing validated in the validation result. Especially if you want to :
     * - test if a specific property of the object is valid or not
     * - if you want to retrieve only the errors of this specific property
     * - if you want to communicate the errors to a human
     * 
     * If we take the previous example of the {@link SignUpForm}, it would be more practical to have the errors 
     * reported like a map where keys are the property names and the values are the lists of errors.
     * 
     * Example (not valid java) :
     * <code>
     * {
     *     "userName" : [
     *          "It must only contain alphanumeric characters and '_'",
     *          "The length must be between 3 and 16"
     *     ],
     *     ...
     * }
     * </code>
     * 
     */
    @Nested
    @DisplayName("Examples of how to validate object and report errors in a structured way")
    class StructuredErrorReporting {
        
        @Test
        @DisplayName("Simple example")
        void example0() {
            // Let's first create a validator for a user name 
            Validator<String, String, String> usernameValidator = all(
                    matches("^[\\w]+$", () -> "It must only contain alphanumeric characters and '_'"),
                    lengthBetween(3, 16, () -> "The length must be between 3 and 16"));

            // Then we want to index the result of this validator with the key 'userName'
            Validator<String, String, String> userNameKValidator = Core.keyed("userName", usernameValidator);
            ValidationResult<String, String, String> result = userNameKValidator.validate("");

            assertAll(
                () -> assertThat(result.isInvalid()).isTrue(),

                () -> assertThat(result.hasErrors("userName")).isTrue(),

                () -> assertThat(result.getErrors("userName"))
                        .containsExactly(
                            "It must only contain alphanumeric characters and '_'",
                            "The length must be between 3 and 16")
            );
        }

        @Test
        @DisplayName("Sign up form validation")
        void example1() {
            Validator<SignUpForm, SignUpForm, SignUpFormError> formValidator = and(
                all(
                    keyed("firstName", 
                        optional(SignUpForm::firstName, 
                            lengthBetween(3, 42, () -> new FirstNameLengthError()))),

                    keyed("lastName",
                        optional(SignUpForm::lastName, 
                            lengthBetween(3, 42, () -> new LastNameLengthError()))),

                    keyed("userName",
                        required(SignUpForm::userName, 
                                all(
                                    matches("^[\\w]+$", () -> new UserNameComplexityError()),
                                    lengthBetween(3, 16, () -> new UserNameLengthError())
                                ), 
                                () -> new UserNameRequiredError())),

                    keyed("password",
                        required(SignUpForm::password, 
                                and(
                                    matches("^.*(?=.{3,})(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!$#%]).*$",
                                        () -> new PasswordComplexityError()),
                                    lengthBetween(8, 42, () -> new PasswordLengthError())
                                ), 
                                () -> new PasswordRequiredError())),

                    keyed("passwordConfirmation",
                        required(SignUpForm::passwordConfirmation,
                                 identity(),
                                 () -> new ConfirmedPasswordRequiredError()))),

                globally(
                    cond(
                        form -> form.password().equals(form.passwordConfirmation()),
                        () -> new PasswordsDontMatchError())));

            ValidationResult<SignUpFormError, SignUpForm, SignUpForm> result = formValidator.validate(new SignUpForm(null, null, null, null, null));

            assertAll(
                () -> assertThat(result.isInvalid())
                            .isTrue(),

                () -> assertThat(result.hasErrors("firstName"))
                            .isFalse(), // firstName is optional

                () -> assertThat(result.hasErrors("lastName"))
                            .isFalse(), // lastName is false

                () -> assertThat(result.hasErrors("userName"))
                            .isTrue(),
                () -> assertThat(result.getErrors("userName"))
                            .containsExactly(new UserNameRequiredError()),

                () -> assertThat(result.hasErrors("password"))
                            .isTrue(),
                () -> assertThat(result.getErrors("password"))
                            .containsExactly(new PasswordRequiredError()),

                () -> assertThat(result.hasErrors("passwordConfirmation"))
                            .isTrue());
        }

        @Test
        @DisplayName("Sign up form with more concise api")
        void example2() {
            Validator<SignUpForm, SignUpForm, SignUpFormError> formValidator = and(
                all(
                    optional("firstName", 
                             SignUpForm::firstName, 
                             lengthBetween(3, 42, () -> new FirstNameLengthError())),

                    optional("lastName",
                             SignUpForm::lastName, 
                             lengthBetween(3, 42, () -> new LastNameLengthError())),

                    required("userName",
                             SignUpForm::userName, 
                             all(
                                 matches("^[\\w]+$", () -> new UserNameComplexityError()),
                                 lengthBetween(3, 16, () -> new UserNameLengthError())
                             ), 
                             () -> new UserNameRequiredError()),

                    required("password",
                             SignUpForm::password, 
                            and(
                                matches("^.*(?=.{3,})(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!$#%]).*$",
                                    () -> new PasswordComplexityError()),
                                lengthBetween(8, 42, () -> new PasswordLengthError())
                            ), 
                            () -> new PasswordRequiredError()),

                    required("passwordConfirmation",
                             SignUpForm::passwordConfirmation,
                             identity(),
                             () -> new ConfirmedPasswordRequiredError())),
                globally(
                    cond(
                        form -> form.password().equals(form.passwordConfirmation()),
                        () -> new PasswordsDontMatchError())));

            ValidationResult<SignUpFormError, SignUpForm, SignUpForm> result = formValidator.validate(new SignUpForm(null, null, null, null, null));

            assertAll(
                () -> assertThat(result.isInvalid())
                            .isTrue(),

                () -> assertThat(result.hasErrors("firstName"))
                            .isFalse(), // firstName is optional

                () -> assertThat(result.hasErrors("lastName"))
                            .isFalse(), // lastName is false

                () -> assertThat(result.hasErrors("userName"))
                            .isTrue(),
                () -> assertThat(result.getErrors("userName"))
                            .containsExactly(new UserNameRequiredError()),

                () -> assertThat(result.hasErrors("password"))
                            .isTrue(),
                () -> assertThat(result.getErrors("password"))
                            .containsExactly(new PasswordRequiredError()),

                () -> assertThat(result.hasErrors("passwordConfirmation"))
                            .isTrue());
        }

        @Test
        @DisplayName("Sign up form with default errors")
        void example3() {
            Validator<SignUpForm, SignUpForm, DefaultErrors.ValidationError> formValidator = 
            
            and(
                all(
                    optional("firstName", 
                             SignUpForm::firstName, 
                             lengthBetween(3, 42)),

                    optional("lastName",
                             SignUpForm::lastName, 
                             lengthBetween(3, 42)),

                    required("userName",
                             SignUpForm::userName, 
                             all(
                                 matches("^[\\w]+$"),
                                 lengthBetween(3, 16)
                             )),

                    required("password",
                             SignUpForm::password, 
                            and(
                                matches("^.*(?=.{3,})(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!$#%]).*$"),
                                lengthBetween(8, 42)
                            )),

                    required("passwordConfirmation",
                             SignUpForm::passwordConfirmation,
                             identity())),
                globally(
                    cond(
                        form -> form.password().equals(form.passwordConfirmation()),
                        form -> error("PasswordsDontMatch", form.password(), form.passwordConfirmation()))));

            ValidationResult<DefaultErrors.ValidationError, SignUpForm, SignUpForm> result = formValidator.validate(new SignUpForm(null, null, null, null, null));

            assertAll(
                () -> assertThat(result.isInvalid())
                            .isTrue(),

                () -> assertThat(result.hasErrors("firstName"))
                            .isFalse(), // firstName is optional

                () -> assertThat(result.hasErrors("lastName"))
                            .isFalse(), // lastName is false

                () -> assertThat(result.hasErrors("userName"))
                            .isTrue(),
                () -> assertThat(result.getErrors("userName"))
                            .containsExactly(error(DefaultErrors.VALUE_REQUIRED)),

                () -> assertThat(result.hasErrors("password"))
                            .isTrue(),
                () -> assertThat(result.getErrors("password"))
                            .containsExactly(error(DefaultErrors.VALUE_REQUIRED)),

                () -> assertThat(result.hasErrors("passwordConfirmation"))
                            .isTrue());
        }
    }

    // types used in the tests above
    
    // Let's define what data we ask in a sign up form
    static record SignUpForm(String firstName, String lastName, String userName, String password, String passwordConfirmation) {}

    // let's define some typed error for our SignUpForm validation
    static interface ValidationError {
        String getMessage();
    }

    static interface SignUpFormError extends ValidationError { }

    @Value
    static class FirstNameLengthError implements SignUpFormError {

        private static final String MSG = "The length must be between 2 and 42";

        @Override
        public String getMessage() { return MSG; }

    }

    @Value
    static class LastNameLengthError implements SignUpFormError {

        private static final String MSG = "The length must be between 2 and 42";

        @Override
        public String getMessage() { return MSG; }

    }

    @Value
    static class PasswordComplexityError implements SignUpFormError {

        private static final String MSG = "Password should contains lower case letters, upper case letters, digits and special characters (!, $, #, or %)";

        @Override
        public String getMessage() { return MSG; }
        
    }

    @Value
    static class PasswordLengthError implements SignUpFormError {

        private static final String MSG = "The length must be between 8 and 42";

        @Override
        public String getMessage() { return MSG; }
    }

    @Value
    static class UserNameComplexityError implements SignUpFormError {

        private static final String MSG = "It must only contain alphanumeric characters and '_'";

        @Override
        public String getMessage() { return MSG; }

    }

    @Value
    static class UserNameLengthError implements SignUpFormError {

        private static final String MSG = "The length must be between 3 and 16";

        @Override
        public String getMessage() { return MSG; }
        
    }

    @Value
    static class PasswordsDontMatchError implements SignUpFormError {

        private static final String MSG = "Passwords must match";

        @Override
        public String getMessage() { return MSG; }

    }

    @Value
    static class UserNameRequiredError implements SignUpFormError {
            
            @Override
            public String getMessage() {
                    return "A username is mandatory";
            }

    }

    @Value
    static class PasswordRequiredError implements SignUpFormError {
            
            @Override
            public String getMessage() {
                    return "A password is mandatory";
            }

    }

    @Value
    static class ConfirmedPasswordRequiredError implements SignUpFormError {
            
            @Override
            public String getMessage() {
                    return "Confirm your password";
            }

    }
}
