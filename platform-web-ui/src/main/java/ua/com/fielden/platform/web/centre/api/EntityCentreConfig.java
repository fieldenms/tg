package ua.com.fielden.platform.web.centre.api;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithCentreContext;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web.app.exceptions.WebUiBuilderException;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.IQueryEnhancer;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.IMultiValueAutocompleterBuilder;
import ua.com.fielden.platform.web.centre.api.crit.ISingleValueAutocompleterBuilder;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.*;
import ua.com.fielden.platform.web.centre.api.exceptions.CentreConfigException;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPointConfig;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IDynamicColumnBuilder;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.centre.api.resultset.scrolling.IScrollConfig;
import ua.com.fielden.platform.web.centre.api.resultset.toolbar.IToolbarConfig;
import ua.com.fielden.platform.web.centre.exceptions.PropertyDefinitionException;
import ua.com.fielden.platform.web.layout.FlexLayout;
import ua.com.fielden.platform.web.sse.IEventSource;
import ua.com.fielden.platform.web.view.master.api.widgets.impl.AbstractWidget;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static java.lang.String.format;
import static java.util.Collections.*;
import static java.util.Optional.*;
import static java.util.stream.Collectors.toList;
import static ua.com.fielden.platform.domaintree.impl.CalculatedProperty.generateNameFrom;
import static ua.com.fielden.platform.web.centre.WebApiUtils.treeName;
import static ua.com.fielden.platform.web.centre.api.EntityCentreConfig.MatcherOptions.HIDE_ACTIVE_ONLY_ACTION;
import static ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints.ALTERNATIVE_VIEW;
import static ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind.*;

/// Represents a final structure of an entity centre as produced by means of using Entity Centre DSL.
///
public class EntityCentreConfig<T extends AbstractEntity<?>> {

    private final boolean egiHidden;
    private final String gridViewIcon;
    private final String gridViewIconStyle;
    private final boolean draggable;
    private final boolean hideCheckboxes;
    private final IToolbarConfig toolbarConfig;
    private final boolean hideToolbar;
    private final IScrollConfig scrollConfig;
    private final boolean retrieveAll;
    private final boolean lockScrollingForInsertionPoints;
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

    /// A list of top level actions represented as pairs of action configurations as keys and optional group names as values.
    ///
    /// If an action belongs to a group then a corresponding value in the pair contains the name of that group. Otherwise, the pair value is empty. The order of actions in the list
    /// is important and should be honoured when building their UI representation.
    ///
    private final List<Pair<EntityActionConfig, Optional<String>>> topLevelActions = new ArrayList<>();

    /////////////////////////////////////////////
    //////// SELECTION CRITERIA ACTIONS /////////
    /////////////////////////////////////////////

    /// A list of custom selection criteria actions.
    ///
    private final List<EntityActionConfig> frontActions = new ArrayList<>();

    /////////////////////////////////////////////
    /////////// INSERTION POINT ACTIONS /////////
    /////////////////////////////////////////////

    /// A list of functional actions that are associated with entity centre insertion points.
    ///
    /// They do not have visual representation (such as an icon or a button) and are used only to instantiate insertion point views (action per view) and execute their own logic
    /// (method `save` of a corresponding functional entity).
    ///
    private final List<InsertionPointConfig> insertionPointConfigs = new ArrayList<>();

    /////////////////////////////////////////////
    ////////////// SELECTION CRIT ///////////////
    /////////////////////////////////////////////

    /// A list of properties that have been added to selection criteria in the added sequential order.
    ///
    /// It is not important whether a property was added as multi-valued, single-valued or range criterion simply because an appropriate selection criteria kind gets determined
    /// automatically from property declaration at the entity type level.
    ///
    /// The part of Entity Centre DSL that provides developer with ability to pick the kind (i.e. `multi()`, `single()` or `range()`)is there only
    /// to facilitate definition fluency and readability.
    ///
    private final List<String> selectionCriteria = new ArrayList<>();

    /// Default value assigner for various kind and types of selection criteria.
    ///
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

