package ua.com.fielden.platform.eql.stage1.elements.conditions;

import java.util.Objects;

import ua.com.fielden.platform.eql.stage1.elements.PropsResolutionContext;
import ua.com.fielden.platform.eql.stage1.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.operands.SubQuery1;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ExistenceTest2;
import ua.com.fielden.platform.eql.stage2.elements.operands.SubQuery2;

public class ExistenceTest1 implements ICondition1<ExistenceTest2> {
    private final boolean negated;
    private final SubQuery1 subQuery;

    public ExistenceTest1(final boolean negated, final SubQuery1 subQuery) {
        this.negated = negated;
        this.subQuery = subQuery;
    }

    @Override
    public TransformationResult<ExistenceTest2> transform(final PropsResolutionContext context) {
        final TransformationResult<SubQuery2> subQueryTr = subQuery.transform(context);
        return new TransformationResult<ExistenceTest2>(new ExistenceTest2(negated, subQueryTr.item), subQueryTr.updatedContext);
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
        
        if (!(obj instanceof ExistenceTest1)) {
            return false;
        }
        
        final ExistenceTest1 other = (ExistenceTest1) obj;

        return Objects.equals(negated, other.negated) && Objects.equals(subQuery, other.subQuery);
    }
}