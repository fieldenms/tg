package ua.com.fielden.platform.web.centre.api;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithCentreContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.exceptions.WebUiBuilderException;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritBooleanValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritStringValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritDateValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritOtherValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritDateValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritOtherValueMnemonic;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPointConfig;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IDynamicColumnBuilder;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfig;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;
import ua.com.fielden.platform.web.layout.FlexLayout;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

/**
 *
 * Represents a final structure of an entity centre as produced by means of using Entity Centre DSL.
 *
 * @author TG Team
 *
 */
public class EntityCentreConfig<T extends AbstractEntity<?>> {

    private final boolean egiHidden;
    private final boolean draggable;
    private final boolean hideCheckboxes;
    private final IToolbarConfig toolbarConfig;
    private final boolean hideToolbar;
    private final IScrollConfig scrollConfig;
    private final int pageCapacity;
    private final int maxPageCapacity;
    private final int visibleRowsCount;
    private final int numberOfHeaderLines;
    private final String egiHeight;
    private final boolean fitToHeight;
    private final String rowHeight;

    /////////////////////////////////////////////
    ///////////// TOP LEVEL ACTIONS /////////////
    /////////////////////////////////////////////

    /**
     * A list of top level actions represented as pairs of action configurations as keys and optional group names as values.
     * <p>
     * If an action belongs to a group then a corresponding value in the pair contains the name of that group. Otherwise, the pair value is empty. The order of actions in the list
     * is important and should be honoured when building their UI representation.
     */
    private final List<Pair<EntityActionConfig, Optional<String>>> topLevelActions = new ArrayList<>();

    /////////////////////////////////////////////
    //////// SELECTION CRITERIA ACTIONS /////////
    /////////////////////////////////////////////

    /**
     * A list of custom selection criteria actions.
     */
    private final List<EntityActionConfig> frontActions = new ArrayList<>();

    /////////////////////////////////////////////
    /////////// INSERTION POINT ACTIONS /////////
    /////////////////////////////////////////////
    /**
     * A list of functional actions that are associated with entity centre insertion points.
     * <p>
     * They do not have visual representation (such as an icon or a button) and are used only to instantiate insertion point views (action per view) and execute their own logic
     * (method <code>save</code> of a corresponding functional entity).
     */
    private final List<InsertionPointConfig> insertionPointConfigs = new ArrayList<>();

    /////////////////////////////////////////////
    ////////////// SELECTION CRIT ///////////////
    /////////////////////////////////////////////

    /**
     * A list of properties that have been added to selection criteria in the added sequential order.
     * <p>
     * It is not important whether a property was added as multi-valued, single-valued or range criterion simply because an appropriate selection criteria kind gets determined
     * automatically from property declaration at the entity type level.
     * <p>
     * The part of Entity Centre DSL that provides developer with ability to pick the kind (i.e. <code>multi()</code>, <code>single()</code> or <code>range()</code>)is there only
     * to facilitate definition fluency and readability.
     */
    private final List<String> selectionCriteria = new ArrayList<>();

    /**
     * Default value assigner for various kind and types of selection criteria.
     */
    private final Map<String, Class<? extends IValueAssigner<MultiCritStringValueMnemonic, T>>> defaultMultiValueAssignersForEntityAndStringSelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends IValueAssigner<MultiCritBooleanValueMnemonic, T>>> defaultMultiValueAssignersForBooleanSelectionCriteria = new HashMap<>();

    private final Map<String, Class<? extends IValueAssigner<RangeCritDateValueMnemonic, T>>> defaultRangeValueAssignersForDateSelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends IValueAssigner<RangeCritOtherValueMnemonic<Integer>, T>>> defaultRangeValueAssignersForIntegerSelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends IValueAssigner<RangeCritOtherValueMnemonic<BigDecimal>, T>>> defaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria = new HashMap<>();

    private final Map<String, Class<? extends IValueAssigner<? extends SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>, T>>> defaultSingleValueAssignersForEntitySelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<String>, T>>> defaultSingleValueAssignersForStringSelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<Boolean>, T>>> defaultSingleValueAssignersForBooleanSelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<Integer>, T>>> defaultSingleValueAssignersForIntegerSelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<BigDecimal>, T>>> defaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria = new HashMap<>();
    private final Map<String, Class<? extends IValueAssigner<SingleCritDateValueMnemonic, T>>> defaultSingleValueAssignersForDateSelectionCriteria = new HashMap<>();

    /**
     * Default values. At the DSL level default values and assigners are mutually exclusive.
     */
    private final Map<String, MultiCritStringValueMnemonic> defaultMultiValuesForEntityAndStringSelectionCriteria = new HashMap<>();
    private final Map<String, MultiCritBooleanValueMnemonic> defaultMultiValuesForBooleanSelectionCriteria = new HashMap<>();

