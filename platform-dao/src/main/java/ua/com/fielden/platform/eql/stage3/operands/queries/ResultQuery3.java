package ua.com.fielden.platform.eql.stage3.operands.queries;

import ua.com.fielden.platform.eql.stage3.QueryComponents3;

public class ResultQuery3 extends AbstractQuery3 {

    public ResultQuery3(final QueryComponents3 queryComponents, final Class<?> resultType) {
        super(queryComponents, resultType);
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