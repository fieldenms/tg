package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;

final class StandaloneConditionVisitor extends AbstractEqlVisitor<EqlCompilationResult.StandaloneCondition> {

    StandaloneConditionVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public EqlCompilationResult.StandaloneCondition visitStandaloneCondExpr(final EQLParser.StandaloneCondExprContext ctx) {
        throw new UnsupportedOperationException();
    }

}
