package ua.com.fielden.platform.eql.antlr.tokens;

import java.util.Objects;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.PROP;

public final class PropToken extends AbstractParameterisedEqlToken {

    public final String propPath;

    public PropToken(String propPath) {
        super(PROP, "prop");
        this.propPath = propPath;
    }

    public String parametersText() {
        return "\"%s\"".formatted(propPath);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof PropToken that &&
                Objects.equals(propPath, that.propPath);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(propPath);
    }

}
