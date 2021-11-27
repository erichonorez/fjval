package org.h5z.jval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class TreeModule {

    public static class Tree {
        private final String path;
        private final List<Tree> forest;

        public Tree(String path, List<Tree> forest) {
            this.path = path;
            this.forest = forest;
        }
    }

    public static final class Path {
        private final List<String> parts;

        public Path(final String... parts) {
            this.parts = Arrays.asList(parts);
        }

        public Path(final List<String> parts) {
            this.parts = parts;
        }

        public List<String> getParts() {
            return parts;
        }

        public static Path path(String... parts) {
            return new Path(parts);
        }

        public static Path ancestors(Path a, Path b) {
            List<String[]> zipped = IntStream.range(0, a.getParts().size())
                .mapToObj(i -> new String[]{a.getParts().get(i), b.getParts().get(i)})
                .collect(Collectors.toList());

            ArrayList<String> ancestors = new ArrayList<>();
            for (String[] ps : zipped) {
                if (!ps[0].equals(ps[1])) {
                    break;
                }
                ancestors.add(ps[0]);
            }
            return new Path(ancestors);
        }

        // TODO rewrite it
        public static String fold(Path p, String zero, BinaryOperator<String> fn) {
            return p.getParts()
                .stream()
                .reduce(zero, fn);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Path)) return false;
            Path path = (Path) o;
            return getParts().equals(path.getParts());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getParts());
        }
    }

}