    /// Default values. At the DSL level default values and assigners are mutually exclusive.
    ///
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

    /// Enumeration that contains options for specifying custom value matcher in selection criteria autocompleters.
    ///
    public enum MatcherOptions {

        /// Hides 'active only' toggle button in selection criteria autocompleter result dialogs for activatable properties.
        /// May be useful for autocompleters with custom matcher, that already filters out inactive values.
        ///
        HIDE_ACTIVE_ONLY_ACTION;
    }

    /// A map between selection criteria properties and their custom value matchers with context configuration and MatcherOptions. If a matcher for some criterion is not provided then a default instance of type
    /// [FallbackValueMatcherWithCentreContext] should be used.
    ///
    private final Map<String, T3<Class<? extends IValueMatcherWithCentreContext<? extends AbstractEntity<?>>>, Optional<CentreContextConfig>, List<MatcherOptions>>> valueMatchersForSelectionCriteria = new HashMap<>();

    /// A map between selection criteria properties that are associated with multi- or single-value autocompleter and the additional properties that should be set up for those
    /// autocompleters to be displayed as part of the autocompletion result list.
    ///
    private final Map<String, List<Pair<String, Boolean>>> additionalPropsForAutocompleter = new HashMap<>();

    /// A map between selection criteria properties that are associated with multi- or -sinle-valued autocompleter and entity types that were provided in calls to `.autocompleter(type)`.
    ///
    private final Map<String, Class<? extends AbstractEntity<?>>> providedTypesForAutocompletedSelectionCriteria = new HashMap<>();

    /// Represents the layout settings for selection criteria.
    ///
    private final FlexLayout selectionCriteriaLayout;

    /// Represents the layout settings for card-based resultset representation.
    ///
    protected final FlexLayout resultsetCollapsedCardLayout;
    protected final FlexLayout resultsetExpansionCardLayout;

    protected final FlexLayout resultsetSummaryCardLayout;

    /// Enumeration that contains options for auto-runnable centres.
    ///
    public enum RunAutomaticallyOptions {

        /// If specified for either standalone or embedded centre, any selection criteria changes would get preserved upon auto-running.
        ///
        /// Also, this parameter preserves any loaded save-as configurations upon auto-run.
        /// For example, if a user loads some save-as configuration for an embedded centre, that configuration would be used for auto-run until such time as user changes it (loads the default or any other save-as configuration).
        ///
        /// Without this parameter the default behaviour is applied. More specifically the default configuration is always loaded before auto-run is performed.
        ///
        ALLOW_CUSTOMISED;
    }

    /// Determines whether centre should run automatically or not.
    ///
    private final boolean runAutomatically;
    private final Set<RunAutomaticallyOptions> runAutomaticallyOptions;

    /// Determines the position of left and right splitters.
    ///
    private final Integer leftSplitterPosition;
    private final Integer rightSplitterPosition;

    /// Determines whether insertion points can be rearranged.
    ///
    private boolean insertionPointCustomLayoutEnabled;

    /// Determines whether centre should forcibly refresh the current page upon a successful save of a related entity (regardless of the presence of that entity on the current page).
    ///
    private final boolean enforcePostSaveRefresh;

    /// Identifies event source class that should be used as part of the topic for distributing server side event on client. If `null` is set then no SSE is required.
    private final Class<? extends IEventSource> eventSourceClass;
    /// The number of seconds before refresh on sse event. This value might be null, then refresh will be immediate.
    /// If the value is zero, then user will have to make decision whether to refresh the center or to skip it.
    /// if the value is greater then 0, then user will have a chance to skip refreshing the specified number of seconds.
    /// After refreshCountdown seconds centre will be refreshed immediately.
    ///
    private final Integer refreshCountdown;

    /////////////////////////////////////////////
    ////////////////// RESULT SET ///////////////
    /////////////////////////////////////////////

    /// A list of result set property definitions, presented in the same order a specified using Entity Centre DSL. Natural (persistent or calculated) properties are intertwined
    /// with custom properties.
    ///
    private final List<ResultSetProp<T>> resultSetProperties = new ArrayList<>();

