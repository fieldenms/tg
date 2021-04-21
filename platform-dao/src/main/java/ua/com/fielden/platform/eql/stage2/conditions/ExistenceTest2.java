package ua.com.fielden.platform.eql.stage2.conditions;

import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage2.operands.TypelessSubQuery2;
import ua.com.fielden.platform.eql.stage3.conditions.ExistenceTest3;
import ua.com.fielden.platform.eql.stage3.operands.TypelessSubQuery3;

public class ExistenceTest2 extends AbstractCondition2<ExistenceTest3> {
    private final boolean negated;
    private final TypelessSubQuery2 subQuery;

    public ExistenceTest2(final boolean negated, final TypelessSubQuery2 subQuery) {
        this.negated = negated;
        this.subQuery = subQuery;
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public TransformationResult<ExistenceTest3> transform(final TransformationContext context) {
        final TransformationResult<TypelessSubQuery3> subQueryTr = subQuery.transform(context);
        return new TransformationResult<>(new ExistenceTest3(negated, subQueryTr.item), subQueryTr.updatedContext);
    }
    
    @Override
    public Set<Prop2> collectProps() {
        return subQuery.collectProps();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + subQuery.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
     
        if (!(obj instanceof ExistenceTest2)) {
            return false;
        }
        
        final ExistenceTest2 other = (ExistenceTest2) obj;
        
        return Objects.equals(subQuery, other.subQuery) && (negated == other.negated);
    }
}