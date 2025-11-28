package ua.com.fielden.platform.web.centre;

import com.google.common.collect.ListMultimap;
import com.google.inject.Injector;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackPropertyDescriptorMatcherWithCentreContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithCentreContext;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.data.generator.IGenerator;
import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.CalculatedPropertyInfo;
import ua.com.fielden.platform.domaintree.impl.CustomProperty;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity_centre.mnemonics.DateRangeConditionEnum;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.jackson.DefaultValueContract;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.ui.config.EntityCentreConfigCo;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemCo;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.app.exceptions.WebUiBuilderException;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.MatcherOptions;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.OrderDirection;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.SummaryPropDef;
import ua.com.fielden.platform.web.centre.api.ICentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.EntityMultiActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.multi.FunctionalMultiActionElement;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.IMultiValueAutocompleterBuilder;
import ua.com.fielden.platform.web.centre.api.crit.ISingleValueAutocompleterBuilder;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.*;
import ua.com.fielden.platform.web.centre.api.crit.impl.*;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPointBuilder;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPointConfig;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IDynamicColumnBuilder;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.layout.FlexLayout;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.sse.IEventSource;
import ua.com.fielden.platform.web.utils.EntityResourceUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.*;
import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.isCritOnlySingle;
import static ua.com.fielden.platform.domaintree.impl.AbstractDomainTree.validateRootType;
import static ua.com.fielden.platform.domaintree.impl.CalculatedProperty.generateNameFrom;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getPropertyAnnotation;
import static ua.com.fielden.platform.reflection.Finder.streamUnionSubProperties;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.utils.EntityUtils.*;
import static ua.com.fielden.platform.web.centre.CentreUpdater.FRESH_CENTRE_NAME;
import static ua.com.fielden.platform.web.centre.CentreUpdater.updateCentre;
import static ua.com.fielden.platform.web.centre.CentreUpdaterUtils.createEmptyCentre;
import static ua.com.fielden.platform.web.centre.EgiConfigurations.*;
import static ua.com.fielden.platform.web.centre.WebApiUtils.dslName;
import static ua.com.fielden.platform.web.centre.WebApiUtils.treeName;
import static ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp.derivePropName;
import static ua.com.fielden.platform.web.centre.api.EntityCentreConfig.RunAutomaticallyOptions.ALLOW_CUSTOMISED;
import static ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints.ALTERNATIVE_VIEW;
import static ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl.CentreToolbar.selectView;
import static ua.com.fielden.platform.web.interfaces.DeviceProfile.DESKTOP;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getOriginalPropertyName;
import static ua.com.fielden.platform.web.utils.EntityResourceUtils.getOriginalType;
import static ua.com.fielden.platform.web.view.master.EntityMaster.flattenedNameOf;
import static ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder.createImports;
import static ua.com.fielden.platform.web.view.master.api.widgets.autocompleter.impl.AbstractEntityAutocompletionWidget.createDefaultAdditionalProps;

/// Represents the entity centre.
///
public class EntityCentre<T extends AbstractEntity<?>> implements ICentre<T> {

    private static final CentreContextConfig defaultCentreContextConfig = new CentreContextConfig(false, false, false, false, null, empty(), empty());

    public static final String IMPORTS = "<!--@imports-->";
    private static final String FULL_ENTITY_TYPE = "@full_entity_type";
    private static final String FULL_MI_TYPE = "@full_mi_type";
    private static final String MI_TYPE = "@mi_type";
    //egi related properties
    private static final String EGI_LAYOUT = "@gridLayout";
    private static final String EGI_LAYOUT_CONFIG = "//gridLayoutConfig";
    private static final String EGI_SHORTCUTS = "@customShortcuts";
    private static final String EGI_VIEW_ICON = "@egiViewIcon";
    private static final String EGI_VIEW_ICON_STYLE = "@egiViewStyle";
    private static final String EGI_TOOLBAR_VISIBLE = "@toolbarVisible";
    private static final String EGI_HIDDEN = "@hidden";
    private static final String EGI_DRAGGABLE = "@canDragFrom";
    private static final String EGI_DRAG_ANCHOR_FIXED = "@dragAnchorFixed";
    private static final String EGI_CHECKBOX_VISIBILITY = "@checkboxVisible";
    private static final String EGI_CHECKBOX_FIXED = "@checkboxesFixed";
    private static final String EGI_CHECKBOX_WITH_PRIMARY_ACTION_FIXED = "@checkboxesWithPrimaryActionsFixed";
    private static final String EGI_NUM_OF_FIXED_COLUMNS = "@numOfFixedCols";
    private static final String EGI_SECONDARY_ACTION_FIXED = "@secondaryActionsFixed";
    private static final String EGI_HEADER_FIXED = "@headerFixed";
    private static final String EGI_SUMMARY_FIXED = "@summaryFixed";
    private static final String EGI_HEIGHT = "@egiHeight";
    private static final String EGI_FIT_TO_HEIGHT = "@fitToHeight";
    private static final String EGI_ROW_HEIGHT = "@egiRowHeight";
    private static final String EGI_ACTIONS = "//generatedActionObjects";
    private static final String EGI_PRIMARY_ACTION = "//generatedPrimaryAction";
    private static final String EGI_SECONDARY_ACTIONS = "//generatedSecondaryActions";
    private static final String EGI_PROPERTY_ACTIONS = "//generatedPropActions";
    private static final String EGI_DOM = "<!--@egi_columns-->";
    private static final String EGI_EDITORS = "<!--@egi_editors-->";
    private static final String EGI_FUNCTIONAL_ACTION_DOM = "<!--@functional_actions-->";
    private static final String EGI_PRIMARY_ACTION_DOM = "<!--@primary_action-->";
    private static final String EGI_SECONDARY_ACTIONS_DOM = "<!--@secondary_actions-->";
    //Custom toolbar action for switching view
    private static final String SWITCH_VIEW_ACTION_DOM = "<!--@switch_view_actions-->";
    // Front actions
    private static final String FRONT_ACTIONS_DOM = "<!--@custom-front-actions-->";
    private static final String FRONT_ACTIONS = "//generatedFrontActionObjects";
    // Share actions
    private static final String SHARE_ACTIONS_DOM = "<!--@custom-share-actions-->";
    private static final String SHARE_ACTIONS = "//generatedShareActionObjects";
    // Toolbar related
    private static final String TOOLBAR_DOM = "<!--@toolbar-->";
    private static final String TOOLBAR_JS = "//toolbarGeneratedFunction";
    private static final String TOOLBAR_STYLES = "/*toolbarStyles*/";
    // Selection criteria related
    private static final String QUERY_ENHANCER_CONFIG = "@queryEnhancerContextConfig";
    private static final String CRITERIA_DOM = "<!--@criteria_editors-->";
    private static final String SELECTION_CRITERIA_LAYOUT_CONFIG = "//@layoutConfig";
    // Insertion points
    private static final String INSERTION_POINT_ACTIONS = "//generatedInsertionPointActions";
    private static final String INSERTION_POINT_ACTIONS_DOM = "<!--@insertion_point_actions-->";
    private static final String LEFT_INSERTION_POINT_DOM = "<!--@left_insertion_points-->";
    private static final String RIGHT_INSERTION_POINT_DOM = "<!--@right_insertion_points-->";
    private static final String TOP_INSERTION_POINT_DOM = "<!--@top_insertion_points-->";
    private static final String BOTTOM_INSERTION_POINT_DOM = "<!--@bottom_insertion_points-->";
    private static final String ALTERNATIVE_VIEW_INSERTION_POINT_DOM = "<!--@alternative_view_insertion_points-->";
    //centre related  config properties
    private static final String CENTRE_RETRIEVE_ALL_OPTION = "@retrieveAll";
    private static final String CENTRE_SCROLL="@centreScroll";
    private static final String LEFT_SPLITTER_POSITION = "@leftSplitterPositionPlacehoder";
    private static final String RIGHT_SPLITTER_POSITION = "@rightSplitterPositionPlacehoder";
    private static final String INSERTION_POINT_CUSTOM_LAYOUT_ENABLED = "@insertionPointCustomLayoutEnabled";
    private static final String SSE_REFRESH_COUNTDOWN = "@sseRefreshCountdown";
    // generic custom code
    private static final String READY_CUSTOM_CODE = "//@centre-is-ready-custom-code";
    private static final String ATTACHED_CUSTOM_CODE = "//@centre-has-been-attached-custom-code";

    private final Logger logger = getLogger(getClass());
    private final String name;
    private final EntityCentreConfig<T> dslDefaultConfig;
    private final Injector injector;
    private final Class<T> entityType;
    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final ICompanionObjectFinder companionFinder;
    private final UnaryOperator<ICentreDomainTreeManagerAndEnhancer> postCentreCreated;
    private Optional<JsCode> customCode = empty();
    private Optional<JsCode> customCodeOnAttach = empty();
    private Optional<JsCode> customImports = empty();

    private final IWebUiConfig webUiConfig;
    private final EntityCentreConfigCo eccCompanion;
    private final MainMenuItemCo mmiCompanion;
    private final IUser userCompanion;

    /// Constructs an entity centre based on the specified configuration.
    ///
    /// @param miType a menu item type representing an entry point for the entity centre being constructed.
    /// @param dslDefaultConfig an entity centre configuration.
    /// @param injector needed for dynamic instantiation of the companion finder and other infrastructural types.
    ///
    public EntityCentre(final Class<? extends MiWithConfigurationSupport<?>> miType, final EntityCentreConfig<T> dslDefaultConfig, final Injector injector) {
        this(miType, miType.getSimpleName(), dslDefaultConfig, injector, null);
    }

    /// Creates new [EntityCentre] instance for the menu item type and with specified name.
    ///
    /// @param miType the menu item type for which this entity centre is to be created.
    /// @param name the name for this entity centre.
    /// @param dslDefaultConfig default configuration taken from Centre DSL
    ///
    public EntityCentre(final Class<? extends MiWithConfigurationSupport<?>> miType, final String name, final EntityCentreConfig<T> dslDefaultConfig, final Injector injector, final UnaryOperator<ICentreDomainTreeManagerAndEnhancer> postCentreCreated) {
        this.name = name;
        this.dslDefaultConfig = dslDefaultConfig;

        this.injector = injector;

        validateMenuItemTypeRootType(miType);
        validateViewConfiguration(miType, dslDefaultConfig);

        this.miType = miType;
        this.entityType = EntityResourceUtils.getEntityType(miType);
        this.companionFinder = this.injector.getInstance(ICompanionObjectFinder.class);
        this.postCentreCreated = postCentreCreated;

        webUiConfig = injector.getInstance(IWebUiConfig.class);
        eccCompanion = companionFinder.find(ua.com.fielden.platform.ui.config.EntityCentreConfig.class);
        mmiCompanion = companionFinder.find(MainMenuItem.class);
        userCompanion = companionFinder.find(User.class);
    }

    /// Validates root type corresponding to `menuItemType`.
    ///
    private static void validateMenuItemTypeRootType(final Class<? extends MiWithConfigurationSupport<?>> miType) {
        final EntityType etAnnotation = miType.getAnnotation(EntityType.class);
        if (etAnnotation == null || etAnnotation.value() == null) {
            throw new WebUiBuilderException(format("The menu item type %s has no 'EntityType' annotation, which is necessary to specify the root type of the centre.", miType.getSimpleName()));
        }
        final Class<?> root = etAnnotation.value();
        validateRootType(root);
    }

