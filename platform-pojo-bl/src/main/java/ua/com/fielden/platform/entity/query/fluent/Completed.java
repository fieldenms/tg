package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

class Completed<ET extends AbstractEntity<?>> extends CompletedAndYielded<ET> implements ICompleted<ET> {

    @Override
    public IFunctionLastArgument<ICompleted<ET>, ET> groupBy() {
        
        return copy(new FunctionLastArgument<ICompleted<ET>, ET>(){

			@Override
			ICompleted<ET> getParent3() {
				return new Completed<ET>();
			}
        	
        }, getTokens().groupBy());
    }
}