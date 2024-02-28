package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionEnd;

abstract class CaseWhenFunctionEnd<T> //
		extends AbstractQueryLink //
		implements ICaseWhenFunctionEnd<T> {

	protected CaseWhenFunctionEnd(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T nextForCaseWhenFunctionEnd(final EqlSentenceBuilder builder);

	@Override
	public T end() {
		return nextForCaseWhenFunctionEnd(builder.endOfFunction());
	}

	@Override
	public T endAsInt() {
		return nextForCaseWhenFunctionEnd(builder.endOfFunction(TypeCastAsInteger.INSTANCE));
	}

	@Override
	public T endAsBool() {
		return nextForCaseWhenFunctionEnd(builder.endOfFunction(TypeCastAsBoolean.INSTANCE));
	}

	@Override
	public T endAsStr(final int length) {
		return nextForCaseWhenFunctionEnd(builder.endOfFunction(TypeCastAsString.getInstance(length)));
	}

	@Override
	public T endAsDecimal(final int presicion, final int scale) {
		return nextForCaseWhenFunctionEnd(builder.endOfFunction(TypeCastAsDecimal.getInstance(presicion, scale)));
	}

}
