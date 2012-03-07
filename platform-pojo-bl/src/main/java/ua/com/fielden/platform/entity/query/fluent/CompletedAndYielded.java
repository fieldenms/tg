package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompletedAndYielded;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

class CompletedAndYielded extends CompletedCommon implements ICompletedAndYielded {

    CompletedAndYielded(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public <T extends AbstractEntity<?>> EntityResultQueryModel<T> model() {
	return new EntityResultQueryModel<T>(getTokens().getTokens());
    }

    @Override
    public <T extends AbstractEntity<?>> EntityResultQueryModel<T> modelAsEntity(final Class<T> resultType) {
	return new EntityResultQueryModel<T>(getTokens().getTokens(), resultType);
    }

    @Override
    public IFunctionYieldedLastArgument<IFirstYieldedItemAlias<ISubsequentCompletedAndYielded>> yield() {
	return new FunctionYieldedLastArgument<IFirstYieldedItemAlias<ISubsequentCompletedAndYielded>>(getTokens().yield(), new FirstYieldedItemAlias<ISubsequentCompletedAndYielded>(getTokens(), new SubsequentCompletedAndYielded(getTokens())));
    }
}
