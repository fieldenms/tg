package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;

abstract class ConcatFunctionWith<T, ET extends AbstractEntity<?>> extends AbstractQueryLink implements IConcatFunctionWith<T, ET> {

	abstract T getParent();

    @Override
    public T end() {
        return copy(getParent(), getTokens().endOfFunction());
    }

    @Override
    public IConcatFunctionArgument<T, ET> with() {
    	return copy(new ConcatFunctionArgument<T, ET>(){

			@Override
			T getParent3() {
				return ConcatFunctionWith.this.getParent();
			}}, getTokens());
    }
}