package ua.com.fielden.platform.eql.stage1.elements;

import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.EntQuery2;
import ua.com.fielden.platform.eql.stage2.elements.ExistenceTest2;

public class ExistenceTest1 implements ICondition1<ExistenceTest2> {
    private final boolean negated;
    private final EntQuery1 subQuery;

    public ExistenceTest1(final boolean negated, final EntQuery1 subQuery) {
        this.negated = negated;
        this.subQuery = subQuery;
    }

    @Override
    public TransformationResult<ExistenceTest2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<EntQuery2> subQueryTransformationResult = subQuery.transform(resolutionContext);
        return new TransformationResult<ExistenceTest2>(new ExistenceTest2(negated, subQueryTransformationResult.getItem()), subQueryTransformationResult.getUpdatedContext());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + ((subQuery == null) ? 0 : subQuery.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ExistenceTest1)) {
            return false;
        }
        final ExistenceTest1 other = (ExistenceTest1) obj;
        if (negated != other.negated) {
            return false;
        }
        if (subQuery == null) {
            if (other.subQuery != null) {
                return false;
            }
        } else if (!subQuery.equals(other.subQuery)) {
            return false;
        }
        return true;
    }
}