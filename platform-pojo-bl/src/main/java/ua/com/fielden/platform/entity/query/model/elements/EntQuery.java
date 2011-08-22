package ua.com.fielden.platform.entity.query.model.elements;

import java.util.HashSet;
import java.util.Set;


public class EntQuery implements ISingleOperand {
    private final EntQuerySourcesModel sources;
    private final ConditionsModel conditions;
    private final YieldsModel yields;
    private final GroupsModel groups;

    public EntQuery(final EntQuerySourcesModel sources, final ConditionsModel conditions, final YieldsModel yields, final GroupsModel groups) {
	super();
	this.sources = sources;
	this.conditions = conditions;
	this.yields = yields;
	this.groups = groups;
    }

    public EntQuerySourcesModel getSources() {
	return sources;
    }

    public ConditionsModel getConditions() {
	return conditions;
    }

    public YieldsModel getYields() {
	return yields;
    }

    public GroupsModel getGroups() {
	return groups;
    }

    @Override
    public Set<String> getPropNames() {
	final Set<String> result = new HashSet<String>();

	result.addAll(getPropNamesFromYields());
	result.addAll(getPropNamesFromGroups());
	result.addAll(conditions.getPropNames());
	result.addAll(getPropNamesFromSources());

	return result;
    }

    public Set<String> getQrySourcesNames() {
	final Set<String> result = new HashSet<String>();
	if (sources.getMain().getAlias() != null) {
	    result.add(sources.getMain().getAlias());
	}
	for (final EntQueryCompoundSourceModel compSource : sources.getCompounds()) {
	    result.add(compSource.getSource().getAlias());
	}
	return result;
    }

    private Set<String> getPropNamesFromYields() {
	final Set<String> result = new HashSet<String>();
	for (final YieldModel yield : yields.getYields()) {
	    result.addAll(yield.getOperand().getPropNames());
	}
	return result;
    }

    private Set<String> getPropNamesFromGroups() {
	final Set<String> result = new HashSet<String>();
	for (final GroupModel group : groups.getGroups()) {
	    result.addAll(group.getOperand().getPropNames());
	}
	return result;
    }

    private Set<String> getPropNamesFromSources() {
	final Set<String> result = new HashSet<String>();
	for (final EntQueryCompoundSourceModel compSource : sources.getCompounds()) {
	    result.addAll(compSource.getPropNames());
	}
	return result;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
	result = prime * result + ((groups == null) ? 0 : groups.hashCode());
	result = prime * result + ((sources == null) ? 0 : sources.hashCode());
	result = prime * result + ((yields == null) ? 0 : yields.hashCode());
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
	if (!(obj instanceof EntQuery)) {
	    return false;
	}
	final EntQuery other = (EntQuery) obj;
	if (conditions == null) {
	    if (other.conditions != null) {
		return false;
	    }
	} else if (!conditions.equals(other.conditions)) {
	    return false;
	}
	if (groups == null) {
	    if (other.groups != null) {
		return false;
	    }
	} else if (!groups.equals(other.groups)) {
	    return false;
	}
	if (sources == null) {
	    if (other.sources != null) {
		return false;
	    }
	} else if (!sources.equals(other.sources)) {
	    return false;
	}
	if (yields == null) {
	    if (other.yields != null) {
		return false;
	    }
	} else if (!yields.equals(other.yields)) {
	    return false;
	}
	return true;
    }
}
