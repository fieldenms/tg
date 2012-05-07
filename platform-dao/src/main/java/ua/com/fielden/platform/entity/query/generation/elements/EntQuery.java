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

import ua.com.fielden.platform.dao.DomainPersistenceMetadataAnalyser;
import ua.com.fielden.platform.dao.PropertyPersistenceInfo;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.generation.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.generation.StandAloneExpressionBuilder;
import ua.com.fielden.platform.entity.query.generation.elements.AbstractSource.PropResolutionInfo;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EntQuery implements ISingleOperand {

    private static String id = "id";

    private final Sources sources;
    private final Conditions conditions;
    private final Yields yields;
    private final GroupBys groups;
    private final OrderBys orderings;
    private final Class resultType;
    private final QueryCategory category;
    private final DomainPersistenceMetadataAnalyser domainPersistenceMetadataAnalyser;

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

    /**
     * modifiable set of unresolved props (introduced for performance reason - in order to avoid multiple execution of the same search against all query props while searching for
     * unresolved only; if at some master the property of this subquery is resolved - it should be removed from here
     */
    private List<EntProp> unresolvedProps;

    @Override
    public String toString() {
	return sql();
    }

    public String sql() {
	sources.assignSqlAliases(getMasterIndex());

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
	sb.append(orderings.sql());
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

    public Yield getYield(final String yieldName) {
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
	    final Yield idModel = new Yield(yields.getYields().values().iterator().next().getOperand(), id);
	    yields.getYields().clear();
	    yields.getYields().put(idModel.getAlias(), idModel);
	} else if (allPropsYieldEnhancementRequired()) {
	    final String yieldPropAliasPrefix = getSources().getMain().getAlias() == null ? "" : getSources().getMain().getAlias() + ".";
	    for (final PropertyPersistenceInfo ppi : domainPersistenceMetadataAnalyser.getEntityPPIs(type())) {
		if (/*!ppi.isCompositeProperty() && */!ppi.isCollection()/* && !ppi.isCalculated()*/) {
		    final ResultQueryYieldDetails rqyd = new ResultQueryYieldDetails(ppi.getName(), ppi.getJavaType(), ppi.getHibType(), ppi.getColumn());
		    yields.getYields().put(rqyd.getName(), new Yield(new EntProp(yieldPropAliasPrefix + rqyd.getName()), rqyd.getName(), rqyd));
		}
	    }
	} else if (idPropYieldEnhancementRequired()) {
	    final String yieldPropAliasPrefix = getSources().getMain().getAlias() == null ? "" : getSources().getMain().getAlias() + ".";
	    yields.getYields().put(id, new Yield(new EntProp(yieldPropAliasPrefix + id), id));
	}
    }

    private void assignPropertyPersistenceInfoToYields() {
	int yieldIndex = 0;
	for (final Yield yield : yields.getYields().values()) {
	    yieldIndex = yieldIndex + 1;
	    final ResultQueryYieldDetails ppi = new ResultQueryYieldDetails(yield.getAlias(), determineYieldJavaType(yield), determineYieldHibType(yield), "C" + yieldIndex, determineYieldNullability(yield));
	    yield.setInfo(ppi);
	}
    }

    private Object determineYieldHibType(final Yield yield) {
	final PropertyPersistenceInfo finalPropInfo = domainPersistenceMetadataAnalyser.getInfoForDotNotatedProp(type(), yield.getAlias());
	if (finalPropInfo != null) {
	    return finalPropInfo.getHibType();
	} else {
	    return yield.getOperand().hibType();
	}
    }

    private boolean determineYieldNullability(final Yield yield) {
	    return yield.getOperand().isNullable();
    }

    private Class determineYieldJavaType(final Yield yield) {
	if (EntityUtils.isPersistedEntityType(type())) {
	    final Class yieldTypeAccordingToQuerySources = yield.getOperand().type();
	    final Class yieldTypeAccordingToQueryResultType = PropertyTypeDeterminator.determinePropertyType(type(), yield.getAlias());

	    if (yieldTypeAccordingToQuerySources != null && !yieldTypeAccordingToQuerySources.equals(yieldTypeAccordingToQueryResultType)) {
		if (!(EntityUtils.isPersistedEntityType(yieldTypeAccordingToQuerySources) && Long.class.equals(yieldTypeAccordingToQueryResultType)) && //
			!(EntityUtils.isPersistedEntityType(yieldTypeAccordingToQueryResultType) && Long.class.equals(yieldTypeAccordingToQuerySources))) {
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
	    value.setSqlParamName("P" + paramCount);
	}
    }

    public Map<String, Object> getValuesForSqlParams() {
	final Map<String, Object> result = new HashMap<String, Object>();
	for (final EntValue value : getAllValues()) {
	    result.put(value.getSqlParamName(), value.getValue());
	}
	return result;
    }

    private Sources enhanceSourcesWithUserDataFiltering(final IFilter filter, final String username, final Sources sources, final EntQueryGenerator generator) {
	final ISource newMain =
		(sources.getMain() instanceof TypeBasedSource && filter != null && filter.enhance(sources.getMain().sourceType(), username) != null) ?
	    new QueryBasedSource(sources.getMain().getAlias(), domainPersistenceMetadataAnalyser, generator.generateEntQueryAsSourceQuery(filter.enhance(sources.getMain().sourceType(), username))) : null;

	return newMain != null ? new Sources(newMain, sources.getCompounds()) : sources;
    }

    public EntQuery(final Sources sources, final Conditions conditions, final Yields yields, final GroupBys groups, final OrderBys orderings, //
	    final Class resultType, final QueryCategory category, final DomainPersistenceMetadataAnalyser domainPersistenceMetadataAnalyser, //
	    final IFilter filter, final String username, final EntQueryGenerator generator) {
	super();
	this.category = category;
	this.domainPersistenceMetadataAnalyser = domainPersistenceMetadataAnalyser;
	this.sources = enhanceSourcesWithUserDataFiltering(filter, username, sources, generator);
	this.conditions = conditions;
	this.yields = yields;
	this.groups = groups;
	this.orderings = orderings;
	this.resultType = resultType != null ? resultType : (yields.getYields().size() == 0 ? this.sources.getMain().sourceType() : null);

	enhanceToFinalState(generator);
    }

    private Pair<List<EntProp>, List<EntProp>> separateInternalFromExternalProps(final List<EntProp> allProps) {
	final List<EntProp> internal = new ArrayList<EntProp>();
	final List<EntProp> external = new ArrayList<EntProp>();

	for (final EntProp entProp : allProps) {
	    if (entProp.isExternal()) {
		external.add(entProp);
	    } else {
		internal.add(entProp);
	    }
	}

	return new Pair<List<EntProp>, List<EntProp>>(internal, external);
    }

    private void enhanceToFinalState(final EntQueryGenerator generator) {
	for (final Pair<ISource, Boolean> sourceAndItsJoinType : getSources().getAllSourcesAndTheirJoinType()) {
	    final ISource source = sourceAndItsJoinType.getKey();
	    source.assignNullability(sourceAndItsJoinType.getValue());
	    source.populateSourceItems(sourceAndItsJoinType.getValue());
	}

	enhanceYieldsModel(); //!! adds new properties in yield section

	boolean allCalculatedPropsResolved = false;

	List<EntQuery> immediateSubqueries = null;

	final List<EntProp> extProps = new ArrayList<EntProp>();

	while (!allCalculatedPropsResolved) {
	    immediateSubqueries = getImmediateSubqueries();
	    associateSubqueriesWithMasterQuery(immediateSubqueries);

	    final Pair<List<EntProp>, List<EntProp>> immediateAllProperties = separateInternalFromExternalProps(getImmediateProps());
	    final List<EntProp> immediateProperties = immediateAllProperties.getKey();
	    final List<EntProp> immediateExtProperties = immediateAllProperties.getValue();
	    associatePropertiesWithHoldingQuery(immediateProperties);

	    final List<EntProp> propsToBeResolved = new ArrayList<EntProp>();
	    propsToBeResolved.addAll(immediateProperties);
	    propsToBeResolved.addAll(collectUnresolvedPropsFromSubqueries(immediateSubqueries));

	    final Pair<List<EntProp>, Boolean> result = resolveProps(propsToBeResolved, generator);
	    unresolvedProps = result.getKey();
	    extProps.addAll(immediateExtProperties);
	    allCalculatedPropsResolved = result.getValue();
	}

	if (!isSubQuery() && unresolvedProps.size() > 0) {
	    throw new RuntimeException("Couldn't resolve the following props: " + unresolvedProps);
	}

	for (final Pair<ISource, Boolean> sourceAndItsJoinType : getSources().getAllSourcesAndTheirJoinType()) {
	    final ISource source = sourceAndItsJoinType.getKey();
	    getSources().getCompounds().addAll(source.generateMissingSources(source.getReferencingProps()));
	}

	final List<EntProp> immediatePropertiesFinally = separateInternalFromExternalProps(getImmediateProps()).getKey();
	associatePropertiesWithHoldingQuery(immediatePropertiesFinally);

	final List<EntProp> propsToBeResolvedFinally = new ArrayList<EntProp>();
	propsToBeResolvedFinally.addAll(immediatePropertiesFinally);
	propsToBeResolvedFinally.addAll(collectUnresolvedPropsFromSubqueries(immediateSubqueries));
	propsToBeResolvedFinally.removeAll(unresolvedProps);

	final List<EntProp> unresolvedFinalProps = resolvePropsFinally(propsToBeResolvedFinally);

	if (unresolvedFinalProps.size() > 0) {
	    throw new RuntimeException("Couldn't finally resolve the following props: " + unresolvedFinalProps);
	}

	for (final EntProp entProp : extProps) {
		entProp.setExternal(false);
		unresolvedProps.add(entProp);
	}

	assignPropertyPersistenceInfoToYields();

	if (isResultQuery()) {
	    assignSqlParamNames();
	}
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
	    //entQuery.unresolvedProps.clear();
	}
	return unresolvedPropsFromSubqueries;
    }

    private Pair<List<EntProp>, Boolean> resolveProps(final List<EntProp> propsToBeResolved, final EntQueryGenerator generator) {
	final List<EntProp> unresolvedProps = new ArrayList<EntProp>();
	boolean allCalculatedPropsResolved = true;

	for (final EntProp propToBeResolvedPair : propsToBeResolved) {
	    final Map<ISource, PropResolutionInfo> sourceCandidates = findSourceMatchCandidates(propToBeResolvedPair);
	    if (sourceCandidates.size() == 0) {
		unresolvedProps.add(propToBeResolvedPair);
	    } else {
		final Pair<PropResolutionInfo, ISource> propResolutionResult = performPropResolveAction(sourceCandidates);
		final PropResolutionInfo pri = propResolutionResult.getKey();
		if (pri.getProp().expressionModel == null || (pri.getProp().expressionModel != null && pri.getEntProp().isExpression())) {
		    propResolutionResult.getValue().addReferencingProp(pri);
		} else {
		    //System.out.println(pri.getProp().getName() + "_______________________________ " + pri.getProp().expression);
		    allCalculatedPropsResolved = false;
		    pri.getEntProp().setExpression((Expression) new StandAloneExpressionBuilder(generator, Collections.<String, Object> emptyMap(), pri.getProp().expressionModel).getResult().getValue(), pri.getProp().expressionModel.isContextPrefixNeeded());
		}
	    }
	}

	return new Pair<List<EntProp>, Boolean>(unresolvedProps, allCalculatedPropsResolved);
    }

    private List<EntProp> resolvePropsFinally(final List<EntProp> propsToBeResolved) {
	final List<EntProp> unresolvedProps = new ArrayList<EntProp>();

	for (final EntProp propToBeResolvedPair : propsToBeResolved) {
	    final Map<ISource, PropResolutionInfo> sourceCandidates = findSourceMatchCandidates(propToBeResolvedPair);
	    if (sourceCandidates.size() > 0) {
		final Pair<PropResolutionInfo, ISource> propResolutionResult = performPropResolveAction(sourceCandidates);
		propResolutionResult.getValue().addFinalReferencingProp(propResolutionResult.getKey());
	    } else {
		unresolvedProps.add(propToBeResolvedPair);
	    }
	}

	return unresolvedProps;
    }

    private Map<ISource, PropResolutionInfo> findSourceMatchCandidates(final EntProp prop) {
	final Map<ISource, PropResolutionInfo> result = new HashMap<ISource, PropResolutionInfo>();
	for (final ISource source : sources.getAllSources()) {
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
    private Pair<PropResolutionInfo, ISource> performPropResolveAction(final Map<ISource, PropResolutionInfo> candidates) {
	if (candidates.size() == 1) {
	    return new Pair<PropResolutionInfo, ISource>(candidates.values().iterator().next(), candidates.keySet().iterator().next());
	} else {
	    final SortedSet<Integer> preferenceNumbers = new TreeSet<Integer>();
	    final Map<Integer, List<ISource>> sourcesPreferences = new HashMap<Integer, List<ISource>>();
	    for (final Entry<ISource, PropResolutionInfo> entry : candidates.entrySet()) {
		final Integer currPrefNumber = entry.getValue().getPreferenceNumber();
		preferenceNumbers.add(currPrefNumber);
		if (!sourcesPreferences.containsKey(currPrefNumber)) {
		    sourcesPreferences.put(currPrefNumber, new ArrayList<ISource>());
		}
		sourcesPreferences.get(currPrefNumber).add(entry.getKey());
	    }

	    final Integer preferenceResult = preferenceNumbers.first();

	    final List<ISource> preferedSourceList = sourcesPreferences.get(preferenceResult);
	    if (preferedSourceList.size() == 1) {
		return new Pair<PropResolutionInfo, ISource>(candidates.get(sourcesPreferences.get(preferenceResult).get(0)), sourcesPreferences.get(preferenceResult).get(0));
	    } else {
		int notAliasedSourcesCount = 0;
		Pair<PropResolutionInfo, ISource> resultPair = null;
		for (final ISource qrySource : preferedSourceList) {
		    if (qrySource.getAlias() == null) {
			resultPair = new Pair<PropResolutionInfo, ISource>(candidates.get(qrySource), qrySource);
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
	result.addAll(orderings.getLocalProps());
	return result;
    }

    public List<EntQuery> getImmediateSubqueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	result.addAll(yields.getLocalSubQueries());
	result.addAll(groups.getLocalSubQueries());
	result.addAll(orderings.getLocalSubQueries());
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
	result.addAll(orderings.getAllValues());
	result.addAll(yields.getAllValues());
	return result;
    }

    public Sources getSources() {
	return sources;
    }

    public Conditions getConditions() {
	return conditions;
    }

    public Yields getYields() {
	return yields;
    }

    public GroupBys getGroups() {
	return groups;
    }

    public OrderBys getOrderings() {
        return orderings;
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
    public boolean isNullable() {
	if (yields.getYields().size() == 1) {
		  return yields.getYields().values().iterator().next().getInfo().isNullable();
		}
	return true;
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