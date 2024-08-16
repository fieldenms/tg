package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.eql.antlr.tokens.LimitToken;
import ua.com.fielden.platform.eql.antlr.tokens.OffsetToken;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;

import static ua.com.fielden.platform.eql.antlr.EQLParser.StandaloneOrderByContext;
import static ua.com.fielden.platform.eql.antlr.OrderByVisitor.compileLimit;
import static ua.com.fielden.platform.eql.antlr.OrderByVisitor.compileOffset;
import static ua.com.fielden.platform.eql.stage1.sundries.OrderBys1.orderBys1;

final class StandaloneOrderByVisitor extends AbstractEqlVisitor<EqlCompilationResult.StandaloneOrderBy> {

    StandaloneOrderByVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public EqlCompilationResult.StandaloneOrderBy visitStandaloneOrderBy(final StandaloneOrderByContext ctx) {
        final var visitor = new OrderByOperandVisitor(transformer);

        final var orderBys = orderBys1(ctx.operands.stream().flatMap(o -> o.accept(visitor)).toList(),
                                       compileLimit((LimitToken) ctx.limit),
                                       compileOffset((OffsetToken) ctx.offset));
        return new EqlCompilationResult.StandaloneOrderBy(orderBys);
    }

}
