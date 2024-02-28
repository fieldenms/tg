package ua.com.fielden.platform.eql.antlr;

final class OrderByVisitor extends EQLBaseVisitor<EqlCompilationResult.OrderBy> {

    @Override
    public EqlCompilationResult.OrderBy visitOrderBy(final EQLParser.OrderByContext ctx) {
        throw new UnsupportedOperationException();
    }

}
