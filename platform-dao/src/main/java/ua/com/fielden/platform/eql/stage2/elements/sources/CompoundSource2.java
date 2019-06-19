package ua.com.fielden.platform.eql.stage2.elements.sources;

import com.google.common.base.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;

public class CompoundSource2 {
    public final IQrySource2 source;
    public final JoinType joinType;
    public final Conditions2 joinConditions;

    public CompoundSource2(final IQrySource2 source, final JoinType joinType, final Conditions2 joinConditions) {
        this.source = source;
        this.joinType = joinType;
        this.joinConditions = joinConditions;
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

        if (!(obj instanceof CompoundSource2)) {
            return false;
        }
        
        
        final CompoundSource2 other = (CompoundSource2) obj;
        
        return Objects.equal(source, other.source) &&
                Objects.equal(joinType, other.joinType) &&
                Objects.equal(joinConditions, other.joinConditions);
    }
}