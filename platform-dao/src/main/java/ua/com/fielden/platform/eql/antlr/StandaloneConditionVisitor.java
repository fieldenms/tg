package ua.com.fielden.platform.eql.antlr;

final class StandaloneConditionVisitor extends EQLBaseVisitor<EqlCompilationResult.StandaloneCondition> {

    @Override
    public EqlCompilationResult.StandaloneCondition visitStandaloneCondExpr(final EQLParser.StandaloneCondExprContext ctx) {
        throw new UnsupportedOperationException();
    }

}
