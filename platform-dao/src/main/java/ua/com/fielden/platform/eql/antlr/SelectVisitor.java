package ua.com.fielden.platform.eql.antlr;


import java.util.List;

final class SelectVisitor extends AbstractEqlVisitor<EqlCompilationResult.Select> {

    SelectVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public EqlCompilationResult.Select visitQuery_Select(final EQLParser.Query_SelectContext ctx) {
        throw new UnsupportedOperationException();
    }

}
