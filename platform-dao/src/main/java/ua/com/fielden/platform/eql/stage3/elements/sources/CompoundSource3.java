package ua.com.fielden.platform.eql.stage3.elements.sources;

import java.util.Objects;

import ua.com.fielden.platform.entity.query.fluent.enums.JoinType;
import ua.com.fielden.platform.eql.stage3.elements.conditions.Conditions3;

public class CompoundSource3 {
    public final IQrySource3 source;
    public final JoinType joinType;
    public final Conditions3 joinConditions;

    public CompoundSource3(final IQrySource3 source, final JoinType joinType, final Conditions3 joinConditions) {
        this.source = source;
        this.joinType = joinType;
        this.joinConditions = joinConditions;
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

        if (!(obj instanceof CompoundSource3)) {
            return false;
        }
        
        
        final CompoundSource3 other = (CompoundSource3) obj;
        
        return Objects.equals(source, other.source) &&
                Objects.equals(joinType, other.joinType) &&
                Objects.equals(joinConditions, other.joinConditions);
    }
}