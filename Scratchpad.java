import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Scratchpad {


    @FunctionalInterface 
    public interface Predicate<T> {
        boolean apply(T t);
    }

    @FunctionalInterface
    public interface Validator<T, R> {
        Optional<R> apply(T t);
    }

    @FunctionalInterface
    public interface Check<T, R> {
        ValidationResult<T, R> conform(T t);
    }

    public record ErrorTemplate(String id, String messageFmt) {};

    public sealed interface ValidationResult<T, R> {
        default boolean isValid() { return false; }
        default boolean isInvalid() { return true; } 
    }
    public record ValidationError<T, R>(List<String> id, T validated) implements ValidationResult<T, R> {};
    public record ValidationSucess<T, R>(T t) implements ValidationResult<T, R> {
        @Override
        public boolean isValid() { return true; }
        @Override
        public boolean isInvalid() { return false; }
    };

    public static <T> Spec<T, T> spec(Predicate<T> predicate, String description) {
        return new Single<>(
            validator(predicate),
            description,
            new ErrorTemplate(predicate.toString(), "messageFmt"));
    }

    @SafeVarargs
    public static <T, R> Spec<T, R> and(Spec<T, R>... specs) {
        return new And<>(Arrays.asList(specs));
    }

    public static class And<T, R> implements Spec<T, R> {
        public final String description; 
        public final List<Spec<T, R>> specs;
        public And(List<Spec<T, R>> specs) {
            this.specs = specs;
            this.description = "And";
        }
        @Override
        public Optional<R> apply(T t) { 
            return recurApply(specs, Optional.empty(), t);
        }

        private static <T, R> Optional<R> recurApply(List<Spec<T, R>> specs, Optional<R> r, T t) {
            if (specs.size() <1 ) { return r; }
            Spec<T, R> curr = specs.get(0);
            Optional<R> newR = curr.apply(t);
            if (!newR.isPresent()) {
                return Optional.empty();
            }
            
            return recurApply(specs.subList(1, specs.size()), newR, t);
        }

        @Override
        public ValidationResult<T, R> conform(T t) { 
            return this.apply(t).isPresent() 
                ? new ValidationSucess<T, R>(t)
                : new ValidationError<T, R>(
                    Arrays.asList(this.toString()), t);
        }

        @Override
        public String description() {
            return this.description;
        }
    }

    public static interface Spec<T, R> extends Validator<T, R>, Check<T, R> {
        String description();
    }

    public static class Single<T, R> implements Spec<T, R> {
        public final Validator<T, R> validator;
        public final String description;
        public final ErrorTemplate errTmpl;
        public Single(Validator<T, R> validator, String description, ErrorTemplate errTmpl) {
            this.validator = validator;
            this.description = description;
            this.errTmpl = errTmpl;
        }
        @Override
        public Optional<R> apply(T t) { return this.validator.apply(t); }

        @Override
        public ValidationResult<T, R> conform(T t) { 
            return this.validator.apply(t).isPresent() 
                ? new ValidationSucess<T, R>(t)
                : new ValidationError<T, R>(
                    Arrays.asList(this.errTmpl.id), t);
        }

        @Override
        public String description() {
            return this.description;
        }
    }

    public static <T> Validator<T, T> validator(Predicate<T> predicate) {
        return t -> predicate.apply(t) ? Optional.of(t) : Optional.empty();
    }

    public static <T> Optional<T> validate(Predicate<T> predicate, T t) {
        return validator(predicate).apply(t);
    }

    public static <T, R> ValidationResult<T, R> conform(Spec<T, R> spec, T t) {
        return spec.conform(t);
    }

    public static <T, R> String doc(Spec<T, R> spec) {
        return spec.description();
    }

    public static Predicate<Integer> gt(int n) {
        return v -> v > n;
    } 

    public static boolean isString(Object o) {
        return o instanceof String;
    }

    public static void main(String[] args) {
        Scratchpad.spec(Scratchpad.gt(0), "The value should be greater than 0");
    }
}
