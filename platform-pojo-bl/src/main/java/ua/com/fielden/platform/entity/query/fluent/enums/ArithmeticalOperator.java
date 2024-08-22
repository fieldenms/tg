package ua.com.fielden.platform.entity.query.fluent.enums;

public enum ArithmeticalOperator {
    ADD("+"), SUB("-"), DIV("/"), MULT("*"), MOD("%");

    public final String value;

    ArithmeticalOperator(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

}
