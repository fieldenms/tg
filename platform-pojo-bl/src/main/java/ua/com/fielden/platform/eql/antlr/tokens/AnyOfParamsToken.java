package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.wrap;

public final class AnyOfParamsToken extends CommonToken {

    public final List<String> params;

    public AnyOfParamsToken(final List<String> params) {
        super(EQLLexer.ANYOFPARAMS, "anyOfParams");
        this.params = params;
    }

    @Override
    public String getText() {
        return "anyOfParams(%s)".formatted(CollectionUtil.toString(params, p -> wrap(p, '"'), ", "));
    }

}
