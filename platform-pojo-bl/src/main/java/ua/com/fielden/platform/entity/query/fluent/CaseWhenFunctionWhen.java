package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;

abstract class CaseWhenFunctionWhen<T, ET extends AbstractEntity<?>> //
		extends CaseWhenFunctionElseEnd<T, ET> //
		implements ICaseWhenFunctionWhen<T, ET> {

	protected CaseWhenFunctionWhen(final EqlSentenceBuilder builder) {
		super(builder);
	}

	@Override
	public IFunctionWhere0<T, ET> when() {
		return createFunctionWhere0(builder.conditionStart());
	}

	private FunctionWhere0<T, ET> createFunctionWhere0(final EqlSentenceBuilder builder) {
		return new FunctionWhere0<T, ET>(builder) {

			@Override
			protected T nextForFunctionWhere0(final EqlSentenceBuilder builder) {
				return CaseWhenFunctionWhen.this.nextForCaseWhenFunctionEnd(builder);
			}

		};
	}

}
