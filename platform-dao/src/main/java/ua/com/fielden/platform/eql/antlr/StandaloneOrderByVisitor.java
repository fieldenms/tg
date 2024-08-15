package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.sundries.OrderBys1;

import static ua.com.fielden.platform.eql.antlr.EQLParser.StandaloneOrderByContext;

final class StandaloneOrderByVisitor extends AbstractEqlVisitor<EqlCompilationResult.StandaloneOrderBy> {

    StandaloneOrderByVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public EqlCompilationResult.StandaloneOrderBy visitStandaloneOrderBy(final StandaloneOrderByContext ctx) {
        final var visitor = new OrderByOperandVisitor(transformer);
        return new EqlCompilationResult.StandaloneOrderBy(new OrderBys1(ctx.operands.stream().flatMap(o -> o.accept(visitor)).toList()));
    }

}
