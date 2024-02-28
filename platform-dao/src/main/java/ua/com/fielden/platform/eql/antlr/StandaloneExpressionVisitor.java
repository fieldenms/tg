package ua.com.fielden.platform.eql.antlr;

final class StandaloneExpressionVisitor extends EQLBaseVisitor<EqlCompilationResult.StandaloneExpression> {

    @Override
    public EqlCompilationResult.StandaloneExpression visitStandaloneExpression(final EQLParser.StandaloneExpressionContext ctx) {
        throw new UnsupportedOperationException();
    }

}
