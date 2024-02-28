package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;

final class OrderByVisitor extends AbstractEqlVisitor<EqlCompilationResult.OrderBy> {

    OrderByVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public EqlCompilationResult.OrderBy visitOrderBy(final EQLParser.OrderByContext ctx) {
        throw new UnsupportedOperationException();
    }

}
