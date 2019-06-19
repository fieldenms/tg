package ua.com.fielden.platform.eql.stage1.elements.sources;

import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.meta.PropsResolutionContext;
import ua.com.fielden.platform.eql.meta.TransformationResult;
import ua.com.fielden.platform.eql.stage1.elements.ITransformableToS2;
import ua.com.fielden.platform.eql.stage1.elements.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage2.elements.sources.CompoundSource2;
import ua.com.fielden.platform.eql.stage2.elements.sources.IQrySource2;

public class CompoundSource1 implements ITransformableToS2<CompoundSource2>{
    public final IQrySource1<? extends IQrySource2> source;
    public final JoinType joinType;
    public final Conditions1 joinConditions;

    public CompoundSource1(final IQrySource1<? extends IQrySource2> source, final JoinType joinType, final Conditions1 joinConditions) {
        this.source = source;
        this.joinType = joinType;
        this.joinConditions = joinConditions;
    }

    @Override
    public TransformationResult<CompoundSource2> transform(final PropsResolutionContext resolutionContext) {
        final TransformationResult<? extends IQrySource2> sourceTransformationResult = source.transform(resolutionContext);
        final TransformationResult<Conditions2> joinConditionsTransformationResult = joinConditions.transform(sourceTransformationResult.getUpdatedContext());
        return new TransformationResult<CompoundSource2>(new CompoundSource2(sourceTransformationResult.getItem(), joinType, joinConditionsTransformationResult.getItem()), joinConditionsTransformationResult.getUpdatedContext());
    }

    @Override
    public String toString() {
        return joinType + " " + source + " ON " + joinConditions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((joinConditions == null) ? 0 : joinConditions.hashCode());
        result = prime * result + ((joinType == null) ? 0 : joinType.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
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
        if (!(obj instanceof CompoundSource1)) {
            return false;
        }
        final CompoundSource1 other = (CompoundSource1) obj;
        if (joinConditions == null) {
            if (other.joinConditions != null) {
                return false;
            }
        } else if (!joinConditions.equals(other.joinConditions)) {
            return false;
        }
        if (joinType != other.joinType) {
            return false;
        }
        if (source == null) {
            if (other.source != null) {
                return false;
            }
        } else if (!source.equals(other.source)) {
            return false;
        }
        return true;
    }
}