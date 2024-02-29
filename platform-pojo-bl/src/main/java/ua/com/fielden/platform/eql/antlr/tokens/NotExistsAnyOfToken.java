package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import java.util.List;

public final class NotExistsAnyOfToken extends CommonToken {

    public final List<QueryModel> models;

    public NotExistsAnyOfToken(final List<QueryModel> models) {
        super(EQLLexer.NOTEXISTSANYOF, "notExistsAnyOf");
        this.models = models;
    }

}
