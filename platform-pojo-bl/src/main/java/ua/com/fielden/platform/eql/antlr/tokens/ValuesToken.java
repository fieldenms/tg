package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;

public final class ValuesToken extends CommonToken {

    public final List<Object> values;

    public ValuesToken(final List<Object> values) {
        super(EQLLexer.VALUES, "values");
        this.values = List.copyOf(values);
    }


    @Override
    public String getText() {
        // TODO quote strings
        return "values(%s)".formatted(CollectionUtil.toString(values, ", "));
    }

}
