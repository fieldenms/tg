package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionEnd;

abstract class CaseWhenFunctionEnd<T> //
		extends AbstractQueryLink //
		implements ICaseWhenFunctionEnd<T> {

    protected CaseWhenFunctionEnd(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForCaseWhenFunctionEnd(final Tokens tokens);

	@Override
	public T end() {
		return nextForCaseWhenFunctionEnd(getTokens().endOfFunction());
	}

	@Override
	public T endAsInt() {
		return nextForCaseWhenFunctionEnd(getTokens().endOfFunction(TypeCastAsInteger.INSTANCE));
	}

	@Override
	public T endAsBool() {
		return nextForCaseWhenFunctionEnd(getTokens().endOfFunction(TypeCastAsBoolean.INSTANCE));
	}

	@Override
	public T endAsStr(final int length) {
		return nextForCaseWhenFunctionEnd(getTokens().endOfFunction(TypeCastAsString.getInstance(length)));
	}

	@Override
	public T endAsDecimal(final int presicion, final int scale) {
		return nextForCaseWhenFunctionEnd(getTokens().endOfFunction(TypeCastAsDecimal.getInstance(presicion, scale)));
	}
}