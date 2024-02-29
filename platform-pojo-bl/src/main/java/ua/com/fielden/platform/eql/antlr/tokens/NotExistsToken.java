package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

public final class NotExistsToken extends CommonToken {

    public final QueryModel model;

    public NotExistsToken(final QueryModel model) {
        super(EQLLexer.NOTEXISTS, "notExists");
        this.model = model;
    }

}