    /// The key in this structure represent resultset properties that are considered to be originating for the associated with them summaries. Each key may reference several
    /// definitions of summary expressions, hence, the use of a multimap. More specifically, [ListMultimap] is used to preserve the order of summary expression as declared
    /// using Entity Centre DSL.
    ///
    private final ListMultimap<String, SummaryPropDef> summaryExpressions = ArrayListMultimap.create();

    /// A convenient structure to capture result set property definition. It includes either a property name that represents a natural (persistent or calculated) property, or a
    /// custom property definition. The structure guarantees that natural and custom properties are mutually exclusive.
    ///
    /// In any of those cases, a custom action can be provided. The custom action value is optional and can be empty if there is no need to provide custom actions specific for
    /// represented in the result set properties. However, the default actions would still get associated with all properties without a custom action. In order to skip even the
    /// default action, a <code>no action</code> configuration needs to set as custom property action.
    ///
    public static class ResultSetProp<T extends AbstractEntity<?>> {
        public final Optional<String> propName;
        public final boolean presentByDefault;
        public final Optional<String> tooltipProp;
        public final Optional<PropDef<?>> propDef;
        public final Optional<Class<? extends IDynamicColumnBuilder<T>>> dynamicColBuilderType;
        public final Optional<CentreContextConfig> contextConfig;
        public final Optional<BiConsumer<T, Optional<CentreContext<T, ?>>>> entityPreProcessor;
        public final Optional<BiFunction<T, Optional<CentreContext<T, ?>>, Map>> renderingHintsProvider;
        public final Optional<AbstractWidget> widget;
        public final int width;
        public final boolean wordWrap;
        public final boolean isFlexible;

        private Optional<EntityMultiActionConfig> propAction = empty();

        public static <T extends AbstractEntity<?>> ResultSetProp<T> propByName(final String propName, final boolean presentByDefault, final int width, final boolean wordWrap, final boolean isFlexible, final Optional<AbstractWidget> widget, final String tooltipProp, final Optional<EntityMultiActionConfig> propAction) {
            return new ResultSetProp<>(propName, presentByDefault, empty(), empty(), empty(), empty(), width, wordWrap, isFlexible, widget, tooltipProp, null, propAction);
        }

        public static <T extends AbstractEntity<?>> ResultSetProp<T> propByDef(final PropDef<?> propDef, final boolean presentByDefault, final int width, final boolean wordWrap, final boolean isFlexible, final String tooltipProp, final Optional<EntityMultiActionConfig> propAction) {
            return new ResultSetProp<>(null, presentByDefault, empty(), empty(), empty(), empty(), width, wordWrap, isFlexible, Optional.empty(), tooltipProp, propDef, propAction);
        }

        public static <T extends AbstractEntity<?>> ResultSetProp<T> dynamicProps(final CharSequence collectionalPropertyName, final Class<? extends IDynamicColumnBuilder<T>> dynamicPropDefinerClass, final BiConsumer<T, Optional<CentreContext<T, ?>>> entityPreProcessor, final BiFunction<T, Optional<CentreContext<T, ?>>, Map> renderingHintsProvider, final CentreContextConfig contextConfig) {
            return new ResultSetProp<>(collectionalPropertyName.toString(), true, of(dynamicPropDefinerClass), of(contextConfig), of(entityPreProcessor), of(renderingHintsProvider), 0, false, false, empty(), null, null, empty());
        }

        public static <T extends AbstractEntity<?>> ResultSetProp<T> dynamicProps(final CharSequence collectionalPropertyName, final Class<? extends IDynamicColumnBuilder<T>> dynamicPropDefinerClass, final BiConsumer<T, Optional<CentreContext<T, ?>>> entityPreProcessor, final CentreContextConfig contextConfig) {
            return new ResultSetProp<>(collectionalPropertyName.toString(), true, of(dynamicPropDefinerClass), of(contextConfig), of(entityPreProcessor), empty(), 0, false, false, empty(), null, null, empty());
        }