    private final Map<String, RangeCritDateValueMnemonic> defaultRangeValuesForDateSelectionCriteria = new HashMap<>();
    private final Map<String, RangeCritOtherValueMnemonic<Integer>> defaultRangeValuesForIntegerSelectionCriteria = new HashMap<>();
    private final Map<String, RangeCritOtherValueMnemonic<BigDecimal>> defaultRangeValuesForBigDecimalAndMoneySelectionCriteria = new HashMap<>();

    private final Map<String, SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>> defaultSingleValuesForEntitySelectionCriteria = new HashMap<>();
    private final Map<String, SingleCritOtherValueMnemonic<String>> defaultSingleValuesForStringSelectionCriteria = new HashMap<>();
    private final Map<String, SingleCritOtherValueMnemonic<Boolean>> defaultSingleValuesForBooleanSelectionCriteria = new HashMap<>();
    private final Map<String, SingleCritOtherValueMnemonic<Integer>> defaultSingleValuesForIntegerSelectionCriteria = new HashMap<>();
    private final Map<String, SingleCritOtherValueMnemonic<BigDecimal>> defaultSingleValuesForBigDecimalAndMoneySelectionCriteria = new HashMap<>();
    private final Map<String, SingleCritDateValueMnemonic> defaultSingleValuesForDateSelectionCriteria = new HashMap<>();

    /**
     * A map between selection criteria properties and their custom value matchers. If a matcher for some criterion is not provided then a default instance of type
     * {@link FallbackValueMatcherWithCentreContext} should be used.
     */
    private final Map<String, Pair<Class<? extends IValueMatcherWithCentreContext<? extends AbstractEntity<?>>>, Optional<CentreContextConfig>>> valueMatchersForSelectionCriteria = new HashMap<>();

    /**
     * A map between selection criteria properties that are associated with multi- or single-value autocompleter and the additional properties that should be set up for those
     * autocompleters to be displayed as part of the autocompletion result list.
     */
    private final Map<String, List<Pair<String, Boolean>>> additionalPropsForAutocompleter = new HashMap<>();

    /**
     * A map between selection criteria properties that are associated with multi- or -sinle-valued autocompleter and entity types that were provided in calls to <code>.autocompleter(type)</code>.
     */
    private final Map<String, Class<? extends AbstractEntity<?>>> providedTypesForAutocompletedSelectionCriteria = new HashMap<>();

    /**
     * Represents the layout settings for selection criteria.
     */
    private final FlexLayout selectionCriteriaLayout;

    /**
     * Represents the layout settings for card-based resultset representation.
     */
    protected final FlexLayout resultsetCollapsedCardLayout;
    protected final FlexLayout resultsetExpansionCardLayout;

    protected final FlexLayout resultsetSummaryCardLayout;

    /**
     * Determines whether centre should run automatically or not.
     */
    private final boolean runAutomatically;

    /**
     * Determines whether centre should forcibly refresh the current page upon a successful save of a related entity (regardless of the presence of that entity on the current page).
     */
    private final boolean enforcePostSaveRefresh;

    /** Identifies URI for the Server-Side Eventing. If <code>null</code> is set then no SSE is required. */
    private final String sseUri;

    /////////////////////////////////////////////
    ////////////////// RESULT SET ///////////////
    /////////////////////////////////////////////

    /**
     * A list of result set property definitions, presented in the same order a specified using Entity Centre DSL. Natural (persistent or calculated) properties are intertwined
     * with custom properties.
     */
    private final List<ResultSetProp<T>> resultSetProperties = new ArrayList<>();
    /**
     * The key in this structure represent resultset properties that are considered to be originating for the associated with them summaries. Each key may reference several
     * definitions of summary expressions, hence, the use of a multimap. More specifically, {@link ListMultimap} is used to preserve the order of summary expression as declared
     * using Entity Centre DSL.
     */
    private final ListMultimap<String, SummaryPropDef> summaryExpressions = ArrayListMultimap.create();

    /**
     * A convenient structure to capture result set property definition. It includes either a property name that represents a natural (persistent or calculated) property, or a
     * custom property definition. The structure guarantees that natural and custom properties are mutually exclusive.
     * <p>
     * In any of those cases, a custom action can be provided. The custom action value is optional and can be empty if there is no need to provide custom actions specific for
     * represented in the result set properties. However, the default actions would still get associated with all properties without a custom action. In order to skip even the
     * default action, a <code>no action</code> configuration needs to set as custom property action.
     */
    public static class ResultSetProp<T extends AbstractEntity<?>> {
        public final Optional<String> propName;
        public final Optional<String> tooltipProp;
        public final Optional<PropDef<?>> propDef;
        public final Optional<Class<? extends IDynamicColumnBuilder<T>>> dynamicColBuilderType;
        public final Optional<CentreContextConfig> contextConfig;
        public final Optional<BiConsumer> entityPreProcessor;
        public final Supplier<Optional<EntityActionConfig>> propAction;
        public final Optional<AbstractWidget> widget;
        public final int width;
        public final boolean isFlexible;

