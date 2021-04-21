package ua.com.fielden.platform.eql.stage1.sources;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage1.TransformationContext;
import ua.com.fielden.platform.eql.stage1.TransformationResult;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage2.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.sources.CompoundSource2;
import ua.com.fielden.platform.eql.stage2.sources.ISource2;

public class CompoundSource1 {
    public final ISource1<? extends ISource2<?>> source;
    public final JoinType joinType;
    public final Conditions1 joinConditions;

    public CompoundSource1(final ISource1<? extends ISource2<?>> source, final JoinType joinType, final Conditions1 joinConditions) {
        this.source = source;
        this.joinType = joinType;
        this.joinConditions = joinConditions;
    }

    public TransformationResult<CompoundSource2> transform(final TransformationContext context) {
        final ISource2<?> source2 = source.transform(context);
        final TransformationContext enhancedContext = context.cloneWithAdded(source2);
        final Conditions2 joinConditions2 = joinConditions.transform(enhancedContext);
        return new TransformationResult<>(new CompoundSource2(source2, joinType, joinConditions2), enhancedContext);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + joinConditions.hashCode();
        result = prime * result + joinType.hashCode();
        result = prime * result + source.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof CompoundSource1)) {
            return false;
        }

        final CompoundSource1 other = (CompoundSource1) obj;

        return Objects.equals(joinConditions, other.joinConditions) && Objects.equals(joinType, other.joinType) && Objects.equals(source, other.source);
    }
}