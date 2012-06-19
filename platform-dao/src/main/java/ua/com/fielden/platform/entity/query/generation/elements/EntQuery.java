package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.FetchModel;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.generation.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.generation.StandAloneExpressionBuilder;
import ua.com.fielden.platform.entity.query.generation.elements.AbstractSource.PropResolutionInfo;
import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails.YieldDetailsType;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EntQuery implements ISingleOperand {

    private final Sources sources;
    private final Conditions conditions;
    private final Yields yields;
    private final GroupBys groups;
    private final OrderBys orderings;
    private final Class resultType;
    private final QueryCategory category;
    private final DomainMetadataAnalyser domainMetadataAnalyser;

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
    private List<EntProp> unresolvedProps = new ArrayList<EntProp>();

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
            final Yield idModel = new Yield(yields.getYields().values().iterator().next().getOperand(), AbstractEntity.ID);
            yields.getYields().clear();
            yields.getYields().put(idModel.getAlias(), idModel);
        } else if (allPropsYieldEnhancementRequired()) {
            final String yieldPropAliasPrefix = getSources().getMain().getAlias() == null ? "" : getSources().getMain().getAlias() + ".";
            if (getSources().getMain() instanceof TypeBasedSource) {
                for (final PropertyMetadata ppi : domainMetadataAnalyser.getPropertyMetadatasForEntity(type())) {
            	//ppi.isUnionEntity() || ppi.isUnionEntityDetails() || ppi.isUnionEntity() ||
            	final boolean skipProperty =  ppi.isSynthetic() || ppi.isVirtual() || ppi.isCollection() || (ppi.isAggregatedExpression() && !isResultQuery());
          		if (!skipProperty) {
                        final ResultQueryYieldDetails rqyd = new ResultQueryYieldDetails(ppi.getName(), ppi.getJavaType(), ppi.getHibType(), (ppi.getColumn() != null ? ppi.getColumn().getName() : null), ppi.getYieldDetailType());
                        yields.getYields().put(rqyd.getName(), new Yield(new EntProp(yieldPropAliasPrefix + rqyd.getName()), rqyd.getName(), rqyd));
                    }
                }
            } else {
        	final QueryBasedSource sourceModel = (QueryBasedSource) getSources().getMain();
                for (final ResultQueryYieldDetails ppi : sourceModel.sourceItems.values()) {
            	//ppi.isUnionEntity() || ppi.isUnionEntityDetails() || ppi.isUnionEntity() ||
                    final ResultQueryYieldDetails rqyd = new ResultQueryYieldDetails(ppi.getName(), ppi.getJavaType(), ppi.getHibType(), (ppi.getColumn() != null ? ppi.getColumn() : null), ppi.getYieldDetailsType());
                    yields.getYields().put(rqyd.getName(), new Yield(new EntProp(yieldPropAliasPrefix + rqyd.getName()), rqyd.getName(), rqyd));
                }

            }
        } else if (idPropYieldEnhancementRequired()) {
            final String yieldPropAliasPrefix = getSources().getMain().getAlias() == null ? "" : getSources().getMain().getAlias() + ".";
            yields.getYields().put(AbstractEntity.ID, new Yield(new EntProp(yieldPropAliasPrefix + AbstractEntity.ID), AbstractEntity.ID));
        }
    }

    private void adjustYieldsModelAccordingToFetchModel(final FetchModel fetchModel) {
	if (fetchModel != null) {
	    for (final Iterator<Entry<String, Yield>> iterator = yields.getYields().entrySet().iterator(); iterator.hasNext();) {
		final Entry<String, Yield> yieldEntry = iterator.next();
		if (!fetchModel.containsProp(yieldEntry.getKey())) {
		    iterator.remove();
		}
	    }
	}
    }

    private void assignPropertyPersistenceInfoToYields() {
        //System.out.println(category);
	int yieldIndex = 0;
        for (final Yield yield : yields.getYields().values()) {
            yieldIndex = yieldIndex + 1;
            final ResultQueryYieldDetails ppi = new ResultQueryYieldDetails(yield.getAlias(), determineYieldJavaType(yield), determineYieldHibType(yield), "C" + yieldIndex, determineYieldNullability(yield), determineYieldDetailsType(yield));
            //System.out.println("------------- setting info to yield " + yield.getAlias() + " !");
            yield.setInfo(ppi);
        }
    }

    private Object determineYieldHibType(final Yield yield) {
        final PropertyMetadata finalPropInfo = domainMetadataAnalyser.getInfoForDotNotatedProp(type(), yield.getAlias());
        if (finalPropInfo != null) {
            return finalPropInfo.getHibType();
        } else {
            return yield.getOperand().hibType();
        }
    }

    private YieldDetailsType determineYieldDetailsType(final Yield yield) {
        final PropertyMetadata finalPropInfo = domainMetadataAnalyser.getInfoForDotNotatedProp(type(), yield.getAlias());
        if (finalPropInfo != null) {
            return finalPropInfo.getYieldDetailType();
        } else {
            return YieldDetailsType.USUAL_PROP /*TEMP*/;
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
            new QueryBasedSource(sources.getMain().getAlias(), domainMetadataAnalyser, generator.generateEntQueryAsSourceQuery(filter.enhance(sources.getMain().sourceType(), username))) : null;

        return newMain != null ? new Sources(newMain, sources.getCompounds()) : sources;
    }

    public EntQuery(final Sources sources, final Conditions conditions, final Yields yields, final GroupBys groups, final OrderBys orderings, //
            final Class resultType, final QueryCategory category, final DomainMetadataAnalyser domainMetadataAnalyser, //
            final IFilter filter, final String username, final EntQueryGenerator generator, final FetchModel fetchModel) {
        super();
        this.category = category;
        this.domainMetadataAnalyser = domainMetadataAnalyser;
        this.sources = enhanceSourcesWithUserDataFiltering(filter, username, sources, generator);
        this.conditions = conditions;
        this.yields = yields;
        this.groups = groups;
        this.orderings = orderings;
        this.resultType = resultType != null ? resultType : (yields.getYields().size() == 0 ? this.sources.getMain().sourceType() : null);

        enhanceToFinalState(generator, fetchModel);

        assignPropertyPersistenceInfoToYields();

        if (isResultQuery()) {
            assignSqlParamNames();
        }
    }

    private Map<EntPropStage, List<EntProp>> groupPropsByStage(final List<EntProp> props) {
	final Map<EntPropStage, List<EntProp>> result = new HashMap<EntPropStage, List<EntProp>>();
	for (final EntProp entProp : props) {
	    final EntPropStage propStage = entProp.getStage();
	    final List<EntProp> stageProps = result.get(propStage);
	    if (stageProps != null) {
		stageProps.add(entProp);
	    } else {
		final List<EntProp> newStageProps = new ArrayList<EntProp>();
		newStageProps.add(entProp);
		result.put(propStage, newStageProps);
	    }
	}

//	for (final Entry<EntPropStage, List<EntProp>> entProp : result.entrySet()) {
//	    System.out.println("           " + entProp.getKey());
//	    for (final EntProp prop : entProp.getValue()) {
//		System.out.println("                          " + prop);
//	    }
//
//	}

	return result;
    }

    private List<EntProp> getPropsByStage(final List<EntProp> props, final EntPropStage stage) {
	final Map<EntPropStage, List<EntProp>> propsGroupedByStage = groupPropsByStage(props);
	final List<EntProp> foundProps = propsGroupedByStage.get(stage);
	return foundProps != null ? foundProps : Collections.<EntProp> emptyList();
    }

    private void enhanceToFinalState(final EntQueryGenerator generator, final FetchModel fetchModel) {
	for (final Pair<ISource, Boolean> sourceAndItsJoinType : getSources().getAllSourcesAndTheirJoinType()) {
	    final ISource source = sourceAndItsJoinType.getKey();
	    source.assignNullability(sourceAndItsJoinType.getValue());
	    source.populateSourceItems(sourceAndItsJoinType.getValue());
	}

	enhanceYieldsModel(); //!! adds new properties in yield section
	adjustYieldsModelAccordingToFetchModel(fetchModel);

	int countOfUnprocessed = 1;

	while (countOfUnprocessed > 0) {
	    //System.out.println("---------------------------generateMissingSources for getSources().count = " + getSources().getAllSourcesAndTheirJoinType().size());
	    for (final Pair<ISource, Boolean> sourceAndItsJoinType : getSources().getAllSourcesAndTheirJoinType()) {
		final ISource source = sourceAndItsJoinType.getKey();
		getSources().getCompounds().addAll(source.generateMissingSources()); //source.getReferencingProps()
	    }

	    //System.out.println("---------------------------countOfUnprocessed = " + countOfUnprocessed);
	    final List<EntQuery> immediateSubqueries = getImmediateSubqueries();
	    associateSubqueriesWithMasterQuery(immediateSubqueries);

	    final List<EntProp> propsToBeResolved = new ArrayList<EntProp>();
	    propsToBeResolved.addAll(getPropsByStage(getImmediateProps(), EntPropStage.UNPROCESSED));
	    propsToBeResolved.addAll(collectUnresolvedPropsFromSubqueries(immediateSubqueries, EntPropStage.UNPROCESSED));

	    countOfUnprocessed = propsToBeResolved.size();
	    //System.out.println("================propsToBeResolved preliminary=========" + propsToBeResolved);

	    unresolvedProps.addAll(resolveProps(propsToBeResolved, generator));

	    if (!isSubQuery() && unresolvedProps.size() > 0) {
		throw new RuntimeException("Couldn't resolve the following props: " + unresolvedProps);
	    }

	    final List<EntProp> immediatePropertiesFinally = getPropsByStage(getImmediateProps(), EntPropStage.PRELIMINARY_RESOLVED);

	    final List<EntProp> propsToBeResolvedFinally = new ArrayList<EntProp>();
	    propsToBeResolvedFinally.addAll(immediatePropertiesFinally);
	    propsToBeResolvedFinally.addAll(collectUnresolvedPropsFromSubqueries(getImmediateSubqueries(), EntPropStage.PRELIMINARY_RESOLVED));
	    propsToBeResolvedFinally.removeAll(unresolvedProps);

	    resolveProps(propsToBeResolvedFinally, generator);
	}

	for (final EntProp entProp : getPropsByStage(getImmediateProps(), EntPropStage.EXTERNAL)) {
	    entProp.setExternal(false);
	    unresolvedProps.add(entProp);
	    if (!entProp.getStage().equals(EntPropStage.UNPROCESSED)) {
		throw new RuntimeException("IS NOT UNPROCESSED!");
	    }
	}

	for (final EntProp entProp : unresolvedProps) {
	    entProp.setUnresolved(false);
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

    private List<EntProp> collectUnresolvedPropsFromSubqueries(final List<EntQuery> subqueries, final EntPropStage propStage) {
        final List<EntProp> unresolvedPropsFromSubqueries = new ArrayList<EntProp>();
        for (final EntQuery entQuery : subqueries) {
            for (final EntProp entProp : entQuery.unresolvedProps) {
		if (propStage.equals(entProp.getStage())) {
		    unresolvedPropsFromSubqueries.add(entProp);
		}
	    }

            //entQuery.unresolvedProps.clear();
        }
        return unresolvedPropsFromSubqueries;
    }

    private List<EntProp> resolveProps(final List<EntProp> propsToBeResolved, final EntQueryGenerator generator) {
        final List<EntProp> unresolvedProps = new ArrayList<EntProp>();

        for (final EntProp propToBeResolvedPair : propsToBeResolved) {
            if (!propToBeResolvedPair.isFinallyResolved()) {
                final Map<ISource, PropResolutionInfo> sourceCandidates = findSourceMatchCandidates(propToBeResolvedPair);
                if (sourceCandidates.size() == 0) {
            	propToBeResolvedPair.setUnresolved(true);
                    unresolvedProps.add(propToBeResolvedPair);
                } else {
                    final Pair<PropResolutionInfo, ISource> propResolutionResult = performPropResolveAction(sourceCandidates);
                    final PropResolutionInfo pri = propResolutionResult.getKey();
                    propResolutionResult.getValue().addReferencingProp(pri);
                    if (pri.getProp().expressionModel != null && !pri.getEntProp().isExpression()) {
                        pri.getEntProp().setExpression((Expression) new StandAloneExpressionBuilder(generator, Collections.<String, Object> emptyMap(), pri.getProp().expressionModel).getResult().getValue(), pri.getProp().expressionModel.isContextPrefixNeeded());
                    }
                }

            }
        }

        return unresolvedProps;
    }

    private Map<ISource, PropResolutionInfo> findSourceMatchCandidates(final EntProp prop) {
	final Map<ISource, PropResolutionInfo> result = new HashMap<ISource, PropResolutionInfo>();

	for (final ISource source : sources.getAllSources()) {
	    if ((prop.getStage().equals(EntPropStage.PRELIMINARY_RESOLVED) || (prop.getStage().equals(EntPropStage.UNPROCESSED) && !source.generated()))
		    || (prop.isGenerated())) {
		final PropResolutionInfo hasProp = source.containsProperty(prop);
		if (hasProp != null) {
		    result.put(source, hasProp);
		}
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