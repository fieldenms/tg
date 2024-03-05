package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;

public final class AnyOfValuesToken extends CommonToken {

    public final List<Object> values;

    public AnyOfValuesToken(final List<Object> values) {
        super(EQLLexer.ANYOFVALUES, "anyOfValues");
        this.values = values;
    }

    @Override
    public String getText() {
        // TODO quote strings
        return "anyOfValues(%s)".formatted(CollectionUtil.toString(values, ", "));
    }

}
