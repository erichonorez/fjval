package org.h5z.jval;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public abstract class ValidationResult<E> {

    private final List<E> errors;

    public ValidationResult(List<E> errors) {
        this.errors = errors;
    }

    public List<E> getErrors() {
        return errors;
    }

    public abstract <T> T cases(
        Function<Root<E>, T> whenRoot,
        Function<Node<E>, T> whenNode,
        Function<Leaf<E>, T> whenLeaf);

    public static <E> ValidationResult<E> make(E e) {
        return new Root<>(Arrays.asList(e), Collections.emptyList());
    }

    public static <E> ValidationResult<E> append(ValidationResult<E> a, ValidationResult<E> b) {

    }

    public static class Root<E> extends ValidationResult<E> {
        private final List<ValidationResult<E>> validationResults;

        public Root(List<E> errors, List<ValidationResult<E>> validationResults) {
            super(errors);
            this.validationResults = validationResults;
        }

        @Override
        public <T> T cases(Function<Root<E>, T> whenRoot,
                           Function<Node<E>, T> whenNode,
                           Function<Leaf<E>, T> whenLeaf) {
            return whenRoot.apply(this);
        }
    }

    public static final class Node<E> extends ValidationResult<E> {
        private final String path;
        private final List<ValidationResult<E>> validationResults;

        public Node(String name, List<E> errors, List<ValidationResult<E>> validationResults) {
            super(errors);
            this.path = name;
            this.validationResults = validationResults;
        }

        public String getPath() {
            return path;
        }

        public List<ValidationResult<E>> getValidationResults() {
            return validationResults;
        }

        @Override
        public <T> T cases(Function<Root<E>, T> whenRoot,
                           Function<Node<E>, T> whenNode,
                           Function<Leaf<E>, T> whenLeaf) {
            return whenNode.apply(this);
        }
    }


    public static final class Leaf<E> extends ValidationResult<E> {
        private final String name;

        public Leaf(String name, List<E> errors) {
            super(errors);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public <T> T cases(Function<Root<E>, T> whenRoot,
                           Function<Node<E>, T> whenNode,
                           Function<Leaf<E>, T> whenLeaf) {
            return whenLeaf.apply(this);
        }
    }
}
