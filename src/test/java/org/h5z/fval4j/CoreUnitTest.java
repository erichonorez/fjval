package org.h5z.fval4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h5z.fval4j.Core.every;
import static org.h5z.fval4j.Core.keyed;
import static org.h5z.fval4j.Core.list;
import static org.h5z.fval4j.Core.optional;
import static org.h5z.fval4j.Core.prop;
import static org.h5z.fval4j.Core.required;
import static org.h5z.fval4j.Core.sequentially;
import static org.h5z.fval4j.Trie.trie;
import static org.h5z.fval4j.data.ValidationResult.invalid;
import static org.h5z.fval4j.data.ValidationResult.valid;
import static org.h5z.fval4j.data.ValidationResult.validationResult;
import static org.h5z.fval4j.Validators.gt;
import static org.organicdesign.fp.StaticImports.map;
import static org.organicdesign.fp.StaticImports.tup;
import static org.organicdesign.fp.StaticImports.vec;

import org.h5z.fval4j.Core.Validator;
import org.h5z.fval4j.data.ValidationResult;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.organicdesign.fp.collections.ImList;

public class CoreUnitTest {

    @Nested
    @DisplayName("keyed")
    class KeyedTest { // named KeyedTest to avoid collision with the class Keyed

        Validator<String, String, String> containsA = s -> s.contains("a")
                ? valid(s, s)
                : invalid(s, "Does not contains a");

        @Nested
        @DisplayName("With simple validator")
        class WithValidator {

            @Test
            @DisplayName("Returns a keyed validator")
            void t0() {
                Validator<String, String, String> keyedContainsA = keyed("x", containsA);
                ValidationResult<String, String, String> validated = keyedContainsA.validate("");

                assertThat(validated)
                        .isEqualTo(
                            validationResult(
                                trie(vec(), map(
                                    tup("x", trie(vec("Does not contains a"), map())))),
                                "",
                                null));
            }

        }

        @Nested
        @DisplayName("With keyed validator")
        class WithKeyedValidator {

            @Test
            @DisplayName("Returns a keyed validator")
            void t0() {
                var yxContainsA = keyed("y", keyed("x", containsA));
                ValidationResult<String, String, String> validated = yxContainsA.validate("");

                assertThat(validated)
                        .isEqualTo(
                            validationResult(
                                trie(vec(), map(
                                    tup("y", trie(vec(), map(
                                        tup("x", trie(vec("Does not contains a"), map()))))))),
                                "",
                                null));
            }

        }

    }

    @Nested
    @DisplayName("sequentially")
    class Sequentially {

        @Test
        @DisplayName("Returns a valid trie if all validators succeed")
        void t0() {
            Validator<Integer, Integer, String> sequentially = sequentially(
                    keyed("x", gt(0, () -> "Should be gt 0")),
                    keyed("y", gt(0, () -> "Should be gt 0")));

            ValidationResult<String, Integer, Integer> result = sequentially.apply(1);
            assertThat(result)
                    .isEqualTo(
                        validationResult(
                            trie(vec(), map(
                                tup("x", trie(vec(), map())),
                                tup("y", trie(vec(), map())))),
                            1,
                            1));
        }

        @Test
        @DisplayName("Returns a trie with only the errors of the first failed validator")
        void t1() {
            Validator<Integer, Integer, String> sequentially = sequentially(
                    keyed("x", gt(2, () -> "Should be gt 2")),
                    keyed("y", gt(0, () -> "Should be gt 0")));

            ValidationResult<String, Integer, Integer> result = sequentially.apply(1);
            assertThat(result)
                    .isEqualTo(
                        validationResult(
                            trie(vec(), map(
                                tup("x", trie(vec("Should be gt 2"), map())))),
                            1,
                            null));
        }

    }

    @Nested
    @DisplayName("every")
    class Every {

        @Test
        @DisplayName("Returns a valid trie if all validators succeeded")
        void t0() {
            Validator<Integer, Integer, String> every = every(
                    keyed("x", gt(0, () -> "Should be gt 0")),
                    keyed("y", gt(0, () -> "Should be gt 0")));

            ValidationResult<String, Integer, Integer> result = every.apply(1);
            assertThat(result)
                    .isEqualTo( 
                        validationResult(
                            trie(vec(), map(
                                tup("x", trie(vec(), map())),
                                tup("y", trie(vec(), map())))),
                            1,
                            1));
        }

