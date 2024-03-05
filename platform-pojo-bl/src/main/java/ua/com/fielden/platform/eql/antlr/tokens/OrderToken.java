package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import static java.util.stream.Collectors.joining;

public final class OrderToken extends CommonToken {

    public final OrderingModel model;

    public OrderToken(final OrderingModel model) {
        super(EQLLexer.ORDER, "order");
        this.model = model;
    }

    @Override
    public String getText() {
        return "order( %s )".formatted(model.getTokenSource().tokens().stream().map(Token::getText).collect(joining(" ")));
    }

}
