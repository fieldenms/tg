package ua.com.fielden.platform.entity.query.generation.elements;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.AND;
import static ua.com.fielden.platform.entity.query.generation.elements.EntPropStage.EXTERNAL;
import static ua.com.fielden.platform.entity.query.generation.elements.EntPropStage.PRELIMINARY_RESOLVED;
import static ua.com.fielden.platform.entity.query.generation.elements.EntPropStage.UNPROCESSED;
import static ua.com.fielden.platform.entity.query.generation.elements.QueryCategory.RESULT_QUERY;
import static ua.com.fielden.platform.entity.query.generation.elements.QueryCategory.SUB_QUERY;
import static ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails.YieldDetailsType.AGGREGATED_EXPRESSION;
import static ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails.YieldDetailsType.USUAL_PROP;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;
import static ua.com.fielden.platform.utils.EntityUtils.isCompositeEntity;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isSyntheticBasedOnPersistentEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.keyPaths;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.IRetrievalModel;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.generation.EntQueryBlocks;
import ua.com.fielden.platform.entity.query.generation.EntQueryGenerator;
import ua.com.fielden.platform.entity.query.generation.StandAloneConditionBuilder;
import ua.com.fielden.platform.entity.query.generation.StandAloneExpressionBuilder;
import ua.com.fielden.platform.entity.query.generation.elements.AbstractSource.PropResolutionInfo;
import ua.com.fielden.platform.entity.query.generation.elements.ResultQueryYieldDetails.YieldDetailsType;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.metadata.PersistedEntityMetadata;
import ua.com.fielden.platform.entity.query.metadata.PropertyMetadata;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.Pair;

public class EntQuery implements ISingleOperand {

    private static final Logger LOGGER = Logger.getLogger(EntQuery.class);
    private static final String DOT = ".";

    private final boolean resultTypeIsPersistedType;
    private final Sources sources;
    private final Conditions conditions;
    private final Yields yields;
    private final GroupBys groups;
    private final OrderBys orderings;
    private final Class<? extends AbstractEntity<?>> resultType;
    private final QueryCategory category;
    private final DomainMetadataAnalyser domainMetadataAnalyser;
    private final Map<String, Object> paramValues;
    private final boolean yieldAll;

    private EntQuery master;

    private boolean isSubQuery() {
        return SUB_QUERY.equals(category);
    }

    private boolean isResultQuery() {
        return RESULT_QUERY.equals(category);
    }

    /**
     * modifiable set of unresolved props (introduced for performance reason - in order to avoid multiple execution of the same search against all query props while searching for
     * unresolved only; if at some master the property of this subquery is resolved - it should be removed from here
     */
    private final List<EntProp> unresolvedProps = new ArrayList<>();

    @Override
    public String toString() {
        return sql();
    }

