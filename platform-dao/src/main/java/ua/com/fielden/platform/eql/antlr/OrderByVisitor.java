package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.sundries.OrderBys1;

final class OrderByVisitor extends AbstractEqlVisitor<OrderBys1>  {

    OrderByVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public OrderBys1 visitOrderBy(final EQLParser.OrderByContext ctx) {
        final var visitor = new OrderByOperandVisitor(transformer);
        return new OrderBys1(ctx.operands.stream().flatMap(o -> o.accept(visitor)).toList());
    }

}
