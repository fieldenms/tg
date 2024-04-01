package ua.com.fielden.platform.eql.antlr.tokens;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.PROPS;

public final class PropsToken extends AbstractParameterisedEqlToken {

    public final List<String> props;

    public PropsToken(final Collection<? extends String> props) {
        super(PROPS, "props");
        this.props = ImmutableList.copyOf(props);
    }

    public String parametersText() {
        return props.stream().map("\"%s\""::formatted).collect(joining(", "));
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof PropsToken that &&
                Objects.equals(props, that.props);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(props);
    }

}
