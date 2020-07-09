package ua.com.fielden.platform.eql.stage2.elements.sources;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage2.elements.conditions.Conditions2;

public class CompoundSource2 {
    public final IQrySource2<?> source;
    public final JoinType joinType;
    public final Conditions2 joinConditions;

    public CompoundSource2(final IQrySource2<?> source, final JoinType joinType, final Conditions2 joinConditions) {
        this.source = source;
        this.joinType = joinType;
        this.joinConditions = joinConditions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + joinConditions.hashCode();
        result = prime * result + joinType.hashCode();
        result = prime * result + source.contextId().hashCode();
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
        
        return Objects.equals(source.contextId(), other.source.contextId()) &&
                Objects.equals(joinType, other.joinType) &&
                Objects.equals(joinConditions, other.joinConditions);
    }
}