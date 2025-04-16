package ua.com.fielden.platform.eql.antlr.tokens;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.utils.CollectionUtil;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.wrap;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.ALLOFPROPS;

public final class AllOfPropsToken extends AbstractParameterisedEqlToken {

    public final List<String> props;

    public AllOfPropsToken(final Collection<String> props) {
        super(ALLOFPROPS, "allOfProps");
        this.props = ImmutableList.copyOf(props);
    }

    public String parametersText() {
        return CollectionUtil.toString(props, p -> wrap(p, '"'), ", ");
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof AllOfPropsToken that &&
                Objects.equals(props, that.props);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(props);
    }

}
