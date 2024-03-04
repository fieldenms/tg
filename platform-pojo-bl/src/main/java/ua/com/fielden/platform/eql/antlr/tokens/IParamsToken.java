package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import java.util.List;
import java.util.stream.Collectors;

public final class IParamsToken extends CommonToken {

    public final List<String> params;

    public IParamsToken(final List<String> params) {
        super(EQLLexer.IPARAMS, "iParams");
        this.params = params;
    }

    @Override
    public String getText() {
        return "iparams(%s)".formatted(params.stream().map("\"%s\""::formatted).collect(Collectors.joining(", ")));
    }

}