         public static <T extends AbstractEntity<?>> ResultSetProp<T> propByName(final String propName, final int width, final boolean isFlexible, final Optional<AbstractWidget> widget, final String tooltipProp, final Supplier<Optional<EntityActionConfig>> propAction) {
            return new ResultSetProp<>(propName, empty(), empty(), empty(), width, isFlexible, widget, tooltipProp, null, propAction);
        }

        public static <T extends AbstractEntity<?>> ResultSetProp<T> propByDef(final PropDef<?> propDef, final int width, final boolean isFlexible, final String tooltipProp, final Supplier<Optional<EntityActionConfig>> propAction) {
            return new ResultSetProp<>(null, empty(), empty(), empty(), width, isFlexible, Optional.empty(), tooltipProp, propDef, propAction);
        }

        public static <T extends AbstractEntity<?>> ResultSetProp<T> dynamicProps(final String collectionalPropertyName, final Class<? extends IDynamicColumnBuilder<T>> dynamicPropDefinerClass, final BiConsumer<? extends AbstractEntity<?>, Optional<CentreContext<T, ?>>> entityPreProcessor, final CentreContextConfig contextConfig) {
            return new ResultSetProp<>(collectionalPropertyName, of(dynamicPropDefinerClass), of(contextConfig), of(entityPreProcessor), 0, false, Optional.empty(), null, null, () -> Optional.empty());
        }

        private ResultSetProp(
                final String propName,
                final Optional<Class<? extends IDynamicColumnBuilder<T>>> dynColBuilderType,
                final Optional<CentreContextConfig> contextConfig,
                final Optional<BiConsumer> entityPreProcessor,
                final int width,
                final boolean isFlexible,
                final Optional<AbstractWidget> widget,
                final String tooltipProp,
                final PropDef<?> propDef,
                final Supplier<Optional<EntityActionConfig>> propAction) {

            if (propName != null && propDef != null) {
                throw new WebUiBuilderException("Only one of property name or property definition should be provided.");
            }

            if (propAction == null) {
                throw new WebUiBuilderException("Property action suppplier cannot be null.");
            }

            if (StringUtils.isEmpty(propName) && propDef == null) {
                throw new WebUiBuilderException("Either property name or property definition should be provided.");
            }

            this.propName = Optional.ofNullable(propName);
            this.width = width;
            this.isFlexible = isFlexible;
            this.widget = widget;
            this.tooltipProp = Optional.ofNullable(tooltipProp);
            this.propDef = Optional.ofNullable(propDef);
            this.propAction = propAction;
            this.dynamicColBuilderType = dynColBuilderType;
            this.contextConfig = contextConfig;
            this.entityPreProcessor = entityPreProcessor;
        }
    }

    /**
     * This is just a convenience structure for capturing a summary property definition.
     *
     */
    public static class SummaryPropDef {
        public final String alias;
        public final String expression;
        public final String title;
        public final String desc;
        public final int precision;
        public final int scale;

        public SummaryPropDef(
                final String alias,
                final String expression,
                final String title,
                final String desc,
                final int precision,
                final int scale) {
            this.alias = alias;
            this.expression = expression;
            this.title = title;
            this.desc = desc;
            this.precision = precision;
            this.scale = scale;
        }
    }

    /**
     * A map between properties to order by and the ordering direction. The order of elements in this map corresponds to the ordering sequence. That is, the first listed property
     * should be the first in the resultant order statement, the second -- second, and so on.
     */
    private final LinkedHashMap<String, OrderDirection> resultSetOrdering = new LinkedHashMap<>();

    /**
     * This is just a helper enumeration to express result set ordering.
     */
    public enum OrderDirection {
        DESC, ASC;
    }

    /**
     * A primary entity action configuration that is associated with every retrieved and present in the result set entity. It can be <code>null</code> if no primary entity action
     * is needed.
     */
    private final EntityActionConfig resultSetPrimaryEntityAction;

    /**
     * A list of secondary action configurations that are associated with every retrieved and present in the result set entity. It can be empty if no secondary action are
     * necessary.
     */
    private final List<EntityActionConfig> resultSetSecondaryEntityActions = new ArrayList<>();

    /**
     * Represents a type of a contract that is responsible for customisation of rendering for entities and their properties.
     */
    private final Class<? extends IRenderingCustomiser<?>> resultSetRenderingCustomiserType;

