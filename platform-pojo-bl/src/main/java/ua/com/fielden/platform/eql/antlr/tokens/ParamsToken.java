package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import java.util.List;
import java.util.stream.Collectors;

public final class ParamsToken extends CommonToken {

    public final List<String> params;

    public ParamsToken(final List<String> params) {
        super(EQLLexer.PARAMS, "params");
        this.params = params;
    }

    @Override
    public String getText() {
        return "params(%s)".formatted(params.stream().map("\"%s\""::formatted).collect(Collectors.joining(", ")));
    }

}
