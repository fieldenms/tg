package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;

abstract class FunctionCompoundCondition0<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IFunctionCompoundCondition0<T, ET> {

	protected FunctionCompoundCondition0(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T nextForFunctionCompoundCondition0(final EqlSentenceBuilder builder);

	@Override
	public ICaseWhenFunctionArgument<T, ET> then() {
		return createCaseWhenFunctionArgument(builder.then());
	}

	@Override
	public IFunctionWhere0<T, ET> and() {
		return createFunctionWhere0(builder.and());
	}

	@Override
	public IFunctionWhere0<T, ET> or() {
		return createFunctionWhere0(builder.or());
	}

	private FunctionWhere0<T, ET> createFunctionWhere0(final EqlSentenceBuilder builder) {
		return new FunctionWhere0<T, ET>(builder) {

			@Override
			protected T nextForFunctionWhere0(final EqlSentenceBuilder builder) {
				return FunctionCompoundCondition0.this.nextForFunctionCompoundCondition0(builder);
			}

		};
	}

	private CaseWhenFunctionArgument<T, ET> createCaseWhenFunctionArgument(final EqlSentenceBuilder builder) {
		return new CaseWhenFunctionArgument<T, ET>(builder) {

			@Override
			protected T nextForCaseWhenFunctionArgument(final EqlSentenceBuilder builder) {
				return FunctionCompoundCondition0.this.nextForFunctionCompoundCondition0(builder);
			}

		};
	}

}
