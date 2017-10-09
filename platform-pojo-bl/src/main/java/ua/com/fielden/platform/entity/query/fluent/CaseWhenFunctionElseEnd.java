package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionElseEnd;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionLastArgument;

abstract class CaseWhenFunctionElseEnd<T, ET extends AbstractEntity<?>> //
		extends CaseWhenFunctionEnd<T> //
		implements ICaseWhenFunctionElseEnd<T, ET> {

    protected CaseWhenFunctionElseEnd(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	public ICaseWhenFunctionLastArgument<T, ET> otherwise() {
		return createCaseWhenFunctionLastArgument(getTokens());
	}

	private CaseWhenFunctionLastArgument<T, ET> createCaseWhenFunctionLastArgument(final Tokens tokens) {
		return new CaseWhenFunctionLastArgument<T, ET>(tokens) {

			@Override
			protected T nextForCaseWhenFunctionLastArgument(final Tokens tokens) {
				return CaseWhenFunctionElseEnd.this.nextForCaseWhenFunctionEnd(tokens);
			}

		};
	}
}