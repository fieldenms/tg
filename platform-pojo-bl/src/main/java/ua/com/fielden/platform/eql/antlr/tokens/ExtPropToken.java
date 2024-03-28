package ua.com.fielden.platform.eql.antlr.tokens;

import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.wrap;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.EXTPROP;

public final class ExtPropToken extends AbstractParameterisedEqlToken {

    public final String propPath;

    public ExtPropToken(final String propPath) {
        super(EXTPROP, "extProp");
        this.propPath = propPath;
    }

    @Override
    public String parametersText() {
        return wrap(propPath, '"');
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof ExtPropToken that &&
                Objects.equals(propPath, that.propPath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(propPath);
    }

}
