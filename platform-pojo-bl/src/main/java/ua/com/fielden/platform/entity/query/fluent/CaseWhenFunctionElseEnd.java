package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionElseEnd;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionLastArgument;

abstract class CaseWhenFunctionElseEnd<T, ET extends AbstractEntity<?>> extends CaseWhenFunctionEnd<T> implements ICaseWhenFunctionElseEnd<T, ET> {

	@Override
    public ICaseWhenFunctionLastArgument<T, ET> otherwise() {
		return copy(createCaseWhenFunctionLastArgument(), getTokens());
    }

	private CaseWhenFunctionLastArgument<T, ET> createCaseWhenFunctionLastArgument() {
		return new CaseWhenFunctionLastArgument<T, ET>(){

			@Override
			T nextForCaseWhenFunctionLastArgument() {
				return CaseWhenFunctionElseEnd.this.nextForCaseWhenFunctionEnd();
			}
        	
        };
	}
}