package ua.com.fielden.platform.eql.stage2.sources;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage2.TransformationContext;
import ua.com.fielden.platform.eql.stage2.TransformationResult;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.eql.stage3.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.sources.ISource3;
import ua.com.fielden.platform.eql.stage3.sources.ISources3;
import ua.com.fielden.platform.eql.stage3.sources.MultipleNodesSources3;

public class MultipleNodesSources2 implements ISources2<ISources3> {
    public final ISources2<? extends ISources3> leftSource;
    public final ISources2<? extends ISources3> rightSource;
    public final JoinType joinType;
    public final Conditions2 joinConditions;

    public MultipleNodesSources2(final ISources2<?> leftSource, final ISources2<?> rightSource, final JoinType joinType, final Conditions2 joinConditions) {
        this.leftSource = leftSource;
        this.rightSource = rightSource;
        this.joinType = joinType;
        this.joinConditions = joinConditions;
    }

    @Override
    public TransformationResult<ISources3> transform(TransformationContext context) {
        final TransformationResult<? extends ISources3> lsTransformed = leftSource.transform(context);
        final TransformationResult<? extends ISources3> rsTransformed = rightSource.transform(lsTransformed.updatedContext);
        final TransformationResult<Conditions3> jcTransformed = joinConditions.transform(rsTransformed.updatedContext);
        return new TransformationResult<>(new MultipleNodesSources3(lsTransformed.item, rsTransformed.item, joinType, jcTransformed.item), jcTransformed.updatedContext);
    }

    @Override
    public Set<Prop2> collectProps() {
        final Set<Prop2> result = new HashSet<>(); 
        result.addAll(leftSource.collectProps());
        result.addAll(rightSource.collectProps());
        result.addAll(joinConditions.collectProps());
        return result;
    }

    @Override
    public ISource2<? extends ISource3> mainSource() {
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

        if (!(obj instanceof MultipleNodesSources2)) {
            return false;
        }
        
        final MultipleNodesSources2 other = (MultipleNodesSources2) obj;
        
        return Objects.equals(leftSource, other.leftSource) &&
                Objects.equals(rightSource, other.rightSource) &&
                Objects.equals(joinType, other.joinType) &&
                Objects.equals(joinConditions, other.joinConditions);
    }
}