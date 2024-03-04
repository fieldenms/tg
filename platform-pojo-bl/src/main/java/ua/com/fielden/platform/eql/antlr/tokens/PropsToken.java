package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import java.util.List;
import java.util.stream.Collectors;

public final class PropsToken extends CommonToken {

    public final List<String> props;

    public PropsToken(final List<String> props) {
        super(EQLLexer.PROPS, "props");
        this.props = List.copyOf(props);
    }

    @Override
    public String getText() {
        return "props(%s)".formatted(props.stream().map("\"%s\""::formatted).collect(Collectors.joining(", ")));
    }

}
