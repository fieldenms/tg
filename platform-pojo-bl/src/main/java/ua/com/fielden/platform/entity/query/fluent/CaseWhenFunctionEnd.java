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
		return nextForCaseWhenFunctionEnd(builder.endAsInt());
	}

	@Override
	public T endAsBool() {
		return nextForCaseWhenFunctionEnd(builder.endAsBool());
	}

	@Override
	public T endAsStr(final int length) {
		return nextForCaseWhenFunctionEnd(builder.endAsStr(length));
	}

	@Override
	public T endAsDecimal(final int presicion, final int scale) {
		return nextForCaseWhenFunctionEnd(builder.endAsDecimal(presicion, scale));
	}

}
