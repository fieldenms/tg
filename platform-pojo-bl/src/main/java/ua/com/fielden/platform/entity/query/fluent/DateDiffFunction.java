package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunction;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionArgument;

abstract class DateDiffFunction<T, ET extends AbstractEntity<?>> extends AbstractQueryLink implements IDateDiffFunction<T, ET> {

	abstract T getParent();

    @Override
    public IDateDiffFunctionArgument<T, ET> between() {
        return copy(new DateDiffFunctionArgument<T, ET>(){

			@Override
			T getParent3() {
				return DateDiffFunction.this.getParent();
			}
        	
        }, getTokens());
    }
}