        @Test
        @DisplayName("Returns a trie with the errors of all failed validators otherwise.")
        void t1() {
            Validator<Integer, Integer, String> every = every(
                    keyed("a", gt(-1, () -> "Should be gt -1")),
                    keyed("b", gt(1, () -> "Should be gt 1")),
                    keyed("c", gt(2, () -> "Should be gt 2")),
                    keyed("d", gt(3, () -> "Should be gt 3")),
                    keyed("e", gt(-1, () -> "Should be gt -1")));

            ValidationResult<String, Integer, Integer> result = every.apply(0);
            assertThat(result)
                    .isEqualTo(
                        validationResult(
                            trie(vec(), map(
                                tup("a", trie(vec(), map())),
                                tup("b", trie(vec("Should be gt 1"), map())),
                                tup("c", trie(vec("Should be gt 2"), map())),
                                tup("d", trie(vec("Should be gt 3"), map())),
                                tup("e", trie(vec(), map())))),
                            0,
                            null));
        }

    }

    @Nested
    @DisplayName("prop")
    static class Prop {

        Validator<Integer, Integer, String> gt0 = v -> v > 0 ? valid(v, v) : invalid(v, "gt0");
        Validator<RootClass, Integer, String> rootValidator = keyed("x",
                prop(RootClass::getX, keyed("y",
                        prop(NestedClass::getY, gt0))));

        @Test
        @DisplayName("Returns a valid trie if the given validator succeeded")
        void t0() {
            RootClass a = new RootClass(new NestedClass(1));
            assertThat(
                    rootValidator.apply(a))
                            .isEqualTo(
                                validationResult(
                                    trie(vec(), map(tup("x", trie(vec(), map(tup("y", trie(vec(), map()))))))),
                                    a,
                                    1));
        }

        @Test
        @DisplayName("Returns the errors return by the given validator otherwise")
        void t1() {
            RootClass a = new RootClass(new NestedClass(0));
            assertThat(
                    rootValidator.apply(a)).isEqualTo(
                            validationResult(
                                trie(vec(), map(
                                    tup("x", trie(vec(), map(
                                            tup("y", trie(vec("gt0"), map()))))))),
                                a,
                                null));

        }

        static class RootClass {
            private final NestedClass x;

            RootClass(NestedClass x) {
                this.x = x;
            }

            public NestedClass getX() {
                return this.x;
            }
        }

        static class NestedClass {
            private final int y;

            NestedClass(int y) {
                this.y = y;
            }

            public int getY() {
                return this.y;
            }
        }

    }

    @Nested
    @DisplayName("list")
    class List {

        @Nested
        @DisplayName("With simple validator")
        class WithSimpleValidator {

            @Test
            @DisplayName("Retuns a valid trie if all the elements in the list are valid")
            void t0() {
                
                var listValidator = list(gt(0, () -> "Should be gt 0"), Core::everyEl);
                var aList = vec(1, 2, 3, 4, 5);
                assertThat(listValidator.apply(aList))
                        .isEqualTo(
                            validationResult(
                                trie(vec(), map(
                                    tup("0", trie(vec(), map())),
                                    tup("1", trie(vec(), map())),
                                    tup("2", trie(vec(), map())),
                                    tup("3", trie(vec(), map())),
                                    tup("4", trie(vec(), map())))),
                                aList,
                                aList  
                        ));
            }

            @Test
            @DisplayName("Return a trie with the errors of all failed validators")
            void t1() {
                var listValidator = list(gt(0, () -> "Should be gt 0"), Core::everyEl);
                var aList = vec(0, 0, 0, 0, 0);
                assertThat(listValidator.apply(aList))
                        .isEqualTo(
                            validationResult(
                                trie(vec(), map(
                                    tup("0", trie(vec("Should be gt 0"), map())),
                                    tup("1", trie(vec("Should be gt 0"), map())),
                                    tup("2", trie(vec("Should be gt 0"), map())),
                                    tup("3", trie(vec("Should be gt 0"), map())),
                                    tup("4", trie(vec("Should be gt 0"), map())))),
                                aList,
                                vec(null, null, null, null, null)));
            }

        }

        @Nested
        @DisplayName("With keyed validator")
        class WithKeyedValidator {

            Validator<Integer, Integer, String> xValidator = gt(0, () -> "Should be gt 0");
            Validator<Point, Integer, String> pointValidator = keyed("x", prop(Point::getX, xValidator));
            Validator<java.util.List<Point>, java.util.List<Integer>, String> listOfPointValidator = list(pointValidator, Core::everyEl);

