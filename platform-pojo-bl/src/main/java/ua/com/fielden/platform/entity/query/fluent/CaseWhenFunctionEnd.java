package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionEnd;

abstract class CaseWhenFunctionEnd<T> extends AbstractQueryLink implements ICaseWhenFunctionEnd<T> {

	abstract T getParent();

    @Override
    public T end() {
        return copy(getParent(), getTokens().endOfFunction());
    }

    @Override
    public T endAsInt() {
        return copy(getParent(), getTokens().endOfFunction(TypeCastAsInteger.INSTANCE));
    }

    @Override
    public T endAsBool() {
        return copy(getParent(), getTokens().endOfFunction(TypeCastAsBoolean.INSTANCE));
    }

    @Override
    public T endAsStr(final int length) {
        return copy(getParent(), getTokens().endOfFunction(TypeCastAsString.getInstance(length)));
    }

    @Override
    public T endAsDecimal(final int presicion, final int scale) {
        return copy(getParent(), getTokens().endOfFunction(TypeCastAsDecimal.getInstance(presicion, scale)));
    }
}