package ua.com.fielden.platform.eql.antlr;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.query.fluent.Limit;
import ua.com.fielden.platform.eql.antlr.tokens.LimitToken;
import ua.com.fielden.platform.eql.antlr.tokens.OffsetToken;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.sundries.OrderBys1;

import static ua.com.fielden.platform.eql.stage1.sundries.OrderBys1.orderBys1;

final class OrderByVisitor extends AbstractEqlVisitor<OrderBys1>  {

    OrderByVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public OrderBys1 visitOrderBy(final EQLParser.OrderByContext ctx) {
        final var visitor = new OrderByOperandVisitor(transformer);
        return orderBys1(ctx.operands.stream().flatMap(o -> o.accept(visitor)).toList(),
                         compileLimit((LimitToken) ctx.limit),
                         compileOffset((OffsetToken) ctx.offset));
    }

    public static Limit compileLimit(final @Nullable LimitToken limit) {
        return switch (limit) {
            case null -> Limit.all();
            case LimitToken.WithLimit it -> it.limit;
            case LimitToken.WithLong it -> Limit.count(it.limit);
        };
    }

    public static long compileOffset(final @Nullable OffsetToken token) {
        return token == null ? OrderBys1.NO_OFFSET : token.offset;
    }

}
