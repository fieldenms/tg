package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import static java.util.stream.Collectors.joining;

public final class QueryModelToken<T extends QueryModel<?>> extends CommonToken {

    public final T model;

    public QueryModelToken(final T model) {
        super(EQLLexer.MODEL, "model");
        this.model = model;
    }

    @Override
    public String getText() {
        return "model( %s )".formatted(model.getTokenSource().tokens().stream().map(Token::getText).collect(joining(" ")));
    }

}
