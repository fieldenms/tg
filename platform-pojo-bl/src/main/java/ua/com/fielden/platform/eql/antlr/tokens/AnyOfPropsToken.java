package ua.com.fielden.platform.eql.antlr.tokens;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.wrap;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.ANYOFPROPS;

public final class AnyOfPropsToken extends AbstractParameterisedEqlToken {

    public final List<String> props;

    public AnyOfPropsToken(final Collection<? extends String> props) {
        super(ANYOFPROPS, "anyOfProps");
        this.props = ImmutableList.copyOf(props);
    }

    public String parametersText() {
        return props.stream().map(p -> wrap(p, '"')).collect(joining(", "));
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof AnyOfPropsToken that &&
                Objects.equals(props, that.props);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(props);
    }

}