    @Override
    public String sql() {
        if (isResultQuery()) {
            assignSqlParamNames();
        }
        sources.assignSqlAliases(getMasterIndex());

        final StringBuilder sb = new StringBuilder()
                                 .append(isSubQuery() ? "(" : "")
                                 .append("SELECT ")
                                 .append(yields.sql());

        final String sourcesSql = sources.sql().trim();
        if (isNotEmpty(sourcesSql)) {
            sb.append("\nFROM ");
            sb.append(sourcesSql);
        }

        if (!conditions.ignore()) {
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
        return yields.size() == 1 && !yieldAll && yields.getFirstYield().getAlias().equals("");
    }

    private boolean idAliasEnhancementRequired() {
        return onlyOneYieldAndWithoutAlias() && resultTypeIsPersistedType;
    }

    private boolean allPropsYieldEnhancementRequired() {
        return !isSubQuery() && (yieldAll || yields.isEmpty());
    }

    private boolean idPropYieldEnhancementRequired() {
        return yields.size() == 0 && !yieldAll && isResulTypePersisted()/*resultTypeIsPersistedType*/ && isSubQuery();
    }

    private boolean constYieldEnhancementRequired() {
        return yields.size() == 0 && !yieldAll && isSubQuery();
    }

    private boolean mainSourceIsTypeBased() {
        return getSources().getMain() instanceof TypeBasedSource;
    }

    private boolean mainSourceIsQueryBased() {
        return getSources().getMain() instanceof QueryBasedSource;
    }

    private void enhanceYieldsModel() {
        // enhancing short-cuts in yield section (e.g. the following: assign missing "id" alias in case yield().prop("someEntProp").modelAsEntity(entProp.class) is used
        if (idAliasEnhancementRequired()) {
            final Yield idModel = new Yield(yields.getFirstYield().getOperand(), ID);
            yields.clear();
            yields.addYield(idModel);
        } else if (idPropYieldEnhancementRequired()) {
            final String yieldPropAliasPrefix = getSources().getMain().getAlias() == null ? "" : getSources().getMain().getAlias() + DOT;
            yields.addYield(new Yield(new EntProp(yieldPropAliasPrefix + ID), ID));
        } else if (constYieldEnhancementRequired()) {
            yields.addYield(new Yield(new EntValue(0), ""));
        } else if (allPropsYieldEnhancementRequired()) {
            final String yieldPropAliasPrefix = getSources().getMain().getAlias() == null ? "" : getSources().getMain().getAlias() + DOT;
            // LOGGER.debug("enhanceYieldsModel.allPropsYieldEnhancementRequired");
            if (mainSourceIsTypeBased()) {
                final Class<? extends AbstractEntity<?>> mainSourceType = getSources().getMain().sourceType();
                for (final PropertyMetadata ppi : domainMetadataAnalyser.getPropertyMetadatasForEntity(mainSourceType)) {
                    final boolean skipProperty = ppi.isSynthetic() ||
                            ppi.isCompositeKeyExpression() ||
                            ppi.isCollection() ||
                            (ppi.isAggregatedExpression() && !isResultQuery());
                    if (!skipProperty) {
                        // LOGGER.debug(" add yield: " + ppi);
                        yields.addYield(new Yield(new EntProp(yieldPropAliasPrefix + ppi.getName()), ppi.getName()));
                    }
                }
            } else {
                final QueryBasedSource sourceModel = (QueryBasedSource) getSources().getMain();
                for (final ResultQueryYieldDetails ppi : sourceModel.sourceItems.values()) {
                    final boolean skipProperty = (ppi.getYieldDetailsType() == AGGREGATED_EXPRESSION && !isResultQuery());
                    if (!skipProperty) {
                        yields.addYield(new Yield(new EntProp(yieldPropAliasPrefix + ppi.getName()), ppi.getName()));
                    }
                }

                final Class<? extends AbstractEntity<?>> mainSourceType = getSources().getMain().sourceType();

                if (mainSourceType != EntityAggregates.class) {
                    for (final PropertyMetadata ppi : domainMetadataAnalyser.getPropertyMetadatasForEntity(mainSourceType)) {
                        final boolean skipProperty = ppi.isSynthetic() || ppi.isCompositeKeyExpression() || ppi.isCollection() || (ppi.isAggregatedExpression() && !isResultQuery());
                        if ((ppi.isCalculated()) && yields.getYieldByAlias(ppi.getName()) == null && !skipProperty) {
                            yields.addYield(new Yield(new EntProp(yieldPropAliasPrefix + ppi.getName()), ppi.getName()));
                        }
                    }
                }
            }
        }
    }

    private void enhanceGroupBysModelFromYields() {
        final Set<EntProp> toBeAdded = new HashSet<>();
        for (final Yield yield : yields.getYields()) {
            if (yield.getOperand() instanceof EntProp) {
                final String propName = ((EntProp) yield.getOperand()).getName();
                for (final GroupBy groupBy : groups.getGroups()) {
                    if (groupBy.getOperand() instanceof EntProp) {
                        final String groupPropName = ((EntProp) groupBy.getOperand()).getName();
                        if (propName.startsWith(groupPropName + DOT)) {
                            toBeAdded.add((EntProp) yield.getOperand());
                        }
                    }
                }
            }
        }

        for (final EntProp entProp : toBeAdded) {
            groups.getGroups().add(new GroupBy(entProp));
        }
    }

    private void enhanceGroupBysModelFromOrderBys() {
        final Set<EntProp> toBeAdded = new HashSet<>();
        for (final OrderBy orderBy : orderings.getModels()) {
            if (orderBy.getOperand() instanceof EntProp) {
                final String propName = ((EntProp) orderBy.getOperand()).getName();
                for (final GroupBy groupBy : groups.getGroups()) {
                    if (groupBy.getOperand() instanceof EntProp) {
                        final String groupPropName = ((EntProp) groupBy.getOperand()).getName();
                        if (propName.startsWith(groupPropName + DOT)) {
                            toBeAdded.add((EntProp) orderBy.getOperand());
                        }
                    }
                }
            }
        }

        for (final EntProp entProp : toBeAdded) {
            groups.getGroups().add(new GroupBy(entProp));
        }
    }

    private boolean areAllFetchedPropsAggregatedExpressions(final IRetrievalModel fetchModel) {
        boolean result = true;
        for (final Yield yield : yields.getYields()) {
            if (fetchModel.containsProp(yield.getAlias())) {
                result = result && determineYieldDetailsType(yield).equals(ResultQueryYieldDetails.YieldDetailsType.AGGREGATED_EXPRESSION);
            }
        }
        return result;
    }

    private boolean yieldIsOfEntityType(final Yield yield) {
        final Class<?> yieldType = determineYieldJavaType(yield);
        return yieldType != null && AbstractEntity.class.isAssignableFrom(yieldType);
    }

    private void adjustYieldsModelAccordingToFetchModel(final IRetrievalModel fetchModel) {
        if (fetchModel == null) {
            // LOGGER.debug("adjustYieldsModelAccordingToFetchModel: no fetch model was provided -- nothing was removed");
        } else {
            // LOGGER.debug("adjustYieldsModelAccordingToFetchModel: fetchModel\n" + fetchModel);
            final Set<Yield> toBeRemoved = new HashSet<>();

            for (final Yield yield : yields.getYields()) {
                if (shouldYieldBeRemoved(fetchModel, yield)) {
                    toBeRemoved.add(yield);
                    // LOGGER.debug("adjustYieldsModelAccordingToFetchModel: removing property [" + yield.getAlias() + "]");
                } else {
                    // LOGGER.debug("adjustYieldsModelAccordingToFetchModel: retaining property [" + yield.getAlias() + "]");
                }
            }
            yields.removeYields(toBeRemoved);
        }
    }

    private boolean shouldYieldBeRemoved(final IRetrievalModel<?> fetchModel, final Yield yield) {
        final boolean presentInFetchModel = fetchModel.containsProp(yield.getAlias());
        final boolean allFetchedPropsAreAggregatedExpressions = areAllFetchedPropsAggregatedExpressions(fetchModel);
        // this means that all not fetched props should be 100% removed -- in order to get valid sql stmt for entity centre totals query
        final boolean isHeaderOfMoneyType = yields.isHeaderOfSimpleMoneyTypeProperty(yield.getAlias());
        return allFetchedPropsAreAggregatedExpressions ? (!presentInFetchModel || isHeaderOfMoneyType) : !presentInFetchModel;
    }

    private void enhanceOrderBys() {
        final List<OrderBy> toBeAdded = new ArrayList<>();
        for (final OrderBy orderBy : orderings.getModels()) {
            toBeAdded.addAll(orderBy.getYieldName() != null ? enhanceOrderByYield(orderBy) : enhanceOrderByOther(orderBy));
        }
        orderings.getModels().clear();
        orderings.getModels().addAll(toBeAdded);
    }

    private List<OrderBy> enhanceOrderByYield(final OrderBy orderBy) {
        final List<OrderBy> toBeAdded = new ArrayList<>();
        
        if (orderBy.getYieldName().equals(KEY) && isCompositeEntity(resultType)) {
            final List<String> keyOrderProps = keyPaths(resultType);
            for (final String keyMemberProp : keyOrderProps) {
                toBeAdded.add(new OrderBy(new EntProp(keyMemberProp), orderBy.isDesc()));
            }
        } else {
            final Yield correspondingYield = yields.getYieldByAlias(orderBy.getYieldName());
            final Yield correspondingYieldWithAmount = yields.getYieldByAlias(orderBy.getYieldName() + ".amount");
            if (correspondingYieldWithAmount != null) {
                orderBy.setYield(correspondingYieldWithAmount);
                toBeAdded.add(orderBy);
            } else if (correspondingYield != null) {
                orderBy.setYield(correspondingYield);
                toBeAdded.add(orderBy);
            } else {
                toBeAdded.addAll(transformOrderByFromYieldIntoOrderByFromProp(yields.findMostMatchingYield(orderBy.getYieldName()), orderBy));
            }
        }
        return toBeAdded;
    }
    
    private List<OrderBy> enhanceOrderByOther(final OrderBy orderBy) {
        final List<OrderBy> toBeAdded = new ArrayList<>();
        
        if (orderBy.getOperand() instanceof EntProp && ((EntProp) orderBy.getOperand()).getName().equals(KEY) && isCompositeEntity(resultType)) {
            final List<String> keyOrderProps = keyPaths(resultType);
            for (final String keyMemberProp : keyOrderProps) {
                toBeAdded.add(new OrderBy(new EntProp(keyMemberProp), orderBy.isDesc()));
            }
        } else {
            toBeAdded.add(orderBy);
        }

        return toBeAdded;
    }

    
    private List<OrderBy> transformOrderByFromYieldIntoOrderByFromProp(final Yield bestYield, final OrderBy original) {
        if (bestYield == null) {
            throw new IllegalStateException("Could not find best yield match for order by yield [" + original.getYieldName() + "]");
        }

        final List<OrderBy> result = new ArrayList<>();
        final String propName = ((EntProp) bestYield.getOperand()).getName() + original.getYieldName().substring(bestYield.getAlias().length());

        if (original.getYieldName().endsWith(DOT + KEY)) {
            final String prop = original.getYieldName().substring(0, original.getYieldName().length() - 4);
            final PropertyMetadata info = domainMetadataAnalyser.getInfoForDotNotatedProp(resultType, prop);
            if (isCompositeEntity(info.getJavaType())) {
                final List<String> keyOrderProps = keyPaths(info.getJavaType(), propName.substring(0, propName.length() - 4));
                for (final String keyMemberProp : keyOrderProps) {
                    result.add(new OrderBy(new EntProp(keyMemberProp), original.isDesc()));
                }
                return result;
            }
        }

        result.add(new OrderBy(new EntProp(propName), original.isDesc()));

        return result;
    }

    private void assignPropertyPersistenceInfoToYields() {
        int yieldIndex = 0;
        for (final Yield yield : yields.getYields()) {
            yieldIndex = yieldIndex + 1;
            yield.setInfo(new ResultQueryYieldDetails(
                    yield.getAlias(),
                    determineYieldJavaType(yield),
                    determineYieldHibType(yield),
                    "C" + yieldIndex,
                    determineYieldNullability(yield),
                    determineYieldDetailsType(yield)));
        }
    }

    private Object determineYieldHibType(final Yield yield) {
        final PropertyMetadata finalPropInfo = domainMetadataAnalyser.getInfoForDotNotatedProp(resultType, yield.getAlias());
        if (finalPropInfo != null) {
            return finalPropInfo.getHibType();
        } else {
            return yield.getOperand().hibType();
        }
    }

    private YieldDetailsType determineYieldDetailsType(final Yield yield) {
        final PropertyMetadata finalPropInfo = domainMetadataAnalyser.getInfoForDotNotatedProp(resultType, yield.getAlias());
        if (finalPropInfo != null) {
            return finalPropInfo.getYieldDetailType();
        } else {
            return yield.getInfo() != null ? yield.getInfo().getYieldDetailsType() : USUAL_PROP;
        }
    }

    private boolean determineYieldNullability(final Yield yield) {
        if (yield.isRequiredHint()) {
            return false;
        }

        return yield.getOperand().isNullable();
    }

    private Class determineYieldJavaType(final Yield yield) {

        if (!resultTypeIsPersistedType && !isUnionEntityType(resultType)) {
            return yield.getOperand().type();
        }

        final Class qsYt = yield.getOperand().type();
        final PropertyMetadata finalPropInfo = domainMetadataAnalyser.getInfoForDotNotatedProp(resultType, yield.getAlias());
        final Class rtYt = finalPropInfo.getJavaType();

        if (resultTypeIsPersistedType) {
            if (qsYt != null && !qsYt.equals(rtYt)) {
                if (!(isPersistedEntityType(qsYt) && Long.class.equals(rtYt)) &&
                        !(isPersistedEntityType(rtYt) && Long.class.equals(qsYt)) &&
                        !(rtYt.equals(DynamicEntityKey.class) && qsYt.equals(String.class)) && !rtYt.equals(Money.class)) {
                    throw new IllegalStateException("Different types: from source = " + qsYt.getSimpleName() + " from result type = " + rtYt.getSimpleName());
                }
                return rtYt;
            } else {
                return rtYt;
            }
        }

        if (isUnionEntityType(resultType)) {
            return rtYt;
        }

        throw new IllegalStateException("This is impossible situation");
    }

    private void assignSqlParamNames() {
        int paramCount = 0;
        for (final EntValue value : getAllValues()) {
            paramCount = paramCount + 1;
            value.setSqlParamName("P" + paramCount);
        }
    }

    public Map<String, Object> getValuesForSqlParams() {
        final Map<String, Object> result = new HashMap<>();
        for (final EntValue value : getAllValues()) {
            result.put(value.getSqlParamName(), value.getValue());
        }
        return result;
    }

    private Conditions enhanceConditions(final Conditions originalConditions, final boolean filterable, final IFilter filter, final String username, final ISource mainSource, final EntQueryGenerator generator, final Map<String, Object> paramValues) {
        if (filterable && filter != null) {
            final ConditionModel filteringCondition = filter.enhance(mainSource.sourceType(), mainSource.getAlias(), username);
            if (filteringCondition != null) {
                // LOGGER.debug("\nApplied user-driven-filter to query main source type [" + mainSource.sourceType().getSimpleName() + "]");
                final GroupedConditions userDateFilteringCondition = new StandAloneConditionBuilder(generator, paramValues, filteringCondition, false).getModel();
                return originalConditions.ignore()
                       ? new Conditions(userDateFilteringCondition)
                       : new Conditions(userDateFilteringCondition, listOf(new CompoundCondition(AND, new GroupedConditions(false, originalConditions.getFirstCondition(), originalConditions.getOtherConditions()))));
            }
        }

        return originalConditions;
    }

    public EntQuery(final boolean filterable, final EntQueryBlocks queryBlocks, final Class resultType, final QueryCategory category,
            final DomainMetadataAnalyser domainMetadataAnalyser, final IFilter filter, final String username,
            final EntQueryGenerator generator, final IRetrievalModel fetchModel, final Map<String, Object> paramValues) {
        this.category = category;
        this.domainMetadataAnalyser = domainMetadataAnalyser;
        this.sources = queryBlocks.getSources();
        this.conditions = enhanceConditions(queryBlocks.getConditions(), filterable, filter, username, sources.getMain(), generator, paramValues);
        this.yields = queryBlocks.getYields();
        this.yieldAll = queryBlocks.isYieldAll();
        this.groups = queryBlocks.getGroups();
        this.orderings = queryBlocks.getOrderings();
        this.resultType = resultType;// != null ? resultType : (yields.size() == 0 ? this.sources.getMain().sourceType() : null);
        if (this.resultType == null && category != SUB_QUERY) { // only primitive result queries have result type not assigned
            throw new IllegalStateException("This query is not subquery, thus its result type shouldn't be null!\n Query: " + queryBlocks);
        }

        resultTypeIsPersistedType = //isResulTypePersisted();
        (resultType == null || resultType == EntityAggregates.class) ? false
                : (domainMetadataAnalyser.getEntityMetadata(this.resultType) instanceof PersistedEntityMetadata);

        this.paramValues = paramValues;

        enhanceToFinalState(generator, fetchModel);

        assignPropertyPersistenceInfoToYields();
    }

    private boolean isResulTypePersisted() {
        return (resultType == null || resultType == EntityAggregates.class) ? false : (isPersistedEntityType(this.resultType) || isSyntheticBasedOnPersistentEntityType(this.resultType));
    }

    private Map<EntPropStage, List<EntProp>> groupPropsByStage(final List<EntProp> props) {
        final Map<EntPropStage, List<EntProp>> result = new EnumMap<>(EntPropStage.class);
        for (final EntProp entProp : props) {
            final EntPropStage propStage = entProp.getStage();
            final List<EntProp> stageProps = result.get(propStage);
            if (stageProps != null) {
                stageProps.add(entProp);
            } else {
                final List<EntProp> newStageProps = new ArrayList<>();
                newStageProps.add(entProp);
                result.put(propStage, newStageProps);
            }
        }
        return result;
    }

    private List<EntProp> getPropsByStage(final List<EntProp> props, final EntPropStage stage) {
        final Map<EntPropStage, List<EntProp>> propsGroupedByStage = groupPropsByStage(props);
        final List<EntProp> foundProps = propsGroupedByStage.get(stage);
        return foundProps != null ? foundProps : Collections.<EntProp> emptyList();
    }

    private void enhanceToFinalState(final EntQueryGenerator generator, final IRetrievalModel<?> fetchModel) {
        for (final Pair<ISource, Boolean> sourceAndItsJoinType : getSources().getAllSourcesAndTheirJoinType()) {
            final ISource source = sourceAndItsJoinType.getKey();
            source.assignNullability(sourceAndItsJoinType.getValue());
            source.populateSourceItems(sourceAndItsJoinType.getValue());
        }

        enhanceYieldsModel();
        adjustYieldsModelAccordingToFetchModel(fetchModel);
        enhanceOrderBys();
        enhanceGroupBysModelFromYields();
        enhanceGroupBysModelFromOrderBys();

        int countOfUnprocessed = 1;
        while (countOfUnprocessed > 0) {
            for (final Pair<ISource, Boolean> sourceAndItsJoinType : getSources().getAllSourcesAndTheirJoinType()) {
                final ISource source = sourceAndItsJoinType.getKey();
                getSources().getCompounds().addAll(source.generateMissingSources()); //source.getReferencingProps()
            }

            final List<EntQuery> immediateSubqueries = getImmediateSubqueries();
            associateSubqueriesWithMasterQuery(immediateSubqueries);

            final List<EntProp> propsToBeResolved = new ArrayList<>();
            propsToBeResolved.addAll(getPropsByStage(getImmediateProps(), UNPROCESSED));
            propsToBeResolved.addAll(collectUnresolvedPropsFromSubqueries(immediateSubqueries, UNPROCESSED));

            countOfUnprocessed = propsToBeResolved.size();

            unresolvedProps.addAll(resolveProps(propsToBeResolved, generator));

            if (!isSubQuery() && !unresolvedProps.isEmpty()) {
                throw new EqlException("Couldn't resolve the following props: " + unresolvedProps);
            }

            final List<EntProp> immediatePropertiesFinally = getPropsByStage(getImmediateProps(), PRELIMINARY_RESOLVED);

            final List<EntProp> propsToBeResolvedFinally = new ArrayList<>();
            propsToBeResolvedFinally.addAll(immediatePropertiesFinally);
            propsToBeResolvedFinally.addAll(collectUnresolvedPropsFromSubqueries(getImmediateSubqueries(), PRELIMINARY_RESOLVED));
            propsToBeResolvedFinally.removeAll(unresolvedProps);

            resolveProps(propsToBeResolvedFinally, generator);
        }

        for (final EntProp entProp : getPropsByStage(getImmediateProps(), EXTERNAL)) {
            entProp.setExternal(false);
            unresolvedProps.add(entProp);
            if (!entProp.getStage().equals(UNPROCESSED)) {
                throw new EqlException("IS NOT UNPROCESSED!");
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
        final List<EntProp> unresolvedPropsFromSubqueries = new ArrayList<>();
        for (final EntQuery entQuery : subqueries) {
            for (final EntProp entProp : entQuery.unresolvedProps) {
                if (propStage.equals(entProp.getStage())) {
                    unresolvedPropsFromSubqueries.add(entProp);
                }
            }
        }
        return unresolvedPropsFromSubqueries;
    }

    private List<EntProp> resolveProps(final List<EntProp> propsToBeResolved, final EntQueryGenerator generator) {
        final List<EntProp> unresolved = new ArrayList<>();

        for (final EntProp propToBeResolvedPair : propsToBeResolved) {
            if (!propToBeResolvedPair.isFinallyResolved()) {
                final Map<ISource, PropResolutionInfo> sourceCandidates = findSourceMatchCandidates(propToBeResolvedPair);
                if (sourceCandidates.isEmpty()) {
                    propToBeResolvedPair.setUnresolved(true);
                    unresolved.add(propToBeResolvedPair);
                } else {
                    final Pair<PropResolutionInfo, ISource> propResolutionResult = performPropResolveAction(sourceCandidates);
                    final PropResolutionInfo pri = propResolutionResult.getKey();
                    propResolutionResult.getValue().addReferencingProp(pri);
                    if (pri.getProp().getExpressionModel() != null && !pri.getEntProp().isExpression()) {
                        pri.getEntProp().setExpression((Expression) new StandAloneExpressionBuilder(generator, paramValues, pri.getProp().getExpressionModel()).getResult().getValue());
                    }
                }

            }
        }

        return unresolved;
    }

    private Map<ISource, PropResolutionInfo> findSourceMatchCandidates(final EntProp prop) {
        final Map<ISource, PropResolutionInfo> result = new HashMap<>();

        for (final ISource source : sources.getAllSources()) {
            if ((prop.getStage().equals(PRELIMINARY_RESOLVED) || (prop.getStage().equals(UNPROCESSED) && !source.generated())) || (prop.isGenerated())) {
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
            return pair(candidates.values().iterator().next(), candidates.keySet().iterator().next());
        } else {
            final SortedSet<Integer> preferenceNumbers = new TreeSet<>();
            final Map<Integer, List<ISource>> sourcesPreferences = new HashMap<>();
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
                return pair(candidates.get(sourcesPreferences.get(preferenceResult).get(0)), sourcesPreferences.get(preferenceResult).get(0));
            } else {
                int notAliasedSourcesCount = 0;
                Pair<PropResolutionInfo, ISource> resultPair = null;
                for (final ISource qrySource : preferedSourceList) {
                    if (qrySource.getAlias() == null) {
                        resultPair = pair(candidates.get(qrySource), qrySource);
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
        final List<EntProp> result = new ArrayList<>();
        result.addAll(sources.getLocalProps());
        result.addAll(conditions.getLocalProps());
        result.addAll(groups.getLocalProps());
        result.addAll(yields.getLocalProps());
        result.addAll(orderings.getLocalProps());
        return result;
    }

    public List<EntQuery> getImmediateSubqueries() {
        final List<EntQuery> result = new ArrayList<>();
        result.addAll(yields.getLocalSubQueries());
        result.addAll(groups.getLocalSubQueries());
        result.addAll(orderings.getLocalSubQueries());
        result.addAll(conditions.getLocalSubQueries());
        result.addAll(sources.getLocalSubQueries());
        return result;
    }

    @Override
    public List<EntProp> getLocalProps() {
        return emptyList();
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
        return listOf(this);
    }

    @Override
    public List<EntValue> getAllValues() {
        final List<EntValue> result = new ArrayList<>();
        result.addAll(sources.getAllValues());
        result.addAll(conditions.getAllValues());
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

    public List<EntProp> getUnresolvedProps() {
        return Collections.unmodifiableList(unresolvedProps);
    }

    @Override
    public boolean ignore() {
        return false;
    }

    @Override
    public Class<?> type() {
        return resultType;
    }

    @Override
    public Object hibType() {
        if (yields.size() == 1) {
            return yields.getFirstYield().getInfo().getHibType();
        }
        return null;
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((conditions == null) ? 0 : conditions.hashCode());
        result = prime * result + ((groups == null) ? 0 : groups.hashCode());
        result = prime * result + ((orderings == null) ? 0 : orderings.hashCode());
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
        if (!(obj instanceof EntQuery)) {
            return false;
        }

        final EntQuery that = (EntQuery) obj;
        return Objects.equals(conditions, that.conditions)
            && Objects.equals(groups, that.groups)
            && Objects.equals(orderings, that.orderings)
            && Objects.equals(category, that.category)
            && Objects.equals(resultType, that.resultType)
            && Objects.equals(sources, that.sources)
            && Objects.equals(yields, that.yields);
     }
}