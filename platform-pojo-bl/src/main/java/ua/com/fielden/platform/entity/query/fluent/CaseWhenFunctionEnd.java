package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionEnd;

abstract class CaseWhenFunctionEnd<T> extends AbstractQueryLink implements ICaseWhenFunctionEnd<T> {

	abstract T nextForCaseWhenFunctionEnd();

    @Override
    public T end() {
        return copy(nextForCaseWhenFunctionEnd(), getTokens().endOfFunction());
    }

    @Override
    public T endAsInt() {
        return copy(nextForCaseWhenFunctionEnd(), getTokens().endOfFunction(TypeCastAsInteger.INSTANCE));
    }

    @Override
    public T endAsBool() {
        return copy(nextForCaseWhenFunctionEnd(), getTokens().endOfFunction(TypeCastAsBoolean.INSTANCE));
    }

    @Override
    public T endAsStr(final int length) {
        return copy(nextForCaseWhenFunctionEnd(), getTokens().endOfFunction(TypeCastAsString.getInstance(length)));
    }

    @Override
    public T endAsDecimal(final int presicion, final int scale) {
        return copy(nextForCaseWhenFunctionEnd(), getTokens().endOfFunction(TypeCastAsDecimal.getInstance(presicion, scale)));
    }
}