    /**
     * Represents a type of a contract that is responsible for assigning values to custom properties as part of the data retrieval process.
     */
    private final Class<? extends ICustomPropsAssignmentHandler> resultSetCustomPropAssignmentHandlerType;

    ///////////////////////////////////////////////////////////
    ///////// QUERY ENHANCER, GENERATOR and FETCH /////////////
    ///////////////////////////////////////////////////////////

    private final Pair<Class<? extends IQueryEnhancer<T>>, Optional<CentreContextConfig>> queryEnhancerConfig;
    private final Pair<Class<?>, Class<?>> generatorTypes;
    private final IFetchProvider<T> fetchProvider;

    ///////////////////////////////////
    ///////// CONSTRUCTOR /////////////
    ///////////////////////////////////
    public EntityCentreConfig(
            final boolean egiHidden,
            final boolean draggable,
            final boolean hideCheckboxes,
            final IToolbarConfig toolbarConfig,
            final boolean hideToolbar,
            final IScrollConfig scrollConfig,
            final int pageCapacity,
            final int maxPageCapacity,
            final int visibleRowsCount,
            final int numberOfHeaderLines,
            final String egiHeight,
            final boolean fitToHeight,
            final String rowHeight,

            final List<Pair<EntityActionConfig, Optional<String>>> topLevelActions,
            final List<EntityActionConfig> frontActions,
            final List<InsertionPointConfig> insertionPointConfigs,
            final List<String> selectionCriteria,
            final Map<String, Class<? extends IValueAssigner<MultiCritStringValueMnemonic, T>>> defaultMultiValueAssignersForEntityAndStringSelectionCriteria,
            final Map<String, Class<? extends IValueAssigner<MultiCritBooleanValueMnemonic, T>>> defaultMultiValueAssignersForBooleanSelectionCriteria,

            final Map<String, Class<? extends IValueAssigner<RangeCritDateValueMnemonic, T>>> defaultRangeValueAssignersForDateSelectionCriteria,
            final Map<String, Class<? extends IValueAssigner<RangeCritOtherValueMnemonic<Integer>, T>>> defaultRangeValueAssignersForIntegerSelectionCriteria,
            final Map<String, Class<? extends IValueAssigner<RangeCritOtherValueMnemonic<BigDecimal>, T>>> defaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria,

            final Map<String, Class<? extends IValueAssigner<? extends SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>, T>>> defaultSingleValueAssignersForEntitySelectionCriteria,
            final Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<String>, T>>> defaultSingleValueAssignersForStringSelectionCriteria,
            final Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<Boolean>, T>>> defaultSingleValueAssignersForBooleanSelectionCriteria,
            final Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<Integer>, T>>> defaultSingleValueAssignersForIntegerSelectionCriteria,
            final Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<BigDecimal>, T>>> defaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria,
            final Map<String, Class<? extends IValueAssigner<SingleCritDateValueMnemonic, T>>> defaultSingleValueAssignersForDateSelectionCriteria,

            final Map<String, MultiCritStringValueMnemonic> defaultMultiValuesForEntityAndStringSelectionCriteria,
            final Map<String, MultiCritBooleanValueMnemonic> defaultMultiValuesForBooleanSelectionCriteria,

            final Map<String, RangeCritDateValueMnemonic> defaultRangeValuesForDateSelectionCriteria,
            final Map<String, RangeCritOtherValueMnemonic<Integer>> defaultRangeValuesForIntegerSelectionCriteria,
            final Map<String, RangeCritOtherValueMnemonic<BigDecimal>> defaultRangeValuesForBigDecimalAndMoneySelectionCriteria,

            final Map<String, SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>> defaultSingleValuesForEntitySelectionCriteria,
            final Map<String, SingleCritOtherValueMnemonic<String>> defaultSingleValuesForStringSelectionCriteria,
            final Map<String, SingleCritOtherValueMnemonic<Boolean>> defaultSingleValuesForBooleanSelectionCriteria,
            final Map<String, SingleCritOtherValueMnemonic<Integer>> defaultSingleValuesForIntegerSelectionCriteria,
            final Map<String, SingleCritOtherValueMnemonic<BigDecimal>> defaultSingleValuesForBigDecimalAndMoneySelectionCriteria,
            final Map<String, SingleCritDateValueMnemonic> defaultSingleValuesForDateSelectionCriteria,

            final Map<String, Pair<Class<? extends IValueMatcherWithCentreContext<? extends AbstractEntity<?>>>, Optional<CentreContextConfig>>> valueMatchersForSelectionCriteria,
            final Map<String, List<Pair<String, Boolean>>> additionalPropsForAutocompleter,
            final Map<String, Class<? extends AbstractEntity<?>>> providedTypesForAutocompletedSelectionCriteria,

            final boolean runAutomatically,
            final boolean enforcePostSaveRefresh,

            final String sseUri,

            final FlexLayout selectionCriteriaLayout,
            final FlexLayout resultsetCollapsedCardLayout,
            final FlexLayout resultsetExpansionCardLayout,
            final FlexLayout resultsetSummaryCardLayout,

            final List<ResultSetProp<T>> resultSetProperties,
            final ListMultimap<String, SummaryPropDef> summaryExpressions,
            final LinkedHashMap<String, OrderDirection> resultSetOrdering,
            final EntityActionConfig resultSetPrimaryEntityAction,
            final List<EntityActionConfig> resultSetSecondaryEntityActions,
            final Class<? extends IRenderingCustomiser<?>> resultSetRenderingCustomiserType,
            final Class<? extends ICustomPropsAssignmentHandler> resultSetCustomPropAssignmentHandlerType,
            final Pair<Class<? extends IQueryEnhancer<T>>, Optional<CentreContextConfig>> queryEnhancerConfig,
            final Pair<Class<?>, Class<?>> generatorTypes,
            final IFetchProvider<T> fetchProvider) {
        this.egiHidden = egiHidden;
        this.draggable = draggable;
        this.hideCheckboxes = hideCheckboxes;
        this.toolbarConfig = toolbarConfig;
        this.hideToolbar = hideToolbar;
        this.scrollConfig = scrollConfig;
        this.pageCapacity = pageCapacity;
        this.maxPageCapacity = maxPageCapacity;
        this.visibleRowsCount = visibleRowsCount;
        this.numberOfHeaderLines = numberOfHeaderLines;
        this.egiHeight = egiHeight;
        this.fitToHeight = fitToHeight;
        this.rowHeight = rowHeight;

        this.topLevelActions.addAll(topLevelActions);
        this.frontActions.addAll(frontActions);
        this.insertionPointConfigs.addAll(insertionPointConfigs);
        this.selectionCriteria.addAll(selectionCriteria);
        this.defaultMultiValueAssignersForEntityAndStringSelectionCriteria.putAll(defaultMultiValueAssignersForEntityAndStringSelectionCriteria);
        this.defaultMultiValueAssignersForBooleanSelectionCriteria.putAll(defaultMultiValueAssignersForBooleanSelectionCriteria);

        this.defaultRangeValueAssignersForDateSelectionCriteria.putAll(defaultRangeValueAssignersForDateSelectionCriteria);
        this.defaultRangeValueAssignersForIntegerSelectionCriteria.putAll(defaultRangeValueAssignersForIntegerSelectionCriteria);
        this.defaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria.putAll(defaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria);

        this.defaultSingleValueAssignersForEntitySelectionCriteria.putAll(defaultSingleValueAssignersForEntitySelectionCriteria);
        this.defaultSingleValueAssignersForStringSelectionCriteria.putAll(defaultSingleValueAssignersForStringSelectionCriteria);
        this.defaultSingleValueAssignersForBooleanSelectionCriteria.putAll(defaultSingleValueAssignersForBooleanSelectionCriteria);
        this.defaultSingleValueAssignersForIntegerSelectionCriteria.putAll(defaultSingleValueAssignersForIntegerSelectionCriteria);
        this.defaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria.putAll(defaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria);
        this.defaultSingleValueAssignersForDateSelectionCriteria.putAll(defaultSingleValueAssignersForDateSelectionCriteria);

        this.defaultMultiValuesForEntityAndStringSelectionCriteria.putAll(defaultMultiValuesForEntityAndStringSelectionCriteria);
        this.defaultMultiValuesForBooleanSelectionCriteria.putAll(defaultMultiValuesForBooleanSelectionCriteria);

        this.defaultRangeValuesForDateSelectionCriteria.putAll(defaultRangeValuesForDateSelectionCriteria);
        this.defaultRangeValuesForIntegerSelectionCriteria.putAll(defaultRangeValuesForIntegerSelectionCriteria);
        this.defaultRangeValuesForBigDecimalAndMoneySelectionCriteria.putAll(defaultRangeValuesForBigDecimalAndMoneySelectionCriteria);

        this.defaultSingleValuesForEntitySelectionCriteria.putAll(defaultSingleValuesForEntitySelectionCriteria);
        this.defaultSingleValuesForStringSelectionCriteria.putAll(defaultSingleValuesForStringSelectionCriteria);
        this.defaultSingleValuesForBooleanSelectionCriteria.putAll(defaultSingleValuesForBooleanSelectionCriteria);
        this.defaultSingleValuesForIntegerSelectionCriteria.putAll(defaultSingleValuesForIntegerSelectionCriteria);
        this.defaultSingleValuesForBigDecimalAndMoneySelectionCriteria.putAll(defaultSingleValuesForBigDecimalAndMoneySelectionCriteria);
        this.defaultSingleValuesForDateSelectionCriteria.putAll(defaultSingleValuesForDateSelectionCriteria);

        this.valueMatchersForSelectionCriteria.putAll(valueMatchersForSelectionCriteria);
        this.additionalPropsForAutocompleter.putAll(additionalPropsForAutocompleter);
        this.providedTypesForAutocompletedSelectionCriteria.putAll(providedTypesForAutocompletedSelectionCriteria);

        this.selectionCriteriaLayout = selectionCriteriaLayout;
        this.resultsetCollapsedCardLayout = resultsetCollapsedCardLayout;
        this.resultsetExpansionCardLayout = resultsetExpansionCardLayout;
        this.resultsetSummaryCardLayout = resultsetSummaryCardLayout;

        this.runAutomatically = runAutomatically;
        this.enforcePostSaveRefresh = enforcePostSaveRefresh;

        this.sseUri = sseUri;

        this.resultSetProperties.addAll(resultSetProperties);
        this.summaryExpressions.putAll(summaryExpressions);
        this.resultSetOrdering.putAll(resultSetOrdering);
        this.resultSetPrimaryEntityAction = resultSetPrimaryEntityAction;
        this.resultSetSecondaryEntityActions.addAll(resultSetSecondaryEntityActions);
        this.resultSetRenderingCustomiserType = resultSetRenderingCustomiserType;
        this.resultSetCustomPropAssignmentHandlerType = resultSetCustomPropAssignmentHandlerType;
        this.queryEnhancerConfig = queryEnhancerConfig;
        this.generatorTypes = generatorTypes;
        this.fetchProvider = fetchProvider;
    }

