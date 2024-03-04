package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import java.util.List;
import java.util.stream.Collectors;

public final class AnyOfPropsToken extends CommonToken {

    public final List<String> props;

    public AnyOfPropsToken(final List<String> props) {
        super(EQLLexer.ANYOFPROPS, "anyOfProps");
        this.props = props;
    }

    @Override
    public String getText() {
        return "anyOfProps(%s)".formatted(props.stream().map(p -> StringUtils.wrap(p, '"')).collect(Collectors.joining(", ")));
    }

}
