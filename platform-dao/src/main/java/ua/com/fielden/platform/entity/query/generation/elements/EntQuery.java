package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.PropertyPersistenceInfo;
import ua.com.fielden.platform.entity.query.generation.elements.AbstractEntQuerySource.PropResolutionInfo;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EntQuery implements ISingleOperand {

    private static String id = "id";

    private final EntQuerySourcesModel sources;
    private final ConditionsModel conditions;
    private final YieldsModel yields;
    private final GroupsModel groups;
    private final Class resultType;
    private final QueryCategory category;
    private final MappingsGenerator mappingsGenerator;

    private EntQuery master;

    private boolean isSubQuery() {
	return QueryCategory.SUB_QUERY.equals(category);
    }

    private boolean isSourceQuery() {
	return QueryCategory.SOURCE_QUERY.equals(category);
    }

    private boolean isResultQuery() {
	return QueryCategory.RESULT_QUERY.equals(category);
    }

    // need some calculated properties level and position in order to be taken into account in equals(..) and hashCode() methods to be able to handle correctly the same query used as subquery in different places (can be on the same level or different levels - e.g. ..(exists(sq).and.prop("a").gt.val(0)).or.notExist(sq)

    /**
     * modifiable set of unresolved props (introduced for performance reason - in order to avoid multiple execution of the same search against all query props while searching for
     * unresolved only; if at some master the property of this subquery is resolved - it should be removed from here
     */
    private final List<EntProp> unresolvedProps;

    @Override
    public String toString() {
	return sql();
    }

    public String sql() {
	final StringBuffer sb = new StringBuffer();
	sb.append(isSubQuery() ? "(" : "");
	sb.append("SELECT ");
	sb.append(yields.sql());
	sb.append("\nFROM ");
	sb.append(sources.sql());
	if (conditions != null) {
	    sb.append("\nWHERE ");
	    sb.append(conditions.sql());
	}
	sb.append(groups.sql());
	sb.append(isSubQuery() ? ")" : "");
	return sb.toString();
    }

    private int getMasterIndex() {
	int masterIndex = 0;
	EntQuery currMaster = this.master;
	while (currMaster != null) {
	    masterIndex = masterIndex + 1;
	    currMaster = currMaster.master;
	}
	return masterIndex;
    }

    public YieldModel getYield(final String yieldName) {
	return yields.getYields().get(yieldName);
    }

    private boolean onlyOneYieldAndWithoutAlias() {
	return yields.getYields().size() == 1 && yields.getYields().values().iterator().next().getAlias().equals("");
    }

    private boolean idAliasEnhancementRequired() {
	return onlyOneYieldAndWithoutAlias() && EntityUtils.isPersistedEntityType(resultType);
    }

    private boolean allPropsYieldEnhancementRequired() {
	return yields.getYields().size() == 0 && EntityUtils.isPersistedEntityType(resultType) && !isSubQuery();
    }

    private boolean idPropYieldEnhancementRequired() {
	return yields.getYields().size() == 0 && EntityUtils.isPersistedEntityType(resultType) && isSubQuery();
    }

    private void enhanceYieldsModel() {
	// enhancing short-cuts in yield section (e.g. the following: assign missing "id" alias in case yield().prop("someEntProp").modelAsEntity(entProp.class) is used
	if (idAliasEnhancementRequired()) {
	    final YieldModel idModel = new YieldModel(yields.getYields().values().iterator().next().getOperand(), id);
	    yields.getYields().clear();
	    yields.getYields().put(idModel.getAlias(), idModel);
	} else if (allPropsYieldEnhancementRequired()) {
	    final String yieldPropAliasPrefix = getSources().getMain().getAlias() == null ? "" : getSources().getMain().getAlias() + ".";
	    for (final PropertyPersistenceInfo ppi : mappingsGenerator.getEntityPPIs(type())) {
		if (!ppi.isCompositeProperty()) {
		    yields.getYields().put(ppi.getName(), new YieldModel(new EntProp(yieldPropAliasPrefix + ppi.getName()), ppi.getName(), ppi));
		}
	    }
	} else if (idPropYieldEnhancementRequired()) {
	    final String yieldPropAliasPrefix = getSources().getMain().getAlias() == null ? "" : getSources().getMain().getAlias() + ".";
	    yields.getYields().put(id, new YieldModel(new EntProp(yieldPropAliasPrefix + id), id));
	}
    }

    private void assignPropertyPersistenceInfoToYields() {
	int yieldIndex = 0;
	for (final YieldModel yield : yields.getYields().values()) {
	    yieldIndex = yieldIndex + 1;
	    final PropertyPersistenceInfo ppi = new PropertyPersistenceInfo.Builder(yield.getAlias(), determineYieldJavaType(yield)). //
	    column("C" + yieldIndex). //
	    hibType(determineYieldHibType(yield)). //
	    build();
	    yield.setInfo(ppi);
	}
    }

    private Object determineYieldHibType(final YieldModel yield) {
	final PropertyPersistenceInfo finalPropInfo = mappingsGenerator.getPropPersistenceInfoExplicitly(type(), yield.getAlias());
	if (finalPropInfo != null) {
	    return finalPropInfo.getHibType();
	} else {
	    return yield.getOperand().hibType();
	}
    }

    private Class determineYieldJavaType(final YieldModel yield) {
	if (EntityUtils.isPersistedEntityType(type())) {
	    final Class yieldTypeAccordingToQuerySources = yield.getOperand().type();
	    final Class yieldTypeAccordingToQueryResultType = PropertyTypeDeterminator.determinePropertyType(type(), yield.getAlias());

	    if (yieldTypeAccordingToQuerySources != null && !yieldTypeAccordingToQuerySources.equals(yieldTypeAccordingToQueryResultType)) {
		if (!(EntityUtils.isPersistedEntityType(yieldTypeAccordingToQuerySources) && Long.class.equals(yieldTypeAccordingToQueryResultType))) {
		    throw new IllegalStateException("Different types: from source = " + yieldTypeAccordingToQuerySources.getSimpleName() + " from result type = "
			    + yieldTypeAccordingToQueryResultType.getSimpleName());
		}
		return yieldTypeAccordingToQueryResultType;
	    } else {
		return yieldTypeAccordingToQueryResultType;
	    }
	} else {
	    return yield.getOperand().type();
	}
    }

    private void assignSqlParamNames() {
	int paramCount = 0;
	for (final EntValue value : getAllValues()) {
	    paramCount = paramCount + 1;
	    value.setParamName("P" + paramCount);
	}
    }

    public Map<String, Object> getValuesForSqlParams() {
	final Map<String, Object> result = new HashMap<String, Object>();
	for (final EntValue value : getAllValues()) {
	    result.put(value.getParamName(), value.getValue());
	}
	return result;
    }

    public EntQuery(final EntQuerySourcesModel sources, final ConditionsModel conditions, final YieldsModel yields, final GroupsModel groups, final Class resultType, final QueryCategory category, final MappingsGenerator mappingsGenerator) {
	super();
	this.category = category;
	this.mappingsGenerator = mappingsGenerator;
	this.sources = sources;
	this.conditions = conditions;
	this.yields = yields;
	this.groups = groups;
	this.resultType = resultType != null ? resultType : (yields.getYields().size() == 0 ? sources.getMain().sourceType() : null);

	enhanceYieldsModel(); //!! adds new properties in yield section

	final List<EntQuery> immediateSubqueries = getImmediateSubqueries();
	associateSubqueriesWithMasterQuery(immediateSubqueries);

	final List<EntProp> immediateProperties = getImmediateProps();
	associatePropertiesWithHoldingQuery(immediateProperties);

	final List<EntProp> propsToBeResolved = new ArrayList<EntProp>();
	propsToBeResolved.addAll(immediateProperties);
	propsToBeResolved.addAll(collectUnresolvedPropsFromSubqueries(immediateSubqueries));

	unresolvedProps = resolveProps(propsToBeResolved);

	if (!isSubQuery() && unresolvedProps.size() > 0) {
	    throw new RuntimeException("Couldn't resolve the following props: " + unresolvedProps);
	}

	for (final Pair<IEntQuerySource, Boolean> sourceAndItsJoinType : sources.getAllSourcesAndTheirJoinType()) {
	    final IEntQuerySource source = sourceAndItsJoinType.getKey();
	    sources.getCompounds().addAll(source.generateMissingSources(sourceAndItsJoinType.getValue(), source.getReferencingProps()));
	}

	final List<EntProp> immediatePropertiesFinally = getImmediateProps();
	associatePropertiesWithHoldingQuery(immediatePropertiesFinally);

	final List<EntProp> propsToBeResolvedFinally = new ArrayList<EntProp>();
	propsToBeResolvedFinally.addAll(immediatePropertiesFinally);
	propsToBeResolvedFinally.addAll(collectUnresolvedPropsFromSubqueries(immediateSubqueries));
	propsToBeResolvedFinally.removeAll(unresolvedProps);

	sources.assignSqlAliases(getMasterIndex());

	final List<EntProp> unresolvedFinalProps = resolvePropsFinally(propsToBeResolvedFinally);

	if (unresolvedFinalProps.size() > 0) {
	    throw new RuntimeException("Couldn't finally resolve the following props: " + unresolvedFinalProps);
	}

	assignPropertyPersistenceInfoToYields();

	assignSqlParamNames();
    }

    private void setMaster(final EntQuery master) {
	this.master = master;
    }

    private void associateSubqueriesWithMasterQuery(final List<EntQuery> immediateSubqueries) {
	for (final EntQuery entQuery : immediateSubqueries) {
	    entQuery.setMaster(this);
	}
    }

    private void associatePropertiesWithHoldingQuery(final List<EntProp> immediateProperties) {
	for (final EntProp entProp : immediateProperties) {
	    entProp.assignHolderIfNotAssigned(this);
	}
    }

    private List<EntProp> collectUnresolvedPropsFromSubqueries(final List<EntQuery> immediateSubqueries) {
	final List<EntProp> unresolvedPropsFromSubqueries = new ArrayList<EntProp>();
	for (final EntQuery entQuery : immediateSubqueries) {
	    unresolvedPropsFromSubqueries.addAll(entQuery.unresolvedProps);
	    entQuery.unresolvedProps.clear();
	}
	return unresolvedPropsFromSubqueries;
    }

    private List<EntProp> resolveProps(final List<EntProp> propsToBeResolved) {
	final List<EntProp> unresolvedProps = new ArrayList<EntProp>();

	for (final EntProp propToBeResolvedPair : propsToBeResolved) {
	    final Map<IEntQuerySource, PropResolutionInfo> sourceCandidates = findSourceMatchCandidates(propToBeResolvedPair);
	    if (sourceCandidates.size() == 0) {
		unresolvedProps.add(propToBeResolvedPair);
	    } else {
		final Pair<PropResolutionInfo, IEntQuerySource> propResolutionResult = performPropResolveAction(sourceCandidates);
		propResolutionResult.getValue().addReferencingProp(propResolutionResult.getKey());
	    }
	}

	return unresolvedProps;
    }

    private List<EntProp> resolvePropsFinally(final List<EntProp> propsToBeResolved) {
	final List<EntProp> unresolvedProps = new ArrayList<EntProp>();

	for (final EntProp propToBeResolvedPair : propsToBeResolved) {
	    final Map<IEntQuerySource, PropResolutionInfo> sourceCandidates = findSourceMatchCandidates(propToBeResolvedPair);
	    if (sourceCandidates.size() > 0) {
		final Pair<PropResolutionInfo, IEntQuerySource> propResolutionResult = performPropResolveAction(sourceCandidates);
		propResolutionResult.getValue().addFinalReferencingProp(propResolutionResult.getKey());
	    } else {
		unresolvedProps.add(propToBeResolvedPair);
	    }
	}

	return unresolvedProps;
    }

    private Map<IEntQuerySource, PropResolutionInfo> findSourceMatchCandidates(final EntProp prop) {
	final Map<IEntQuerySource, PropResolutionInfo> result = new HashMap<IEntQuerySource, PropResolutionInfo>();
	for (final IEntQuerySource source : sources.getAllSources()) {
	    final PropResolutionInfo hasProp = source.containsProperty(prop);
	    if (hasProp != null) {
		result.put(source, hasProp);
	    }
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
    private Pair<PropResolutionInfo, IEntQuerySource> performPropResolveAction(final Map<IEntQuerySource, PropResolutionInfo> candidates) {
	if (candidates.size() == 1) {
	    return new Pair<PropResolutionInfo, IEntQuerySource>(candidates.values().iterator().next(), candidates.keySet().iterator().next());
	} else {
	    final SortedSet<Integer> preferenceNumbers = new TreeSet<Integer>();
	    final Map<Integer, List<IEntQuerySource>> sourcesPreferences = new HashMap<Integer, List<IEntQuerySource>>();
	    for (final Entry<IEntQuerySource, PropResolutionInfo> entry : candidates.entrySet()) {
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
		return new Pair<PropResolutionInfo, IEntQuerySource>(candidates.get(sourcesPreferences.get(preferenceResult).get(0)), sourcesPreferences.get(preferenceResult).get(0));
	    } else {
		int notAliasedSourcesCount = 0;
		Pair<PropResolutionInfo, IEntQuerySource> resultPair = null;
		for (final IEntQuerySource qrySource : preferedSourceList) {
		    if (qrySource.getAlias() == null) {
			resultPair = new Pair<PropResolutionInfo, IEntQuerySource>(candidates.get(qrySource), qrySource);
			notAliasedSourcesCount = notAliasedSourcesCount + 1;
		    }
		}

		if (notAliasedSourcesCount == 1) {
		    return resultPair;
		}

		throw new IllegalStateException("Ambiguous property: " + candidates.values().iterator().next().getEntProp().getName());
	    }
	}
    }

    /**
     * By immediate props here are meant props used within this query and not within it's (nested) subqueries.
     *
     * @return
     */
    public List<EntProp> getImmediateProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	result.addAll(sources.getLocalProps());
	if (conditions != null) {
	    result.addAll(conditions.getLocalProps());
	}
	result.addAll(groups.getLocalProps());
	result.addAll(yields.getLocalProps());
	return result;
    }

    public List<EntQuery> getImmediateSubqueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	result.addAll(yields.getLocalSubQueries());
	result.addAll(groups.getLocalSubQueries());
	if (conditions != null) {
	    result.addAll(conditions.getLocalSubQueries());
	}
	result.addAll(sources.getLocalSubQueries());
	return result;
    }

    @Override
    public List<EntProp> getLocalProps() {
	return Collections.emptyList();
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
	return Arrays.asList(new EntQuery[] { this });
    }

    @Override
    public List<EntValue> getAllValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	result.addAll(sources.getAllValues());
	if (conditions != null) {
	    result.addAll(conditions.getAllValues());
	}
	result.addAll(groups.getAllValues());
	result.addAll(yields.getAllValues());
	return result;
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

    public List<EntProp> getUnresolvedProps() {
	return unresolvedProps;
    }

    @Override
    public boolean ignore() {
	return false;
    }

    @Override
    public Class type() {
	return resultType;
    }

    @Override
    public Object hibType() {
	if (yields.getYields().size() == 1) {
	  return yields.getYields().values().iterator().next().getInfo().getHibType();
	}
	return null;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
	result = prime * result + ((groups == null) ? 0 : groups.hashCode());
	result = prime * result + ((category == null) ? 0 : category.hashCode());
	result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
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
	if (category != other.category) {
	    return false;
	}
	if (resultType == null) {
	    if (other.resultType != null) {
		return false;
	    }
	} else if (!resultType.equals(other.resultType)) {
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