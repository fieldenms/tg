package ua.com.fielden.platform.eql.stage3.operands.queries;

import ua.com.fielden.platform.eql.stage3.QueryComponents3;

public class TypelessSubQuery3 extends AbstractQuery3 {
    
    public TypelessSubQuery3(final QueryComponents3 queryComponents) {
        super(queryComponents, null);
    }

   @Override
    public int hashCode() {
        final int prime = 31;
        final int result = super.hashCode();
        return prime * result + TypelessSubQuery3.class.getName().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj || super.equals(obj) && obj instanceof TypelessSubQuery3;
    }
}