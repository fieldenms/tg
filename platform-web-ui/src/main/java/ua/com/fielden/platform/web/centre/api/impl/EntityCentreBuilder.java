package ua.com.fielden.platform.web.centre.api.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.OrderDirection;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.SummaryPropDef;
import ua.com.fielden.platform.web.centre.api.IEntityCentreBuilder;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritBooleanValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritStringValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritDateValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritOtherValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritDateValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritOtherValueMnemonic;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActions;
import ua.com.fielden.platform.web.layout.FlexLayout;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * A class implementing the Entity Centre DSL contracts.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class EntityCentreBuilder<T extends AbstractEntity<?>> implements IEntityCentreBuilder<T> {

    private Class<T> entityType;

    protected Optional<String> currGroup = Optional.empty();
    protected final List<Pair<EntityActionConfig, Optional<String>>> topLevelActions = new ArrayList<>();

    ////////////////////////////////////////////////
    //////////////// SELECTION CRITERIA ////////////
    ////////////////////////////////////////////////

    protected Optional<String> currSelectionCrit = Optional.empty();
    protected final List<String> selectionCriteria = new ArrayList<>();

    /**
     * Default value assigner for various kind and types of selection criteria.
     */
    protected final Map<String, Class<? extends IValueAssigner<MultiCritStringValueMnemonic, T>>> defaultMultiValueAssignersForEntityAndStringSelectionCriteria = new HashMap<>();
    protected final Map<String, Class<? extends IValueAssigner<MultiCritBooleanValueMnemonic, T>>> defaultMultiValueAssignersForBooleanSelectionCriteria = new HashMap<>();

    protected final Map<String, Class<? extends IValueAssigner<RangeCritDateValueMnemonic, T>>> defaultRangeValueAssignersForDateSelectionCriteria = new HashMap<>();
    protected final Map<String, Class<? extends IValueAssigner<RangeCritOtherValueMnemonic<Integer>, T>>> defaultRangeValueAssignersForIntegerSelectionCriteria = new HashMap<>();
    protected final Map<String, Class<? extends IValueAssigner<RangeCritOtherValueMnemonic<BigDecimal>, T>>> defaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria = new HashMap<>();

    protected final Map<String, Class<? extends IValueAssigner<? extends SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>, T>>> defaultSingleValueAssignersForEntitySelectionCriteria = new HashMap<>();
    protected final Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<String>, T>>> defaultSingleValueAssignersForStringSelectionCriteria = new HashMap<>();
    protected final Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<Boolean>, T>>> defaultSingleValueAssignersForBooleanSelectionCriteria = new HashMap<>();
    protected final Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<Integer>, T>>> defaultSingleValueAssignersForIntegerSelectionCriteria = new HashMap<>();
    protected final Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<BigDecimal>, T>>> defaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria = new HashMap<>();
    protected final Map<String, Class<? extends IValueAssigner<SingleCritDateValueMnemonic, T>>> defaultSingleValueAssignersForDateSelectionCriteria = new HashMap<>();

    /**
     * Default values. At the DSL level default values and assigners are mutually exclusive.
     */
    protected final Map<String, MultiCritStringValueMnemonic> defaultMultiValuesForEntityAndStringSelectionCriteria = new HashMap<>();
    protected final Map<String, MultiCritBooleanValueMnemonic> defaultMultiValuesForBooleanSelectionCriteria = new HashMap<>();

    protected final Map<String, RangeCritDateValueMnemonic> defaultRangeValuesForDateSelectionCriteria = new HashMap<>();
    protected final Map<String, RangeCritOtherValueMnemonic<Integer>> defaultRangeValuesForIntegerSelectionCriteria = new HashMap<>();
    protected final Map<String, RangeCritOtherValueMnemonic<BigDecimal>> defaultRangeValuesForBigDecimalAndMoneySelectionCriteria = new HashMap<>();

    protected final Map<String, SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>> defaultSingleValuesForEntitySelectionCriteria = new HashMap<>();
    protected final Map<String, SingleCritOtherValueMnemonic<String>> defaultSingleValuesForStringSelectionCriteria = new HashMap<>();
    protected final Map<String, SingleCritOtherValueMnemonic<Boolean>> defaultSingleValuesForBooleanSelectionCriteria = new HashMap<>();
    protected final Map<String, SingleCritOtherValueMnemonic<Integer>> defaultSingleValuesForIntegerSelectionCriteria = new HashMap<>();
    protected final Map<String, SingleCritOtherValueMnemonic<BigDecimal>> defaultSingleValuesForBigDecimalAndMoneySelectionCriteria = new HashMap<>();
    protected final Map<String, SingleCritDateValueMnemonic> defaultSingleValuesForDateSelectionCriteria = new HashMap<>();

    protected final Map<String, Pair<Class<? extends IValueMatcherWithCentreContext<? extends AbstractEntity<?>>>, Optional<CentreContextConfig>>> valueMatchersForSelectionCriteria = new HashMap<>();
    protected final Map<String, List<Pair<String, Boolean>>> additionalPropsForAutocompleter = new HashMap<>();

    protected final FlexLayout selectionCriteriaLayout = new FlexLayout();
    protected final FlexLayout resultsetCollapsedCardLayout = new FlexLayout();
    protected final FlexLayout resultsetExpansionCardLayout = new FlexLayout();
    protected final FlexLayout resultsetSummaryCardLayout = new FlexLayout();

    /////////////////////////////////////////
    ////////////// RESULT SET ///////////////
    /////////////////////////////////////////

    protected final List<ResultSetProp> resultSetProperties = new ArrayList<>();
    protected final SortedMap<Integer, Pair<String, OrderDirection>> resultSetOrdering = new TreeMap<>();
    protected final ListMultimap<String, SummaryPropDef> summaryExpressions = ArrayListMultimap.create();
    protected EntityActionConfig resultSetPrimaryEntityAction;
    protected final List<EntityActionConfig> resultSetSecondaryEntityActions = new ArrayList<>();
    protected Class<? extends IRenderingCustomiser<? extends AbstractEntity<?>, ?>> resultSetRenderingCustomiserType = null;
    protected Class<? extends ICustomPropsAssignmentHandler<? extends AbstractEntity<?>>> resultSetCustomPropAssignmentHandlerType = null;

    protected Pair<Class<? extends IQueryEnhancer<T>>, Optional<CentreContextConfig>> queryEnhancerConfig = null;
    protected IFetchProvider<T> fetchProvider = null;

    protected boolean runAutomatically = false;

    private EntityCentreBuilder() {
    }

    public static <T extends AbstractEntity<?>> ICentreTopLevelActions<T> centreFor(final Class<T> type) {
        return new EntityCentreBuilder<T>().forEntity(type);
    }

    @Override
    public ICentreTopLevelActions<T> forEntity(final Class<T> type) {
        this.entityType = type;
        return new TopLevelActionsBuilder<T>(this);
    }

    public EntityCentreConfig<T> build() {
        // check if there are custom props without default values and no custom values assignment handler
        if (resultSetCustomPropAssignmentHandlerType == null &&
                resultSetProperties.stream().filter(v -> v.propDef.isPresent() && !v.propDef.get().value.isPresent()).count() > 0) {
            throw new IllegalStateException("There are custom properties without default values, but the custom assignment handler is also missing.");
        }

        // compose a correct ordering structure before instantiating an EntityCentreConfig
        final LinkedHashMap<String, OrderDirection> properResultSetOrdering = new LinkedHashMap<>();
        resultSetOrdering.forEach((k, v) -> properResultSetOrdering.put(v.getKey(), v.getValue()));

        return new EntityCentreConfig<T>(
                topLevelActions,
                selectionCriteria,
                defaultMultiValueAssignersForEntityAndStringSelectionCriteria,
                defaultMultiValueAssignersForBooleanSelectionCriteria,
                defaultRangeValueAssignersForDateSelectionCriteria,
                defaultRangeValueAssignersForIntegerSelectionCriteria,
                defaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria,
                defaultSingleValueAssignersForEntitySelectionCriteria,
                defaultSingleValueAssignersForStringSelectionCriteria,
                defaultSingleValueAssignersForBooleanSelectionCriteria,
                defaultSingleValueAssignersForIntegerSelectionCriteria,
                defaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria,
                defaultSingleValueAssignersForDateSelectionCriteria,
                defaultMultiValuesForEntityAndStringSelectionCriteria,
                defaultMultiValuesForBooleanSelectionCriteria,
                defaultRangeValuesForDateSelectionCriteria,
                defaultRangeValuesForIntegerSelectionCriteria,
                defaultRangeValuesForBigDecimalAndMoneySelectionCriteria,
                defaultSingleValuesForEntitySelectionCriteria,
                defaultSingleValuesForStringSelectionCriteria,
                defaultSingleValuesForBooleanSelectionCriteria,
                defaultSingleValuesForIntegerSelectionCriteria,
                defaultSingleValuesForBigDecimalAndMoneySelectionCriteria,
                defaultSingleValuesForDateSelectionCriteria,
                valueMatchersForSelectionCriteria,
                additionalPropsForAutocompleter,
                runAutomatically,
                selectionCriteriaLayout,
                resultsetCollapsedCardLayout,
                resultsetExpansionCardLayout,
                resultsetSummaryCardLayout,
                resultSetProperties,
                summaryExpressions,
                properResultSetOrdering,
                resultSetPrimaryEntityAction,
                resultSetSecondaryEntityActions,
                resultSetRenderingCustomiserType,
                resultSetCustomPropAssignmentHandlerType,
                queryEnhancerConfig,
                fetchProvider);
    }

    public Class<T> getEntityType() {
        return entityType;
    }

}