        private ResultSetProp(
                final String propName,
                final boolean presentByDefault,
                final Optional<Class<? extends IDynamicColumnBuilder<T>>> dynColBuilderType,
                final Optional<CentreContextConfig> contextConfig,
                final Optional<BiConsumer<T, Optional<CentreContext<T, ?>>>> entityPreProcessor,
                final Optional<BiFunction<T, Optional<CentreContext<T, ?>>, Map>> renderingHintsProvider,
                final int width,
                final boolean wordWrap,
                final boolean isFlexible,
                final Optional<AbstractWidget> widget,
                final String tooltipProp,
                final PropDef<?> propDef,
                final Optional<EntityMultiActionConfig> propAction) {

            if (propName != null && propDef != null) {
                throw new WebUiBuilderException("Only one of property name or property definition should be provided.");
            }

            if (propAction == null) {
                throw new WebUiBuilderException("Multiple Property Action cannot be null.");
            }

            if (StringUtils.isEmpty(propName) && propDef == null) {
                throw new WebUiBuilderException("Either property name or property definition should be provided.");
            }

            this.propName = Optional.ofNullable(propName);
            this.presentByDefault = presentByDefault;
            this.width = width;
            this.wordWrap = wordWrap;
            this.isFlexible = isFlexible;
            this.widget = widget;
            this.tooltipProp = Optional.ofNullable(tooltipProp);
            this.propDef = Optional.ofNullable(propDef);
            this.propAction = propAction;
            this.dynamicColBuilderType = dynColBuilderType;
            this.contextConfig = contextConfig;
            this.entityPreProcessor = entityPreProcessor;
            this.renderingHintsProvider = renderingHintsProvider;
        }

        public void setPropAction(final Optional<EntityMultiActionConfig> propAction) {
            this.propAction = propAction;
        }

        public Optional<EntityMultiActionConfig> getPropAction() {
            return propAction;
        }

        /// Returns the property name for specified [ResultSetProp] instance. The returned property name can be used for retrieving and altering data in
        /// [ICentreDomainTreeManager].
        ///
        public static <T extends AbstractEntity<?>> String derivePropName(final ResultSetProp<T> property) {
            if (property.propName.isPresent()) {
                return treeName(property.propName.get());
            } else {
                if (property.propDef.isPresent()) { // represents the 'custom' property
                    return treeName(generateNameFrom(property.propDef.get().title));
                } else {
                    throw new PropertyDefinitionException(format("The state of result-set property [%s] definition is not correct, need to exist either a 'propName' for the property or 'propDef'.", property));
                }
            }
        }
    }

    /// This is just a convenience structure for capturing a summary property definition.
    ///
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

    /// A map between properties to order by and the ordering direction. The order of elements in this map corresponds to the ordering sequence. That is, the first listed property
    /// should be the first in the resultant order statement, the second -- second, and so on.
    ///
    private final LinkedHashMap<String, OrderDirection> resultSetOrdering = new LinkedHashMap<>();

    /// This is just a helper enumeration to express result set ordering.
    ///
    public enum OrderDirection {
        DESC, ASC;
    }

    /// A primary entity action configuration that is associated with every retrieved and present in the result set entity. It can be `null` if no primary entity action
    /// is needed.
    ///
    private final EntityMultiActionConfig resultSetPrimaryEntityAction;

    /// A list of secondary action configurations that are associated with every retrieved and present in the result set entity. It can be empty if no secondary action are
    /// necessary.
    ///
    private final List<EntityMultiActionConfig> resultSetSecondaryEntityActions = new ArrayList<>();

    /// Represents a type of a contract that is responsible for customisation of rendering for entities and their properties.
    ///
    private final Class<? extends IRenderingCustomiser<?>> resultSetRenderingCustomiserType;

    /// Represents a type of a contract that is responsible for assigning values to custom properties as part of the data retrieval process.
    ///
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
            final String gridViewIcon,
            final String gridViewIconStyle,
            final boolean draggable,
            final boolean hideCheckboxes,
            final IToolbarConfig toolbarConfig,
            final boolean hideToolbar,
            final IScrollConfig scrollConfig,
            final boolean retrieveAll,
            final boolean lockScrollingForInsertionPoints,
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

