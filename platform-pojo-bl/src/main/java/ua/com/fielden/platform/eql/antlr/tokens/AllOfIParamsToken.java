package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.wrap;

public final class AllOfIParamsToken extends CommonToken {

    public final List<String> params;

    public AllOfIParamsToken(final List<String> params) {
        super(EQLLexer.ALLOFIPARAMS, "allOfIParams");
        this.params = params;
    }

    @Override
    public String getText() {
        return "allOfIParams(%s)".formatted(CollectionUtil.toString(params, p -> wrap(p, '"'), ", "));
    }

}
