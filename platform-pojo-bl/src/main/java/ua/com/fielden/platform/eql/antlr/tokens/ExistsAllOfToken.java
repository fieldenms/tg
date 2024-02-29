package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import java.util.List;

public final class ExistsAllOfToken extends CommonToken {

    public final List<QueryModel> models;

    public ExistsAllOfToken(final List<QueryModel> models) {
        super(EQLLexer.EXISTSALLOF, "existsAllOf");
        this.models = models;
    }

}
