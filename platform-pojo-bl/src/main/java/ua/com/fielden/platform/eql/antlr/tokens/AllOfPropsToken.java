package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.wrap;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.ALLOFPROPS;

public final class AllOfPropsToken extends AbstractParameterisedEqlToken {

    public final List<String> props;

    public AllOfPropsToken(final List<String> props) {
        super(ALLOFPROPS, "allOfProps");
        this.props = props;
    }

    public String parametersText() {
        return CollectionUtil.toString(props, p -> wrap(p, '"'), ", ");
    }

}
