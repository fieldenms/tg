package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompletedAndYielded;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

class CompletedAndYielded<ET extends AbstractEntity<?>> extends CompletedCommon<ET> implements ICompletedAndYielded<ET> {

    CompletedAndYielded(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public /*<T extends AbstractEntity<?>>*/ EntityResultQueryModel<ET> model() {
	return new EntityResultQueryModel<ET>(getTokens().getValues(), (Class<ET>) getTokens().getMainSourceType());
    }

    @Override
    public <T extends AbstractEntity<?>> EntityResultQueryModel<T> modelAsEntity(final Class<T> resultType) {
	return new EntityResultQueryModel<T>(getTokens().getValues(), resultType);
    }

    @Override
    public IFunctionYieldedLastArgument<IFirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET> yield() {
	return new FunctionYieldedLastArgument<IFirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET>(getTokens().yield(), new FirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>(getTokens(), new SubsequentCompletedAndYielded<ET>(getTokens())));
    }
}