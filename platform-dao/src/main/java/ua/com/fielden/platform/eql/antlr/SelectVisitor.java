package ua.com.fielden.platform.eql.antlr;


import java.util.List;

import static ua.com.fielden.platform.eql.antlr.EQLParser.*;

final class SelectVisitor extends AbstractEqlVisitor<EqlCompilationResult.Select> {

    SelectVisitor(final QueryModelToStage1Transformer transformer) {
        super(transformer);
    }

    @Override
    public EqlCompilationResult.Select visitSelect(final SelectContext ctx) {
        throw new UnsupportedOperationException();
    }

}
