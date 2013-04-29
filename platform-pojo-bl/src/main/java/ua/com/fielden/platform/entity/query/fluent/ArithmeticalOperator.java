package ua.com.fielden.platform.entity.query.fluent;

public enum ArithmeticalOperator {
    ADD("+"), SUB("-"), DIV("/"), MULT("*"), MOD("%");

    private final String value;

    ArithmeticalOperator(final String value) {
	this.value = value;
    }

    public String getValue() {
	return value;
    }

    @Override
    public String toString() {
	return value;
    }
}