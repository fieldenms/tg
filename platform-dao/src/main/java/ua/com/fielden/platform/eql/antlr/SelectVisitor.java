package ua.com.fielden.platform.eql.antlr;


final class SelectVisitor extends EQLBaseVisitor<EqlCompilationResult.Select> {

    @Override
    public EqlCompilationResult.Select visitQuery_Select(final EQLParser.Query_SelectContext ctx) {
        throw new UnsupportedOperationException();
    }

}