            @Test
            @DisplayName("Retuns a valid trie if all the elements in the list are valid")
            void t0() {
                @NotNull
                ImList<Point> points = vec(
                        new Point(1),
                        new Point(1),
                        new Point(1),
                        new Point(1),
                        new Point(1));
                ValidationResult<String, java.util.List<Point>, java.util.List<Integer>> result = listOfPointValidator.apply(points);

                assertThat(result).isEqualTo(
                    validationResult(
                        trie(vec(), map(
                            tup("0", trie(vec(), map(
                                    tup("x", trie(vec(), map()))))),
                            tup("1", trie(vec(), map(
                                    tup("x", trie(vec(), map()))))),
                            tup("2", trie(vec(), map(
                                    tup("x", trie(vec(), map()))))),
                            tup("3", trie(vec(), map(
                                    tup("x", trie(vec(), map()))))),
                            tup("4", trie(vec(), map(
                                    tup("x", trie(vec(), map()))))))),
                        points,
                        vec(1, 1, 1, 1, 1)));
            }

            @Test
            @DisplayName("Return a trie with the errors of all failed validators")
            void t1() {
                @NotNull
                ImList<Point> points = vec(
                        new Point(0),
                        new Point(0),
                        new Point(0),
                        new Point(0),
                        new Point(0));
                assertThat(listOfPointValidator.apply(points)).isEqualTo(
                            validationResult(
                                trie(vec(), map(
                                    tup("0", trie(vec(), map(
                                            tup("x", trie(vec("Should be gt 0"), map()))))),
                                    tup("1", trie(vec(), map(
                                            tup("x", trie(vec("Should be gt 0"), map()))))),
                                    tup("2", trie(vec(), map(
                                            tup("x", trie(vec("Should be gt 0"), map()))))),
                                    tup("3", trie(vec(), map(
                                            tup("x", trie(vec("Should be gt 0"), map()))))),
                                    tup("4", trie(vec(), map(
                                            tup("x", trie(vec("Should be gt 0"), map()))))))),
                                points,
                                vec(null, null, null, null, null)));
            }

            class Point {
                private final int x;

                public Point(int x) {
                    this.x = x;
                }

                public int getX() {
                    return this.x;
                }

            }

        }

    }

    @Nested
    @DisplayName("required")
    class Required {

        Validator<Integer, Integer, String> xValidator = gt(0, () -> "Should be gt 0");
        Validator<Point, Integer, String> pointValidator = required( // the validated object `Point` is required
                keyed("x", prop(Point::getX, xValidator)),
                () -> "Required");

        @Test
        @DisplayName("Returns an invalid trie with the given error if the validated value is null")
        void t0() {
            assertThat(pointValidator.validate(null))
                    .isEqualTo(validationResult(trie(vec("Required"), map()), null, null));
        }

        @Test
        @DisplayName("Returns an invalid trie with the errors of the validators if the validated value is not null and invalid")
        void t1() {
            Point aPoint = new Point(-1);
            assertThat(pointValidator.validate(aPoint))
                    .isEqualTo(
                        validationResult(
                            trie(vec(), map(
                                tup("x", trie(vec("Should be gt 0"), map())))),
                            aPoint,
                            null));
        }

        @Test
        @DisplayName("Returns an valid trie if the validated value is not null and valid")
        void t2() {
            Point aPoint = new Point(1);
            assertThat(pointValidator.validate(aPoint))
                    .isEqualTo(
                        validationResult(
                            trie(vec(), map(
                                tup("x", trie(vec(), map())))),
                            aPoint,
                            1));
        }

        class Point {
            private final Integer x;

            public Point(Integer x) {
                this.x = x;
            }

            public int getX() {
                return this.x;
            }

        }

    }

    @Nested
    @DisplayName("Optional")
    class Optional {
        
        Validator<Integer, Integer, String> xValidator = gt(0, () -> "Should be gt 0");
        Validator<Point, Integer, String> pointValidator = optional( // the validated object `Point` is optional
                keyed("x", prop(Point::getX, xValidator))
        );

        @Test
        @DisplayName("Returns a valid trie if the validated value is null") 
        void t0() {
            assertThat(pointValidator.validate(null))
                    .isEqualTo(
                        validationResult(trie(vec(), map()), null, null));
        }

        @Test
        @DisplayName("Returns a valid trie if the validated value is not null and is valid") 
        void t1() {
            Point aPoint = new Point(1);
            assertThat(pointValidator.validate(aPoint))
                    .isEqualTo(
                        validationResult(
                            trie(vec(), map(
                                tup("x", trie(vec(), map())))),
                            aPoint,
                            1));
        }

        @Test
        @DisplayName("Returns a invalid trie if the validated value is not null and is invalid")
        void t2() {
            Point aPoint = new Point(-1);
            assertThat(pointValidator.validate(aPoint))
                    .isEqualTo(
                        validationResult(
                            trie(vec(), map(
                                tup("x", trie(vec("Should be gt 0"), map())))),
                            aPoint,
                            null));
        }

        class Point {
            private final int x;

            public Point(int x) {
                this.x = x;
            }

            public int getX() {
                return this.x;
            }

        }

    }
}
