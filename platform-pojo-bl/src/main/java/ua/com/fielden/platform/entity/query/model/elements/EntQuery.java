package ua.com.fielden.platform.entity.query.model.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.utils.Pair;

public class EntQuery implements ISingleOperand {
    private final EntQuerySourcesModel sources;
    private final ConditionsModel conditions;
    private final YieldsModel yields;
    private final GroupsModel groups;
    private final Class resultType;
    private EntQuery master;
    // need some calculated properties level and position in order to be taken into account in equals(..) and hashCode() methods to be able to handle correctly the same query used as subquery in different places (can be on the same level or different levels - e.g. ..(exists(sq).and.prop("a").gt.val(0)).or.notExist(sq)
    private final List<Pair<EntQuery, String>> unresolvedProps;
    // modifiable set of unresolved props (introduced for performance reason - in order to avoid multiple execution of the same search against all query props while searching for unresolved only
    // if at some master the property of this subquery is resolved - it should be removed from here



    public EntQuery(final EntQuerySourcesModel sources, final ConditionsModel conditions, final YieldsModel yields, final GroupsModel groups, final Class resultType) {
	super();
	this.sources = sources;
	this.conditions = conditions;
	this.yields = yields;
	this.groups = groups;
	this.resultType = resultType != null ? resultType : (yields.getYields().size() == 0 ? sources.getMain().getType() : null);

	// TODO enhance short-cuts in yield section (e.g. the following:
	// assign missing "id" alias in case yield().prop("someEntProp").modelAsEntity(entProp.class) is used
	if (this.resultType != null && yields.getYields().size() == 1 && AbstractEntity.class.isAssignableFrom(this.resultType) && !EntityAggregates.class.isAssignableFrom(this.resultType)) {
	    final YieldModel idModel = new YieldModel(yields.getYields().get(0).getOperand(), "id");
	    yields.getYields().remove(0);
	    yields.getYields().add(idModel);
	}

	final List<Pair<EntQuery, String>> unresolvedPropsFromSubqueries = new ArrayList<Pair<EntQuery, String>>();
	for (final EntQuery entQuery : getImmediateSubqueries()) {
	    entQuery.master = this;
	    unresolvedPropsFromSubqueries.addAll(entQuery.unresolvedProps);
	    entQuery.unresolvedProps.clear();
	}

	unresolvedProps = resolveProps(unresolvedPropsFromSubqueries);
    }

//    public List<EntQuery> getLeafSubqueries() {
//	final List<EntQuery> result = new ArrayList<EntQuery>();
//	for (final EntQuery entQuery : getImmediateSubqueries()) {
//	    final List<EntQuery> subSubQueries = entQuery.getImmediateSubqueries();
//	    if (subSubQueries.size() > 0) {
//		result.addAll(entQuery.getLeafSubqueries());
//	    } else {
//		result.add(entQuery);
//	    }
//	}
//	return result;
//    }

    private List<Pair<EntQuery, String>> resolveProps(final List<Pair<EntQuery, String>> unresolvedPropsFromSubqueries) {
	final List<Pair<EntQuery, String>> unresolvedProps = new ArrayList<Pair<EntQuery, String>>();
	final Set<String> props = getImmediatePropNames();
	System.out.println("Props: " + props);

	for (final Pair<EntQuery, String> unresolvedPropPair : unresolvedPropsFromSubqueries) {
	    final Pair<EntQuery,String> propResolutionResult = performResolveAction(unresolvedPropPair.getKey(), unresolvedPropPair.getValue());
	    if (propResolutionResult != null) {
		unresolvedProps.add(propResolutionResult);
	    }
	}

	for (final String prop : props) {
	    final Pair<EntQuery,String> propResolutionResult = performResolveAction(this, prop);
	    if (propResolutionResult != null) {
		unresolvedProps.add(propResolutionResult);
	    }
	}

	return unresolvedProps;
    }

    private Pair<EntQuery, String> performResolveAction(final EntQuery holder, final String prop) {
	int resolvedCount = sources.getMain().hasProperty(prop) ? 1 : 0;

	for (final EntQueryCompoundSourceModel source : sources.getCompounds()) {
	    resolvedCount = resolvedCount + (source.getSource().hasProperty(prop) ? 1 : 0);
	}

	if (resolvedCount > 1) {
	    throw new IllegalStateException("Ambiguous property: " + prop);
	}

	return resolvedCount == 0 ? new Pair<EntQuery, String>(holder, prop) : null;
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

    public List<Pair<EntQuery, String>> getUnresolvedProps() {
        return unresolvedProps;
    }

    @Override
    public boolean ignore() {
	return false;
    }
}