package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentYieldedItemAlias;

final class SubsequentYieldedItemAlias<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded<ET>> {

    public SubsequentYieldedItemAlias(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	public ISubsequentCompletedAndYielded<ET> as(final String alias) {
		return new SubsequentCompletedAndYielded<ET>(getTokens().as(alias));
	}

	@Override
	public ISubsequentCompletedAndYielded<ET> asRequired(final String alias) {
		return new SubsequentCompletedAndYielded<ET>(getTokens().asRequired(alias));
	}

	@Override
	public ISubsequentCompletedAndYielded<ET> as(Enum alias) {
		return as(alias.toString());
	}

	@Override
	public ISubsequentCompletedAndYielded<ET> asRequired(Enum alias) {
		return asRequired(alias.toString());
	}
}