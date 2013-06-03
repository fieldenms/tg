package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.JoinType;


public class CompoundSource2 implements IElement2 {
    private final ISource2 source;
    private final JoinType joinType;
    private final Conditions2 joinConditions;

    public CompoundSource2(final ISource2 source, final JoinType joinType, final Conditions2 joinConditions) {
	super();
	this.source = source;
	this.joinType = joinType;
	this.joinConditions = joinConditions;
    }

    @Override
    public String toString() {
        return joinType + " " + source + " ON " + joinConditions;
    }

    public ISource2 getSource() {
        return source;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public Conditions2 getJoinConditions() {
        return joinConditions;
    }

    @Override
    public List<EntProp2> getLocalProps() {
	return joinConditions.getLocalProps();
    }

    @Override
    public List<EntValue2> getAllValues() {
	final List<EntValue2> result = new ArrayList<EntValue2>();
	result.addAll(source.getValues());
	result.addAll(joinConditions.getAllValues());
	return result;
    }

    @Override
    public List<EntQuery2> getLocalSubQueries() {
	return joinConditions.getLocalSubQueries();
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
	if (!(obj instanceof CompoundSource2)) {
	    return false;
	}
	final CompoundSource2 other = (CompoundSource2) obj;
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

    @Override
    public boolean ignore() {
	// TODO Auto-generated method stub
	return false;
    }
}