package org.h5z.jval;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Path {

    public abstract String current();
    public abstract Path next();
    public abstract boolean isNil();

    static class Nil extends Path {
        public Nil() { }

        @Override
        public String current() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Path next() {
            return this;
        }

        @Override
        public boolean isNil() {
            return true;
        }
    }

    static class Cons extends Path {
        private final String head;
        private final Path tail;
        public Cons(String head, Path tail) {
            this.head = head;
            this.tail = tail;
        }

        @Override
        public String current() {
            return this.head;
        }

        @Override
        public Path next() {
            return this.tail;
        }

        @Override
        public boolean isNil() {
            return false;
        }
    }

    public static Path cons(String head, Path tail) { return new Cons(head, tail); }

    public static Path nil() { return new Nil(); }

    public static Path fromList(List<String> xs) {
        ArrayList<String> dest = new ArrayList<>(xs);
        Collections.reverse(dest);
        Path path = nil();
        for (String x : dest) {
            path = cons(x, path);
        }
        return path;
    }

    public static Path parse(String path) {
        if (!path.startsWith("/")) { 
            return nil(); 
        }
        
        if ("/".equals(path)) { 
            return cons("/", nil()); 
        }

        String[] parts = path.split("/");
        List<String> partList = Arrays.asList(parts)
            .subList(1, parts.length);

        return fromList(partList);
    }

}
