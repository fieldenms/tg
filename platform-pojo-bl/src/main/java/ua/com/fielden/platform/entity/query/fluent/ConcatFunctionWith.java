package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;

abstract class ConcatFunctionWith<T, ET extends AbstractEntity<?>> extends AbstractQueryLink implements IConcatFunctionWith<T, ET> {

	abstract T nextForConcatFunctionWith();

    @Override
    public T end() {
        return copy(nextForConcatFunctionWith(), getTokens().endOfFunction());
    }

    @Override
    public IConcatFunctionArgument<T, ET> with() {
    	return copy(createConcatFunctionArgument(), getTokens());
    }

	private ConcatFunctionArgument<T, ET> createConcatFunctionArgument() {
		return new ConcatFunctionArgument<T, ET>(){

			@Override
			T nextForConcatFunctionArgument() {
				return ConcatFunctionWith.this.nextForConcatFunctionWith();
			}};
	}
}