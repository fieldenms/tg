package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionEnd;

public class CaseWhenFunctionEnd<T> extends AbstractQueryLink implements ICaseWhenFunctionEnd<T> {

    T parent;

    CaseWhenFunctionEnd(final Tokens queryTokens, final T parent) {
        super(queryTokens);
        this.parent = parent;
    }

    @Override
    public T end() {
        return copy(parent, getTokens().endOfFunction());
    }

    @Override
    public T endAsInt() {
        return copy(parent, getTokens().endOfFunction(TypeCastAsInteger.INSTANCE));
    }

    @Override
    public T endAsBool() {
        return copy(parent, getTokens().endOfFunction(TypeCastAsBoolean.INSTANCE));
    }

    @Override
    public T endAsStr(final int length) {
        return copy(parent, getTokens().endOfFunction(TypeCastAsString.getInstance(length)));
    }

    @Override
    public T endAsDecimal(final int presicion, final int scale) {
        return copy(parent, getTokens().endOfFunction(TypeCastAsDecimal.getInstance(presicion, scale)));
    }
}