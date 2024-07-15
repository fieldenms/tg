package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentYieldedItemAlias;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

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
    public ISubsequentCompletedAndYielded<ET> as(final Enum alias) {
        return as(alias.toString());
    }

    @Override
    public ISubsequentCompletedAndYielded<ET> asRequired(final Enum alias) {
        return asRequired(alias.toString());
    }

    @Override
    public ISubsequentCompletedAndYielded<ET> as(final IConvertableToPath alias) {
        return as(alias.toPath());
    }

    @Override
    public ISubsequentCompletedAndYielded<ET> asRequired(final IConvertableToPath alias) {
        return asRequired(alias.toPath());
    }

}