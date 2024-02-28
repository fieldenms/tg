package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;

final class StandaloneExpressionVisitor extends AbstractEqlVisitor<EqlCompilationResult.StandaloneExpression> {

    StandaloneExpressionVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public EqlCompilationResult.StandaloneExpression visitStandaloneExpression(final EQLParser.StandaloneExpressionContext ctx) {
        throw new UnsupportedOperationException();
    }

}
