package ua.com.fielden.platform.eql.antlr.tokens;

import static ua.com.fielden.platform.eql.antlr.EQLLexer.OFFSET;

public final class OffsetToken extends AbstractParameterisedEqlToken {

    public final long offset;

    public OffsetToken(final long offset) {
        super(OFFSET, "offset");
        this.offset = offset;
    }

    @Override
    public String parametersText() {
        return String.valueOf(offset);
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof OffsetToken that && offset == that.offset;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(offset);
    }

}
