package ua.com.fielden.platform.eql.antlr;

import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;

abstract class AbstractEqlVisitor<T> extends EQLBaseVisitor<T> {

    protected final QueryModelToStage1Transformer transformer;

    AbstractEqlVisitor(QueryModelToStage1Transformer transformer) {
        this.transformer = transformer;
    }

}