    ///////////////////////////////////////////
    /////////////// GETTERS ///////////////////
    ///////////////////////////////////////////
    /**
     * Provides access to the layout settings of selection criteria.
     *
     * @return
     */
    public FlexLayout getSelectionCriteriaLayout() {
        return selectionCriteriaLayout;
    }

    /**
     * Provides access to the layout of the top part of the result set card-based representation, which corresponds to the collapsed card state.
     *
     * @return
     */
    public FlexLayout getResultsetCollapsedCardLayout() {
        return resultsetCollapsedCardLayout;
    }

    /**
     * Provides access to the layout of the expanded part of the resultset card-based representation.
     *
     * @return
     */
    public FlexLayout getResultsetExpansionCardLayout() {
        return resultsetExpansionCardLayout;
    }

    public FlexLayout getResultsetSummaryCardLayout() {
        return resultsetSummaryCardLayout;
    }

    public boolean isRunAutomatically() {
        return runAutomatically;
    }

    public boolean shouldEnforcePostSaveRefresh() {
        return enforcePostSaveRefresh;
    }

    public Optional<Pair<Class<? extends IQueryEnhancer<T>>, Optional<CentreContextConfig>>> getQueryEnhancerConfig() {
        return Optional.ofNullable(queryEnhancerConfig);
    }