    /// Validates entity centre's views and their availability.
    ///
    private static <T extends AbstractEntity<?>> void validateViewConfiguration(final Class<? extends MiWithConfigurationSupport<?>> miType, final EntityCentreConfig<T> dslDefaultConfig) {
        final long altViewCount = dslDefaultConfig.getInsertionPointConfigs().stream()
            .filter(ip -> ip.getInsertionPointAction().whereToInsertView.map(whereToInsert -> whereToInsert == ALTERNATIVE_VIEW).orElse(FALSE)).count();
        final long insPointCount = dslDefaultConfig.getInsertionPointConfigs().size() - altViewCount;

        if (dslDefaultConfig.isEgiHidden() && insPointCount == 0 && altViewCount == 0) {
            throw new WebUiBuilderException(format("At least one result view should be available for entity centre %s.", miType.getSimpleName()));
        }
    }

    /// Generates default centre from DSL config and postCentreCreated callback (user unspecific).
    ///
    private ICentreDomainTreeManagerAndEnhancer createUserUnspecificDefaultCentre(final EntityCentreConfig<T> dslDefaultConfig, final EntityFactory entityFactory, final UnaryOperator<ICentreDomainTreeManagerAndEnhancer> postCentreCreated) {
        return createDefaultCentre0(dslDefaultConfig, entityFactory, postCentreCreated, false);
    }

    /// Generates default centre from DSL config and postCentreCreated callback (user specific).
    ///
    private ICentreDomainTreeManagerAndEnhancer createDefaultCentre(final EntityCentreConfig<T> dslDefaultConfig, final EntityFactory entityFactory, final UnaryOperator<ICentreDomainTreeManagerAndEnhancer> postCentreCreated) {
        return createDefaultCentre0(dslDefaultConfig, entityFactory, postCentreCreated, true);
    }

    /// Creates calculated / custom property containers from Centre DSL definition. This is to be used when constructing [CentreDomainTreeManagerAndEnhancer] instances.
    ///
    private static <T extends AbstractEntity<?>>T2<Map<Class<?>, Set<CalculatedPropertyInfo>>, Map<Class<?>, List<CustomProperty>>> createCalculatedAndCustomProperties(final Class<?> entityType, final Optional<List<ResultSetProp<T>>> resultSetProps, final ListMultimap<String, SummaryPropDef> summaryExpressions) {
        final Map<Class<?>, List<CustomProperty>> customProperties = new LinkedHashMap<>();
        customProperties.put(entityType, new ArrayList<CustomProperty>());
        if (resultSetProps.isPresent()) {
            for (final ResultSetProp<T> property : resultSetProps.get()) {
                if (!property.propName.isPresent()) {
                    if (property.propDef.isPresent()) { // represents the 'custom' property
                        final PropDef<?> propDef = property.propDef.get();
                        final Class<?> managedType = entityType; // getManagedType(entityType); -- please note that mutual custom props validation is not be performed -- apply method invokes at the end after adding all custom / calculated properties
                        customProperties.get(entityType).add(new CustomProperty(entityType, managedType, "" /* this is the contextPath */, generateNameFrom(propDef.title), propDef.title, propDef.desc, propDef.type, IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE));
                    } else {
                        throw new IllegalStateException(format("The state of result-set property [%s] definition is not correct, need to exist either a 'propName' for the property or 'propDef'.", property));
                    }
                }
            }
        }

        final Map<Class<?>, Set<CalculatedPropertyInfo>> calculatedPropertiesInfo = new LinkedHashMap<>();
        calculatedPropertiesInfo.put(entityType, new LinkedHashSet<>());
        for (final Entry<String, Collection<SummaryPropDef>> entry : summaryExpressions.asMap().entrySet()) {
            final String originationProperty = treeName(entry.getKey());
            for (final SummaryPropDef summaryProp : entry.getValue()) {
                calculatedPropertiesInfo.get(entityType).add(new CalculatedPropertyInfo(entityType, "", summaryProp.alias,
                        summaryProp.expression,
                        summaryProp.title,
                        CalculatedPropertyAttribute.NO_ATTR,
                        "".equals(originationProperty) ? "SELF" : originationProperty, summaryProp.desc,
                        summaryProp.precision,
                        summaryProp.scale));
            }
        }

        return t2(calculatedPropertiesInfo, customProperties);
    }

    /// Creates default centre from Centre DSL configuration by adding calculated / custom props, applying selection crit defaults, EGI column widths / ordering etc.
    ///
    public static <T extends AbstractEntity<?>> ICentreDomainTreeManagerAndEnhancer createDefaultCentreFrom(
        final EntityCentreConfig<T> dslDefaultConfig,
        final EntityFactory entityFactory,
        final UnaryOperator<ICentreDomainTreeManagerAndEnhancer> postCentreCreated,
        final boolean userSpecific,
        final Class<T> entityType,
        final Class<? extends MiWithConfigurationSupport<?>> miType,
        final Injector injector) {

        final Optional<List<ResultSetProp<T>>> resultSetProps = dslDefaultConfig.getResultSetProperties();
        final ListMultimap<String, SummaryPropDef> summaryExpressions = dslDefaultConfig.getSummaryExpressions();

        final ICentreDomainTreeManagerAndEnhancer cdtmae = createEmptyCentre(entityType, entityFactory, createCalculatedAndCustomProperties(entityType, resultSetProps, summaryExpressions), miType);

        cdtmae.setPreferredView(calculatePreferredViewIndex(dslDefaultConfig));

        final Optional<List<String>> selectionCriteria = dslDefaultConfig.getSelectionCriteria();
        if (selectionCriteria.isPresent()) {
            for (final String property : selectionCriteria.get()) {
                cdtmae.getFirstTick().check(entityType, treeName(property), true);

                if (userSpecific) {
                    provideDefaultsFor(property, cdtmae, dslDefaultConfig, entityType, injector);
                }
            }
        }

        if (resultSetProps.isPresent()) {
            final Map<String, Integer> growFactors = calculateGrowFactors(resultSetProps.get());
            for (final ResultSetProp<T> property : resultSetProps.get()) {
                final String propertyName = derivePropName(property);
                if (!property.dynamicColBuilderType.isPresent()) {
                    cdtmae.getSecondTick().check(entityType, propertyName, true);
                    if (property.presentByDefault) {
                        cdtmae.getSecondTick().use(entityType, propertyName, true);
                    }
                    cdtmae.getSecondTick().setWidth(entityType, propertyName, property.width);
                    if (growFactors.containsKey(propertyName)) {
                        cdtmae.getSecondTick().setGrowFactor(entityType, propertyName, growFactors.get(propertyName));
                    }
                }
            }
        }
        for (final Entry<String, Collection<SummaryPropDef>> entry : summaryExpressions.asMap().entrySet()) {
            for (final SummaryPropDef summaryProp : entry.getValue()) {
                cdtmae.getSecondTick().check(entityType, summaryProp.alias, true);
            }
        }

        final Optional<Map<String, OrderDirection>> propOrdering = dslDefaultConfig.getResultSetOrdering();
        if (propOrdering.isPresent()) {

            // by default ordering occurs by "this" that is why it needs to be switched off in the presence of alternative ordering configuration
            final List<Pair<String, Ordering>> orderedPropsByDefault = cdtmae.getSecondTick().orderedProperties(entityType);
            if (!orderedPropsByDefault.isEmpty()) {
                orderedPropsByDefault.forEach(propOrder -> {
                    if (propOrder.getValue() == Ordering.ASCENDING) {
                        cdtmae.getSecondTick().toggleOrdering(entityType, propOrder.getKey());
                        cdtmae.getSecondTick().toggleOrdering(entityType, propOrder.getKey());
                    } else if (propOrder.getValue() == Ordering.DESCENDING) {
                        cdtmae.getSecondTick().toggleOrdering(entityType, propOrder.getKey());
                    }
                });
            }

            // let's now apply the ordering as per configuration
            for (final Map.Entry<String, OrderDirection> propAndOrderDirection : propOrdering.get().entrySet()) {
                if (OrderDirection.ASC == propAndOrderDirection.getValue()) {
                    cdtmae.getSecondTick().toggleOrdering(entityType, treeName(propAndOrderDirection.getKey()));
                } else { // OrderDirection.DESC
                    cdtmae.getSecondTick().toggleOrdering(entityType, treeName(propAndOrderDirection.getKey())).toggleOrdering(entityType, treeName(propAndOrderDirection.getKey()));
                }
            }
        }

        cdtmae.getSecondTick().setPageCapacity(dslDefaultConfig.getPageCapacity());
        cdtmae.getSecondTick().setMaxPageCapacity(dslDefaultConfig.getMaxPageCapacity());
        cdtmae.getSecondTick().setVisibleRowsCount(dslDefaultConfig.getVisibleRowsCount());
        cdtmae.getSecondTick().setNumberOfHeaderLines(dslDefaultConfig.getNumberOfHeaderLines());

        return postCentreCreated == null ? cdtmae : postCentreCreated.apply(cdtmae);
    }

    /// Calculates preferred view index. It can be 1 (EGI) or other alternative view index (2, 3...).
    ///
    private static <T extends AbstractEntity<?>> Integer calculatePreferredViewIndex(final EntityCentreConfig<T> dslDefaultConfig) {
        final List<InsertionPointConfig> altViews = dslDefaultConfig.getInsertionPointConfigs().stream()
            .filter(ip -> ip.getInsertionPointAction().whereToInsertView.map(whereToInsert -> whereToInsert == ALTERNATIVE_VIEW).orElse(FALSE))
            .collect(toList());
        final long insPointCount = dslDefaultConfig.getInsertionPointConfigs().size() - altViews.size();
        final AtomicInteger preferredViewIndex = new AtomicInteger(!dslDefaultConfig.isEgiHidden() || insPointCount > 0 ? 1 : 2);
        for (int idx = 0; idx < altViews.size(); idx++) {
            if (altViews.get(idx).isPreferred()) {
                preferredViewIndex.set(2 + idx); // should be shifted by 2 to take into account EGI and selection criteria view indices
            }
        }
        return preferredViewIndex.get();
    }

    /// Creates default centre from Centre DSL configuration by adding calculated / custom props, applying selection crit defaults, EGI column widths / ordering etc.
    ///
    private ICentreDomainTreeManagerAndEnhancer createDefaultCentre0(final EntityCentreConfig<T> dslDefaultConfig, final EntityFactory entityFactory, final UnaryOperator<ICentreDomainTreeManagerAndEnhancer> postCentreCreated, final boolean userSpecific) {
        return createDefaultCentreFrom(dslDefaultConfig, entityFactory, postCentreCreated, userSpecific, entityType, miType, injector);
    }

    private static <T extends AbstractEntity<?>> void provideDefaultsFor(final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae, final EntityCentreConfig<T> dslDefaultConfig, final Class<T> entityType, final Injector injector) {
        final String property = treeName(dslProperty);
        final Class<?> managedType = cdtmae.getEnhancer().getManagedType(entityType);

        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, property);

