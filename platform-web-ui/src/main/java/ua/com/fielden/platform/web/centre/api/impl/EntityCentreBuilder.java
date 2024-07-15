package ua.com.fielden.platform.web.centre.api.impl;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp.derivePropName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.MatcherOptions;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.OrderDirection;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.SummaryPropDef;
import ua.com.fielden.platform.web.centre.api.IEntityCentreBuilder;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritBooleanValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritStringValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritDateValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritOtherValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritDateValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritOtherValueMnemonic;
import ua.com.fielden.platform.web.centre.api.exceptions.CentreConfigException;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPointConfig;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfig;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.impl.ScrollConfig;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl.CentreToolbar;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActionsWithRunConfig;
import ua.com.fielden.platform.web.centre.exceptions.EntityCentreConfigurationException;
import ua.com.fielden.platform.web.layout.FlexLayout;
import ua.com.fielden.platform.web.sse.IEventSource;

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
    protected final List<EntityActionConfig> frontActions = new ArrayList<>();
    protected final List<InsertionPointConfig> insertionPointConfigs = new ArrayList<>();

    protected boolean egiHidden = false;
    protected String gridViewIcon = "tg-icons:grid";
    protected String gridViewIconStyle = "";
    protected boolean draggable = false;
    protected boolean hideCheckboxes = false;
    protected IToolbarConfig toolbarConfig = new CentreToolbar();
    protected boolean hideToolbar = false;
    protected IScrollConfig scrollConfig = ScrollConfig.configScroll().done();
    protected boolean retrieveAll = false;
    protected boolean lockScrollingForInsertionPoints = false;
    protected int pageCapacity = 30;
    protected int maxPageCapacity = 300;
    //EGI height related properties
    private int headerLineNumber = 3;
    protected int visibleRowsCount = 0;
    protected String egiHeight = "";
    protected boolean fitToHeight = false;
    protected String rowHeight = "1.5rem";

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

    protected final Map<String, T3<Class<? extends IValueMatcherWithCentreContext<? extends AbstractEntity<?>>>, Optional<CentreContextConfig>, List<MatcherOptions>>> valueMatchersForSelectionCriteria = new HashMap<>();
    protected final Map<String, List<Pair<String, Boolean>>> additionalPropsForAutocompleter = new HashMap<>();
    protected final Map<String, Class<? extends AbstractEntity<?>>> providedTypesForAutocompletedSelectionCriteria = new HashMap<>();

    protected final FlexLayout selectionCriteriaLayout = new FlexLayout("sel_crit");
    protected final FlexLayout resultsetCollapsedCardLayout = new FlexLayout("collapsed_card");
    protected final FlexLayout resultsetExpansionCardLayout = new FlexLayout("expansion_card");
    protected final FlexLayout resultsetSummaryCardLayout = new FlexLayout("summary_card");

    /////////////////////////////////////////
    ////////////// RESULT SET ///////////////
    /////////////////////////////////////////

    private final List<ResultSetProp<T>> resultSetProperties = new ArrayList<>();
    protected final SortedMap<Integer, Pair<String, OrderDirection>> resultSetOrdering = new TreeMap<>();
    protected final ListMultimap<String, SummaryPropDef> summaryExpressions = ArrayListMultimap.create();
    protected EntityMultiActionConfig resultSetPrimaryEntityAction;
    protected final List<EntityMultiActionConfig> resultSetSecondaryEntityActions = new ArrayList<>();
    protected Class<? extends IRenderingCustomiser<?>> resultSetRenderingCustomiserType = null;
    protected Class<? extends ICustomPropsAssignmentHandler> resultSetCustomPropAssignmentHandlerType = null;

    protected Pair<Class<? extends IQueryEnhancer<T>>, Optional<CentreContextConfig>> queryEnhancerConfig = null;
    protected Pair<Class<?>, Class<?>> generatorTypes = null;
    protected IFetchProvider<T> fetchProvider = null;

    protected boolean runAutomatically = false;
    protected boolean enforcePostSaveRefresh = false;
    protected Integer leftSplitterPosition = null;
    protected Integer rightSplitterPosition = null;
    protected Class<? extends IEventSource> eventSourceClass = null;
    protected Integer refreshCountdown = null;

    private EntityCentreBuilder() {
    }

    public static <T extends AbstractEntity<?>> ICentreTopLevelActionsWithRunConfig<T> centreFor(final Class<T> type) {
        return new EntityCentreBuilder<T>().forEntity(type);
    }

    @Override
    public ICentreTopLevelActionsWithRunConfig<T> forEntity(final Class<T> type) {
        this.entityType = type;
        return new GenericCentreConfigBuilder<>(this);
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

        return new EntityCentreConfig<>(
                egiHidden,
                gridViewIcon,
                gridViewIconStyle,
                draggable,
                hideCheckboxes,
                toolbarConfig,
                hideToolbar,
                scrollConfig,
                retrieveAll,
                lockScrollingForInsertionPoints,
                pageCapacity,
                maxPageCapacity,
                visibleRowsCount,
                headerLineNumber,
                egiHeight,
                fitToHeight,
                rowHeight,
                topLevelActions,
                frontActions,
                insertionPointConfigs,
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
                providedTypesForAutocompletedSelectionCriteria,
                runAutomatically,
                enforcePostSaveRefresh,
                leftSplitterPosition,
                rightSplitterPosition,
                eventSourceClass,
                refreshCountdown,
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
                generatorTypes,
                fetchProvider);
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public EntityCentreBuilder<T> setHeaderLineNumber(final int headerLineNumber) {
        // let's validate the argument
        if (headerLineNumber < 1 || 3 < headerLineNumber) {
            throw new CentreConfigException("The number of lines in EGI headers should be between 1 and 3.");
        }

        this.headerLineNumber = headerLineNumber;
        return this;
    }

    /**
     * Add result set property definition if it is not a duplicated. Otherwise throws {@link EntityCentreConfigurationException}.
     * @param rsp
     */
    protected void addToResultSet(final ResultSetProp<T> rsp) {
        final String rspName = derivePropName(rsp);
        if (resultSetProperties.stream().map(p -> derivePropName(p))
                .anyMatch(name -> EntityUtils.equalsEx(name, rspName))) {
            throw new EntityCentreConfigurationException(format("Property [%s] has been already added to the result set for entity [%s].", derivePropName(rsp), getEntityType().getSimpleName()));
        }
        this.resultSetProperties.add(rsp);
    }

    /**
     * Convenient way to access result set properties.
     * @return
     */
    protected Stream<ResultSetProp<T>> resultSetProperties() {
        return resultSetProperties.stream();
    }
}
