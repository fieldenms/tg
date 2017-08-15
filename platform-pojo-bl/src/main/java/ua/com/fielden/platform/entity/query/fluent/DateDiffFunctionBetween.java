package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionBetween;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

abstract class DateDiffFunctionBetween<T, ET extends AbstractEntity<?>> extends AbstractQueryLink implements IDateDiffFunctionBetween<T, ET> {

	abstract T nextForDateDiffFunctionBetween();

    @Override
    public IFunctionLastArgument<T, ET> and() {
    	return copy(createFunctionLastArgument(), getTokens());
    }

	private FunctionLastArgument<T, ET> createFunctionLastArgument() {
		return new FunctionLastArgument<T, ET>(){

			@Override
			T nextForFunctionLastArgument() {
				return DateDiffFunctionBetween.this.nextForDateDiffFunctionBetween();
			}
        	
        };
	}
}