        if (isCritOnlySingle(managedType, property)) {
            if (isEntityType(propertyType)) {
                provideDefaultsEntitySingle(() -> dslDefaultConfig.getDefaultSingleValuesForEntitySelectionCriteria(), () -> dslDefaultConfig.getDefaultSingleValueAssignersForEntitySelectionCriteria(), dslProperty, cdtmae, entityType, injector);
            } else if (isString(propertyType) || isRichText(propertyType)) {
                provideDefaultsSingle(() -> dslDefaultConfig.getDefaultSingleValuesForStringSelectionCriteria(), () -> dslDefaultConfig.getDefaultSingleValueAssignersForStringSelectionCriteria(), dslProperty, cdtmae, entityType, injector);
            } else if (isBoolean(propertyType)) {
                provideDefaultsSingle(() -> dslDefaultConfig.getDefaultSingleValuesForBooleanSelectionCriteria(), () -> dslDefaultConfig.getDefaultSingleValueAssignersForBooleanSelectionCriteria(), dslProperty, cdtmae, entityType, injector);
            } else if (isInteger(propertyType)) {
                provideDefaultsSingle(() -> dslDefaultConfig.getDefaultSingleValuesForIntegerSelectionCriteria(), () -> dslDefaultConfig.getDefaultSingleValueAssignersForIntegerSelectionCriteria(), dslProperty, cdtmae, entityType, injector);
            } else if (isRangeType(propertyType) && !EntityUtils.isDate(propertyType)) {
                provideDefaultsSingle(() -> dslDefaultConfig.getDefaultSingleValuesForBigDecimalAndMoneySelectionCriteria(), () -> dslDefaultConfig.getDefaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria(), dslProperty, cdtmae, entityType, injector);
            } else if (isDate(propertyType)) {
                provideDefaultsDateSingle(() -> dslDefaultConfig.getDefaultSingleValuesForDateSelectionCriteria(), () -> dslDefaultConfig.getDefaultSingleValueAssignersForDateSelectionCriteria(), dslProperty, cdtmae, entityType, injector);
            } else {
                throw new UnsupportedOperationException(String.format("The single-crit type [%s] is currently unsupported.", propertyType));
            }
        } else {
            if (isEntityType(propertyType) || isString(propertyType) || isRichText(propertyType)) {
                provideDefaultsEntityOrString(() -> dslDefaultConfig.getDefaultMultiValuesForEntityAndStringSelectionCriteria(), () -> dslDefaultConfig.getDefaultMultiValueAssignersForEntityAndStringSelectionCriteria(), dslProperty, cdtmae, isString(propertyType) || isRichText(propertyType), entityType, injector);
            } else if (isBoolean(propertyType)) {
                provideDefaultsBoolean(() -> dslDefaultConfig.getDefaultMultiValuesForBooleanSelectionCriteria(), () -> dslDefaultConfig.getDefaultMultiValueAssignersForBooleanSelectionCriteria(), dslProperty, cdtmae, entityType, injector);
            } else if (isInteger(propertyType)) {
                provideDefaultsRange(() -> dslDefaultConfig.getDefaultRangeValuesForIntegerSelectionCriteria(), () -> dslDefaultConfig.getDefaultRangeValueAssignersForIntegerSelectionCriteria(), dslProperty, cdtmae, entityType, injector);
            } else if (isRangeType(propertyType) && !isDate(propertyType)) {
                provideDefaultsRange(() -> dslDefaultConfig.getDefaultRangeValuesForBigDecimalAndMoneySelectionCriteria(), () -> dslDefaultConfig.getDefaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria(), dslProperty, cdtmae, entityType, injector);
            } else if (isDate(propertyType)) {
                provideDefaultsDateRange(() -> dslDefaultConfig.getDefaultRangeValuesForDateSelectionCriteria(), () -> dslDefaultConfig.getDefaultRangeValueAssignersForDateSelectionCriteria(), dslProperty, cdtmae, entityType, injector);
            } else {
                throw new UnsupportedOperationException(String.format("The multi-crit type [%s] is currently unsupported.", propertyType));
            }
        }
    }

    private static <T extends AbstractEntity<?>> void provideDefaultsEntityOrString(final Supplier<Optional<Map<String, MultiCritStringValueMnemonic>>> mnemonicSupplier, final Supplier<Optional<Map<String, Class<? extends IValueAssigner<MultiCritStringValueMnemonic, T>>>>> assignerSupplier, final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae, final boolean isString, final Class<T> entityType, final Injector injector) {
        final String property = treeName(dslProperty);
        if (mnemonicSupplier.get().isPresent() && mnemonicSupplier.get().get().get(dslProperty) != null) {
            provideMnemonicDefaultsEntityOrString(mnemonicSupplier.get().get().get(dslProperty), cdtmae, property, isString, entityType);
        } else {
            if (assignerSupplier.get().isPresent() && assignerSupplier.get().get().get(dslProperty) != null) {
                provideAssignerDefaultsEntityOrString(assignerSupplier.get().get().get(dslProperty), cdtmae, property, isString, entityType, injector);
            } else {
            }
        }
    }

    private static <T extends AbstractEntity<?>> void provideDefaultsBoolean(final Supplier<Optional<Map<String, MultiCritBooleanValueMnemonic>>> mnemonicSupplier, final Supplier<Optional<Map<String, Class<? extends IValueAssigner<MultiCritBooleanValueMnemonic, T>>>>> assignerSupplier, final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae, final Class<T> entityType, final Injector injector) {
        final String property = treeName(dslProperty);
        if (mnemonicSupplier.get().isPresent() && mnemonicSupplier.get().get().get(dslProperty) != null) {
            provideMnemonicDefaultsBoolean(mnemonicSupplier.get().get().get(dslProperty), cdtmae, property, entityType);
        } else {
            if (assignerSupplier.get().isPresent() && assignerSupplier.get().get().get(dslProperty) != null) {
                provideAssignerDefaultsBoolean(assignerSupplier.get().get().get(dslProperty), cdtmae, property, entityType, injector);
            } else {
            }
        }
    }

    private static <T extends AbstractEntity<?>> void provideDefaultsEntitySingle(final Supplier<Optional<Map<String, SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>>>> mnemonicSupplier, final Supplier<Optional<Map<String, Class<? extends IValueAssigner<? extends SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>, T>>>>> assignerSupplier, final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae, final Class<T> entityType, final Injector injector) {
        final String property = treeName(dslProperty);
        if (mnemonicSupplier.get().isPresent() && mnemonicSupplier.get().get().get(dslProperty) != null) {
            provideMnemonicDefaultsSingle(mnemonicSupplier.get().get().get(dslProperty), cdtmae, property, entityType);
        } else {
            if (assignerSupplier.get().isPresent() && assignerSupplier.get().get().get(dslProperty) != null) {
                provideAssignerDefaultsEntitySingle(assignerSupplier.get().get().get(dslProperty), cdtmae, property, entityType, injector);
            } else {
            }
        }
    }

    private static <T extends AbstractEntity<?>> void provideDefaultsDateSingle(final Supplier<Optional<Map<String, SingleCritDateValueMnemonic>>> mnemonicSupplier, final Supplier<Optional<Map<String, Class<? extends IValueAssigner<SingleCritDateValueMnemonic, T>>>>> assignerSupplier, final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae, final Class<T> entityType, final Injector injector) {
        final String property = treeName(dslProperty);
        if (mnemonicSupplier.get().isPresent() && mnemonicSupplier.get().get().get(dslProperty) != null) {
            provideMnemonicDefaultsDateSingle(mnemonicSupplier.get().get().get(dslProperty), cdtmae, property, entityType);
        } else {
            if (assignerSupplier.get().isPresent() && assignerSupplier.get().get().get(dslProperty) != null) {
                provideAssignerDefaultsDateSingle(assignerSupplier.get().get().get(dslProperty), cdtmae, property, entityType, injector);
            } else {
            }
        }
    }

    private static <T extends AbstractEntity<?>> void provideDefaultsDateRange(final Supplier<Optional<Map<String, RangeCritDateValueMnemonic>>> mnemonicSupplier, final Supplier<Optional<Map<String, Class<? extends IValueAssigner<RangeCritDateValueMnemonic, T>>>>> assignerSupplier, final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae, final Class<T> entityType, final Injector injector) {
        final String property = treeName(dslProperty);
        if (mnemonicSupplier.get().isPresent() && mnemonicSupplier.get().get().get(dslProperty) != null) {
            provideMnemonicDefaultsDateRange(mnemonicSupplier.get().get().get(dslProperty), cdtmae, property, entityType);
        } else {
            if (assignerSupplier.get().isPresent() && assignerSupplier.get().get().get(dslProperty) != null) {
                provideAssignerDefaultsDateRange(assignerSupplier.get().get().get(dslProperty), cdtmae, property, entityType, injector);
            } else {
            }
        }
    }

    private static <M, T extends AbstractEntity<?>> void provideDefaultsSingle(final Supplier<Optional<Map<String, SingleCritOtherValueMnemonic<M>>>> mnemonicSupplier, final Supplier<Optional<Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<M>, T>>>>> assignerSupplier, final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae, final Class<T> entityType, final Injector injector) {
        final String property = treeName(dslProperty);
        if (mnemonicSupplier.get().isPresent() && mnemonicSupplier.get().get().get(dslProperty) != null) {
            provideMnemonicDefaultsSingle(mnemonicSupplier.get().get().get(dslProperty), cdtmae, property, entityType);
        } else {
            if (assignerSupplier.get().isPresent() && assignerSupplier.get().get().get(dslProperty) != null) {
                provideAssignerDefaultsSingle(assignerSupplier.get().get().get(dslProperty), cdtmae, property, entityType, injector);
            } else {
            }
        }
    }

    private static <M, T extends AbstractEntity<?>> void provideDefaultsRange(final Supplier<Optional<Map<String, RangeCritOtherValueMnemonic<M>>>> mnemonicSupplier, final Supplier<Optional<Map<String, Class<? extends IValueAssigner<RangeCritOtherValueMnemonic<M>, T>>>>> assignerSupplier, final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae, final Class<T> entityType, final Injector injector) {
        final String property = treeName(dslProperty);
        if (mnemonicSupplier.get().isPresent() && mnemonicSupplier.get().get().get(dslProperty) != null) {
            provideMnemonicDefaultsRange(mnemonicSupplier.get().get().get(dslProperty), cdtmae, property, entityType);
        } else {
            if (assignerSupplier.get().isPresent() && assignerSupplier.get().get().get(dslProperty) != null) {
                provideAssignerDefaultsRange(assignerSupplier.get().get().get(dslProperty), cdtmae, property, entityType, injector);
            } else {
            }
        }
    }

    private static <T extends AbstractEntity<?>> void provideAssignerDefaultsEntityOrString(final Class<? extends IValueAssigner<MultiCritStringValueMnemonic, T>> assignerType, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property, final boolean isString, final Class<T> entityType, final Injector injector) {
        /* TODO at this stage there is no implementation for centre context processing -- master entity for dependent centres is the only applicable context -- will be implemented later */
        final Optional<MultiCritStringValueMnemonic> value = injector.getInstance(assignerType).getValue(null, dslName(property));
        if (value.isPresent()) {
            provideMnemonicDefaultsEntityOrString(value.get(), cdtmae, property, isString, entityType);
        }
    }

    private static <T extends AbstractEntity<?>> void provideAssignerDefaultsBoolean(final Class<? extends IValueAssigner<MultiCritBooleanValueMnemonic, T>> assignerType, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property, final Class<T> entityType, final Injector injector) {
        /* TODO at this stage there is no implementation for centre context processing -- master entity for dependent centres is the only applicable context -- will be implemented later */
        final Optional<MultiCritBooleanValueMnemonic> value = injector.getInstance(assignerType).getValue(null, dslName(property));
        if (value.isPresent()) {
            provideMnemonicDefaultsBoolean(value.get(), cdtmae, property, entityType);
        }
    }

    private static <T extends AbstractEntity<?>> void provideAssignerDefaultsDateSingle(final Class<? extends IValueAssigner<SingleCritDateValueMnemonic, T>> assignerType, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property, final Class<T> entityType, final Injector injector) {
        /* TODO at this stage there is no implementation for centre context processing -- master entity for dependent centres is the only applicable context -- will be implemented later */
        final Optional<SingleCritDateValueMnemonic> value = injector.getInstance(assignerType).getValue(null, dslName(property));
        if (value.isPresent()) {
            provideMnemonicDefaultsDateSingle(value.get(), cdtmae, property, entityType);
        }
    }

    private static <T extends AbstractEntity<?>> void provideAssignerDefaultsDateRange(final Class<? extends IValueAssigner<RangeCritDateValueMnemonic, T>> assignerType, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property, final Class<T> entityType, final Injector injector) {
        /* TODO at this stage there is no implementation for centre context processing -- master entity for dependent centres is the only applicable context -- will be implemented later */
        final Optional<RangeCritDateValueMnemonic> value = injector.getInstance(assignerType).getValue(null, dslName(property));
        if (value.isPresent()) {
            provideMnemonicDefaultsDateRange(value.get(), cdtmae, property, entityType);
        }
    }

    private static <M, T extends AbstractEntity<?>> void provideAssignerDefaultsSingle(final Class<? extends IValueAssigner<? extends SingleCritOtherValueMnemonic<M>, T>> assignerType, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property, final Class<T> entityType, final Injector injector) {
        /* TODO at this stage there is no implementation for centre context processing -- master entity for dependent centres is the only applicable context -- will be implemented later */
        final Optional<? extends SingleCritOtherValueMnemonic<M>> value = injector.getInstance(assignerType).getValue(null, dslName(property));
        if (value.isPresent()) {
            provideMnemonicDefaultsSingle(value.get(), cdtmae, property, entityType);
        }
    }

    private static <T extends AbstractEntity<?>> void provideAssignerDefaultsEntitySingle(final Class<? extends IValueAssigner<? extends SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>, T>> assignerType, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property, final Class<T> entityType, final Injector injector) {
        /* TODO at this stage there is no implementation for centre context processing -- master entity for dependent centres is the only applicable context -- will be implemented later */
        final Optional<SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>> value = (Optional<SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>>) injector.getInstance(assignerType).getValue(null, dslName(property));
        if (value.isPresent()) {
            provideMnemonicDefaultsSingle(value.get(), cdtmae, property, entityType);
        }
    }

    private static <M, T extends AbstractEntity<?>> void provideAssignerDefaultsRange(final Class<? extends IValueAssigner<? extends RangeCritOtherValueMnemonic<M>, T>> assignerType, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property, final Class<T> entityType, final Injector injector) {
        /* TODO at this stage there is no implementation for centre context processing -- master entity for dependent centres is the only applicable context -- will be implemented later */
        final Optional<? extends RangeCritOtherValueMnemonic<M>> value = injector.getInstance(assignerType).getValue(null, dslName(property));
        if (value.isPresent()) {
            provideMnemonicDefaultsRange(value.get(), cdtmae, property, entityType);
        }
    }

    private static <T extends AbstractEntity<?>> void provideMnemonicDefaultsEntityOrString(final MultiCritStringValueMnemonic mnemonic, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property, final boolean isString, final Class<T> entityType) {
        if (mnemonic.values.isPresent()) {
            cdtmae.getFirstTick().setValue(entityType, property, isString ? String.join(",", mnemonic.values.get()) : mnemonic.values.get());
        }
        if (mnemonic.checkForMissingValue) {
            cdtmae.getFirstTick().setOrNull(entityType, property, true);
        }
        if (mnemonic.negateCondition) {
            cdtmae.getFirstTick().setNot(entityType, property, true);
        }
    }

    private static <T extends AbstractEntity<?>> void provideMnemonicDefaultsBoolean(final MultiCritBooleanValueMnemonic mnemonic, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property, final Class<T> entityType) {
        if (mnemonic.isValue.isPresent()) {
            cdtmae.getFirstTick().setValue(entityType, property, mnemonic.isValue.get());
        }
        if (mnemonic.isNotValue.isPresent()) {
            cdtmae.getFirstTick().setValue2(entityType, property, mnemonic.isNotValue.get());
        }
        if (mnemonic.checkForMissingValue) {
            cdtmae.getFirstTick().setOrNull(entityType, property, true);
        }
        if (mnemonic.negateCondition) {
            cdtmae.getFirstTick().setNot(entityType, property, true);
        }
    }

    private static <T extends AbstractEntity<?>> void provideMnemonicDefaultsDateSingle(final SingleCritDateValueMnemonic mnemonic, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property, final Class<T> entityType) {
        if (mnemonic.value.isPresent()) {
            cdtmae.getFirstTick().setValue(entityType, property, mnemonic.value.get());
        }
        if (mnemonic.checkForMissingValue) {
            cdtmae.getFirstTick().setOrNull(entityType, property, true);
        }
        if (mnemonic.negateCondition) {
            cdtmae.getFirstTick().setNot(entityType, property, true);
        }

        // date mnemonics
        if (mnemonic.prefix.isPresent()) {
            cdtmae.getFirstTick().setDatePrefix(entityType, property, mnemonic.prefix.get());
        }
        if (mnemonic.period.isPresent()) {
            cdtmae.getFirstTick().setDateMnemonic(entityType, property, mnemonic.period.get());
        }
        if (mnemonic.beforeOrAfter.isPresent()) {
            cdtmae.getFirstTick().setAndBefore(entityType, property, DateRangeConditionEnum.BEFORE.equals(mnemonic.beforeOrAfter.get()) ? Boolean.TRUE : Boolean.FALSE);
        }

        // exclusiveness
        if (mnemonic.excludeFrom.isPresent()) {
            cdtmae.getFirstTick().setExclusive(entityType, property, mnemonic.excludeFrom.get());
        }
        if (mnemonic.excludeTo.isPresent()) {
            cdtmae.getFirstTick().setExclusive2(entityType, property, mnemonic.excludeTo.get());
        }
    }

    private static <T extends AbstractEntity<?>> void provideMnemonicDefaultsDateRange(final RangeCritDateValueMnemonic mnemonic, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property, final Class<T> entityType) {
        if (mnemonic.fromValue.isPresent()) {
            cdtmae.getFirstTick().setValue(entityType, property, mnemonic.fromValue.get());
        }
        if (mnemonic.toValue.isPresent()) {
            cdtmae.getFirstTick().setValue2(entityType, property, mnemonic.toValue.get());
        }
        if (mnemonic.checkForMissingValue) {
            cdtmae.getFirstTick().setOrNull(entityType, property, true);
        }
        if (mnemonic.negateCondition) {
            cdtmae.getFirstTick().setNot(entityType, property, true);
        }

        // date mnemonics
        if (mnemonic.prefix.isPresent()) {
            cdtmae.getFirstTick().setDatePrefix(entityType, property, mnemonic.prefix.get());
        }
        if (mnemonic.period.isPresent()) {
            cdtmae.getFirstTick().setDateMnemonic(entityType, property, mnemonic.period.get());
        }
        if (mnemonic.beforeOrAfter.isPresent()) {
            cdtmae.getFirstTick().setAndBefore(entityType, property, DateRangeConditionEnum.BEFORE.equals(mnemonic.beforeOrAfter.get()) ? Boolean.TRUE : Boolean.FALSE);
        }

        // exclusiveness
        if (mnemonic.excludeFrom.isPresent()) {
            cdtmae.getFirstTick().setExclusive(entityType, property, mnemonic.excludeFrom.get());
        }
        if (mnemonic.excludeTo.isPresent()) {
            cdtmae.getFirstTick().setExclusive2(entityType, property, mnemonic.excludeTo.get());
        }
    }

    private static <M, T extends AbstractEntity<?>> void provideMnemonicDefaultsSingle(final SingleCritOtherValueMnemonic<M> mnemonic, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property, final Class<T> entityType) {
        if (mnemonic.value.isPresent()) {
            cdtmae.getFirstTick().setValue(entityType, property, mnemonic.value.get());
        }
        if (mnemonic.checkForMissingValue) {
            cdtmae.getFirstTick().setOrNull(entityType, property, true);
        }
        if (mnemonic.negateCondition) {
            cdtmae.getFirstTick().setNot(entityType, property, true);
        }
    }

    private static <M, T extends AbstractEntity<?>> void provideMnemonicDefaultsRange(final RangeCritOtherValueMnemonic<M> mnemonic, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property, final Class<T> entityType) {
        if (mnemonic.fromValue.isPresent()) {
            cdtmae.getFirstTick().setValue(entityType, property, mnemonic.fromValue.get());
        }
        if (mnemonic.toValue.isPresent()) {
            cdtmae.getFirstTick().setValue2(entityType, property, mnemonic.toValue.get());
        }
        if (mnemonic.checkForMissingValue) {
            cdtmae.getFirstTick().setOrNull(entityType, property, true);
        }
        if (mnemonic.negateCondition) {
            cdtmae.getFirstTick().setNot(entityType, property, true);
        }

        if (mnemonic.excludeFrom.isPresent()) {
            cdtmae.getFirstTick().setExclusive(entityType, property, mnemonic.excludeFrom.get());
        }
        if (mnemonic.excludeTo.isPresent()) {
            cdtmae.getFirstTick().setExclusive2(entityType, property, mnemonic.excludeTo.get());
        }
    }

    /// Returns the menu item type for this [EntityCentre] instance.
    ///
    public Class<? extends MiWithConfigurationSupport<?>> getMenuItemType() {
        return miType;
    }

    /// Returns the entity type for which this entity centre was created.
    ///
    public Class<T> getEntityType() {
        return entityType;
    }

    /// Returns the entity centre name.
    ///
    public String getName() {
        return name;
    }

    /// Returns action configuration for concrete action kind and its number in that kind's space.
    ///
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        return dslDefaultConfig.actionConfig(actionKind, actionNumber);
    }

    /// Returns the value that indicates whether centre must run automatically or not.
    ///
    public boolean isRunAutomatically() {
        return dslDefaultConfig.isRunAutomatically();
    }

    /// Returns the value that indicates whether the centre should run automatically (by default; can be changed) and its behaviour is not customised: criteria should be cleared in the process (default configs) and config moved to empty default (save-as configs, embedded only)
    ///
    public boolean isRunAutomaticallyAndNotAllowCustomised() {
        return dslDefaultConfig.isRunAutomatically() && !allowCustomised();
    }

    private boolean allowCustomised() {
        return dslDefaultConfig.getRunAutomaticallyOptions().contains(ALLOW_CUSTOMISED);
    }

    /// Indicates whether centre should forcibly refresh the current page upon successful saving of a related entity.
    ///
    public boolean shouldEnforcePostSaveRefresh() {
        return dslDefaultConfig.shouldEnforcePostSaveRefresh();
    }

    /// Returns a class name of an SSE event source, associated with this entity centre. This event source is used at the client-side to subscribe to a postal event to refresh this entity centre.
    ///
    public Optional<Class<? extends IEventSource>> eventSourceClass() {
        return dslDefaultConfig.getEventSourceClass();
    }

    /// Returns the instance of rendering customiser for this entity centre.
    ///
    public Optional<IRenderingCustomiser<?>> getRenderingCustomiser() {
        if (dslDefaultConfig.getResultSetRenderingCustomiserType().isPresent()) {
            return Optional.of(injector.getInstance(dslDefaultConfig.getResultSetRenderingCustomiserType().get()));
        } else {
            return Optional.empty();
        }
    }

    /// Creates and returns instance of multi-action selector in case of primary multi-action specified in Centre DSL.
    /// Returns empty [Optional] otherwise.
    ///
    public Optional<? extends IEntityMultiActionSelector> createPrimaryActionSelector() {
        return dslDefaultConfig
            .getResultSetPrimaryEntityAction()
            .map(multiActionConfig -> injector.getInstance(multiActionConfig.actionSelectorClass()));
    }

    /// Creates and returns instance of multi-action selector in case of property multi-action specified in Centre DSL.
    /// Returns map between property names and property action selector.
    ///
    public Map<String, ? extends IEntityMultiActionSelector> createPropertyActionSelectors() {
        return dslDefaultConfig.getResultSetProperties().map(resultProps -> {
            return resultProps.stream()
                    .filter(resultProp -> resultProp.getPropAction().isPresent() && (resultProp.getPropAction().get().actions().size() > 0))
                    .map(resultProp -> t2(derivePropName(resultProp), injector.getInstance(resultProp.getPropAction().get().actionSelectorClass())))
                    .collect(toMap(tt -> tt._1, tt -> tt._2));
        }).orElse(new HashMap<>());
    }

    /// Creates instances of multi-action selectors in case of secondary multi-actions (or single actions) specified in Centre DSL.
    /// Returns empty [List] if there were no secondary multi-actions (or single actions) specified in Centre DSL.
    ///
    public List<? extends IEntityMultiActionSelector> createSecondaryActionSelectors() {
        return dslDefaultConfig
            .getResultSetSecondaryEntityActions()
            .stream().map(config -> injector.getInstance(config.actionSelectorClass())).collect(toList());
    }

    public Optional<IDynamicColumnBuilder<T>> getDynamicColumnBuilderFor(final ResultSetProp<T> resProp) {
        return resProp.dynamicColBuilderType.map(clazz -> injector.getInstance(clazz));
    }

    public List<ResultSetProp<T>> getDynamicProperties () {
        return dslDefaultConfig.getResultSetProperties()
                .orElse(new ArrayList<>()).stream()
                .filter(resProp -> resProp.dynamicColBuilderType.isPresent()).collect(Collectors.toList());
    }

    @Override
    public IRenderable buildFor() {
        return createRenderableRepresentation(getAssociatedEntityCentreManager());
    }

    private final ICentreDomainTreeManagerAndEnhancer getAssociatedEntityCentreManager() {
        final User user = getUser();
        if (user == null) {
            return createUserUnspecificDefaultCentre(dslDefaultConfig, injector.getInstance(EntityFactory.class), postCentreCreated);
        } else {
            return updateCentre(user, miType, FRESH_CENTRE_NAME, empty(), DESKTOP, webUiConfig, eccCompanion, mmiCompanion, userCompanion, companionFinder);
        }
    }

    public static String egiRepresentationFor(final Class<?> propertyType, final Optional<String> timeZone, final Optional<String> timePortionToDisplay) {
        final Class<?> type = DynamicEntityClassLoader.getOriginalType(propertyType);
        String typeRes = EntityUtils.isEntityType(type) ? type.getName() : (EntityUtils.isBoolean(type) ? "Boolean" : type.getSimpleName());
        if (Date.class.isAssignableFrom(type)) {
            typeRes += ":" + timeZone.orElse("");
            typeRes += ":" + timePortionToDisplay.orElse("");
        }
        return typeRes;
    }

    /// Returns default centre manager that was formed using DSL configuration and postCentreCreated hook.
    ///
    public ICentreDomainTreeManagerAndEnhancer createDefaultCentre() {
        return createDefaultCentre(dslDefaultConfig, injector.getInstance(EntityFactory.class), postCentreCreated);
    }

    private IRenderable createRenderableRepresentation(final ICentreDomainTreeManagerAndEnhancer centre) {

        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add("master/tg-entity-master");

        logger.debug("Initiating layout...");

        final FlexLayout layout = this.dslDefaultConfig.getSelectionCriteriaLayout();

        final DomElement editorContainer = layout.render().attr("context", "[[_currEntity]]");

        importPaths.add(layout.importPath());

        final Class<? extends AbstractEntity<?>> root = this.entityType;

        logger.debug("Initiating criteria widgets...");

        final List<AbstractCriterionWidget> criteriaWidgets = createCriteriaWidgets(centre, root);
        criteriaWidgets.forEach(widget -> {
            importPaths.add(widget.importPath());
            importPaths.addAll(widget.editorsImportPaths());
            editorContainer.add(widget.render());
        });

        final String prefix = ",\n";

        logger.debug("Initiating property columns...");

        final List<PropertyColumnElement> propertyColumns = new ArrayList<>();
        final Optional<List<ResultSetProp<T>>> resultProps = dslDefaultConfig.getResultSetProperties();
        final ListMultimap<String, SummaryPropDef> summaryProps = dslDefaultConfig.getSummaryExpressions();
        final Class<?> managedType = centre.getEnhancer().getManagedType(root);
        if (resultProps.isPresent()) {
            final AtomicInteger actionIndex = new AtomicInteger(0);
            for (final ResultSetProp<T> resultProp : resultProps.get()) {
                final String tooltipProp = resultProp.tooltipProp.isPresent() ? resultProp.tooltipProp.get() : null;
                final String resultPropName = derivePropName(resultProp);
                final boolean isEntityItself = "".equals(resultPropName); // empty property means "entity itself"
                final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, resultPropName);

                final List<FunctionalActionElement> actions =
                        resultProp.getPropAction()
                        .map(multiAction -> multiAction.actions()).orElse(new ArrayList<>()).stream()
                        .map(actionConfig -> new FunctionalActionElement(actionConfig, actionIndex.getAndIncrement(), resultPropName))
                        .collect(toList());

                final PropertyColumnElement el = new PropertyColumnElement(resultPropName,
                        resultProp.widget,
                        resultProp.dynamicColBuilderType.isPresent(),
                        !resultProp.propDef.isPresent(),
                        resultProp.width,
                        resultProp.dynamicColBuilderType.isPresent() ? 0 : centre.getSecondTick().getGrowFactor(root, resultPropName), // collectional dynamic columns are always unchecked -- skip getGrowFactor() invocation and use 0 as default grow factor
                        resultProp.wordWrap,
                        resultProp.isFlexible,
                        tooltipProp,
                        egiRepresentationFor(
                                propertyType,
                                Optional.ofNullable(EntityUtils.isDate(propertyType) ? DefaultValueContract.getTimeZone(managedType, resultPropName) : null),
                                Optional.ofNullable(EntityUtils.isDate(propertyType) ? DefaultValueContract.getTimePortionToDisplay(managedType, resultPropName) : null)),
                        CriteriaReflector.getCriteriaTitleAndDesc(managedType, resultPropName),
                        actions);
                if (summaryProps.containsKey(dslName(resultPropName))) {
                    final List<SummaryPropDef> summaries = summaryProps.get(dslName(resultPropName));
                    summaries.forEach(summary -> el.addSummary(summary.alias, PropertyTypeDeterminator.determinePropertyType(managedType, summary.alias), new Pair<>(summary.title, summary.desc)));
                }
                propertyColumns.add(el);
            }
        }

        logger.debug("Initiating prop actions...");

        final DomContainer egiColumns = new DomContainer();
        final DomContainer egiEditors = new DomContainer();
        final StringBuilder propActionsObject = new StringBuilder();
        propertyColumns.forEach(column -> {
            importPaths.add(column.importPath());
            column.widgetImportPath().ifPresent(path -> importPaths.add(path));
            if (column.hasSummary()) {
                importPaths.add(column.getSummary(0).importPath());
            }
            column.getActions().forEach(action -> {
                importPaths.add(action.importPath());
                propActionsObject.append(prefix + createActionObject(action));
            });
            egiColumns.add(column.render());
            column.renderWidget().ifPresent(widget -> egiEditors.add(widget));
        });

        logger.debug("Initiating top-level actions...");
        final Optional<List<Pair<EntityActionConfig, Optional<String>>>> topLevelActions = this.dslDefaultConfig.getTopLevelActions();

        final List<List<FunctionalActionElement>> actionGroups = new ArrayList<>();
        if (topLevelActions.isPresent()) {

            String currentGroup = null;
            for (int i = 0; i < topLevelActions.get().size(); i++) {
                final Pair<EntityActionConfig, Optional<String>> topLevelAction = topLevelActions.get().get(i);
                final String cg = getGroup(topLevelAction.getValue());
                if (!EntityUtils.equalsEx(cg, currentGroup)) {
                    actionGroups.add(new ArrayList<>());
                    currentGroup = cg;
                }
                addToLastGroup(actionGroups, topLevelAction.getKey(), i);
            }
        }

        logger.debug("Initiating functional actions...");
        final StringBuilder functionalActionsObjects = new StringBuilder();

        final DomElement functionalActionsDom = new DomContainer();

        for (int i = 0; i < actionGroups.size(); i++) {
            final DomElement groupElement = createActionGroupDom(i);
            for (final FunctionalActionElement el : actionGroups.get(i)) {
                importPaths.add(el.importPath());
                groupElement.add(el.render());
                functionalActionsObjects.append(prefix + createActionObject(el));
            }
            functionalActionsDom.add(groupElement);
        }

        logger.debug("Initiating primary actions...");
        //////////////////// Primary result-set action ////////////////////
        final Optional<EntityMultiActionConfig> resultSetPrimaryEntityAction = this.dslDefaultConfig.getResultSetPrimaryEntityAction();
        final DomContainer primaryActionDom = new DomContainer();
        final StringBuilder primaryActionObject = new StringBuilder();

        if (resultSetPrimaryEntityAction.isPresent()) {
            final FunctionalMultiActionElement el = new FunctionalMultiActionElement(resultSetPrimaryEntityAction.get(), 0, FunctionalActionKind.PRIMARY_RESULT_SET);

            importPaths.add(el.importPath());
            primaryActionDom.add(el.render().attr("slot", "primary-action").attr("hidden", null));
            primaryActionObject.append(prefix + el.createActionObject(importPaths));
        }
        ////////////////////Primary result-set action [END] //////////////

        logger.debug("Initiating front actions...");
        //////////////////// front actions ////////////////////
        final StringBuilder frontActionsObjects = new StringBuilder();
        final DomElement frontActionsDom = new DomElement("div").attr("selectable-elements-container", null).attr("slot", "custom-front-action").clazz("first-group");

        final List<EntityActionConfig> frontActions = this.dslDefaultConfig.getFrontActions();
        for (int actionIndex = 0; actionIndex < frontActions.size(); actionIndex++) {
            final FunctionalActionElement actionElement = new FunctionalActionElement(frontActions.get(actionIndex), actionIndex, FunctionalActionKind.FRONT);
            importPaths.add(actionElement.importPath());
            frontActionsDom.add(actionElement.render());
            frontActionsObjects.append(prefix + createActionObject(actionElement));
        }
        //////////////////// front actions (END) ////////////////////

        logger.debug("Initiating share actions...");
        //////////////////// share actions ////////////////////
        final StringBuilder shareActionsObjects = new StringBuilder();
        final DomContainer shareActionsDom = new DomContainer();

        final List<EntityActionConfig> shareActions = webUiConfig.centreConfigShareActions();
        for (int actionIndex = 0; actionIndex < shareActions.size(); actionIndex++) {
            final FunctionalActionElement actionElement = new FunctionalActionElement(shareActions.get(actionIndex), actionIndex, FunctionalActionKind.SHARE);
            importPaths.add(actionElement.importPath());
            shareActionsDom.add(actionElement.render());
            shareActionsObjects.append(prefix + createActionObject(actionElement));
        }
        //////////////////// share actions (END) ////////////////////

        logger.debug("Initiating secondary actions...");

        final DomContainer secondaryActionsDom = new DomContainer();
        final StringBuilder secondaryActionsObjects = new StringBuilder();
        final List<EntityMultiActionConfig> resultSetSecondaryEntityActions = this.dslDefaultConfig.getResultSetSecondaryEntityActions();
        if (!resultSetSecondaryEntityActions.isEmpty()) {
            int numberOfAction = 0;
            for (final EntityMultiActionConfig multiActionConfig: resultSetSecondaryEntityActions) {
                final FunctionalMultiActionElement el = new FunctionalMultiActionElement(multiActionConfig, numberOfAction, FunctionalActionKind.SECONDARY_RESULT_SET);
                importPaths.add(el.importPath());
                secondaryActionsDom.add(el.render().attr("slot", "secondary-action"));
                secondaryActionsObjects.append(prefix + el.createActionObject(importPaths));
                numberOfAction += multiActionConfig.actions().size();
            }
        }

        logger.debug("Initiating insertion point actions...");

        final List<InsertionPointBuilder> insertionPointActionsElements = new ArrayList<>();
        final List<InsertionPointConfig> insertionPointConfigs = this.dslDefaultConfig.getInsertionPointConfigs();
        for (int index = 0; index < insertionPointConfigs.size(); index++) {
            final InsertionPointBuilder el = new InsertionPointBuilder(insertionPointConfigs.get(index), index);
            insertionPointActionsElements.add(el);
        }

        final DomContainer insertionPointActionsDom = new DomContainer();
        final StringBuilder insertionPointActionsObjects = new StringBuilder();
        for (final InsertionPointBuilder el : insertionPointActionsElements) {
            importPaths.addAll(el.importPaths());
            insertionPointActionsDom.add(el.renderInsertionPointAction());
            insertionPointActionsObjects.append(prefix + el.code());
        }
        importPaths.add(dslDefaultConfig.getToolbarConfig().importPath());

        final List<String> toolbarCode = new ArrayList<>();
        toolbarCode.add(dslDefaultConfig.getToolbarConfig().code(entityType).toString());
        final List<String> toolbarStyles = new ArrayList<>();
        toolbarStyles.add(dslDefaultConfig.getToolbarConfig().styles().toString());

        final DomContainer topInsertionPointsDom = new DomContainer();
        final DomContainer leftInsertionPointsDom = new DomContainer();
        final DomContainer rightInsertionPointsDom = new DomContainer();
        final DomContainer bottomInsertionPointsDom = new DomContainer();
        final List<String> alternativeViewsDom = new ArrayList<>();
        //Alternative view actions should be generated into EGI's top action config list, that's why the order of alternative view actions should starts from the last index of EGI's top action + 1.
        final AtomicInteger alternativeViewActionOrder = new AtomicInteger(this.dslDefaultConfig.getTopLevelActions().map(actions -> actions.size()).orElse(0));
        for (final InsertionPointBuilder el : insertionPointActionsElements) {
            final DomElement insertionPoint = el.render();
            el.toolbar().ifPresent(toolbar -> {
                toolbarCode.add(toolbar.code(entityType).toString());
                toolbarStyles.add(toolbar.styles().toString());
            });
            if (el.whereToInsert() == InsertionPoints.TOP) {
                topInsertionPointsDom.add(insertionPoint);
            } else if (el.whereToInsert() == InsertionPoints.LEFT) {
                leftInsertionPointsDom.add(insertionPoint);
            } else if (el.whereToInsert() == InsertionPoints.RIGHT) {
                rightInsertionPointsDom.add(insertionPoint);
            } else if (el.whereToInsert() == InsertionPoints.BOTTOM) {
                bottomInsertionPointsDom.add(insertionPoint);
            } else if (el.whereToInsert() == InsertionPoints.ALTERNATIVE_VIEW) {
                final Optional<DomElement> switchButtons = switchViewButtons(insertionPointActionsElements, Optional.of(el));
                final Optional<DomElement> topActions = alternativeViewActions(el, importPaths, functionalActionsObjects, alternativeViewActionOrder);
                alternativeViewsDom.add(insertionPoint.toString()
                        .replace(SWITCH_VIEW_ACTION_DOM, switchButtons.map(domElem -> domElem.toString()).orElse(""))
                        .replace(EGI_FUNCTIONAL_ACTION_DOM, topActions.map(domElem -> domElem.toString()).orElse("")));
            } else {
                throw new IllegalArgumentException("Unexpected insertion point type.");
            }
        }

        final Optional<DomElement> egiSwitchViewButtons = switchViewButtons(insertionPointActionsElements, Optional.empty());

        //Generating shortcuts for EGI
        final StringBuilder shortcuts = new StringBuilder();

        for (final String shortcut : dslDefaultConfig.getToolbarConfig().getAvailableShortcuts()) {
            shortcuts.append(shortcut + " ");
        }

        if (topLevelActions.isPresent()) {

            for (int i = 0; i < topLevelActions.get().size(); i++) {
                final Pair<EntityActionConfig, Optional<String>> topLevelAction = topLevelActions.get().get(i);
                final EntityActionConfig config = topLevelAction.getKey();
                if (config.shortcut.isPresent()) {
                    shortcuts.append(config.shortcut.get() + " ");
                }
            }
        }

        ///////////////////////////////////////
        final String frontActionString = frontActionsObjects.toString();
        final String shareActionsString = shareActionsObjects.toString();
        final String funcActionString = functionalActionsObjects.toString();
        final String secondaryActionString = secondaryActionsObjects.toString();
        final String insertionPointActionsString = insertionPointActionsObjects.toString();
        final String primaryActionObjectString = primaryActionObject.toString();
        final String propActionsString = propActionsObject.toString();
        final Pair<String, String> gridLayoutConfig = generateGridLayoutConfig();
        final int prefixLength = prefix.length();
        logger.debug("Initiating template...");
        final String text = ResourceLoader.getText("ua/com/fielden/platform/web/centre/tg-entity-centre-template.js");
        logger.debug("Replacing some parts...");
        final String entityCentreStr = text.
                replace(IMPORTS, createImports(importPaths) + customImports.map(ci -> ci.toString()).orElse("")).
                replace(EGI_LAYOUT, gridLayoutConfig.getKey()).
                replace(FULL_ENTITY_TYPE, entityType.getName()).
                replace(MI_TYPE, flattenedNameOf(miType)).
                //egi related properties
                replace(EGI_SHORTCUTS, shortcuts).
                replace(EGI_VIEW_ICON, dslDefaultConfig.getGridViewIcon()).
                replace(EGI_VIEW_ICON_STYLE, dslDefaultConfig.getGridViewIconStyle()).
                replace(EGI_HIDDEN, HIDDEN.eval(dslDefaultConfig.isEgiHidden())).
                replace(EGI_DRAGGABLE, DRAGGABLE.eval(dslDefaultConfig.isDraggable())).
                replace(EGI_TOOLBAR_VISIBLE, TOOLBAR_VISIBLE.eval(!dslDefaultConfig.shouldHideToolbar())).
                replace(EGI_CHECKBOX_VISIBILITY, CHECKBOX_VISIBLE.eval(!dslDefaultConfig.shouldHideCheckboxes())).
                replace(EGI_DRAG_ANCHOR_FIXED, DRAG_ANCHOR_FIXED.eval(dslDefaultConfig.getScrollConfig().isDragAnchorFixed())).
                replace(EGI_CHECKBOX_FIXED, CHECKBOX_FIXED.eval(dslDefaultConfig.getScrollConfig().isCheckboxesFixed())).
                replace(EGI_CHECKBOX_WITH_PRIMARY_ACTION_FIXED, CHECKBOX_WITH_PRIMARY_ACTION_FIXED.eval(dslDefaultConfig.getScrollConfig().isCheckboxesWithPrimaryActionsFixed())).
                replace(EGI_NUM_OF_FIXED_COLUMNS, Integer.toString(dslDefaultConfig.getScrollConfig().getNumberOfFixedColumns())).
                replace(EGI_SECONDARY_ACTION_FIXED, SECONDARY_ACTION_FIXED.eval(dslDefaultConfig.getScrollConfig().isSecondaryActionsFixed())).
                replace(EGI_HEADER_FIXED, HEADER_FIXED.eval(dslDefaultConfig.getScrollConfig().isHeaderFixed())).
                replace(EGI_SUMMARY_FIXED, SUMMARY_FIXED.eval(dslDefaultConfig.getScrollConfig().isSummaryFixed())).
                replace(EGI_HEIGHT, dslDefaultConfig.getEgiHeight()).
                replace(EGI_ROW_HEIGHT, dslDefaultConfig.getRowHeight()).
                replace(EGI_FIT_TO_HEIGHT, FIT_TO_HEIGHT.eval(dslDefaultConfig.isFitToHeight())).
                ///////////////////////
                replace(TOOLBAR_DOM, dslDefaultConfig.getToolbarConfig().render().toString()
                        .replace(EGI_FUNCTIONAL_ACTION_DOM, functionalActionsDom.toString())
                        .replace(SWITCH_VIEW_ACTION_DOM, egiSwitchViewButtons.map(domEl -> domEl.toString()).orElse(""))).
                replace(TOOLBAR_JS, join(toolbarCode, "\n")).
                replace(TOOLBAR_STYLES, join(toolbarStyles, "\n")).
                replace(FULL_MI_TYPE, miType.getName()).
                replace(QUERY_ENHANCER_CONFIG, queryEnhancerContextConfigString()).
                replace(CRITERIA_DOM, editorContainer.toString()).
                replace(EGI_DOM, egiColumns.toString()).
                replace(EGI_EDITORS, egiEditors.toString()).
                replace(FRONT_ACTIONS_DOM, frontActionsDom.toString()).
                replace(FRONT_ACTIONS, frontActionString.length() > prefixLength ? frontActionString.substring(prefixLength): frontActionString).
                replace(SHARE_ACTIONS_DOM, shareActionsDom.toString()).
                replace(SHARE_ACTIONS, shareActionsString.length() > prefixLength ? shareActionsString.substring(prefixLength): shareActionsString).
                replace(EGI_ACTIONS, funcActionString.length() > prefixLength ? funcActionString.substring(prefixLength) : funcActionString).
                replace(EGI_SECONDARY_ACTIONS, secondaryActionString.length() > prefixLength ? secondaryActionString.substring(prefixLength) : secondaryActionString).
                replace(INSERTION_POINT_ACTIONS, insertionPointActionsString.length() > prefixLength ? insertionPointActionsString.substring(prefixLength)
                        : insertionPointActionsString).
                replace(EGI_PRIMARY_ACTION, primaryActionObjectString.length() > prefixLength ? primaryActionObjectString.substring(prefixLength)
                        : primaryActionObjectString).
                replace(EGI_PROPERTY_ACTIONS, propActionsString.length() > prefixLength ? propActionsString.substring(prefixLength)
                        : propActionsString).
                replace(SELECTION_CRITERIA_LAYOUT_CONFIG, layout.code().toString()).
                replace(EGI_LAYOUT_CONFIG, gridLayoutConfig.getValue()).
                replace(EGI_PRIMARY_ACTION_DOM, primaryActionDom.toString()).
                replace(EGI_SECONDARY_ACTIONS_DOM, secondaryActionsDom.toString()).
                replace(INSERTION_POINT_ACTIONS_DOM, insertionPointActionsDom.toString()).
                replace(LEFT_INSERTION_POINT_DOM, leftInsertionPointsDom.toString()).
                replace(RIGHT_INSERTION_POINT_DOM, rightInsertionPointsDom.toString()).
                replace(LEFT_SPLITTER_POSITION, dslDefaultConfig.getLeftSplitterPosition().map(pos -> format("left-splitter-position=\"%s\"", pos/100.0)).orElse("")).
                replace(RIGHT_SPLITTER_POSITION, dslDefaultConfig.getRightSplitterPosition().map(pos -> format("right-splitter-position=\"%s\"", pos/100.0)).orElse("")).
                replace(INSERTION_POINT_CUSTOM_LAYOUT_ENABLED, dslDefaultConfig.isInsertionPointCustomLayoutEnabled() ? "insertion-point-custom-layout-enabled" : "").
                replace(TOP_INSERTION_POINT_DOM, topInsertionPointsDom.toString()).
                replace(BOTTOM_INSERTION_POINT_DOM, bottomInsertionPointsDom.toString()).
                replace(ALTERNATIVE_VIEW_INSERTION_POINT_DOM, join(alternativeViewsDom, "\n")).
                replace(CENTRE_RETRIEVE_ALL_OPTION, Boolean.toString(dslDefaultConfig.shouldRetrieveAll())).
                replace(SSE_REFRESH_COUNTDOWN, dslDefaultConfig.getRefreshCountdown().map(seconds -> format("self.countdown=%s;", seconds)).orElse("")).
                replace("@" + ALLOW_CUSTOMISED.name(), allowCustomised() ? "\nself.allowCustomised = true;" : "").
                replace(CENTRE_SCROLL, dslDefaultConfig.isLockScrollingForInsertionPoints() ? "centre-scroll" : "").
                replace(READY_CUSTOM_CODE, customCode.map(code -> code.toString()).orElse("")).
                replace(ATTACHED_CUSTOM_CODE, customCodeOnAttach.map(code -> code.toString()).orElse(""));
        logger.debug("Finishing...");
        final IRenderable representation = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityCentreStr);
            }
        };
        logger.debug("Done.");
        return representation;
    }

   /// Generates DOM for alternative view actions. Also updates action's order, import path and action object.
   ///
    private Optional<DomElement> alternativeViewActions(final InsertionPointBuilder el, final LinkedHashSet<String> importPaths, final StringBuilder functionalActionsObjects, final AtomicInteger alternativeViewActionOrder) {
        if (!el.getActions().isEmpty()) {
            final DomElement domContainer = new DomContainer();
            for (final EntityActionConfig actionConfig: el.getActions()) {
                final FunctionalActionElement funcAction = new FunctionalActionElement(actionConfig, alternativeViewActionOrder.getAndIncrement(), FunctionalActionKind.TOP_LEVEL);
                importPaths.add(funcAction.importPath());
                domContainer.add(funcAction.render());
                functionalActionsObjects.append(",\n" + createActionObject(funcAction));
            }
            return of(domContainer);
        }
        return empty();
    }

    /// Creates dropdown button with selected insertionPoint, that allows to switch between other alternative views including EGI.
    /// It might return empty optional if this centre doesn't have enough alternative views to switch between
    /// (i.e. there is only EGI or only one alternative view with hidden EGI and without other insertion points)
    ///
    private Optional<DomElement> switchViewButtons(final List<InsertionPointBuilder> insertionPointActionsElements, final Optional<InsertionPointBuilder> insertionPoint) {
        final List<InsertionPointBuilder> altViews = insertionPointActionsElements.stream().filter(insPoint -> insPoint.whereToInsert() == ALTERNATIVE_VIEW).collect(toList());
        final long otherViewCount = insertionPointActionsElements.size() - altViews.size();//Calculate the number of insertion points those are not an alternative view.
        final long allViewCount = altViews.size() + (!dslDefaultConfig.isEgiHidden() || otherViewCount > 0 ? 1 : 0);//Calculate the number of views to switch between.
        if (allViewCount > 1) {//If there are more than one available views (EGI and alternative views) then create switch view button
            if (!insertionPoint.isPresent()) {//Create switch view button for EGI view.
                return of(selectView(1, dslDefaultConfig.getToolbarConfig().getSwitchViewButtonWidth()));
            } else {//Create switch view button for alternative view.
                return of(selectView(altViews.indexOf(insertionPoint.get()) + 2, insertionPoint.get().toolbar().map(toolbar -> toolbar.getSwitchViewButtonWidth()).orElse(0)));
            }
        }
        return empty(); //Otherwise return empty switch view button indicating that there are no enough available views to switch between
    }

    private DomElement createActionGroupDom(final int groupIndex) {
        return new DomElement("div").attr("selectable-elements-container", null).attr("slot", "entity-specific-action").clazz("entity-specific-action", groupIndex == 0 ? "first-group" : "group");
    }

    /// Calculates the relative grow factor for all columns.
    ///
    private static <T extends AbstractEntity<?>> Map<String, Integer> calculateGrowFactors(final List<ResultSetProp<T>> propertyColumns) {
        // Searching for the minimal column width which are not flexible and their width is greater than 0.
        final int minWidth = propertyColumns.stream()
                .filter(column -> column.isFlexible && column.width > 0)
                .reduce(Integer.MAX_VALUE,
                        (min, column) -> min > column.width ? column.width : min,
                        (min1, min2) -> min1 < min2 ? min1 : min2);
        // Map each resultSetProp which is not flexible and has width greater than 0 to it's grow factor.
        return propertyColumns.stream()
                .filter(column -> column.isFlexible && column.width > 0)
                .collect(Collectors.toMap(
                        column -> derivePropName(column),
                        column -> Math.round((float) column.width / minWidth)));
    }

    private Pair<String, String> generateGridLayoutConfig() {
        final StringBuilder resultsetLayoutJs = new StringBuilder();
        final StringBuilder resultsetLayoutHtml = new StringBuilder();
        final FlexLayout collapseLayout = dslDefaultConfig.getResultsetCollapsedCardLayout();
        final FlexLayout expandLayout = dslDefaultConfig.getResultsetExpansionCardLayout();
        final FlexLayout summaryLayout = dslDefaultConfig.getResultsetSummaryCardLayout();

        final StringBuilder shortLayout = new StringBuilder();
        if (collapseLayout.hasLayoutFor(Device.DESKTOP, null)) {
            shortLayout.append("desktop: " + collapseLayout.getLayout(Device.DESKTOP, null).get());
        }
        if (collapseLayout.hasLayoutFor(Device.TABLET, null)) {
            shortLayout.append((shortLayout.length() > 0 ? ",\n" : "") + "tablet: " + collapseLayout.getLayout(Device.TABLET, null).get());
        }
        if (collapseLayout.hasLayoutFor(Device.MOBILE, null)) {
            shortLayout.append((shortLayout.length() > 0 ? ",\n" : "") + "mobile: " + collapseLayout.getLayout(Device.MOBILE, null).get());
        }
        if (shortLayout.length() > 0) {
            resultsetLayoutJs.append("self.gridShortLayout={\n" + shortLayout.toString() + "\n};");
            resultsetLayoutHtml.append("short-layout='[[gridShortLayout]]'");
        }

        final StringBuilder longLayout = new StringBuilder();
        if (expandLayout.hasLayoutFor(Device.DESKTOP, null)) {
            longLayout.append("desktop: " + expandLayout.getLayout(Device.DESKTOP, null).get());
        }
        if (expandLayout.hasLayoutFor(Device.TABLET, null)) {
            longLayout.append((longLayout.length() > 0 ? ",\n" : "") + "tablet: " + expandLayout.getLayout(Device.TABLET, null).get());
        }
        if (expandLayout.hasLayoutFor(Device.MOBILE, null)) {
            longLayout.append((longLayout.length() > 0 ? ",\n" : "") + "mobile: " + expandLayout.getLayout(Device.MOBILE, null).get());
        }
        if (longLayout.length() > 0) {
            resultsetLayoutJs.append("self.gridLongLayout={\n" + longLayout.toString() + "\n};");
            resultsetLayoutHtml.append(" long-layout='[[gridLongLayout]]'");
        }

        final StringBuilder gridSummaryLayout = new StringBuilder();
        if (summaryLayout.hasLayoutFor(Device.DESKTOP, null)) {
            gridSummaryLayout.append("desktop: " + summaryLayout.getLayout(Device.DESKTOP, null).get());
        }
        if (summaryLayout.hasLayoutFor(Device.TABLET, null)) {
            gridSummaryLayout.append((gridSummaryLayout.length() > 0 ? ",\n" : "") + "tablet: " + summaryLayout.getLayout(Device.TABLET, null).get());
        }
        if (summaryLayout.hasLayoutFor(Device.MOBILE, null)) {
            gridSummaryLayout.append((gridSummaryLayout.length() > 0 ? ",\n" : "") + "mobile: " + summaryLayout.getLayout(Device.MOBILE, null).get());
        }
        if (gridSummaryLayout.length() > 0) {
            resultsetLayoutJs.append("self.summaryLayout={\n" + gridSummaryLayout.toString() + "\n};");
            resultsetLayoutHtml.append(" summary-layout='[[summaryLayout]]'");
        }
        return new Pair<>(resultsetLayoutHtml.toString(), resultsetLayoutJs.toString());
    }

    /// Returns user for this concrete thread (the user has been populated through the Web UI authentication mechanism -- see DefaultWebResourceGuard).
    ///
    private User getUser() {
        return injector.getInstance(IUserProvider.class).getUser();
    }

    private String queryEnhancerContextConfigString() {
        final StringBuilder sb = new StringBuilder();

        if (dslDefaultConfig.getQueryEnhancerConfig().isPresent() && dslDefaultConfig.getQueryEnhancerConfig().get().getValue().isPresent()) {
            final CentreContextConfig centreContextConfig = dslDefaultConfig.getQueryEnhancerConfig().get().getValue().get();
            if (centreContextConfig.withSelectionCrit) {
                // disregarded -- sends every time, because the selection criteria is needed for running the centre query
            }
            sb.append("require-selected-entities=\"" + (centreContextConfig.withCurrentEtity ? "ONE" : (centreContextConfig.withAllSelectedEntities ? "ALL" : "NONE")) + "\" ");
            sb.append("require-master-entity=\"" + (centreContextConfig.withMasterEntity ? "true" : "false") + "\"");
        } else {
            sb.append("require-selected-entities=\"NONE\" ");
            sb.append("require-master-entity=\"false\"");
        }

        return sb.toString();
    }

    /// Creates the widgets for criteria.
    ///
    private List<AbstractCriterionWidget> createCriteriaWidgets(final ICentreDomainTreeManagerAndEnhancer centre, final Class<? extends AbstractEntity<?>> root) {
        final Class<?> managedType = centre.getEnhancer().getManagedType(root);
        final List<AbstractCriterionWidget> criteriaWidgets = new ArrayList<>();
        for (final String critProp : centre.getFirstTick().checkedProperties(root)) {
            if (!AbstractDomainTree.isPlaceholder(critProp)) {
                final boolean isEntityItself = "".equals(critProp); // empty property means "entity itself"
                final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, critProp);

                final AbstractCriterionWidget criterionWidget;
                if (AbstractDomainTree.isCritOnlySingle(managedType, critProp)) {
                    if (EntityUtils.isEntityType(propertyType)) {
                        final List<Pair<String, Boolean>> additionalProps = dslDefaultConfig.getAdditionalPropsForAutocompleter(critProp);
                        criterionWidget = new EntitySingleCriterionWidget(root, managedType, critProp, additionalProps, getCentreContextConfigFor(critProp));
                    } else if (EntityUtils.isString(propertyType) || EntityUtils.isRichText(propertyType)) {
                        criterionWidget = new StringSingleCriterionWidget(root, managedType, critProp);
                    } else if (EntityUtils.isBoolean(propertyType)) {
                        criterionWidget = new BooleanSingleCriterionWidget(root, managedType, critProp);
                    } else if (Integer.class.isAssignableFrom(propertyType) || Long.class.isAssignableFrom(propertyType)) {
                        criterionWidget = new IntegerSingleCriterionWidget(root, managedType, critProp);
                    } else if (BigDecimal.class.isAssignableFrom(propertyType)) {
                        criterionWidget = new DecimalSingleCriterionWidget(root, managedType, critProp);
                    } else if (Money.class.isAssignableFrom(propertyType)) {
                        criterionWidget = new MoneySingleCriterionWidget(root, managedType, critProp);
                    } else if (EntityUtils.isDate(propertyType)) {
                        criterionWidget = new DateSingleCriterionWidget(root, managedType, critProp);
                    } else {
                        throw new UnsupportedOperationException(String.format("The crit-only single editor type [%s] is currently unsupported.", propertyType));
                    }
                } else {
                    if (EntityUtils.isEntityType(propertyType)) {
                        final List<Pair<String, Boolean>> additionalProps = dslDefaultConfig.getAdditionalPropsForAutocompleter(critProp);
                        criterionWidget = new EntityCriterionWidget(root, managedType, critProp, additionalProps, getCentreContextConfigFor(critProp));
                    } else if (EntityUtils.isString(propertyType) && dslDefaultConfig.getProvidedTypeForAutocompletedSelectionCriterion(critProp).isPresent()) {
                        final List<Pair<String, Boolean>> additionalProps = dslDefaultConfig.getAdditionalPropsForAutocompleter(critProp);
                        criterionWidget = new EntityStringCriterionWidget(root, managedType, critProp, dslDefaultConfig.getProvidedTypeForAutocompletedSelectionCriterion(critProp).get(), additionalProps, getCentreContextConfigFor(critProp));
                    } else if (EntityUtils.isString(propertyType) || EntityUtils.isRichText(propertyType)) {
                        criterionWidget = new StringCriterionWidget(root, managedType, critProp);
                    } else if (EntityUtils.isBoolean(propertyType)) {
                        criterionWidget = new BooleanCriterionWidget(root, managedType, critProp);
                    } else if (Integer.class.isAssignableFrom(propertyType) || Long.class.isAssignableFrom(propertyType)) {
                        criterionWidget = new IntegerCriterionWidget(root, managedType, critProp);
                    } else if (BigDecimal.class.isAssignableFrom(propertyType)) { // TODO do not forget about Money later (after Money widget will be available)
                        criterionWidget = new DecimalCriterionWidget(root, managedType, critProp);
                    } else if (Money.class.isAssignableFrom(propertyType)) {
                        criterionWidget = new MoneyCriterionWidget(root, managedType, critProp);
                    } else if (EntityUtils.isDate(propertyType)) {
                        criterionWidget = new DateCriterionWidget(root, managedType, critProp);
                    } else {
                        throw new UnsupportedOperationException(String.format("The multi / range editor type [%s] is currently unsupported.", propertyType));
                    }
                }
                criteriaWidgets.add(criterionWidget);
            }
        }
        return criteriaWidgets;
    }

    private void addToLastGroup(final List<List<FunctionalActionElement>> actionGroups, final EntityActionConfig actionConfig, final int i) {
        if (actionGroups.isEmpty()) {
            actionGroups.add(new ArrayList<>());
        }
        final FunctionalActionElement el = new FunctionalActionElement(actionConfig, i, FunctionalActionKind.TOP_LEVEL);
        actionGroups.get(actionGroups.size() - 1).add(el);
    }

    private String getGroup(final Optional<String> groupIfAny) {
        return groupIfAny.isPresent() ? groupIfAny.get() : null;
    }

    private String createActionObject(final FunctionalActionElement element) {
        return element.createActionObject();
    }

    private CentreContextConfig getCentreContextConfigFor(final String critProp) {
        final String dslProp = dslName(critProp);
        return dslDefaultConfig.getValueMatchersForSelectionCriteria().map(m -> m.get(dslProp)).flatMap(t3 -> t3._2).orElse(defaultCentreContextConfig);
    }

    /// Creates value matcher instance with its context configuration; additionally returns original property with its type.
    ///
    public <V extends AbstractEntity<?>> T3<IValueMatcherWithCentreContext<V>, Optional<CentreContextConfig>, T2<String, Class<V>>> createValueMatcherAndContextConfig(final Class<? extends AbstractEntity<?>> criteriaType, final String criterionPropertyName) {
        final String originalPropertyName = getOriginalPropertyName(criteriaType, criterionPropertyName);
        final Class<V> propType = dslDefaultConfig.getProvidedTypeForAutocompletedSelectionCriterion(originalPropertyName)
                .map(propertyType -> (Class<V>) propertyType)
                .orElseGet(() -> (Class<V>) ("".equals(originalPropertyName) ? getOriginalType(criteriaType) : determinePropertyType(getOriginalType(criteriaType), originalPropertyName)));

        final boolean isPropDescriptor = isPropertyDescriptor(propType);
        final T3<IValueMatcherWithCentreContext<V>, Optional<CentreContextConfig>, T2<String, Class<V>>> matcherAndConfigAndPropWithType =
            dslDefaultConfig.getValueMatchersForSelectionCriteria() // take all matchers
            .map(matchers -> matchers.get(dslName(originalPropertyName))) // choose single matcher with concrete property name
            .map(customMatcherAndConfig -> t3((IValueMatcherWithCentreContext<V>) injector.getInstance(customMatcherAndConfig._1), customMatcherAndConfig._2, t2(originalPropertyName, propType))) // instantiate the matcher: [matcherType; config] => [matcherInstance; config]
            .orElseGet(() -> t3( // if no custom matcher was created then create default matcher
                isPropDescriptor
                    ? (IValueMatcherWithCentreContext<V>) new FallbackPropertyDescriptorMatcherWithCentreContext<>((Class<AbstractEntity<?>>) getPropertyAnnotation(IsProperty.class, getOriginalType(criteriaType), originalPropertyName).value())
                    : new FallbackValueMatcherWithCentreContext<>(companionFinder.find(propType)),
                empty(),
                t2(originalPropertyName, propType)
            ));

        // provide fetch model for created matcher
        if (!isPropDescriptor) {
            matcherAndConfigAndPropWithType._1.setFetch(createFetchModelForAutocompleter(originalPropertyName, propType));
        }
        return matcherAndConfigAndPropWithType;
    }

    /// Creates fetch model for entity-typed criteria autocompleted values. Fetches key and description complemented with additional properties specified in Centre DSL configuration.
    ///
    private <V extends AbstractEntity<?>> fetch<V> createFetchModelForAutocompleter(final String originalPropertyName, final Class<V> propType) {
        final Set<String> nonDefaultAdditionalProperties = dslDefaultConfig.getAdditionalPropsForAutocompleter(originalPropertyName).stream().map(Pair::getKey).collect(toSet());
        final Set<String> additionalProperties = nonDefaultAdditionalProperties.isEmpty() ? createDefaultAdditionalProps(propType).keySet() : nonDefaultAdditionalProperties;
        return createFetchModelForAutocompleterFrom(propType, additionalProperties);
    }

    /// Creates lean fetch model for autocompleted values with deep keys for entity itself and deep keys for every `additionalProperties`.
    ///
    /// Deep keys are needed for conversion of entity itself and its additional properties to string in client application.
    ///
    /// Includes 'active' property for activatable `propType`.
    ///
    public static <V extends AbstractEntity<?>> fetch<V> createFetchModelForAutocompleterFrom(final Class<V> propType, final Set<String> additionalProperties) {
        // Always include 'active' property to render inactive activatables as grayed-out in client application.
        // Take into account union-typed activatables too.
        return concat(additionalProperties.stream(),
                      isActivatableEntityType(propType)
                              ? Stream.of(ACTIVE)
                              : isUnionEntityType(propType) ? streamUnionSubProperties((Class<? extends AbstractUnionEntity>) propType, ACTIVE) : Stream.of())
                .reduce(fetchNone(propType),
                        (fp, additionalProp) -> fp.addPropWithKeys(additionalProp, true), // adding deep keys [and first-level 'desc' property, if exists] for additional [dot-notated] property
                        (fp1, fp2) -> {throw new UnsupportedOperationException("Combining is not applicable here.");}
                ).addPropWithKeys("", false) // adding deep keys for entity itself (no 'desc' property is required, it should be explicitly added by withProps() API or otherwise it will be in default additional properties)
                .fetchModel();
    }

    public Optional<Class<? extends ICustomPropsAssignmentHandler>> getCustomPropertiesAsignmentHandler() {
        return dslDefaultConfig.getResultSetCustomPropAssignmentHandlerType();
    }

    public Optional<List<ResultSetProp<T>>> getCustomPropertiesDefinitions() {
        return dslDefaultConfig.getResultSetProperties();
    }

    public ICustomPropsAssignmentHandler createAssignmentHandlerInstance(final Class<? extends ICustomPropsAssignmentHandler> assignmentHandlerType) {
        return injector.getInstance(assignmentHandlerType);
    }

    public Optional<IFetchProvider<T>> getAdditionalFetchProvider() {
        return dslDefaultConfig.getFetchProvider();
    }

    /// Returns fetch provider consisting only of 'tooltip properties': properties that are used as tooltips for other properties.
    ///
    public Optional<IFetchProvider<T>> getAdditionalFetchProviderForTooltipProperties() {
        final Set<String> tooltipProps = new LinkedHashSet<>();
        final Optional<List<ResultSetProp<T>>> resultSetProps = dslDefaultConfig.getResultSetProperties();
        resultSetProps.ifPresent(resultProps ->
            resultProps.stream().forEach(property -> {
                if (property.tooltipProp.isPresent()) {
                    tooltipProps.add(property.tooltipProp.get());
                }
            }));
        return tooltipProps.isEmpty() ? Optional.empty() : Optional.of(EntityUtils.fetchNotInstrumented(entityType).with(tooltipProps));
    }

    public Optional<Pair<IQueryEnhancer<T>, Optional<CentreContextConfig>>> getQueryEnhancerConfig() {
        final Optional<Pair<Class<? extends IQueryEnhancer<T>>, Optional<CentreContextConfig>>> queryEnhancerConfig = dslDefaultConfig.getQueryEnhancerConfig();
        if (queryEnhancerConfig.isPresent()) {
            final Class<? extends IQueryEnhancer<T>> queryEnhancerType = queryEnhancerConfig.get().getKey();
            return Optional.of(new Pair<>(injector.getInstance(queryEnhancerType), queryEnhancerConfig.get().getValue()));
        } else {
            return Optional.empty();
        }
    }

    public Optional<Pair<Class<?>, Class<?>>> getGeneratorTypes() {
        return dslDefaultConfig.getGeneratorTypes();
    }

    /// Creates generic [IGenerator] instance from injector based on assumption that `generatorType` is of appropriate type (such checks are performed on API implementation level).
    ///
    @SuppressWarnings("rawtypes")
    public IGenerator createGeneratorInstance(final Class<?> generatorType) {
        return (IGenerator) injector.getInstance(generatorType);
    }

    /// Injects custom JavaScript code into centre implementation. This code will be executed after
    /// centre component creation.
    ///
    public EntityCentre<T> injectCustomCode(final JsCode customCode) {
        this.customCode = Optional.of(customCode);
        return this;
    }

    /// Injects custom JavaScript code into centre implementation. This code will be executed every time
    /// centre component is attached to client application's DOM.
    ///
    public EntityCentre<T> injectCustomCodeOnAttach(final JsCode customCode) {
        this.customCodeOnAttach = Optional.of(customCode);
        return this;
    }

    /// Injects custom JavaScript imports into centre implementation.
    ///
    public EntityCentre<T> injectCustomImports(final JsCode customImports) {
        this.customImports = of(customImports);
        return this;
    }

    /// Indicates whether 'active only' action was deliberately hidden by specifying [MatcherOptions#HIDE_ACTIVE_ONLY_ACTION] option in following methods:\
    /// [ISingleValueAutocompleterBuilder#withMatcher(Class, MatcherOptions, MatcherOptions...)]\
    /// [ISingleValueAutocompleterBuilder#withMatcher(Class, CentreContextConfig, MatcherOptions, MatcherOptions...)]\
    /// [IMultiValueAutocompleterBuilder#withMatcher(Class, MatcherOptions, MatcherOptions...)]\
    /// [IMultiValueAutocompleterBuilder#withMatcher(Class, CentreContextConfig, MatcherOptions, MatcherOptions...)]
    ///
    public boolean isActiveOnlyActionHidden(final String property) {
        return dslDefaultConfig.isActiveOnlyActionHidden(property);
    }

    /// Creates a stream of all action configurations present in this centre.
    ///
    public Stream<EntityActionConfig> streamActionConfigs() {
        return dslDefaultConfig.streamActionConfigs();
    }

}
