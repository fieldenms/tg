package fielden.platform.bnf;

public record VarArityParameter(Class<?> type) implements Parameter {

    @Override
    public String toString() {
        return "%s...".formatted(type.getSimpleName());
    }

}
