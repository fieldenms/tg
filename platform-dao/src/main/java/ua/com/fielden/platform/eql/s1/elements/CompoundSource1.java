package ua.com.fielden.platform.eql.s1.elements;

import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.eql.meta.TransformatorToS2;
import ua.com.fielden.platform.eql.s2.elements.CompoundSource2;


public class CompoundSource1 implements IElement1<CompoundSource2> {
    private final ISource1 source;
    private final JoinType joinType;
    private final Conditions1 joinConditions;

    public CompoundSource1(final ISource1 source, final JoinType joinType, final Conditions1 joinConditions) {
	super();
	this.source = source;
	this.joinType = joinType;
	this.joinConditions = joinConditions;
    }

    @Override
    public CompoundSource2 transform(final TransformatorToS2 resolver) {
	// TODO EQL
	return new CompoundSource2(null, joinType, joinConditions.transform(resolver));
    }

    @Override
    public String toString() {
        return joinType + " " + source + " ON " + joinConditions;
    }

    public ISource1 getSource() {
        return source;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public Conditions1 getJoinConditions() {
        return joinConditions;
    }

    @Override
    public List<EntProp1> getLocalProps() {
	return joinConditions.getLocalProps();
    }

    @Override
    public List<EntQuery1> getLocalSubQueries() {
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

    @Override
    public boolean ignore() {
	// TODO Auto-generated method stub
	return false;
    }
}