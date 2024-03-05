package ua.com.fielden.platform.eql.antlr.tokens;

import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.wrap;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.ANYOFPROPS;

public final class AnyOfPropsToken extends AbstractParameterisedEqlToken {

    public final List<String> props;

    public AnyOfPropsToken(final List<String> props) {
        super(ANYOFPROPS, "anyOfProps");
        this.props = props;
    }

    public String parametersText() {
        return props.stream().map(p -> wrap(p, '"')).collect(joining(", "));
    }

}
