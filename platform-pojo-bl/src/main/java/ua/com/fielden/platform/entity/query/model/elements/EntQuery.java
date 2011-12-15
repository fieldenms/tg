package ua.com.fielden.platform.entity.query.model.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EntQuery implements ISingleOperand {
    private final EntQuerySourcesModel sources;
    private final ConditionsModel conditions;
    private final YieldsModel yields;
    private final GroupsModel groups;
    private final Class resultType;
    private EntQuery master;

    public EntQuery(final EntQuerySourcesModel sources, final ConditionsModel conditions, final YieldsModel yields, final GroupsModel groups, final Class resultType) {
	super();
	this.sources = sources;
	this.conditions = conditions;
	this.yields = yields;
	this.groups = groups;
	this.resultType = resultType != null ? resultType : (sources.getMain().getType());

	for (final EntQuery entQuery : getImmediateSubqueries()) {
	    entQuery.master = this;
	}
    }

    public List<EntQuery> getLeafSubqueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	for (final EntQuery entQuery : getImmediateSubqueries()) {
	    final List<EntQuery> subSubQueries = entQuery.getImmediateSubqueries();
	    if (subSubQueries.size() > 0) {
		result.addAll(entQuery.getLeafSubqueries());
	    } else {
		result.add(entQuery);
	    }
	}
	return result;
    }

    public List<String> resolveProps() {
	final List<String> result = new ArrayList<String>();
	final Set<String> props = getImmediatePropNames();
	System.out.println("Props: " + props);

	for (final String prop : props) {
	    int resolvedCount = sources.getMain().hasProperty(prop) ? 1 : 0;

	    for (final EntQueryCompoundSourceModel source : sources.getCompounds()) {
		resolvedCount = resolvedCount + (source.getSource().hasProperty(prop) ? 1 : 0);
	    }

	    if (resolvedCount > 1) {
		throw new IllegalStateException("Ambiguous property: " + prop);
	    }

	    if (resolvedCount == 0) {
		result.add(prop);
	    }
	}

	return result;
    }

    /**
     * By immediate prop names here are meant props used within this query and not within it's (nested) subqueries.
     *
     * @return
     */
    public Set<String> getImmediatePropNames() {
	final Set<String> result = new HashSet<String>();
	result.addAll(getPropNamesFromYields());
	result.addAll(getPropNamesFromGroups());
	if (conditions != null) {
	    result.addAll(conditions.getPropNames());
	}
	result.addAll(getPropNamesFromSources());
	return result;
    }

    public List<EntQuery> getImmediateSubqueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	result.addAll(getSubqueriesFromYields());
	result.addAll(getSubqueriesFromGroups());
	if (conditions != null) {
	    result.addAll(conditions.getSubqueries());
	}
	result.addAll(getSubqueriesFromSources());
	return result;
    }

    public Set<String> getPropNames() {
	return Collections.emptySet();
    }

    public List<EntQuery> getSubqueries() {
	return Arrays.asList(new EntQuery[] { this });
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
	    result.addAll(compSource.getJoinConditions().getPropNames());
	}
	return result;
    }

    private List<EntQuery> getSubqueriesFromYields() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	for (final YieldModel yield : yields.getYields()) {
	    result.addAll(yield.getOperand().getSubqueries());
	}
	return result;
    }

    private List<EntQuery> getSubqueriesFromGroups() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	for (final GroupModel group : groups.getGroups()) {
	    result.addAll(group.getOperand().getSubqueries());
	}
	return result;
    }

    private List<EntQuery> getSubqueriesFromSources() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	for (final EntQueryCompoundSourceModel compSource : sources.getCompounds()) {
	    result.addAll(compSource.getJoinConditions().getSubqueries());
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

    public EntQuery getMaster() {
	return master;
    }

    public Class getResultType() {
        return resultType;
    }
}

//public Set<String> getQrySourcesNames() {
//	final Set<String> result = new HashSet<String>();
//	if (sources.getMain().getAlias() != null) {
//	    result.add(sources.getMain().getAlias());
//	}
//	for (final EntQueryCompoundSourceModel compSource : sources.getCompounds()) {
//	    result.add(compSource.getSource().getAlias());
//	}
//	return result;
//}