            final Map<String, T3<Class<? extends IValueMatcherWithCentreContext<? extends AbstractEntity<?>>>, Optional<CentreContextConfig>, List<MatcherOptions>>> valueMatchersForSelectionCriteria,
            final Map<String, List<Pair<String, Boolean>>> additionalPropsForAutocompleter,
            final Map<String, Class<? extends AbstractEntity<?>>> providedTypesForAutocompletedSelectionCriteria,

            final boolean runAutomatically,
            final Set<RunAutomaticallyOptions> runAutomaticallyOptions,
            final boolean enforcePostSaveRefresh,

            final Integer leftSplitterPosition,
            final Integer rightSplitterPosition,

            final Class<? extends IEventSource> eventSourceClass,
            final Integer refreshCountdown,

            final FlexLayout selectionCriteriaLayout,
            final FlexLayout resultsetCollapsedCardLayout,
            final FlexLayout resultsetExpansionCardLayout,
            final FlexLayout resultsetSummaryCardLayout,

            final List<ResultSetProp<T>> resultSetProperties,
            final ListMultimap<String, SummaryPropDef> summaryExpressions,
            final LinkedHashMap<String, OrderDirection> resultSetOrdering,
            final EntityMultiActionConfig resultSetPrimaryEntityAction,
            final List<EntityMultiActionConfig> resultSetSecondaryEntityActions,
            final Class<? extends IRenderingCustomiser<?>> resultSetRenderingCustomiserType,
            final Class<? extends ICustomPropsAssignmentHandler> resultSetCustomPropAssignmentHandlerType,
            final Pair<Class<? extends IQueryEnhancer<T>>, Optional<CentreContextConfig>> queryEnhancerConfig,
            final Pair<Class<?>, Class<?>> generatorTypes,
            final IFetchProvider<T> fetchProvider,
            final boolean insertionPointCustomLayoutEnabled)
    {
        this.egiHidden = egiHidden;
        this.gridViewIcon = gridViewIcon;
        this.gridViewIconStyle = gridViewIconStyle;
        this.draggable = draggable;
        this.hideCheckboxes = hideCheckboxes;
        this.toolbarConfig = toolbarConfig;
        this.hideToolbar = hideToolbar;
        this.scrollConfig = scrollConfig;
        this.retrieveAll = retrieveAll;
        this.lockScrollingForInsertionPoints = lockScrollingForInsertionPoints;
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
        this.runAutomaticallyOptions = runAutomaticallyOptions;
        this.enforcePostSaveRefresh = enforcePostSaveRefresh;
        this.leftSplitterPosition = leftSplitterPosition;
        this.rightSplitterPosition = rightSplitterPosition;

        this.eventSourceClass = eventSourceClass;
        this.refreshCountdown = refreshCountdown;

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
        this.insertionPointCustomLayoutEnabled = insertionPointCustomLayoutEnabled;
    }

    ///////////////////////////////////////////
    /////////////// GETTERS ///////////////////
    ///////////////////////////////////////////

    /// Provides access to the layout settings of selection criteria.
    ///
    public FlexLayout getSelectionCriteriaLayout() {
        return selectionCriteriaLayout;
    }

    /// Provides access to the layout of the top part of the result set card-based representation, which corresponds to the collapsed card state.
    ///
    public FlexLayout getResultsetCollapsedCardLayout() {
        return resultsetCollapsedCardLayout;
    }

    /// Provides access to the layout of the expanded part of the resultset card-based representation.
    ///
    public FlexLayout getResultsetExpansionCardLayout() {
        return resultsetExpansionCardLayout;
    }

    public FlexLayout getResultsetSummaryCardLayout() {
        return resultsetSummaryCardLayout;
    }

    public boolean isRunAutomatically() {
        return runAutomatically;
    }

    public Set<RunAutomaticallyOptions> getRunAutomaticallyOptions() {
        return unmodifiableSet(runAutomaticallyOptions);
    }

    public Optional<Integer> getLeftSplitterPosition() {
        return ofNullable(leftSplitterPosition);
    }

    public Optional<Integer> getRightSplitterPosition() {
        return ofNullable(rightSplitterPosition);
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

    public boolean isInsertionPointCustomLayoutEnabled() {
        return insertionPointCustomLayoutEnabled;
    }

    public Optional<EntityMultiActionConfig> getResultSetPrimaryEntityAction() {
        return ofNullable(resultSetPrimaryEntityAction);
    }

    public List<EntityMultiActionConfig> getResultSetSecondaryEntityActions() {
        return unmodifiableList(resultSetSecondaryEntityActions);
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

    public Optional<Map<String, T3<Class<? extends IValueMatcherWithCentreContext<? extends AbstractEntity<?>>>, Optional<CentreContextConfig>, List<MatcherOptions>>>> getValueMatchersForSelectionCriteria() {
        if (valueMatchersForSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(valueMatchersForSelectionCriteria));
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
        return Optional.of(unmodifiableMap(defaultMultiValueAssignersForEntityAndStringSelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<RangeCritDateValueMnemonic, T>>>> getDefaultRangeValueAssignersForDateSelectionCriteria() {
        if (defaultRangeValueAssignersForDateSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultRangeValueAssignersForDateSelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<RangeCritOtherValueMnemonic<Integer>, T>>>> getDefaultRangeValueAssignersForIntegerSelectionCriteria() {
        if (defaultRangeValueAssignersForIntegerSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultRangeValueAssignersForIntegerSelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<RangeCritOtherValueMnemonic<BigDecimal>, T>>>> getDefaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria() {
        if (defaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<? extends SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>, T>>>> getDefaultSingleValueAssignersForEntitySelectionCriteria() {
        if (defaultSingleValueAssignersForEntitySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultSingleValueAssignersForEntitySelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<String>, T>>>> getDefaultSingleValueAssignersForStringSelectionCriteria() {
        if (defaultSingleValueAssignersForStringSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultSingleValueAssignersForStringSelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<Boolean>, T>>>> getDefaultSingleValueAssignersForBooleanSelectionCriteria() {
        if (defaultSingleValueAssignersForBooleanSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultSingleValueAssignersForBooleanSelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<Integer>, T>>>> getDefaultSingleValueAssignersForIntegerSelectionCriteria() {
        if (defaultSingleValueAssignersForIntegerSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultSingleValueAssignersForIntegerSelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<BigDecimal>, T>>>> getDefaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria() {
        if (defaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<SingleCritDateValueMnemonic, T>>>> getDefaultSingleValueAssignersForDateSelectionCriteria() {
        if (defaultSingleValueAssignersForDateSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultSingleValueAssignersForDateSelectionCriteria));
    }

    public Optional<Map<String, Class<? extends IValueAssigner<MultiCritBooleanValueMnemonic, T>>>> getDefaultMultiValueAssignersForBooleanSelectionCriteria() {
        if (defaultMultiValueAssignersForBooleanSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultMultiValueAssignersForBooleanSelectionCriteria));
    }

    public Optional<Map<String, MultiCritStringValueMnemonic>> getDefaultMultiValuesForEntityAndStringSelectionCriteria() {
        if (defaultMultiValuesForEntityAndStringSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultMultiValuesForEntityAndStringSelectionCriteria));
    }

    public Optional<Map<String, MultiCritBooleanValueMnemonic>> getDefaultMultiValuesForBooleanSelectionCriteria() {
        if (defaultMultiValuesForBooleanSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultMultiValuesForBooleanSelectionCriteria));
    }

    public Optional<Map<String, RangeCritDateValueMnemonic>> getDefaultRangeValuesForDateSelectionCriteria() {
        if (defaultRangeValuesForDateSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultRangeValuesForDateSelectionCriteria));
    }

    public Optional<Map<String, RangeCritOtherValueMnemonic<Integer>>> getDefaultRangeValuesForIntegerSelectionCriteria() {
        if (defaultRangeValuesForIntegerSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultRangeValuesForIntegerSelectionCriteria));
    }

    public Optional<Map<String, RangeCritOtherValueMnemonic<BigDecimal>>> getDefaultRangeValuesForBigDecimalAndMoneySelectionCriteria() {
        if (defaultRangeValuesForBigDecimalAndMoneySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultRangeValuesForBigDecimalAndMoneySelectionCriteria));
    }

    public Optional<Map<String, SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>>> getDefaultSingleValuesForEntitySelectionCriteria() {
        if (defaultSingleValuesForEntitySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultSingleValuesForEntitySelectionCriteria));
    }

    public Optional<Map<String, SingleCritOtherValueMnemonic<String>>> getDefaultSingleValuesForStringSelectionCriteria() {
        if (defaultSingleValuesForStringSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultSingleValuesForStringSelectionCriteria));
    }

    public Optional<Map<String, SingleCritOtherValueMnemonic<Boolean>>> getDefaultSingleValuesForBooleanSelectionCriteria() {
        if (defaultSingleValuesForBooleanSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultSingleValuesForBooleanSelectionCriteria));
    }

    public Optional<Map<String, SingleCritOtherValueMnemonic<Integer>>> getDefaultSingleValuesForIntegerSelectionCriteria() {
        if (defaultSingleValuesForIntegerSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultSingleValuesForIntegerSelectionCriteria));
    }

    public Optional<Map<String, SingleCritOtherValueMnemonic<BigDecimal>>> getDefaultSingleValuesForBigDecimalAndMoneySelectionCriteria() {
        if (defaultSingleValuesForBigDecimalAndMoneySelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultSingleValuesForBigDecimalAndMoneySelectionCriteria));
    }

    public Optional<Map<String, SingleCritDateValueMnemonic>> getDefaultSingleValuesForDateSelectionCriteria() {
        if (defaultSingleValuesForDateSelectionCriteria.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(defaultSingleValuesForDateSelectionCriteria));
    }

    public Optional<Class<? extends IRenderingCustomiser<?>>> getResultSetRenderingCustomiserType() {
        return Optional.ofNullable(resultSetRenderingCustomiserType);
    }

    public Optional<Map<String, OrderDirection>> getResultSetOrdering() {
        if (resultSetOrdering.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(unmodifiableMap(resultSetOrdering));
    }

    public Optional<Class<? extends ICustomPropsAssignmentHandler>> getResultSetCustomPropAssignmentHandlerType() {
        return Optional.ofNullable(resultSetCustomPropAssignmentHandlerType);
    }

    /// Returns action configuration for concrete action kind and its number in that kind's space.
    ///
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        if (TOP_LEVEL == actionKind) {
            // Top actions for entity centre consists of EGI's top action + alternative views actions.
            // 1. Get the lists of those two types of actions
            final Optional<List<Pair<EntityActionConfig, Optional<String>>>> optionalActions = getTopLevelActions();
            final List<EntityActionConfig> altViewActions = getInsertionPointConfigs().stream()
                    .filter(insPointConfig -> insPointConfig.getInsertionPointAction().whereToInsertView.isPresent()
                            && insPointConfig.getInsertionPointAction().whereToInsertView.get() == ALTERNATIVE_VIEW
                            && !insPointConfig.getActions().isEmpty())
                    .flatMap(insPointConfig -> insPointConfig.getActions().stream())
                    .collect(toList());
            // 2. Make sure that there are some actions at all.
            if (!optionalActions.isPresent() && altViewActions.isEmpty()) {
                throw new CentreConfigException("No top-level action exists.");
            }
            // 3. If actionNumber is among indices for EGI's top actions then return that action, otherwise actionNumber represents one of the alternative view actions – adjust the index and return a corresponding action configuration.
            if (optionalActions.isPresent() && actionNumber < optionalActions.get().size()) {
                return optionalActions.get().get(actionNumber).getKey();
            } else {
                return altViewActions.get(actionNumber - optionalActions.map(actions -> actions.size()).orElse(0));
            }
        } else if (PRIMARY_RESULT_SET == actionKind) {
            if (!getResultSetPrimaryEntityAction().isPresent()) {
                throw new CentreConfigException("No primary result-set action exists.");
            }
            return getResultSetPrimaryEntityAction().get().actions().get(actionNumber);
        } else if (SECONDARY_RESULT_SET == actionKind) {
            return getSecondaryActionFor(actionNumber).orElseThrow(() -> new IllegalArgumentException("No secondary result-set action exists."));
        } else if (PROP == actionKind) {
            if (!getResultSetProperties().isPresent()) {
                throw new CentreConfigException("No result-set property exists.");
            }
            return getResultSetProperties().get().stream()
                    .filter(resultSetProp -> resultSetProp.propAction.isPresent())
                    .map(resultSetProp -> resultSetProp.propAction.get().actions())
                    .flatMap(Collection::stream)
                    .collect(toList())
                    .get(actionNumber);
        } else if (INSERTION_POINT == actionKind) {
            if (getInsertionPointConfigs().isEmpty()) {
                throw new CentreConfigException("No insertion point exists.");
            }
            return getInsertionPointConfigs().get(actionNumber).getInsertionPointAction();
        } else if (FRONT == actionKind) {
            if (getFrontActions().isEmpty()) {
                throw new CentreConfigException("No front action exists.");
            }
            return getFrontActions().get(actionNumber);
        } else if (SHARE == actionKind) {
            return null; // computation is not neccessary so identification of action config too
        }
        // TODO implement other types
        throw new UnsupportedOperationException(actionKind + " is not supported yet.");
    }

    /// Finds action configuration with `actionNumber` in the list of secondary actions/multi-actions, if it exists.
    /// The value of `actionNumber` represents an absolute index across all actions as if they were linear (i.e. regardless of how actions might be grouped by multi-actions).
    ///
    private Optional<EntityActionConfig> getSecondaryActionFor(final int actionNumber) {
        int currentActionNumber = actionNumber;
        for (final EntityMultiActionConfig config: getResultSetSecondaryEntityActions()) {
            final List<EntityActionConfig> configActions = config.actions();
            if (currentActionNumber < configActions.size()) {
                return of(configActions.get(currentActionNumber));
            } else {
                currentActionNumber -= configActions.size();
            }
        }
        return empty();
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

    public List<InsertionPointConfig> getInsertionPointConfigs() {
        return Collections.unmodifiableList(insertionPointConfigs);
    }

    public Optional<Class<? extends IEventSource>> getEventSourceClass() {
        return Optional.ofNullable(eventSourceClass);
    }

    public Optional<Integer> getRefreshCountdown() {
        return Optional.ofNullable(refreshCountdown);
    }

    public boolean isEgiHidden() {
        return egiHidden;
    }

    public String getGridViewIcon() {
        return gridViewIcon;
    }

    public String getGridViewIconStyle() {
        return gridViewIconStyle;
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

    public boolean shouldRetrieveAll() {
        return retrieveAll;
    }

    public boolean isLockScrollingForInsertionPoints() {
        return lockScrollingForInsertionPoints;
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

    /// Indicates whether 'active only' action was deliberately hidden by specifying [MatcherOptions#HIDE_ACTIVE_ONLY_ACTION] option in following methods:\
    /// [ISingleValueAutocompleterBuilder#withMatcher(Class, MatcherOptions, MatcherOptions...)]\
    /// [ISingleValueAutocompleterBuilder#withMatcher(Class, CentreContextConfig, MatcherOptions, MatcherOptions...)]\
    /// [IMultiValueAutocompleterBuilder#withMatcher(Class, MatcherOptions, MatcherOptions...)]\
    /// [IMultiValueAutocompleterBuilder#withMatcher(Class, CentreContextConfig, MatcherOptions, MatcherOptions...)]
    ///
    public boolean isActiveOnlyActionHidden(final String property) {
        return getValueMatchersForSelectionCriteria()
            .map(matchersInfo -> matchersInfo.get(property))
            .map(matcherInfo -> matcherInfo._3.contains(HIDE_ACTIVE_ONLY_ACTION))
            .orElse(false);
    }

}
