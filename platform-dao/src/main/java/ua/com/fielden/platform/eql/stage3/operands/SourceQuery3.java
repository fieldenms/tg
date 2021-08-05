package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.eql.stage3.QueryBlocks3;

public class SourceQuery3 extends AbstractQuery3 {

    public SourceQuery3(final QueryBlocks3 queryBlocks, final Class<?> resultType) {
        super(queryBlocks, resultType);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + SourceQuery3.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof SourceQuery3;
    }
}