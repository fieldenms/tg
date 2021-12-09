package ua.com.fielden.platform.eql.stage1.sources;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage1.TransformationResult;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;
import ua.com.fielden.platform.eql.stage2.sources.ISources2;
import ua.com.fielden.platform.eql.stage2.sources.MultipleNodesSources2;

public class MultipleNodesSources1 implements ISources1<MultipleNodesSources2> {
    public final ISources1<? extends ISources2<?>> leftSource;
    public final ISources1<? extends ISources2<?>> rightSource;
    public final JoinType joinType;
    public final Conditions1 joinConditions;

    public MultipleNodesSources1(final ISources1<?> leftSource, final ISources1<?> rightSource, final JoinType joinType, final Conditions1 joinConditions) {
        this.leftSource = leftSource;
        this.rightSource = rightSource;
        this.joinType = joinType;
        this.joinConditions = joinConditions;
    }

    @Override
    public TransformationResult<MultipleNodesSources2> transform(TransformationContext context) {
        final TransformationResult<? extends ISources2<?>> lsTransformed = leftSource.transform(context);
        final TransformationResult<? extends ISources2<?>> rsTransformed = rightSource.transform(lsTransformed.updatedContext);
        final Conditions2 jcTransformed = joinConditions.transform(rsTransformed.updatedContext);
        return new TransformationResult<>(new MultipleNodesSources2(lsTransformed.item, rsTransformed.item, joinType, jcTransformed), rsTransformed.updatedContext);
    }

    @Override
    public ISource1<? extends ISource2<?>> mainSource() {
        return leftSource.mainSource();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + leftSource.hashCode();
        result = prime * result + rightSource.hashCode();
        result = prime * result + joinConditions.hashCode();
        result = prime * result + joinType.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MultipleNodesSources1)) {
            return false;
        }
        
        final MultipleNodesSources1 other = (MultipleNodesSources1) obj;
        
        return Objects.equals(leftSource, other.leftSource) &&
                Objects.equals(rightSource, other.rightSource) &&
                Objects.equals(joinType, other.joinType) &&
                Objects.equals(joinConditions, other.joinConditions);
    }
}