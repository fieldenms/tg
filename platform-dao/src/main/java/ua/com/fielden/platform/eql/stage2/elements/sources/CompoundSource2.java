package ua.com.fielden.platform.eql.stage2.elements.sources;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage2.elements.TransformationContext;
import ua.com.fielden.platform.eql.stage2.elements.TransformationResult;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;
import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;
import ua.com.fielden.platform.eql.stage3.elements.sources.CompoundSource3;
import ua.com.fielden.platform.eql.stage3.elements.sources.IQrySource3;

public class CompoundSource2 {
    public final IQrySource2 source;
    public final JoinType joinType;
    public final Conditions2 joinConditions;

    public CompoundSource2(final IQrySource2 source, final JoinType joinType, final Conditions2 joinConditions) {
        this.source = source;
        this.joinType = joinType;
        this.joinConditions = joinConditions;
    }
    
    public TransformationResult<CompoundSource3> transform(final TransformationContext resolutionContext) {
        final TransformationResult<? extends IQrySource3> sourceTransformationResult = source.transform(resolutionContext);
        final TransformationResult<Conditions3> joinConditionsTransformationResult = joinConditions.transform(sourceTransformationResult.updatedContext);
        return new TransformationResult<CompoundSource3>(new CompoundSource3(sourceTransformationResult.item, joinType, joinConditionsTransformationResult.item), joinConditionsTransformationResult.updatedContext);
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

        if (!(obj instanceof CompoundSource2)) {
            return false;
        }
        
        
        final CompoundSource2 other = (CompoundSource2) obj;
        
        return Objects.equals(source, other.source) &&
                Objects.equals(joinType, other.joinType) &&
                Objects.equals(joinConditions, other.joinConditions);
    }
}