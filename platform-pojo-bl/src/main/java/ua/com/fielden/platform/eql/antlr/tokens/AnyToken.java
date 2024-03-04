package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;
import ua.com.fielden.platform.utils.CollectionUtil;

public final class AnyToken extends CommonToken {

    public final SingleResultQueryModel model;

    public AnyToken(final SingleResultQueryModel model) {
        super(EQLLexer.ANY, "any");
        this.model = model;
    }

    @Override
    public String getText() {
        return "any(%s)".formatted(CollectionUtil.toString(model.getTokenSource().tokens(), Token::getText, " "));
    }

}
