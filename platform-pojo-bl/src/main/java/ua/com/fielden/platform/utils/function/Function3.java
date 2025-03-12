package ua.com.fielden.platform.utils.function;

@FunctionalInterface
public interface Function3<A1, A2, A3, R> {

    R apply(A1 a1, A2 a2, A3 a3);

    static <X1, X2, X3> Function3<X1, X2, X3, X1> firstArg() {
        return (x1, x2, x3) -> x1;
    }

    static <X1, X2, X3> Function3<X1, X2, X3, X3> thirdArg() {
        return (x1, x2, x3) -> x3;
    }

}
