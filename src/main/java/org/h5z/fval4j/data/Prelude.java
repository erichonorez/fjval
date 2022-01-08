package org.h5z.fval4j.data;

import java.io.Serializable;

public final class Prelude {
 
    @FunctionalInterface
    public interface F1<A, R> {
        R apply(A a);
    }

    @FunctionalInterface
    public interface F2<A, B, R> {
        R apply(A a, B b);
    }

    @FunctionalInterface
    public interface F3<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    @FunctionalInterface
    public interface F4<A, B, C, D, R> {
        R apply(A a, B b, C c, D d);
    }

    @FunctionalInterface
    public interface F5<A, B, C, D, E, R> {
        R apply(A a, B b, C c, D d, E e);
    }

    @FunctionalInterface
    public interface F6<A, B, C, D, E, F$, R> {
        R apply(A a, B b, C c, D d, E e, F$ f);
    }

    @FunctionalInterface
    public interface F7<A, B, C, D, E, F$, G, R> {
        R apply(A a, B b, C c, D d, E e, F$ f, G g);
    }

    @FunctionalInterface
    public interface F8<A, B, C, D, E, F$, G, H, R> {
        R apply(A a, B b, C c, D d, E e, F$ f, G g, H h);
    }

    public static class Tuple2<A, B> implements Serializable {
        private final A _1;
        private final B _2;

        public Tuple2(A _1, B _2) {
            this._1 = _1;
            this._2 = _2;
        }

