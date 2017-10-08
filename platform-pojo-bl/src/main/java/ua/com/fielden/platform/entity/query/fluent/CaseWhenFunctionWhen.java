package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;

abstract class CaseWhenFunctionWhen<T, ET extends AbstractEntity<?>> //
		extends CaseWhenFunctionElseEnd<T, ET> //
		implements ICaseWhenFunctionWhen<T, ET> {

    protected CaseWhenFunctionWhen(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	public IFunctionWhere0<T, ET> when() {
		return createFunctionWhere0(getTokens().conditionStart());
	}
	
	private FunctionWhere0<T, ET> createFunctionWhere0(final Tokens tokens) {
		return new FunctionWhere0<T, ET>(tokens) {

			@Override
			protected T nextForFunctionWhere0(final Tokens tokens) {
				return CaseWhenFunctionWhen.this.nextForCaseWhenFunctionEnd(tokens);
			}

		};
	}
}