package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentYieldedItemAlias;

public class SubsequentYieldedItemAlias<T> extends AbstractQueryLink implements ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded> {
    T parent;

    SubsequentYieldedItemAlias(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    public ISubsequentCompletedAndYielded as(final String alias) {
	return new SubsequentCompletedAndYielded(getTokens().as(alias));
    }
}