    public Optional<Pair<Class<?>, Class<?>>> getGeneratorTypes() {
        return Optional.ofNullable(generatorTypes);
    }

    public Optional<IFetchProvider<T>> getFetchProvider() {
        return Optional.ofNullable(fetchProvider);
    }

    public Optional<EntityActionConfig> getResultSetPrimaryEntityAction() {
        return Optional.ofNullable(resultSetPrimaryEntityAction);
    }

    public Optional<List<EntityActionConfig>> getResultSetSecondaryEntityActions() {
        if (resultSetSecondaryEntityActions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableList(resultSetSecondaryEntityActions));
    }

    public Optional<List<ResultSetProp<T>>> getResultSetProperties() {
        if (resultSetProperties.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableList(resultSetProperties));
    }

    public ListMultimap<String, SummaryPropDef> getSummaryExpressions() {
        return ImmutableListMultimap.copyOf(summaryExpressions);
    }

    public Optional<Map<String, Pair<Class<? extends IValueMatcherWithCentreContext<? extends AbstractEntity<?>>>, Optional<CentreContextConfig>>>> getValueMatchersForSelectionCriteria() {
        if (valueMatchersForSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(valueMatchersForSelectionCriteria));
    }

    public List<Pair<String, Boolean>> getAdditionalPropsForAutocompleter(final String critName) {
        final List<Pair<String, Boolean>> props = additionalPropsForAutocompleter.get(StringUtils.isEmpty(critName) ? "this" : critName);
        return props != null ? props : new ArrayList<>();
    }

    public Optional<Class<? extends AbstractEntity<?>>> getProvidedTypeForAutocompletedSelectionCriterion(final String critName) {
        return Optional.ofNullable(providedTypesForAutocompletedSelectionCriteria.get(StringUtils.isEmpty(critName) ? "this" : critName));
    }

