package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentYieldedItemAlias;

final class SubsequentYieldedItemAlias<T, ET extends AbstractEntity<?>> //
        extends AbstractQueryLink //
        implements ISubsequentYieldedItemAlias<ISubsequentCompletedAndYielded<ET>> {

    public SubsequentYieldedItemAlias(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public ISubsequentCompletedAndYielded<ET> as(final CharSequence alias) {
        return new SubsequentCompletedAndYielded<ET>(builder.as(alias));
    }

    @Override
    public ISubsequentCompletedAndYielded<ET> asRequired(final CharSequence alias) {
        return new SubsequentCompletedAndYielded<ET>(builder.asRequired(alias));
    }

    @Override
    public ISubsequentCompletedAndYielded<ET> as(final Enum alias) {
        return as(alias.toString());
    }

    @Override
    public ISubsequentCompletedAndYielded<ET> asRequired(final Enum alias) {
        return asRequired(alias.toString());
    }

}
