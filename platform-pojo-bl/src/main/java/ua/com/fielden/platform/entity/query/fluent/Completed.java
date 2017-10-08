package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

class Completed<ET extends AbstractEntity<?>> //
		extends CompletedAndYielded<ET> //
		implements ICompleted<ET> {

    public Completed(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	public IFunctionLastArgument<ICompleted<ET>, ET> groupBy() {
		return createFunctionLastArgument(getTokens().groupBy());
	}

	private FunctionLastArgument<ICompleted<ET>, ET> createFunctionLastArgument(final Tokens tokens) {
		return new FunctionLastArgument<ICompleted<ET>, ET> (tokens) {

			@Override
			protected ICompleted<ET> nextForFunctionLastArgument(final Tokens tokens) {
				return new Completed<ET>(tokens);
			}

		};
	}
}