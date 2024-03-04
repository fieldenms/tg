package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.wrap;

public final class AnyOfIParamsToken extends CommonToken {

    public final List<String> params;

    public AnyOfIParamsToken(final List<String> params) {
        super(EQLLexer.ANYOFIPARAMS, "anyOfIParams");
        this.params = params;
    }

    @Override
    public String getText() {
        return "anyOfIParams(%s)".formatted(CollectionUtil.toString(params, p -> wrap(p, '"'), ", "));
    }

}
