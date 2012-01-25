package ua.com.fielden.platform.entity.query.model.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.elements.AbstractEntQuerySource.PropResolutionInfo;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EntQuery implements ISingleOperand {

    private static String id = "id";

    private final EntQuerySourcesEnhancer entQrySourcesEnhancer = new EntQuerySourcesEnhancer();
    private final EntQuerySourcesModel sources;
    private final ConditionsModel conditions;
    private final YieldsModel yields;
    private final GroupsModel groups;
    private final Class resultType;
    private EntQuery master;
    private final boolean subquery;

    // need some calculated properties level and position in order to be taken into account in equals(..) and hashCode() methods to be able to handle correctly the same query used as subquery in different places (can be on the same level or different levels - e.g. ..(exists(sq).and.prop("a").gt.val(0)).or.notExist(sq)
    private final List<Pair<EntQuery, EntProp>> unresolvedProps;

    // modifiable set of unresolved props (introduced for performance reason - in order to avoid multiple execution of the same search against all query props while searching for unresolved only
    // if at some master the property of this subquery is resolved - it should be removed from here

    public YieldModel getYield(final String yieldName) {
	return yields.getYields().get(yieldName);
    }

    private boolean resultTypeIsRealEntity() {
	return resultType != null && AbstractEntity.class.isAssignableFrom(resultType) && !EntityAggregates.class.isAssignableFrom(resultType);
    }

    private boolean onlyOneYieldAndWithoutAlias() {
	return yields.getYields().size() == 1 && yields.getYields().keySet().iterator().next() == null;
    }

    private boolean idAliasEnhancementRequired() {
	return onlyOneYieldAndWithoutAlias() && resultTypeIsRealEntity();
    }

    private boolean allPropsYieldEnhancementRequired() {
	return yields.getYields().size() == 0 && resultTypeIsRealEntity() && !subquery;
    }

    private boolean idPropYieldEnhancementRequired() {
	return yields.getYields().size() == 0 && resultTypeIsRealEntity() && subquery;
    }

    public EntQuery(final EntQuerySourcesModel sources, final ConditionsModel conditions, final YieldsModel yields, final GroupsModel groups, final Class resultType, final boolean subquery) {
	super();
	this.subquery = subquery;
	this.sources = sources;
	this.conditions = conditions;
	this.yields = yields;
	this.groups = groups;
	this.resultType = resultType != null ? resultType : (yields.getYields().size() == 0 ? sources.getMain().getType() : null);

	final List<EntQuery> immediateSubqueries = getImmediateSubqueries();

	associateSubqueriesWithMasterQuery(immediateSubqueries);

	// enhancing short-cuts in yield section (e.g. the following: assign missing "id" alias in case yield().prop("someEntProp").modelAsEntity(entProp.class) is used
	if (idAliasEnhancementRequired()) {
	    final YieldModel idModel = new YieldModel(yields.getYields().values().iterator().next().getOperand(), id);
	    yields.getYields().clear();
	    yields.getYields().put(idModel.getAlias(), idModel);
	} else if (allPropsYieldEnhancementRequired()) {
	    final String yieldPropAlias = getSources().getMain().getAlias() == null ? "" : getSources().getMain().getAlias() + ".";
	    for (final String propName : EntityUtils.getPersistedPropertiesNames(type())) {
		yields.getYields().put(propName, new YieldModel(new EntProp(yieldPropAlias + propName), propName));
	    }
	} else if (idPropYieldEnhancementRequired()) {
	    final String yieldPropAlias = getSources().getMain().getAlias() == null ? "" : getSources().getMain().getAlias() + ".";
	    yields.getYields().put(id, new YieldModel(new EntProp(yieldPropAlias + id), id));
	}

	unresolvedProps = resolveProps(collectUnresolvedPropsFromSubqueries(immediateSubqueries));

	for (final Pair<IEntQuerySource, Boolean> sourceWithJoinType : sources.getAllSourcesWithJoinType()) {
	    sources.getCompounds().addAll(generateImplicitSources(sourceWithJoinType.getKey(), sourceWithJoinType.getValue()));
	}

	//resolveProps(unresolvedPropsFromSubqueries);
    }


    private void assignMaster(final EntQuery master) {
	this.master = master;
    }

    private void associateSubqueriesWithMasterQuery(final List<EntQuery> immediateSubqueries) {
	for (final EntQuery entQuery : immediateSubqueries) {
	    entQuery.assignMaster(this);
	}
    }

    private List<Pair<EntQuery, EntProp>> collectUnresolvedPropsFromSubqueries(final List<EntQuery> immediateSubqueries) {
	final List<Pair<EntQuery, EntProp>> unresolvedPropsFromSubqueries = new ArrayList<Pair<EntQuery, EntProp>>();
	for (final EntQuery entQuery : immediateSubqueries) {
	    unresolvedPropsFromSubqueries.addAll(entQuery.unresolvedProps);
	    entQuery.unresolvedProps.clear();
	}
	return unresolvedPropsFromSubqueries;
    }

    private List<EntQueryCompoundSourceModel> generateImplicitSources(final IEntQuerySource source, final boolean leftJoined) {
	final List<EntQueryCompoundSourceModel> result = new ArrayList<EntQueryCompoundSourceModel>();
	final Set<PropTree> propTrees = entQrySourcesEnhancer.produceSourcesTree(source, source.getAlias(), leftJoined, extractNames(source.getReferencingProps(), source.getAlias()));
	for (final PropTree propTree : propTrees) {
	    result.addAll(propTree.getSourceModels());
	}
	return result;
    }

    private List<Pair<EntQuery, EntProp>> resolveProps(final List<Pair<EntQuery, EntProp>> unresolvedPropsFromSubqueries) {
	final List<Pair<EntQuery, EntProp>> unresolvedProps = new ArrayList<Pair<EntQuery, EntProp>>();
	final List<EntProp> props = getImmediateProps();

	for (final Pair<EntQuery, EntProp> unresolvedPropPair : unresolvedPropsFromSubqueries) {
	    final Pair<EntQuery, EntProp> propResolutionResult = performPropResolveAction(unresolvedPropPair.getKey(), unresolvedPropPair.getValue());
	    if (propResolutionResult != null) {
		unresolvedProps.add(propResolutionResult);
	    }
	}

	for (final EntProp prop : props) {
	    final Pair<EntQuery, EntProp> propResolutionResult = performPropResolveAction(this, prop);
	    if (propResolutionResult != null) {
		unresolvedProps.add(propResolutionResult);
	    }
	}

	return unresolvedProps;
    }

    private Set<String> extractNames(final List<EntProp> props, final String sourceAlias) {
	final Set<String> result = new HashSet<String>();
	for (final EntProp prop : props) {
	    result.add(sourceAlias != null && prop.getName().startsWith(sourceAlias + ".") ? prop.getName().substring(sourceAlias.length() + 1) : prop.getName());
	}
	return result;
    }

    /**
     * If property is found within holder query sources then establish link between them (inlc. prop type setting) and return null, else return pair (holder, prop).
     *
     * @param holder
     * @param prop
     * @return
     */
    private Pair<EntQuery, EntProp> performPropResolveAction(final EntQuery holder, final EntProp prop) {
	final Map<IEntQuerySource, PropResolutionInfo> result = new HashMap<IEntQuerySource, PropResolutionInfo>();

	for (final IEntQuerySource source : sources.getAllSources()) {
	    final Pair<Boolean, PropResolutionInfo> hasProp = source.containsProperty(prop);
	    if (hasProp.getKey()) {
		result.put(source, hasProp.getValue());
	    }
	}

	if (result.size() == 0) {
	    return new Pair<EntQuery, EntProp>(holder, prop);
	} else if (result.size() == 1) {
	    prop.setPropType(result.values().iterator().next().getPropType());
	    result.keySet().iterator().next().addReferencingProp(prop);
	    return null;
	} else {
	    final SortedSet<Integer> preferenceNumbers = new TreeSet<Integer>();
	    final Map<Integer, List<IEntQuerySource>> sourcesPreferences = new HashMap<Integer, List<IEntQuerySource>>();
	    for (final Entry<IEntQuerySource, PropResolutionInfo> entry : result.entrySet()) {
		final Integer currPrefNumber = entry.getValue().getPreferenceNumber();
		preferenceNumbers.add(currPrefNumber);
		if (!sourcesPreferences.containsKey(currPrefNumber)) {
		    sourcesPreferences.put(currPrefNumber, new ArrayList<IEntQuerySource>());
		}
		sourcesPreferences.get(currPrefNumber).add(entry.getKey());
	    }

	    final Integer preferenceResult = preferenceNumbers.first();

	    final List<IEntQuerySource> preferedSourceList = sourcesPreferences.get(preferenceResult);
	    if (preferedSourceList.size() == 1) {
		sourcesPreferences.get(preferenceResult).get(0).addReferencingProp(prop);
		prop.setPropType(result.get(sourcesPreferences.get(preferenceResult).get(0)).getPropType());
		return null;
	    } else {
		int notAliasedSourcesCount = 0;
		for (final IEntQuerySource qrySource : preferedSourceList) {
		    if (result.get(qrySource).getAliasPart() == null) {
			qrySource.addReferencingProp(prop);
			prop.setPropType(result.get(qrySource).getPropType());
			notAliasedSourcesCount = notAliasedSourcesCount + 1;
		    }
		}

		if (notAliasedSourcesCount == 1) {
		    return null;
		}

		throw new IllegalStateException("Ambiguous property: " + prop.getName());
	    }
	}
    }

    public static class PropTree implements Comparable<PropTree>{
	boolean leftJoin;
	String parentName;
	String parentFullName;
	Class parentType;
	Set<PropTree> subprops;

	public PropTree(final String parentName, final String parentFullName, final Class parentType, final boolean leftJoin, final Set<PropTree> subprops) {
	    this.parentName = parentName;
	    this.parentFullName = parentFullName;
	    this.parentType = parentType;
	    this.subprops = subprops;
	    this.leftJoin = leftJoin;
	}

	private ConditionsModel joinCondition(final String leftProp, final String rightProp) {
	    return new ConditionsModel(new ComparisonTestModel(new EntProp(leftProp), ComparisonOperator.EQ, new EntProp(rightProp)));
	}

	private IEntQuerySource querySource(final Class entityType, final String sourceAlias) {
	    return new EntQuerySourceAsEntity(entityType, sourceAlias, true);
	}

	private JoinType joinType(final boolean leftJoin) {
	    return leftJoin ? JoinType.LJ : JoinType.IJ;
	}

	public List<EntQueryCompoundSourceModel> getSourceModels() {
	    final List<EntQueryCompoundSourceModel> result = new ArrayList<EntQueryCompoundSourceModel>();
	    result.add(new EntQueryCompoundSourceModel(querySource(parentType, parentFullName), joinType(leftJoin), joinCondition(parentFullName, parentFullName + ".id")));
	    for (final PropTree subPropTree : subprops) {
		result.addAll(subPropTree.getSourceModels());
	    }
	    return result;
	}

	@Override
	public String toString() {
	    return parentName + " _ " + parentFullName + " _ " + parentType.getSimpleName() + " _ " + subprops + " _ " + leftJoin;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + (leftJoin ? 1231 : 1237);
	    result = prime * result + ((parentFullName == null) ? 0 : parentFullName.hashCode());
	    result = prime * result + ((parentName == null) ? 0 : parentName.hashCode());
	    result = prime * result + ((parentType == null) ? 0 : parentType.hashCode());
	    result = prime * result + ((subprops == null) ? 0 : subprops.hashCode());
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
	    if (!(obj instanceof PropTree)) {
		return false;
	    }
	    final PropTree other = (PropTree) obj;
	    if (leftJoin != other.leftJoin) {
		return false;
	    }
	    if (parentFullName == null) {
		if (other.parentFullName != null) {
		    return false;
		}
	    } else if (!parentFullName.equals(other.parentFullName)) {
		return false;
	    }
	    if (parentName == null) {
		if (other.parentName != null) {
		    return false;
		}
	    } else if (!parentName.equals(other.parentName)) {
		return false;
	    }
	    if (parentType == null) {
		if (other.parentType != null) {
		    return false;
		}
	    } else if (!parentType.equals(other.parentType)) {
		return false;
	    }
	    if (subprops == null) {
		if (other.subprops != null) {
		    return false;
		}
	    } else if (!subprops.equals(other.subprops)) {
		return false;
	    }
	    return true;
	}

	@Override
	public int compareTo(final PropTree o) {
	    return this.parentFullName.compareTo(o.parentFullName);
	}
    }

    /**
     * By immediate props here are meant props used within this query and not within it's (nested) subqueries.
     *
     * @return
     */
    public List<EntProp> getImmediateProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	result.addAll(getPropsFromSources());
	if (conditions != null) {
	    result.addAll(conditions.getProps());
	}
	result.addAll(getPropsFromGroups());
	result.addAll(getPropsFromYields());
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

    @Override
    public List<EntProp> getProps() {
	return Collections.emptyList();
    }

    public List<EntQuery> getSubqueries() {
	return Arrays.asList(new EntQuery[] { this });
    }

    private List<EntProp> getPropsFromYields() {
	final List<EntProp> result = new ArrayList<EntProp>();
	for (final YieldModel yield : yields.getYields().values()) {
	    result.addAll(yield.getOperand().getProps());
	}
	return result;
    }

    private List<EntProp> getPropsFromGroups() {
	final List<EntProp> result = new ArrayList<EntProp>();
	for (final GroupModel group : groups.getGroups()) {
	    result.addAll(group.getOperand().getProps());
	}
	return result;
    }

    private List<EntProp> getPropsFromSources() {
	final List<EntProp> result = new ArrayList<EntProp>();
	for (final EntQueryCompoundSourceModel compSource : sources.getCompounds()) {
	    result.addAll(compSource.getJoinConditions().getProps());
	}
	return result;
    }

    private List<EntQuery> getSubqueriesFromYields() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	for (final YieldModel yield : yields.getYields().values()) {
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

    public List<Pair<EntQuery, EntProp>> getUnresolvedProps() {
	return unresolvedProps;
    }

    @Override
    public boolean ignore() {
	return false;
    }

    public void validate() {
	if (unresolvedProps.size() > 0) {
	    final StringBuffer sb = new StringBuffer();
	    for (final Pair<EntQuery, EntProp> pair : unresolvedProps) {
		sb.append(pair.getValue().getName() + "\n");
	    }
	    throw new RuntimeException("Couldn't resolve all properties: \n" + sb.toString());
	}
    }

    @Override
    public Class type() {
	return resultType;
    }
}