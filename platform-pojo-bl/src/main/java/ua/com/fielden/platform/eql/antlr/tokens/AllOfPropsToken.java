package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.wrap;

public final class AllOfPropsToken extends CommonToken {

    public final List<String> props;

    public AllOfPropsToken(final List<String> props) {
        super(EQLLexer.ALLOFPROPS, "allOfProps");
        this.props = props;
    }

    @Override
    public String getText() {
        return "allOfProps(%s)".formatted(CollectionUtil.toString(props, p -> wrap(p, '"'), ", "));
    }

}