        public A _1() { return this._1; }
        public B _2() { return this._2; }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_1 == null) ? 0 : _1.hashCode());
            result = prime * result + ((_2 == null) ? 0 : _2.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Tuple2 other = (Tuple2) obj;
            if (_1 == null) {
                if (other._1 != null)
                    return false;
            } else if (!_1.equals(other._1))
                return false;
            if (_2 == null) {
                if (other._2 != null)
                    return false;
            } else if (!_2.equals(other._2))
                return false;
            return true;
        }
    }

    public static class Tuple3<A, B, C> implements Serializable {
        private final A _1;
        private final B _2;
        private final C _3;

        public Tuple3(A _1, B _2, C _3) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
        }

        public A _1() { return this._1; }
        public B _2() { return this._2; }
        public C _3() { return this._3; }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_1 == null) ? 0 : _1.hashCode());
            result = prime * result + ((_2 == null) ? 0 : _2.hashCode());
            result = prime * result + ((_3 == null) ? 0 : _3.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Tuple3 other = (Tuple3) obj;
            if (_1 == null) {
                if (other._1 != null)
                    return false;
            } else if (!_1.equals(other._1))
                return false;
            if (_2 == null) {
                if (other._2 != null)
                    return false;
            } else if (!_2.equals(other._2))
                return false;
            if (_3 == null) {
                if (other._3 != null)
                    return false;
            } else if (!_3.equals(other._3))
                return false;
            return true;
        }
    }

    public static class Tuple4<A, B, C, D> implements Serializable {
        private final A _1;
        private final B _2;
        private final C _3;
        private final D _4;

        public Tuple4(A _1, B _2, C _3, D _4) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
            this._4 = _4;
        }

        public A _1() { return this._1; }
        public B _2() { return this._2; }
        public C _3() { return this._3; }
        public D _4() { return this._4; }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_1 == null) ? 0 : _1.hashCode());
            result = prime * result + ((_2 == null) ? 0 : _2.hashCode());
            result = prime * result + ((_3 == null) ? 0 : _3.hashCode());
            result = prime * result + ((_4 == null) ? 0 : _4.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Tuple4 other = (Tuple4) obj;
            if (_1 == null) {
                if (other._1 != null)
                    return false;
            } else if (!_1.equals(other._1))
                return false;
            if (_2 == null) {
                if (other._2 != null)
                    return false;
            } else if (!_2.equals(other._2))
                return false;
            if (_3 == null) {
                if (other._3 != null)
                    return false;
            } else if (!_3.equals(other._3))
                return false;
            if (_4 == null) {
                if (other._4 != null)
                    return false;
            } else if (!_4.equals(other._4))
                return false;
            return true;
        }
    }
    
    public static class Tuple5<A, B, C, D, E> implements Serializable {
        private final A _1;
        private final B _2;
        private final C _3;
        private final D _4;
        private final E _5;

        public Tuple5(A _1, B _2, C _3, D _4, E _5) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
            this._4 = _4;
            this._5 = _5;
        }

        public A _1() { return this._1; }
        public B _2() { return this._2; }
        public C _3() { return this._3; }
        public D _4() { return this._4; }
        public E _5() { return this._5; }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_1 == null) ? 0 : _1.hashCode());
            result = prime * result + ((_2 == null) ? 0 : _2.hashCode());
            result = prime * result + ((_3 == null) ? 0 : _3.hashCode());
            result = prime * result + ((_4 == null) ? 0 : _4.hashCode());
            result = prime * result + ((_5 == null) ? 0 : _5.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Tuple5 other = (Tuple5) obj;
            if (_1 == null) {
                if (other._1 != null)
                    return false;
            } else if (!_1.equals(other._1))
                return false;
            if (_2 == null) {
                if (other._2 != null)
                    return false;
            } else if (!_2.equals(other._2))
                return false;
            if (_3 == null) {
                if (other._3 != null)
                    return false;
            } else if (!_3.equals(other._3))
                return false;
            if (_4 == null) {
                if (other._4 != null)
                    return false;
            } else if (!_4.equals(other._4))
                return false;
            if (_5 == null) {
                if (other._5 != null)
                    return false;
            } else if (!_5.equals(other._5))
                return false;
            return true;
        }
    }

    public static class Tuple6<A, B, C, D, E, F$> implements Serializable {
        private final A _1;
        private final B _2;
        private final C _3;
        private final D _4;
        private final E _5;
        private final F$ _6;

        public Tuple6(A _1, B _2, C _3, D _4, E _5, F$ _6) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
            this._4 = _4;
            this._5 = _5;
            this._6 = _6;
        }

        public A _1() { return this._1; }
        public B _2() { return this._2; }
        public C _3() { return this._3; }
        public D _4() { return this._4; }
        public E _5() { return this._5; }
        public F$ _6() { return this._6; }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_1 == null) ? 0 : _1.hashCode());
            result = prime * result + ((_2 == null) ? 0 : _2.hashCode());
            result = prime * result + ((_3 == null) ? 0 : _3.hashCode());
            result = prime * result + ((_4 == null) ? 0 : _4.hashCode());
            result = prime * result + ((_5 == null) ? 0 : _5.hashCode());
            result = prime * result + ((_6 == null) ? 0 : _6.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Tuple6 other = (Tuple6) obj;
            if (_1 == null) {
                if (other._1 != null)
                    return false;
            } else if (!_1.equals(other._1))
                return false;
            if (_2 == null) {
                if (other._2 != null)
                    return false;
            } else if (!_2.equals(other._2))
                return false;
            if (_3 == null) {
                if (other._3 != null)
                    return false;
            } else if (!_3.equals(other._3))
                return false;
            if (_4 == null) {
                if (other._4 != null)
                    return false;
            } else if (!_4.equals(other._4))
                return false;
            if (_5 == null) {
                if (other._5 != null)
                    return false;
            } else if (!_5.equals(other._5))
                return false;
            if (_6 == null) {
                if (other._6 != null)
                    return false;
            } else if (!_6.equals(other._6))
                return false;
            return true;
        }
    }

    public static class Tuple7<A, B, C, D, E, F$, G> implements Serializable {
        private final A _1;
        private final B _2;
        private final C _3;
        private final D _4;
        private final E _5;
        private final F$ _6;
        private final G _7;

        public Tuple7(A _1, B _2, C _3, D _4, E _5, F$ _6, G _7) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
            this._4 = _4;
            this._5 = _5;
            this._6 = _6;
            this._7 = _7;
        }

        public A _1() { return this._1; }
        public B _2() { return this._2; }
        public C _3() { return this._3; }
        public D _4() { return this._4; }
        public E _5() { return this._5; }
        public F$ _6() { return this._6; }
        public G _7() { return this._7; }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_1 == null) ? 0 : _1.hashCode());
            result = prime * result + ((_2 == null) ? 0 : _2.hashCode());
            result = prime * result + ((_3 == null) ? 0 : _3.hashCode());
            result = prime * result + ((_4 == null) ? 0 : _4.hashCode());
            result = prime * result + ((_5 == null) ? 0 : _5.hashCode());
            result = prime * result + ((_6 == null) ? 0 : _6.hashCode());
            result = prime * result + ((_7 == null) ? 0 : _7.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Tuple7 other = (Tuple7) obj;
            if (_1 == null) {
                if (other._1 != null)
                    return false;
            } else if (!_1.equals(other._1))
                return false;
            if (_2 == null) {
                if (other._2 != null)
                    return false;
            } else if (!_2.equals(other._2))
                return false;
            if (_3 == null) {
                if (other._3 != null)
                    return false;
            } else if (!_3.equals(other._3))
                return false;
            if (_4 == null) {
                if (other._4 != null)
                    return false;
            } else if (!_4.equals(other._4))
                return false;
            if (_5 == null) {
                if (other._5 != null)
                    return false;
            } else if (!_5.equals(other._5))
                return false;
            if (_6 == null) {
                if (other._6 != null)
                    return false;
            } else if (!_6.equals(other._6))
                return false;
            if (_7 == null) {
                if (other._7 != null)
                    return false;
            } else if (!_7.equals(other._7))
                return false;
            return true;
        }
    }

    public static class Tuple8<A, B, C, D, E, F$, G, H> implements Serializable {
        private final A _1;
        private final B _2;
        private final C _3;
        private final D _4;
        private final E _5;
        private final F$ _6;
        private final G _7;
        private final H _8;

        public Tuple8(A _1, B _2, C _3, D _4, E _5, F$ _6, G _7, H _8) {
            this._1 = _1;
            this._2 = _2;
            this._3 = _3;
            this._4 = _4;
            this._5 = _5;
            this._6 = _6;
            this._7 = _7;
            this._8 = _8;
        }

        public A _1() { return this._1; }
        public B _2() { return this._2; }
        public C _3() { return this._3; }
        public D _4() { return this._4; }
        public E _5() { return this._5; }
        public F$ _6() { return this._6; }
        public G _7() { return this._7; }
        public H _8() { return this._8; }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_1 == null) ? 0 : _1.hashCode());
            result = prime * result + ((_2 == null) ? 0 : _2.hashCode());
            result = prime * result + ((_3 == null) ? 0 : _3.hashCode());
            result = prime * result + ((_4 == null) ? 0 : _4.hashCode());
            result = prime * result + ((_5 == null) ? 0 : _5.hashCode());
            result = prime * result + ((_6 == null) ? 0 : _6.hashCode());
            result = prime * result + ((_7 == null) ? 0 : _7.hashCode());
            result = prime * result + ((_8 == null) ? 0 : _8.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Tuple8 other = (Tuple8) obj;
            if (_1 == null) {
                if (other._1 != null)
                    return false;
            } else if (!_1.equals(other._1))
                return false;
            if (_2 == null) {
                if (other._2 != null)
                    return false;
            } else if (!_2.equals(other._2))
                return false;
            if (_3 == null) {
                if (other._3 != null)
                    return false;
            } else if (!_3.equals(other._3))
                return false;
            if (_4 == null) {
                if (other._4 != null)
                    return false;
            } else if (!_4.equals(other._4))
                return false;
            if (_5 == null) {
                if (other._5 != null)
                    return false;
            } else if (!_5.equals(other._5))
                return false;
            if (_6 == null) {
                if (other._6 != null)
                    return false;
            } else if (!_6.equals(other._6))
                return false;
            if (_7 == null) {
                if (other._7 != null)
                    return false;
            } else if (!_7.equals(other._7))
                return false;
            if (_8 == null) {
                if (other._8 != null)
                    return false;
            } else if (!_8.equals(other._8))
                return false;
            return true;
        }
    }

}
