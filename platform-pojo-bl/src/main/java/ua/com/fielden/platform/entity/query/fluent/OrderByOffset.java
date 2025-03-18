package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderByOffset;

final class OrderByOffset<ET extends AbstractEntity<?>>
        extends CompletedAndYielded<ET>
        implements IOrderByOffset<ET>
{

    public OrderByOffset(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public EntityQueryProgressiveInterfaces.ICompletedAndYielded<ET> offset(final long n) {
        return new CompletedAndYielded<>(builder.offset(n));
    }

}
