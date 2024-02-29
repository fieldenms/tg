package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class ModelToken extends CommonToken {

    public final SingleResultQueryModel model;

    public ModelToken(final SingleResultQueryModel model) {
        super(EQLLexer.MODEL, "model");
        this.model = model;
    }

}
