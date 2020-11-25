package ua.com.fielden.platform.eql.stage3.operands;

import ua.com.fielden.platform.eql.stage3.EntQueryBlocks3;

public class ResultQuery3 extends AbstractQuery3 {

    public ResultQuery3(final EntQueryBlocks3 queryBlocks, final Class<?> resultType) {
        super(queryBlocks, resultType);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + ResultQuery3.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof ResultQuery3;
    }
}