    public Optional<List<String>> getSelectionCriteria() {
        if (selectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableList(selectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<MultiCritStringValueMnemonic, T>>>> getDefaultMultiValueAssignersForEntityAndStringSelectionCriteria() {
        if (defaultMultiValueAssignersForEntityAndStringSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultMultiValueAssignersForEntityAndStringSelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<RangeCritDateValueMnemonic, T>>>> getDefaultRangeValueAssignersForDateSelectionCriteria() {
        if (defaultRangeValueAssignersForDateSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultRangeValueAssignersForDateSelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<RangeCritOtherValueMnemonic<Integer>, T>>>> getDefaultRangeValueAssignersForIntegerSelectionCriteria() {
        if (defaultRangeValueAssignersForIntegerSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultRangeValueAssignersForIntegerSelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<RangeCritOtherValueMnemonic<BigDecimal>, T>>>> getDefaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria() {
        if (defaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<? extends SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>, T>>>> getDefaultSingleValueAssignersForEntitySelectionCriteria() {
        if (defaultSingleValueAssignersForEntitySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultSingleValueAssignersForEntitySelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<String>, T>>>> getDefaultSingleValueAssignersForStringSelectionCriteria() {
        if (defaultSingleValueAssignersForStringSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultSingleValueAssignersForStringSelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<Boolean>, T>>>> getDefaultSingleValueAssignersForBooleanSelectionCriteria() {
        if (defaultSingleValueAssignersForBooleanSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultSingleValueAssignersForBooleanSelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<Integer>, T>>>> getDefaultSingleValueAssignersForIntegerSelectionCriteria() {
        if (defaultSingleValueAssignersForIntegerSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultSingleValueAssignersForIntegerSelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<BigDecimal>, T>>>> getDefaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria() {
        if (defaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<SingleCritDateValueMnemonic, T>>>> getDefaultSingleValueAssignersForDateSelectionCriteria() {
        if (defaultSingleValueAssignersForDateSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultSingleValueAssignersForDateSelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<MultiCritBooleanValueMnemonic, T>>>> getDefaultMultiValueAssignersForBooleanSelectionCriteria() {
        if (defaultMultiValueAssignersForBooleanSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultMultiValueAssignersForBooleanSelectionCriteria));
    }

    public Optional<Map<String, MultiCritStringValueMnemonic>> getDefaultMultiValuesForEntityAndStringSelectionCriteria() {
        if (defaultMultiValuesForEntityAndStringSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultMultiValuesForEntityAndStringSelectionCriteria));
    }

    public Optional<Map<String, MultiCritBooleanValueMnemonic>> getDefaultMultiValuesForBooleanSelectionCriteria() {
        if (defaultMultiValuesForBooleanSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultMultiValuesForBooleanSelectionCriteria));
    }

    public Optional<Map<String, RangeCritDateValueMnemonic>> getDefaultRangeValuesForDateSelectionCriteria() {
        if (defaultRangeValuesForDateSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultRangeValuesForDateSelectionCriteria));
    }

    public Optional<Map<String, RangeCritOtherValueMnemonic<Integer>>> getDefaultRangeValuesForIntegerSelectionCriteria() {
        if (defaultRangeValuesForIntegerSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultRangeValuesForIntegerSelectionCriteria));
    }

    public Optional<Map<String, RangeCritOtherValueMnemonic<BigDecimal>>> getDefaultRangeValuesForBigDecimalAndMoneySelectionCriteria() {
        if (defaultRangeValuesForBigDecimalAndMoneySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultRangeValuesForBigDecimalAndMoneySelectionCriteria));
    }

    public Optional<Map<String, SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>>> getDefaultSingleValuesForEntitySelectionCriteria() {
        if (defaultSingleValuesForEntitySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultSingleValuesForEntitySelectionCriteria));
    }

    public Optional<Map<String, SingleCritOtherValueMnemonic<String>>> getDefaultSingleValuesForStringSelectionCriteria() {
        if (defaultSingleValuesForStringSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultSingleValuesForStringSelectionCriteria));
    }

    public Optional<Map<String, SingleCritOtherValueMnemonic<Boolean>>> getDefaultSingleValuesForBooleanSelectionCriteria() {
        if (defaultSingleValuesForBooleanSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultSingleValuesForBooleanSelectionCriteria));
    }

    public Optional<Map<String, SingleCritOtherValueMnemonic<Integer>>> getDefaultSingleValuesForIntegerSelectionCriteria() {
        if (defaultSingleValuesForIntegerSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultSingleValuesForIntegerSelectionCriteria));
    }

    public Optional<Map<String, SingleCritOtherValueMnemonic<BigDecimal>>> getDefaultSingleValuesForBigDecimalAndMoneySelectionCriteria() {
        if (defaultSingleValuesForBigDecimalAndMoneySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultSingleValuesForBigDecimalAndMoneySelectionCriteria));
    }

    public Optional<Map<String, SingleCritDateValueMnemonic>> getDefaultSingleValuesForDateSelectionCriteria() {
        if (defaultSingleValuesForDateSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(defaultSingleValuesForDateSelectionCriteria));
    }

    public Optional<Class<? extends IRenderingCustomiser<?>>> getResultSetRenderingCustomiserType() {
        return Optional.ofNullable(resultSetRenderingCustomiserType);
    }

    public Optional<Map<String, OrderDirection>> getResultSetOrdering() {
        if (resultSetOrdering.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableMap(resultSetOrdering));
    }

    public Optional<Class<? extends ICustomPropsAssignmentHandler>> getResultSetCustomPropAssignmentHandlerType() {
        return Optional.ofNullable(resultSetCustomPropAssignmentHandlerType);
    }

    /**
     * Returns action configuration for concrete action kind and its number in that kind's space.
     *
     * @param actionKind
     * @param actionNumber
     * @return
     */
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        if (FunctionalActionKind.TOP_LEVEL == actionKind) {
            if (!getTopLevelActions().isPresent()) {
                throw new IllegalArgumentException("No top-level action exists.");
            }
            return getTopLevelActions().get().get(actionNumber).getKey();
        } else if (FunctionalActionKind.PRIMARY_RESULT_SET == actionKind) {
            if (!getResultSetPrimaryEntityAction().isPresent()) {
                throw new IllegalArgumentException("No primary result-set action exists.");
            }
            return getResultSetPrimaryEntityAction().get();
        } else if (FunctionalActionKind.SECONDARY_RESULT_SET == actionKind) {
            if (!getResultSetSecondaryEntityActions().isPresent()) {
                throw new IllegalArgumentException("No secondary result-set action exists.");
            }
            return getResultSetSecondaryEntityActions().get().get(actionNumber);
        } else if (FunctionalActionKind.PROP == actionKind) {
            if (!getResultSetProperties().isPresent()) {
                throw new IllegalArgumentException("No result-set property exists.");
            }
            return getResultSetProperties().get().stream()
                    .filter(resultSetProp -> resultSetProp.propAction.get().isPresent())
                    .map(resultSetProp -> resultSetProp.propAction.get().get())
                    .collect(Collectors.toList())
                    .get(actionNumber);
        } else if (FunctionalActionKind.INSERTION_POINT == actionKind) {
            if (!getInsertionPointConfigs().isPresent()) {
                throw new IllegalArgumentException("No insertion point exists.");
            }
            return getInsertionPointConfigs().get().get(actionNumber).getInsertionPointAction();
        } else if (FunctionalActionKind.FRONT == actionKind) {
            if (getFrontActions().isEmpty()) {
                throw new IllegalArgumentException("No front action exists.");
            }
            return getFrontActions().get(actionNumber);
        }
        // TODO implement other types
        throw new UnsupportedOperationException(actionKind + " is not supported yet.");
    }

    public Optional<List<Pair<EntityActionConfig, Optional<String>>>> getTopLevelActions() {
        if (topLevelActions.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableList(topLevelActions));
    }

    public List<EntityActionConfig> getFrontActions() {
        return Collections.unmodifiableList(frontActions);
    }

    public Optional<List<InsertionPointConfig>> getInsertionPointConfigs() {
        if (insertionPointConfigs.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Collections.unmodifiableList(insertionPointConfigs));
    }

    public Optional<String> getSseUri() {
        return Optional.ofNullable(sseUri);
    }

    public boolean isEgiHidden() {
        return egiHidden;
    }

    public boolean shouldHideCheckboxes() {
        return hideCheckboxes;
    }

    public boolean isDraggable() {
        return draggable;
    }

    public IToolbarConfig getToolbarConfig() {
        return toolbarConfig;
    }

    public boolean shouldHideToolbar() {
        return hideToolbar;
    }

    public IScrollConfig getScrollConfig() {
        return scrollConfig;
    }

    public int getPageCapacity() {
        return pageCapacity;
    }

    public int getMaxPageCapacity() {
        return maxPageCapacity;
    }

    public int getVisibleRowsCount() {
        return visibleRowsCount;
    }

    public int getNumberOfHeaderLines() {
        return numberOfHeaderLines;
    }

    public String getEgiHeight() {
        return egiHeight;
    }

    public boolean isFitToHeight() {
        return fitToHeight;
    }

    public String getRowHeight() {
        return rowHeight;
    }
}
