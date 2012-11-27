package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;



public class Conditions extends AbstractCondition {
    private final ICondition firstCondition;
    private final List<CompoundCondition> otherConditions;

    public String sql() {
	final List<List<ICondition>> orGroups = formConditionIntoLogicalGroups();

	final StringBuffer orSb = new StringBuffer();
	for (final List<ICondition> andGroup : orGroups) {
	    final StringBuffer andSb = new StringBuffer();
	    for (final ICondition condition : andGroup) {
		if (!condition.ignore()) {
		    if (andSb.length() > 0) {
			andSb.append(" AND ");
		    }
		    andSb.append(condition.sql());
		}
	    }

	    if (andSb.length() > 0) {
		if (orSb.length() > 0) {
		    orSb.append(" OR ");
		}
		orSb.append(andSb.toString());
	    }
	}
	return orSb.toString();
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        sb.append(firstCondition);
        for (final CompoundCondition compound : otherConditions) {
            sb.append(" " + compound);
        }
        return sb.toString();
    }

    public Conditions(final ICondition firstCondition, final List<CompoundCondition> otherConditions) {
	this.firstCondition = firstCondition;
	this.otherConditions = otherConditions;
    }

    public Conditions(final ICondition firstCondition) {
	this.firstCondition = firstCondition;
	this.otherConditions = Collections.emptyList();
    }

    public ICondition getFirstCondition() {
        return firstCondition;
    }

    public List<CompoundCondition> getOtherConditions() {
        return otherConditions;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((firstCondition == null) ? 0 : firstCondition.hashCode());
	result = prime * result + ((otherConditions == null) ? 0 : otherConditions.hashCode());
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
	if (!(obj instanceof Conditions)) {
	    return false;
	}
	final Conditions other = (Conditions) obj;
	if (firstCondition == null) {
	    if (other.firstCondition != null) {
		return false;
	    }
	} else if (!firstCondition.equals(other.firstCondition)) {
	    return false;
	}
	if (otherConditions == null) {
	    if (other.otherConditions != null) {
		return false;
	    }
	} else if (!otherConditions.equals(other.otherConditions)) {
	    return false;
	}
	return true;
    }

    @Override
    public boolean ignore() {
	if (firstCondition != null && !firstCondition.ignore()) {
	    return false;
	}

	for (final CompoundCondition compoundCondition : otherConditions) {
	    if (!compoundCondition.getCondition().ignore()) {
		return false;
	    }
	}

	return true;
    }

    @Override
    protected List<IPropertyCollector> getCollection() {
	final List<IPropertyCollector> result = new ArrayList<IPropertyCollector>();
	if (firstCondition != null && !firstCondition.ignore()) {
	    result.add(firstCondition);
	}

	for (final CompoundCondition compoundCondition : otherConditions) {
	    if (!compoundCondition.getCondition().ignore()) {
		result.add(compoundCondition.getCondition());
	    }
	}
	return result;
    }

    private List<List<ICondition>> formConditionIntoLogicalGroups() {
	final List<List<ICondition>> result = new ArrayList<List<ICondition>>();
	List<ICondition> currGroup = new ArrayList<ICondition>();
	currGroup.add(firstCondition);

	for (final CompoundCondition compoundCondition : otherConditions) {
	    if (compoundCondition.getLogicalOperator() == LogicalOperator.AND) {
		currGroup.add(compoundCondition.getCondition());
	    } else {
		result.add(currGroup);
		currGroup = new ArrayList<ICondition>();
		currGroup.add(compoundCondition.getCondition());
	    }
	}

	result.add(currGroup);

	return result;
    }

    private static class LogicalConditionGroup extends AbstractCondition {
	    private final LogicalOperator logicalOperator;
	    private final List<ICondition> conditions = new ArrayList<ICondition>();

	    public LogicalConditionGroup(final LogicalOperator logicalOperator) {
		this.logicalOperator = logicalOperator;
	    }

	    public void add(final ICondition condition) {
		conditions.add(condition);
	    }

	    @Override
	    public boolean ignore() {
		for (final ICondition condition : conditions) {
		    if (!condition.ignore()) {
			return false;
		    }
		}

		return true;
	    }

	    @Override
	    public String sql() {
		// TODO Auto-generated method stub
		return null;
	    }

	    @Override
	    protected List<IPropertyCollector> getCollection() {
		// TODO Auto-generated method stub
		return null;
	    }
    }
}