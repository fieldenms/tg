package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;

abstract class CaseWhenFunctionWhen<T, ET extends AbstractEntity<?>> extends CaseWhenFunctionElseEnd<T, ET> implements ICaseWhenFunctionWhen<T, ET> {

	private FunctionWhere0<T, ET> createFunctionWhere0() {
    	return new FunctionWhere0<T, ET>(){

			@Override
			T nextForFunctionWhere0() {
				return CaseWhenFunctionWhen.this.nextForCaseWhenFunctionEnd();
			}
        	
        };
	}
	
    @Override
    public IFunctionWhere0<T, ET> when() {
    	return copy(createFunctionWhere0(), getTokens().conditionStart());
    }
}