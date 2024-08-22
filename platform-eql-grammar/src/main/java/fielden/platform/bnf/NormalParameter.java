package fielden.platform.bnf;

public record NormalParameter(Class<?> type) implements Parameter {

    @Override
    public String toString() {
        return type.getSimpleName();
    }

}
