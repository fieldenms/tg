package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionElseEnd;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionLastArgument;

abstract class CaseWhenFunctionElseEnd<T, ET extends AbstractEntity<?>> //
		extends CaseWhenFunctionEnd<T> //
		implements ICaseWhenFunctionElseEnd<T, ET> {

	protected CaseWhenFunctionElseEnd(final EqlSentenceBuilder builder) {
		super(builder);
	}

	@Override
	public ICaseWhenFunctionLastArgument<T, ET> otherwise() {
		return createCaseWhenFunctionLastArgument(builder.otherwise());
	}

	private CaseWhenFunctionLastArgument<T, ET> createCaseWhenFunctionLastArgument(final EqlSentenceBuilder builder) {
		return new CaseWhenFunctionLastArgument<T, ET>(builder) {

			@Override
			protected T nextForCaseWhenFunctionLastArgument(final EqlSentenceBuilder builder) {
				return CaseWhenFunctionElseEnd.this.nextForCaseWhenFunctionEnd(builder);
			}

		};
	}

}
