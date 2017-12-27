package ua.com.fielden.platform.web.centre;

import static java.lang.String.format;
import static ua.com.fielden.platform.utils.Pair.pair;
import static ua.com.fielden.platform.web.centre.EgiConfigurations.CHECKBOX_FIXED;
import static ua.com.fielden.platform.web.centre.EgiConfigurations.CHECKBOX_VISIBLE;
import static ua.com.fielden.platform.web.centre.EgiConfigurations.CHECKBOX_WITH_PRIMARY_ACTION_FIXED;
import static ua.com.fielden.platform.web.centre.EgiConfigurations.HEADER_FIXED;
import static ua.com.fielden.platform.web.centre.EgiConfigurations.SECONDARY_ACTION_FIXED;
import static ua.com.fielden.platform.web.centre.EgiConfigurations.SUMMARY_FIXED;
import static ua.com.fielden.platform.web.centre.EgiConfigurations.TOOLBAR_VISIBLE;
import static ua.com.fielden.platform.web.centre.WebApiUtils.dslName;
import static ua.com.fielden.platform.web.centre.WebApiUtils.treeName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.google.common.collect.ListMultimap;
import com.google.inject.Injector;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithCentreContext;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.data.generator.IGenerator;
import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.jackson.DefaultValueContract;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.OrderDirection;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.SummaryPropDef;
import ua.com.fielden.platform.web.centre.api.ICentre;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.crit.defaults.assigners.IValueAssigner;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritBooleanValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.MultiCritStringValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritDateValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.RangeCritOtherValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritDateValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.defaults.mnemonics.SingleCritOtherValueMnemonic;
import ua.com.fielden.platform.web.centre.api.crit.impl.AbstractCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.BooleanCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.BooleanSingleCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.DateCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.DateSingleCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.DecimalCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.DecimalSingleCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.EntityCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.EntitySingleCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.IntegerCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.IntegerSingleCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.MoneyCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.MoneySingleCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.StringCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.StringSingleCriterionWidget;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPointBuilder;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPointConfig;
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement;
import ua.com.fielden.platform.web.centre.exceptions.PropertyDefinitionException;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.layout.FlexLayout;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.utils.EntityResourceUtils;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.snappy.DateRangeConditionEnum;

/**
 * Represents the entity centre.
 *
 * @author TG Team
 *
 */
public class EntityCentre<T extends AbstractEntity<?>> implements ICentre<T> {

    private static final CentreContextConfig defaultCentreContextConfig = new CentreContextConfig(false, false, false, false, null);
    
    private final String IMPORTS = "<!--@imports-->";
    private final String FULL_ENTITY_TYPE = "@full_entity_type";
    private final String FULL_MI_TYPE = "@full_mi_type";
    private final String MI_TYPE = "@mi_type";
    //egi related properties
    private final String EGI_LAYOUT = "@gridLayout";
    private final String EGI_LAYOUT_CONFIG = "//gridLayoutConfig";
    private final String EGI_SHORTCUTS = "@customShortcuts";
    private final String EGI_TOOLBAR_VISIBLE = "@toolbarVisible";
    private final String EGI_CHECKBOX_VISIBILITY = "@checkboxVisible";
    private final String EGI_CHECKBOX_FIXED = "@checkboxesFixed";
    private final String EGI_CHECKBOX_WITH_PRIMARY_ACTION_FIXED = "@checkboxesWithPrimaryActionsFixed";
    private final String EGI_NUM_OF_FIXED_COLUMNS = "@numOfFixedCols";
    private final String EGI_SECONDARY_ACTION_FIXED = "@secondaryActionsFixed";
    private final String EGI_HEADER_FIXED = "@headerFixed";
    private final String EGI_SUMMARY_FIXED = "@summaryFixed";
    private final String EGI_VISIBLE_ROW_COUNT = "@visibleRowCount";
    private final String EGI_PAGE_CAPACITY = "@pageCapacity";
    private final String EGI_ACTIONS = "//generatedActionObjects";
    private final String EGI_PRIMARY_ACTION = "//generatedPrimaryAction";
    private final String EGI_SECONDARY_ACTIONS = "//generatedSecondaryActions";
    private final String EGI_CHILD_ACTIONS = "//generatedChildActions";
    private final String EGI_PROPERTY_ACTIONS = "//generatedPropActions";
    private final String EGI_DOM = "<!--@egi_columns-->";
    private final String EGI_FUNCTIONAL_ACTION_DOM = "<!--@functional_actions-->";
    private final String EGI_PRIMARY_ACTION_DOM = "<!--@primary_action-->";
    private final String EGI_SECONDARY_ACTIONS_DOM = "<!--@secondary_actions-->";
    private final String EGI_CHILD_ACTIONS_DOM = "<!--@child_actions-->";
    //Toolbar related
    private final String TOOLBAR_DOM = "<!--@toolbar-->";
    private final String TOOLBAR_JS = "//toolbarGeneratedFunction";
    private final String TOOLBAR_STYLES = "/*toolbarStyles*/";
    //Selection criteria related
    private final String QUERY_ENHANCER_CONFIG = "@queryEnhancerContextConfig";
    private final String CRITERIA_DOM = "<!--@criteria_editors-->";
    private final String SELECTION_CRITERIA_LAYOUT_CONFIG = "//@layoutConfig";
    //Insertion points
    private final String INSERTION_POINT_ACTIONS = "//generatedInsertionPointActions";
    private final String INSERTION_POINT_ACTIONS_DOM = "<!--@insertion_point_actions-->";
    private final String LEFT_INSERTION_POINT_DOM = "<!--@left_insertion_points-->";
    private final String RIGHT_INSERTION_POINT_DOM = "<!--@right_insertion_points-->";
    private final String BOTTOM_INSERTION_POINT_DOM = "<!--@bottom_insertion_points-->";
    // generic custom code
    private final String READY_CUSTOM_CODE = "//@centre-is-ready-custom-code";
    private final String ATTACHED_CUSTOM_CODE = "//@centre-has-been-attached-custom-code";


    private final Logger logger = Logger.getLogger(getClass());
    private final Class<? extends MiWithConfigurationSupport<?>> menuItemType;
    private final String name;
    private final EntityCentreConfig<T> dslDefaultConfig;
    private final Injector injector;
    private final Class<T> entityType;
    private final Class<? extends MiWithConfigurationSupport<?>> miType;
    private final ICompanionObjectFinder coFinder;
    private final UnaryOperator<ICentreDomainTreeManagerAndEnhancer> postCentreCreated;
    private ICentreDomainTreeManagerAndEnhancer defaultCentre;
    private Optional<JsCode> customCode = Optional.empty();
    private Optional<JsCode> customCodeOnAttach = Optional.empty();

    /**
     * Creates new {@link EntityCentre} instance for the menu item type and with specified name.
     *
     * @param miType
     *            - the menu item type for which this entity centre is to be created.
     * @param name
     *            - the name for this entity centre.
     * @param dslDefaultConfig
     *            -- default configuration taken from Centre DSL
     */
    public EntityCentre(final Class<? extends MiWithConfigurationSupport<?>> miType, final String name, final EntityCentreConfig<T> dslDefaultConfig, final Injector injector, final UnaryOperator<ICentreDomainTreeManagerAndEnhancer> postCentreCreated) {
        this.menuItemType = miType;
        this.name = name;
        this.dslDefaultConfig = dslDefaultConfig;

        this.injector = injector;
        this.miType = miType;
        this.entityType = EntityResourceUtils.getEntityType(miType);
        this.coFinder = this.injector.getInstance(ICompanionObjectFinder.class);
        this.postCentreCreated = postCentreCreated;
    }

    /**
     * Generates default centre from DSL config and postCentreCreated callback (user unspecific).
     *
     * @param dslDefaultConfig
     * @param postCentreCreated
     * @return
     */
    private ICentreDomainTreeManagerAndEnhancer createUserUnspecificDefaultCentre(final EntityCentreConfig<T> dslDefaultConfig, final ISerialiser serialiser, final UnaryOperator<ICentreDomainTreeManagerAndEnhancer> postCentreCreated) {
        return createDefaultCentre0(dslDefaultConfig, serialiser, postCentreCreated, false);
    }

    /**
     * Generates default centre from DSL config and postCentreCreated callback (user specific).
     *
     * @param dslDefaultConfig
     * @param postCentreCreated
     * @return
     */
    private ICentreDomainTreeManagerAndEnhancer createDefaultCentre(final EntityCentreConfig<T> dslDefaultConfig, final ISerialiser serialiser, final UnaryOperator<ICentreDomainTreeManagerAndEnhancer> postCentreCreated) {
        return createDefaultCentre0(dslDefaultConfig, serialiser, postCentreCreated, true);
    }

    private ICentreDomainTreeManagerAndEnhancer createDefaultCentre0(final EntityCentreConfig<T> dslDefaultConfig, final ISerialiser serialiser, final UnaryOperator<ICentreDomainTreeManagerAndEnhancer> postCentreCreated, final boolean userSpecific) {
        final ICentreDomainTreeManagerAndEnhancer cdtmae = GlobalDomainTreeManager.createEmptyCentre(entityType, serialiser);

        final Optional<List<String>> selectionCriteria = dslDefaultConfig.getSelectionCriteria();
        if (selectionCriteria.isPresent()) {
            for (final String property : selectionCriteria.get()) {
                cdtmae.getFirstTick().check(entityType, treeName(property), true);

                if (userSpecific) {
                    provideDefaultsFor(property, cdtmae, dslDefaultConfig);
                }
            }
        }

        final Optional<List<ResultSetProp>> resultSetProps = dslDefaultConfig.getResultSetProperties();
        final Optional<ListMultimap<String, SummaryPropDef>> summaryExpressions = dslDefaultConfig.getSummaryExpressions();

        if (resultSetProps.isPresent()) {
            for (final ResultSetProp property : resultSetProps.get()) {
                if (property.propName.isPresent()) {
                } else {
                    if (property.propDef.isPresent()) { // represents the 'custom' property
                        final String customPropName = CalculatedProperty.generateNameFrom(property.propDef.get().title);
                        enhanceCentreManagerWithCustomProperty(cdtmae, entityType, customPropName, property.propDef.get(), dslDefaultConfig.getResultSetCustomPropAssignmentHandlerType());
                    } else {
                        throw new IllegalStateException(String.format("The state of result-set property [%s] definition is not correct, need to exist either a 'propName' for the property or 'propDef'.", property));
                    }
                }
            }
        }
        if (summaryExpressions.isPresent()) {
            for (final Entry<String, Collection<SummaryPropDef>> entry : summaryExpressions.get().asMap().entrySet()) {
                final String originationProperty = treeName(entry.getKey());
                for (final SummaryPropDef summaryProp : entry.getValue()) {
                    cdtmae.getEnhancer().addCalculatedProperty(entityType, "", summaryProp.alias, summaryProp.expression, summaryProp.title, summaryProp.desc, CalculatedPropertyAttribute.NO_ATTR, "".equals(originationProperty) ? "SELF"
                            : originationProperty);
                }
            }
        }
        cdtmae.getEnhancer().apply();
        if (resultSetProps.isPresent()) {
            final Map<String, Integer> growFactors = calculateGrowFactors(resultSetProps.get());
            for (final ResultSetProp property : resultSetProps.get()) {
                final String propertyName = getPropName(property);
                cdtmae.getSecondTick().check(entityType, propertyName, true);
                cdtmae.getSecondTick().use(entityType, propertyName, true);
                cdtmae.getSecondTick().setWidth(entityType, propertyName, property.width);
                if (growFactors.containsKey(propertyName)) {
                    cdtmae.getSecondTick().setGrowFactor(entityType, propertyName, growFactors.get(propertyName));
                }
            }
        }
        if (summaryExpressions.isPresent()) {
            for (final Entry<String, Collection<SummaryPropDef>> entry : summaryExpressions.get().asMap().entrySet()) {
                for (final SummaryPropDef summaryProp : entry.getValue()) {
                    cdtmae.getSecondTick().check(entityType, summaryProp.alias, true);
                }
            }
        }

        final Optional<Map<String, OrderDirection>> propOrdering = dslDefaultConfig.getResultSetOrdering();
        if (propOrdering.isPresent()) {

            // by default ordering occurs by "this" that is why it needs to be switched off in the presence of alternative ordering configuration
            if (cdtmae.getSecondTick().isChecked(entityType, "")) {
                cdtmae.getSecondTick().toggleOrdering(entityType, "");
                cdtmae.getSecondTick().toggleOrdering(entityType, "");
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

        return postCentreCreated == null ? cdtmae : postCentreCreated.apply(cdtmae);
    }

    /**
     * Returns the property name for specified {@link ResultSetProp} instance. The returned property name can be used for retrieving and altering data in
     * {@link ICentreDomainTreeManager}.
     *
     * @param property
     * @return
     */
    private static String getPropName(final ResultSetProp property) {
        if (property.propName.isPresent()) {
            return treeName(property.propName.get());
        } else {
            if (property.propDef.isPresent()) { // represents the 'custom' property
                final String customPropName = CalculatedProperty.generateNameFrom(property.propDef.get().title);
                return treeName(customPropName);
            } else {
                throw new PropertyDefinitionException(format("The state of result-set property [%s] definition is not correct, need to exist either a 'propName' for the property or 'propDef'.", property));
            }
        }
    }

    /**
     * Returns the 'managed type' for the 'centre' manager.
     *
     * @param root
     * @param centre
     * @return
     */
    private Class<?> managedType(final ICentreDomainTreeManagerAndEnhancer centre) {
        return centre.getEnhancer().getManagedType(entityType);
    }

    private void provideDefaultsFor(final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae, final EntityCentreConfig<T> dslDefaultConfig) {
        final String property = treeName(dslProperty);

        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? managedType(cdtmae) : PropertyTypeDeterminator.determinePropertyType(managedType(cdtmae), property);

        if (AbstractDomainTree.isCritOnlySingle(managedType(cdtmae), property)) {
            if (EntityUtils.isEntityType(propertyType)) {
                provideDefaultsEntitySingle(() -> dslDefaultConfig.getDefaultSingleValuesForEntitySelectionCriteria(), () -> dslDefaultConfig.getDefaultSingleValueAssignersForEntitySelectionCriteria(), dslProperty, cdtmae);
            } else if (EntityUtils.isString(propertyType)) {
                provideDefaultsSingle(() -> dslDefaultConfig.getDefaultSingleValuesForStringSelectionCriteria(), () -> dslDefaultConfig.getDefaultSingleValueAssignersForStringSelectionCriteria(), dslProperty, cdtmae);
            } else if (EntityUtils.isBoolean(propertyType)) {
                provideDefaultsSingle(() -> dslDefaultConfig.getDefaultSingleValuesForBooleanSelectionCriteria(), () -> dslDefaultConfig.getDefaultSingleValueAssignersForBooleanSelectionCriteria(), dslProperty, cdtmae);
            } else if (EntityUtils.isInteger(propertyType)) {
                provideDefaultsSingle(() -> dslDefaultConfig.getDefaultSingleValuesForIntegerSelectionCriteria(), () -> dslDefaultConfig.getDefaultSingleValueAssignersForIntegerSelectionCriteria(), dslProperty, cdtmae);
            } else if (EntityUtils.isRangeType(propertyType) && !EntityUtils.isDate(propertyType)) {
                provideDefaultsSingle(() -> dslDefaultConfig.getDefaultSingleValuesForBigDecimalAndMoneySelectionCriteria(), () -> dslDefaultConfig.getDefaultSingleValueAssignersForBigDecimalAndMoneySelectionCriteria(), dslProperty, cdtmae);
            } else if (EntityUtils.isDate(propertyType)) {
                provideDefaultsDateSingle(() -> dslDefaultConfig.getDefaultSingleValuesForDateSelectionCriteria(), () -> dslDefaultConfig.getDefaultSingleValueAssignersForDateSelectionCriteria(), dslProperty, cdtmae);
            } else {
                throw new UnsupportedOperationException(String.format("The single-crit type [%s] is currently unsupported.", propertyType));
            }
        } else {
            if (EntityUtils.isEntityType(propertyType) || EntityUtils.isString(propertyType)) {
                provideDefaultsEntityOrString(() -> dslDefaultConfig.getDefaultMultiValuesForEntityAndStringSelectionCriteria(), () -> dslDefaultConfig.getDefaultMultiValueAssignersForEntityAndStringSelectionCriteria(), dslProperty, cdtmae, EntityUtils.isString(propertyType));
            } else if (EntityUtils.isBoolean(propertyType)) {
                provideDefaultsBoolean(() -> dslDefaultConfig.getDefaultMultiValuesForBooleanSelectionCriteria(), () -> dslDefaultConfig.getDefaultMultiValueAssignersForBooleanSelectionCriteria(), dslProperty, cdtmae);
            } else if (EntityUtils.isInteger(propertyType)) {
                provideDefaultsRange(() -> dslDefaultConfig.getDefaultRangeValuesForIntegerSelectionCriteria(), () -> dslDefaultConfig.getDefaultRangeValueAssignersForIntegerSelectionCriteria(), dslProperty, cdtmae);
            } else if (EntityUtils.isRangeType(propertyType) && !EntityUtils.isDate(propertyType)) {
                provideDefaultsRange(() -> dslDefaultConfig.getDefaultRangeValuesForBigDecimalAndMoneySelectionCriteria(), () -> dslDefaultConfig.getDefaultRangeValueAssignersForBigDecimalAndMoneySelectionCriteria(), dslProperty, cdtmae);
            } else if (EntityUtils.isDate(propertyType)) {
                provideDefaultsDateRange(() -> dslDefaultConfig.getDefaultRangeValuesForDateSelectionCriteria(), () -> dslDefaultConfig.getDefaultRangeValueAssignersForDateSelectionCriteria(), dslProperty, cdtmae);
            } else {
                throw new UnsupportedOperationException(String.format("The multi-crit type [%s] is currently unsupported.", propertyType));
            }
        }
    }

    private void provideDefaultsEntityOrString(final Supplier<Optional<Map<String, MultiCritStringValueMnemonic>>> mnemonicSupplier, final Supplier<Optional<Map<String, Class<? extends IValueAssigner<MultiCritStringValueMnemonic, T>>>>> assignerSupplier, final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae, final boolean isString) {
        final String property = treeName(dslProperty);
        if (mnemonicSupplier.get().isPresent() && mnemonicSupplier.get().get().get(dslProperty) != null) {
            provideMnemonicDefaultsEntityOrString(mnemonicSupplier.get().get().get(dslProperty), cdtmae, property, isString);
        } else {
            if (assignerSupplier.get().isPresent() && assignerSupplier.get().get().get(dslProperty) != null) {
                provideAssignerDefaultsEntityOrString(assignerSupplier.get().get().get(dslProperty), cdtmae, property, isString);
            } else {
            }
        }
    }

    private void provideDefaultsBoolean(final Supplier<Optional<Map<String, MultiCritBooleanValueMnemonic>>> mnemonicSupplier, final Supplier<Optional<Map<String, Class<? extends IValueAssigner<MultiCritBooleanValueMnemonic, T>>>>> assignerSupplier, final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae) {
        final String property = treeName(dslProperty);
        if (mnemonicSupplier.get().isPresent() && mnemonicSupplier.get().get().get(dslProperty) != null) {
            provideMnemonicDefaultsBoolean(mnemonicSupplier.get().get().get(dslProperty), cdtmae, property);
        } else {
            if (assignerSupplier.get().isPresent() && assignerSupplier.get().get().get(dslProperty) != null) {
                provideAssignerDefaultsBoolean(assignerSupplier.get().get().get(dslProperty), cdtmae, property);
            } else {
            }
        }
    }

    private void provideDefaultsEntitySingle(final Supplier<Optional<Map<String, SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>>>> mnemonicSupplier, final Supplier<Optional<Map<String, Class<? extends IValueAssigner<? extends SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>, T>>>>> assignerSupplier, final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae) {
        final String property = treeName(dslProperty);
        if (mnemonicSupplier.get().isPresent() && mnemonicSupplier.get().get().get(dslProperty) != null) {
            provideMnemonicDefaultsSingle(mnemonicSupplier.get().get().get(dslProperty), cdtmae, property);
        } else {
            if (assignerSupplier.get().isPresent() && assignerSupplier.get().get().get(dslProperty) != null) {
                provideAssignerDefaultsEntitySingle(assignerSupplier.get().get().get(dslProperty), cdtmae, property);
            } else {
            }
        }
    }

    private void provideDefaultsDateSingle(final Supplier<Optional<Map<String, SingleCritDateValueMnemonic>>> mnemonicSupplier, final Supplier<Optional<Map<String, Class<? extends IValueAssigner<SingleCritDateValueMnemonic, T>>>>> assignerSupplier, final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae) {
        final String property = treeName(dslProperty);
        if (mnemonicSupplier.get().isPresent() && mnemonicSupplier.get().get().get(dslProperty) != null) {
            provideMnemonicDefaultsDateSingle(mnemonicSupplier.get().get().get(dslProperty), cdtmae, property);
        } else {
            if (assignerSupplier.get().isPresent() && assignerSupplier.get().get().get(dslProperty) != null) {
                provideAssignerDefaultsDateSingle(assignerSupplier.get().get().get(dslProperty), cdtmae, property);
            } else {
            }
        }
    }

    private void provideDefaultsDateRange(final Supplier<Optional<Map<String, RangeCritDateValueMnemonic>>> mnemonicSupplier, final Supplier<Optional<Map<String, Class<? extends IValueAssigner<RangeCritDateValueMnemonic, T>>>>> assignerSupplier, final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae) {
        final String property = treeName(dslProperty);
        if (mnemonicSupplier.get().isPresent() && mnemonicSupplier.get().get().get(dslProperty) != null) {
            provideMnemonicDefaultsDateRange(mnemonicSupplier.get().get().get(dslProperty), cdtmae, property);
        } else {
            if (assignerSupplier.get().isPresent() && assignerSupplier.get().get().get(dslProperty) != null) {
                provideAssignerDefaultsDateRange(assignerSupplier.get().get().get(dslProperty), cdtmae, property);
            } else {
            }
        }
    }

    private <M> void provideDefaultsSingle(final Supplier<Optional<Map<String, SingleCritOtherValueMnemonic<M>>>> mnemonicSupplier, final Supplier<Optional<Map<String, Class<? extends IValueAssigner<SingleCritOtherValueMnemonic<M>, T>>>>> assignerSupplier, final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae) {
        final String property = treeName(dslProperty);
        if (mnemonicSupplier.get().isPresent() && mnemonicSupplier.get().get().get(dslProperty) != null) {
            provideMnemonicDefaultsSingle(mnemonicSupplier.get().get().get(dslProperty), cdtmae, property);
        } else {
            if (assignerSupplier.get().isPresent() && assignerSupplier.get().get().get(dslProperty) != null) {
                provideAssignerDefaultsSingle(assignerSupplier.get().get().get(dslProperty), cdtmae, property);
            } else {
            }
        }
    }

    private <M> void provideDefaultsRange(final Supplier<Optional<Map<String, RangeCritOtherValueMnemonic<M>>>> mnemonicSupplier, final Supplier<Optional<Map<String, Class<? extends IValueAssigner<RangeCritOtherValueMnemonic<M>, T>>>>> assignerSupplier, final String dslProperty, final ICentreDomainTreeManagerAndEnhancer cdtmae) {
        final String property = treeName(dslProperty);
        if (mnemonicSupplier.get().isPresent() && mnemonicSupplier.get().get().get(dslProperty) != null) {
            provideMnemonicDefaultsRange(mnemonicSupplier.get().get().get(dslProperty), cdtmae, property);
        } else {
            if (assignerSupplier.get().isPresent() && assignerSupplier.get().get().get(dslProperty) != null) {
                provideAssignerDefaultsRange(assignerSupplier.get().get().get(dslProperty), cdtmae, property);
            } else {
            }
        }
    }

    private void provideAssignerDefaultsEntityOrString(final Class<? extends IValueAssigner<MultiCritStringValueMnemonic, T>> assignerType, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property, final boolean isString) {
        /* TODO at this stage there is no implementation for centre context processing -- master entity for dependent centres is the only applicable context -- will be implemented later */
        final Optional<MultiCritStringValueMnemonic> value = injector.getInstance(assignerType).getValue(null, dslName(property));
        if (value.isPresent()) {
            provideMnemonicDefaultsEntityOrString(value.get(), cdtmae, property, isString);
        }
    }

    private void provideAssignerDefaultsBoolean(final Class<? extends IValueAssigner<MultiCritBooleanValueMnemonic, T>> assignerType, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property) {
        /* TODO at this stage there is no implementation for centre context processing -- master entity for dependent centres is the only applicable context -- will be implemented later */
        final Optional<MultiCritBooleanValueMnemonic> value = injector.getInstance(assignerType).getValue(null, dslName(property));
        if (value.isPresent()) {
            provideMnemonicDefaultsBoolean(value.get(), cdtmae, property);
        }
    }

    private void provideAssignerDefaultsDateSingle(final Class<? extends IValueAssigner<SingleCritDateValueMnemonic, T>> assignerType, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property) {
        /* TODO at this stage there is no implementation for centre context processing -- master entity for dependent centres is the only applicable context -- will be implemented later */
        final Optional<SingleCritDateValueMnemonic> value = injector.getInstance(assignerType).getValue(null, dslName(property));
        if (value.isPresent()) {
            provideMnemonicDefaultsDateSingle(value.get(), cdtmae, property);
        }
    }

    private void provideAssignerDefaultsDateRange(final Class<? extends IValueAssigner<RangeCritDateValueMnemonic, T>> assignerType, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property) {
        /* TODO at this stage there is no implementation for centre context processing -- master entity for dependent centres is the only applicable context -- will be implemented later */
        final Optional<RangeCritDateValueMnemonic> value = injector.getInstance(assignerType).getValue(null, dslName(property));
        if (value.isPresent()) {
            provideMnemonicDefaultsDateRange(value.get(), cdtmae, property);
        }
    }

    private <M> void provideAssignerDefaultsSingle(final Class<? extends IValueAssigner<? extends SingleCritOtherValueMnemonic<M>, T>> assignerType, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property) {
        /* TODO at this stage there is no implementation for centre context processing -- master entity for dependent centres is the only applicable context -- will be implemented later */
        final Optional<? extends SingleCritOtherValueMnemonic<M>> value = injector.getInstance(assignerType).getValue(null, dslName(property));
        if (value.isPresent()) {
            provideMnemonicDefaultsSingle(value.get(), cdtmae, property);
        }
    }

    private void provideAssignerDefaultsEntitySingle(final Class<? extends IValueAssigner<? extends SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>, T>> assignerType, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property) {
        /* TODO at this stage there is no implementation for centre context processing -- master entity for dependent centres is the only applicable context -- will be implemented later */
        final Optional<? extends SingleCritOtherValueMnemonic<? extends AbstractEntity<?>>> value = injector.getInstance(assignerType).getValue(null, dslName(property));
        if (value.isPresent()) {
            provideMnemonicDefaultsSingle(value.get(), cdtmae, property);
        }
    }

    private <M> void provideAssignerDefaultsRange(final Class<? extends IValueAssigner<? extends RangeCritOtherValueMnemonic<M>, T>> assignerType, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property) {
        /* TODO at this stage there is no implementation for centre context processing -- master entity for dependent centres is the only applicable context -- will be implemented later */
        final Optional<? extends RangeCritOtherValueMnemonic<M>> value = injector.getInstance(assignerType).getValue(null, dslName(property));
        if (value.isPresent()) {
            provideMnemonicDefaultsRange(value.get(), cdtmae, property);
        }
    }

    private void provideMnemonicDefaultsEntityOrString(final MultiCritStringValueMnemonic mnemonic, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property, final boolean isString) {
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

    private void provideMnemonicDefaultsBoolean(final MultiCritBooleanValueMnemonic mnemonic, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property) {
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

    private void provideMnemonicDefaultsDateSingle(final SingleCritDateValueMnemonic mnemonic, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property) {
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

    private void provideMnemonicDefaultsDateRange(final RangeCritDateValueMnemonic mnemonic, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property) {
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

    private <M> void provideMnemonicDefaultsSingle(final SingleCritOtherValueMnemonic<M> mnemonic, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property) {
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

    private <M> void provideMnemonicDefaultsRange(final RangeCritOtherValueMnemonic<M> mnemonic, final ICentreDomainTreeManagerAndEnhancer cdtmae, final String property) {
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

    /**
     * Returns the menu item type for this {@link EntityCentre} instance.
     *
     * @return
     */
    public Class<? extends MiWithConfigurationSupport<?>> getMenuItemType() {
        return this.menuItemType;
    }

    /**
     * Returns the entity type for which this entity centre was created.
     *
     * @return
     */
    public Class<T> getEntityType() {
        return entityType;
    }

    /**
     * Returns the entity centre name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Returns action configuration for concrete action kind and its number in that kind's space.
     *
     * @param actionKind
     * @param actionNumber
     * @return
     */
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        return dslDefaultConfig.actionConfig(actionKind, actionNumber);
    }

    /**
     * Returns the value that indicates whether centre must run automatically or not.
     *
     * @return
     */
    public boolean isRunAutomatically() {
        return dslDefaultConfig.isRunAutomatically();
    }

    /**
     * Indicates whether centre should forcibly refresh the current page upon successful saving of a related entity.
     *
     * @return
     */
    public boolean shouldEnforcePostSaveRefresh() {
        return dslDefaultConfig.shouldEnforcePostSaveRefresh();
    }

    /**
     * Return an optional Event Source URI.
     *
     * @return
     */
    public Optional<String> eventSourceUri() {
        return dslDefaultConfig.getSseUri();
    }

    /**
     * Returns the instance of rendering customiser for this entity centre.
     *
     * @return
     */
    public Optional<IRenderingCustomiser<?>> getRenderingCustomiser() {
        if (dslDefaultConfig.getResultSetRenderingCustomiserType().isPresent()) {
            return Optional.of(injector.getInstance(dslDefaultConfig.getResultSetRenderingCustomiserType().get()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public IRenderable build() {
        logger.debug("Initiating fresh centre...");
        return createRenderableRepresentation(getAssociatedEntityCentreManager());
    }

    private final ICentreDomainTreeManagerAndEnhancer getAssociatedEntityCentreManager() {
        final IGlobalDomainTreeManager userSpecificGlobalManager = getUserSpecificGlobalManager();
        if (userSpecificGlobalManager == null) {
            return createUserUnspecificDefaultCentre(dslDefaultConfig, injector.getInstance(ISerialiser.class), postCentreCreated);
        } else {
            return CentreUpdater.updateCentre(userSpecificGlobalManager, this.menuItemType, CentreUpdater.FRESH_CENTRE_NAME);
        }
    }

    private String egiRepresentationFor(final Class<?> propertyType, final Optional<String> timeZone, final Optional<String> timePortionToDisplay) {
        final Class<?> type = DynamicEntityClassLoader.getOriginalType(propertyType);
        String typeRes = EntityUtils.isEntityType(type) ? type.getName() : (EntityUtils.isBoolean(type) ? "Boolean" : type.getSimpleName());
        if (Date.class.isAssignableFrom(type)) {
            typeRes += ":" + timeZone.orElse("");
            typeRes += ":" + timePortionToDisplay.orElse("");
        }
        return typeRes;
    }

    /**
     * Returns default centre manager that was formed using DSL configuration and postCentreCreated hook.
     *
     * @return
     */
    public ICentreDomainTreeManagerAndEnhancer getDefaultCentre() {
        if (defaultCentre == null) {
            defaultCentre = createDefaultCentre(dslDefaultConfig, injector.getInstance(ISerialiser.class), postCentreCreated);
        }
        return defaultCentre;
    }

    private IRenderable createRenderableRepresentation(final ICentreDomainTreeManagerAndEnhancer centre) {

        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add("polymer/polymer/polymer");
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
        
        // current global index of all child actions
        int childActionIndex = 0;
        // gathers all child actions from all 1) prop 2) primary 3) secondary actions; they can be differentiated by 'parentElementAlias' property and end-developer-defined 'childName'
        final List<FunctionalActionElement> childActions = new ArrayList<>(); 
        
        final List<PropertyColumnElement> propertyColumns = new ArrayList<>();
        final Optional<List<ResultSetProp>> resultProps = dslDefaultConfig.getResultSetProperties();
        final Optional<ListMultimap<String, SummaryPropDef>> summaryProps = dslDefaultConfig.getSummaryExpressions();
        final Class<?> managedType = centre.getEnhancer().getManagedType(root);
        if (resultProps.isPresent()) {
            int actionIndex = 0;
            for (final ResultSetProp resultProp : resultProps.get()) {
                final String tooltipProp = resultProp.tooltipProp.isPresent() ? resultProp.tooltipProp.get() : null;
                final String resultPropName = getPropName(resultProp);
                final boolean isEntityItself = "".equals(resultPropName); // empty property means "entity itself"
                final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, resultPropName);

                final Optional<FunctionalActionElement> action;
                final Optional<EntityActionConfig> actionConfig = resultProp.propAction.get();
                if (actionConfig.isPresent()) {
                    final FunctionalActionElement actionElement = new FunctionalActionElement(actionConfig.get(), actionIndex, resultPropName);
                    action = Optional.of(actionElement);
                    actionIndex += 1;
                    
                    // adds all child actions from currently processed PROP action with appropriate indexing
                    for (final Entry<String, EntityActionConfig> nameAndChild: actionElement.entityActionConfig.childActions().entrySet()) {
                        childActions.add(createChildActionElement(actionElement.getElementAlias(), childActionIndex, nameAndChild));
                        childActionIndex += 1;
                    }
                } else {
                    action = Optional.empty();
                }

                final PropertyColumnElement el = new PropertyColumnElement(resultPropName,
                        null,
                        resultProp.width,
                        centre.getSecondTick().getGrowFactor(root, resultPropName),
                        resultProp.isFlexible,
                        tooltipProp,
                        egiRepresentationFor(
                                propertyType,
                                Optional.ofNullable(EntityUtils.isDate(propertyType) ? DefaultValueContract.getTimeZone(managedType, resultPropName) : null),
                                Optional.ofNullable(EntityUtils.isDate(propertyType) ? DefaultValueContract.getTimePortionToDisplay(managedType, resultPropName) : null)),
                        CriteriaReflector.getCriteriaTitleAndDesc(managedType, resultPropName),
                        action);
                if (summaryProps.isPresent() && summaryProps.get().containsKey(dslName(resultPropName))) {
                    final List<SummaryPropDef> summaries = summaryProps.get().get(dslName(resultPropName));
                    summaries.forEach(summary -> el.addSummary(summary.alias, PropertyTypeDeterminator.determinePropertyType(managedType, summary.alias), new Pair<>(summary.title, summary.desc)));
                }
                propertyColumns.add(el);
            }
        }

        logger.debug("Initiating prop actions...");

        final DomContainer egiColumns = new DomContainer();
        final StringBuilder propActionsObject = new StringBuilder();
        propertyColumns.forEach(column -> {
            importPaths.add(column.importPath());
            if (column.hasSummary()) {
                importPaths.add(column.getSummary(0).importPath());
            }
            if (column.getAction().isPresent()) {
                importPaths.add(column.getAction().get().importPath());
                propActionsObject.append(prefix + createActionObject(column.getAction().get()));
            }
            egiColumns.add(column.render());
        });

        logger.debug("Initiating top-level actions...");
        final Optional<List<Pair<EntityActionConfig, Optional<String>>>> topLevelActions = this.dslDefaultConfig.getTopLevelActions();

        final List<List<FunctionalActionElement>> actionGroups = new ArrayList<>();
        if (topLevelActions.isPresent()) {

            final String currentGroup = null;
            for (int i = 0; i < topLevelActions.get().size(); i++) {
                final Pair<EntityActionConfig, Optional<String>> topLevelAction = topLevelActions.get().get(i);
                final String cg = getGroup(topLevelAction.getValue());
                if (!EntityUtils.equalsEx(cg, currentGroup)) {
                    actionGroups.add(new ArrayList<>());
                }
                addToLastGroup(actionGroups, topLevelAction.getKey(), i);
            }
        }

        logger.debug("Initiating functional actions...");
        final StringBuilder functionalActionsObjects = new StringBuilder();

        final DomContainer functionalActionsDom = new DomContainer();

        for (final List<FunctionalActionElement> group : actionGroups) {
            final DomElement groupElement = new DomElement("div").clazz("entity-specific-action", "group");
            for (final FunctionalActionElement el : group) {
                importPaths.add(el.importPath());
                groupElement.add(el.render());
                functionalActionsObjects.append(prefix + createActionObject(el));
            }
            functionalActionsDom.add(groupElement);
        }

        logger.debug("Initiating primary actions...");
        //////////////////// Primary result-set action ////////////////////
        final Optional<EntityActionConfig> resultSetPrimaryEntityAction = this.dslDefaultConfig.getResultSetPrimaryEntityAction();
        final DomContainer primaryActionDom = new DomContainer();
        final StringBuilder primaryActionObject = new StringBuilder();

        if (resultSetPrimaryEntityAction.isPresent() && !resultSetPrimaryEntityAction.get().isNoAction()) {
            final FunctionalActionElement actionElement = new FunctionalActionElement(resultSetPrimaryEntityAction.get(), 0, FunctionalActionKind.PRIMARY_RESULT_SET);

            importPaths.add(actionElement.importPath());
            primaryActionDom.add(actionElement.render().clazz("primary-action").attr("hidden", null));
            primaryActionObject.append(prefix + createActionObject(actionElement));
            
            // adds all child actions from currently processed PRIMARY action with appropriate indexing
            for (final Entry<String, EntityActionConfig> nameAndChild: actionElement.entityActionConfig.childActions().entrySet()) {
                childActions.add(createChildActionElement(actionElement.getElementAlias(), childActionIndex, nameAndChild));
                childActionIndex += 1;
            }
        }

        //////////////////// Primary result-set action [END] //////////////
        logger.debug("Initiating secondary actions...");

        final List<FunctionalActionElement> secondaryActionElements = new ArrayList<>();
        final Optional<List<EntityActionConfig>> resultSetSecondaryEntityActions = this.dslDefaultConfig.getResultSetSecondaryEntityActions();
        if (resultSetSecondaryEntityActions.isPresent()) {
            for (int i = 0; i < resultSetSecondaryEntityActions.get().size(); i++) {
                final FunctionalActionElement actionElement = new FunctionalActionElement(resultSetSecondaryEntityActions.get().get(i), i, FunctionalActionKind.SECONDARY_RESULT_SET);
                secondaryActionElements.add(actionElement);
                
                // adds all child actions from currently processed SECONDARY action with appropriate indexing
                for (final Entry<String, EntityActionConfig> nameAndChild: actionElement.entityActionConfig.childActions().entrySet()) {
                    childActions.add(createChildActionElement(actionElement.getElementAlias(), childActionIndex, nameAndChild));
                    childActionIndex += 1;
                }
            }
        }
        
        // gathers DOM element and action objects to be used for generation
        final DomContainer childActionsDom = new DomContainer();
        final StringBuilder childActionsObjects = new StringBuilder();
        for (final FunctionalActionElement childAction: childActions) {
            importPaths.add(childAction.importPath());
            childActionsDom.add(childAction.render().clazz("child-action").attr("hidden", null));
            childActionsObjects.append(prefix + createActionObject(childAction));
        }
        
        final DomContainer secondaryActionsDom = new DomContainer();
        final StringBuilder secondaryActionsObjects = new StringBuilder();
        for (final FunctionalActionElement el : secondaryActionElements) {
            importPaths.add(el.importPath());
            secondaryActionsDom.add(el.render().clazz("secondary-action").attr("hidden", null));
            secondaryActionsObjects.append(prefix + createActionObject(el));
        }

        logger.debug("Initiating insertion point actions...");

        final List<InsertionPointBuilder> insertionPointActionsElements = new ArrayList<>();
        final Optional<List<InsertionPointConfig>> insertionPointConfigs = this.dslDefaultConfig.getInsertionPointConfigs();
        if (insertionPointConfigs.isPresent()) {
            for (int index = 0; index < insertionPointConfigs.get().size(); index++) {
                final InsertionPointBuilder el = new InsertionPointBuilder(insertionPointConfigs.get().get(index), index);
                insertionPointActionsElements.add(el);
            }
        }

        final DomContainer insertionPointActionsDom = new DomContainer();
        final StringBuilder insertionPointActionsObjects = new StringBuilder();
        for (final InsertionPointBuilder el : insertionPointActionsElements) {
            importPaths.addAll(el.importPaths());
            insertionPointActionsDom.add(el.renderInsertionPointAction());
            insertionPointActionsObjects.append(prefix + el.code());
        }
        importPaths.add(dslDefaultConfig.getToolbarConfig().importPath());

        final DomContainer leftInsertionPointsDom = new DomContainer();
        final DomContainer rightInsertionPointsDom = new DomContainer();
        final DomContainer bottomInsertionPointsDom = new DomContainer();
        for (final InsertionPointBuilder el : insertionPointActionsElements) {
            final DomElement insertionPoint = el.render();
            if (el.whereToInsert() == InsertionPoints.LEFT) {
                leftInsertionPointsDom.add(insertionPoint);
            } else if (el.whereToInsert() == InsertionPoints.RIGHT) {
                rightInsertionPointsDom.add(insertionPoint);
            } else if (el.whereToInsert() == InsertionPoints.BOTTOM) {
                bottomInsertionPointsDom.add(insertionPoint);
            } else {
                throw new IllegalArgumentException("Unexpected insertion point type.");
            }
        }
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
        final String funcActionString = functionalActionsObjects.toString();
        final String secondaryActionsString = secondaryActionsObjects.toString();
        final String childActionsString = childActionsObjects.toString();
        final String insertionPointActionsString = insertionPointActionsObjects.toString();
        final String primaryActionObjectString = primaryActionObject.toString();
        final String propActionsString = propActionsObject.toString();
        final Pair<String, String> gridLayoutConfig = generateGridLayoutConfig();
        final int prefixLength = prefix.length();
        logger.debug("Initiating template...");
        final String text = ResourceLoader.getText("ua/com/fielden/platform/web/centre/tg-entity-centre-template.html");
        logger.debug("Replacing some parts...");
        final String entityCentreStr = text.
                replace(IMPORTS, SimpleMasterBuilder.createImports(importPaths)).
                replace(EGI_LAYOUT, gridLayoutConfig.getKey()).
                replace(FULL_ENTITY_TYPE, entityType.getName()).
                replace(MI_TYPE, miType.getSimpleName()).
                //egi related properties
                replace(EGI_SHORTCUTS, shortcuts).
                replace(EGI_TOOLBAR_VISIBLE, TOOLBAR_VISIBLE.eval(!dslDefaultConfig.shouldHideToolbar())).
                replace(EGI_CHECKBOX_VISIBILITY, CHECKBOX_VISIBLE.eval(!dslDefaultConfig.shouldHideCheckboxes())).
                replace(EGI_CHECKBOX_FIXED, CHECKBOX_FIXED.eval(dslDefaultConfig.getScrollConfig().isCheckboxesFixed())).
                replace(EGI_CHECKBOX_WITH_PRIMARY_ACTION_FIXED, CHECKBOX_WITH_PRIMARY_ACTION_FIXED.eval(dslDefaultConfig.getScrollConfig().isCheckboxesWithPrimaryActionsFixed())).
                replace(EGI_NUM_OF_FIXED_COLUMNS, Integer.toString(dslDefaultConfig.getScrollConfig().getNumberOfFixedColumns())).
                replace(EGI_SECONDARY_ACTION_FIXED, SECONDARY_ACTION_FIXED.eval(dslDefaultConfig.getScrollConfig().isSecondaryActionsFixed())).
                replace(EGI_HEADER_FIXED, HEADER_FIXED.eval(dslDefaultConfig.getScrollConfig().isHeaderFixed())).
                replace(EGI_SUMMARY_FIXED, SUMMARY_FIXED.eval(dslDefaultConfig.getScrollConfig().isSummaryFixed())).
                replace(EGI_VISIBLE_ROW_COUNT, dslDefaultConfig.getVisibleRowsCount() + "").
                ///////////////////////
                replace(TOOLBAR_DOM, dslDefaultConfig.getToolbarConfig().render().toString()).
                replace(TOOLBAR_JS, dslDefaultConfig.getToolbarConfig().code(entityType).toString()).
                replace(TOOLBAR_STYLES, dslDefaultConfig.getToolbarConfig().styles().toString()).
                replace(FULL_MI_TYPE, miType.getName()).
                replace(EGI_PAGE_CAPACITY, Integer.toString(dslDefaultConfig.getPageCapacity())).
                replace(QUERY_ENHANCER_CONFIG, queryEnhancerContextConfigString()).
                replace(CRITERIA_DOM, editorContainer.toString()).
                replace(EGI_DOM, egiColumns.toString()).
                replace(EGI_ACTIONS, funcActionString.length() > prefixLength ? funcActionString.substring(prefixLength) : funcActionString).
                replace(EGI_SECONDARY_ACTIONS, secondaryActionsString.length() > prefixLength ? secondaryActionsString.substring(prefixLength) : secondaryActionsString).
                replace(EGI_CHILD_ACTIONS, childActionsString.length() > prefixLength ? childActionsString.substring(prefixLength) : childActionsString).
                replace(INSERTION_POINT_ACTIONS, insertionPointActionsString.length() > prefixLength ? insertionPointActionsString.substring(prefixLength)
                        : insertionPointActionsString).
                replace(EGI_PRIMARY_ACTION, primaryActionObjectString.length() > prefixLength ? primaryActionObjectString.substring(prefixLength)
                        : primaryActionObjectString).
                replace(EGI_PROPERTY_ACTIONS, propActionsString.length() > prefixLength ? propActionsString.substring(prefixLength)
                        : propActionsString).
                replace(SELECTION_CRITERIA_LAYOUT_CONFIG, layout.code().toString()).
                replace(EGI_LAYOUT_CONFIG, gridLayoutConfig.getValue()).
                replace(EGI_FUNCTIONAL_ACTION_DOM, functionalActionsDom.toString()).
                replace(EGI_PRIMARY_ACTION_DOM, primaryActionDom.toString()).
                replace(EGI_SECONDARY_ACTIONS_DOM, secondaryActionsDom.toString()).
                replace(EGI_CHILD_ACTIONS_DOM, childActionsDom.toString()).
                replace(INSERTION_POINT_ACTIONS_DOM, insertionPointActionsDom.toString()).
                replace(LEFT_INSERTION_POINT_DOM, leftInsertionPointsDom.toString()).
                replace(RIGHT_INSERTION_POINT_DOM, rightInsertionPointsDom.toString()).
                replace(BOTTOM_INSERTION_POINT_DOM, bottomInsertionPointsDom.toString()).
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

    /**
     * Creates {@link FunctionalActionElement} with type {@link FunctionalActionKind#CHILD}.
     * 
     * @param parentElementAlias -- unique identifier of generated parent action element
     * @param childActionIndex -- number of child action in context of all other child actions
     * @param nameAndAction -- user-defined string key of action and action object itself
     * @return
     */
    private FunctionalActionElement createChildActionElement(final String parentElementAlias, final int childActionIndex, final Entry<String, EntityActionConfig> nameAndAction) {
        return FunctionalActionElement.createChildActionElement(nameAndAction.getValue(), childActionIndex, nameAndAction.getKey(), parentElementAlias);
    }

    /**
     * Calculates the relative grow factor for all columns.
     */
    private Map<String, Integer> calculateGrowFactors(final List<ResultSetProp> propertyColumns) {
        //Searching for the minimal column width which are not flexible and their width is greater than 0.
        final int minWidth = propertyColumns.stream()
                .filter(column -> column.isFlexible && column.width > 0)
                .reduce(Integer.MAX_VALUE,
                        (min, column) -> min > column.width ? column.width : min,
                        (min1, min2) -> min1 < min2 ? min1 : min2);
        //Map each resultSetProp which is not flexible and has width greater than 0 to it's grow factor.
        return propertyColumns.stream()
                .filter(column -> column.isFlexible && column.width > 0)
                .collect(Collectors.toMap(
                        column -> getPropName(column),
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

    /**
     * Returns the global manager for the user for this concrete thread (the user has been populated through the Web UI authentication mechanism -- see DefaultWebResourceGuard).
     *
     * @return
     */
    private IGlobalDomainTreeManager getUserSpecificGlobalManager() {
        final IServerGlobalDomainTreeManager serverGdtm = injector.getInstance(IServerGlobalDomainTreeManager.class);
        final User user = injector.getInstance(IUserProvider.class).getUser();
        if (user == null) { // the user is unknown at this stage!
            return null; // no user-specific global exists for unknown user!
        }
        final String userName = user.getKey();
        return serverGdtm.get(userName);
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

    /**
     * Enhances the type of centre entity with custom property definition.
     *
     * @param centre
     * @param root
     * @param propDef
     * @param resultSetCustomPropAssignmentHandlerType
     */
    private void enhanceCentreManagerWithCustomProperty(final ICentreDomainTreeManagerAndEnhancer centre, final Class<?> root, final String propName, final PropDef<?> propDef, final Optional<Class<? extends ICustomPropsAssignmentHandler>> resultSetCustomPropAssignmentHandlerType) {
        centre.getEnhancer().addCustomProperty(root, "" /* this is the contextPath */, propName, propDef.title, propDef.desc, propDef.type);
    }

    /**
     * Creates the widgets for criteria.
     *
     * @param centre
     * @param root
     * @return
     */
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
                    } else if (EntityUtils.isString(propertyType)) {
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
                    } else if (EntityUtils.isString(propertyType)) {
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
        return dslDefaultConfig.getValueMatchersForSelectionCriteria().map(m -> m.get(dslProp)).flatMap(Pair::getValue).orElse(defaultCentreContextConfig);
    }

    /**
     * Creates value matcher instance.
     *
     * @param injector
     * @return
     */
    public <V extends AbstractEntity<?>> Pair<IValueMatcherWithCentreContext<V>, Optional<CentreContextConfig>> createValueMatcherAndContextConfig(final Class<? extends AbstractEntity<?>> criteriaType, final String criterionPropertyName) {

        final String originalPropertyName = EntityResourceUtils.getOriginalPropertyName(criteriaType, criterionPropertyName);
        final String dslProp = dslName(originalPropertyName);
        logger.debug(String.format("createValueMatcherAndContextConfig: propertyName = %s originalPropertyName = %s", criterionPropertyName, dslProp));
                
        final Optional<Pair<Class<? extends IValueMatcherWithCentreContext<? extends AbstractEntity<?>>>, Optional<CentreContextConfig>>> matcherConfig = dslDefaultConfig.getValueMatchersForSelectionCriteria().map(m -> m.get(dslProp));
               
        return matcherConfig.map(p -> pair((IValueMatcherWithCentreContext<V>) injector.getInstance(p.getKey()), p.getValue()))
                .orElse(createDefaultValueMatcherAndContextConfig(EntityResourceUtils.getOriginalType(criteriaType), originalPropertyName, coFinder));
    }

    /**
     * Creates default value matcher and context config for the specified entity property.
     *
     * @param propertyName
     * @param criteriaType
     * @param coFinder
     * @return
     */
    private <V extends AbstractEntity<?>> Pair<IValueMatcherWithCentreContext<V>, Optional<CentreContextConfig>> createDefaultValueMatcherAndContextConfig(final Class<? extends AbstractEntity<?>> originalType, final String originalPropertyName, final ICompanionObjectFinder coFinder) {
        final Class<V> propType = dslDefaultConfig.getProvidedTypeForAutocompletedSelectionCriterion(originalPropertyName)
                                   .map(propertyType -> (Class<V>) propertyType)
                                   .orElseGet(() -> (Class<V>) ("".equals(originalPropertyName) ? originalType : PropertyTypeDeterminator.determinePropertyType(originalType, originalPropertyName)));

        final IEntityDao<V> co = coFinder.find(propType);
        final IValueMatcherWithCentreContext<V> matcher = new FallbackValueMatcherWithCentreContext<>(co);
        final fetch<V> fetch = dslDefaultConfig.getAdditionalPropsForAutocompleter(originalPropertyName).stream()
                                .map(Pair::getKey)
                                .reduce(EntityQueryUtils.fetchKeyAndDescOnly(propType), (f, propName) -> f.with(propName), (f1, f2) -> {throw new UnsupportedOperationException("Parallelisation is not supported.");});
        
        matcher.setFetch(fetch);

        return pair(matcher, Optional.empty());
    }

    public Optional<Class<? extends ICustomPropsAssignmentHandler>> getCustomPropertiesAsignmentHandler() {
        return dslDefaultConfig.getResultSetCustomPropAssignmentHandlerType();
    }

    public Optional<List<ResultSetProp>> getCustomPropertiesDefinitions() {
        return dslDefaultConfig.getResultSetProperties();
    }

    public ICustomPropsAssignmentHandler createAssignmentHandlerInstance(final Class<? extends ICustomPropsAssignmentHandler> assignmentHandlerType) {
        return injector.getInstance(assignmentHandlerType);
    }

    public Optional<IFetchProvider<T>> getAdditionalFetchProvider() {
        return dslDefaultConfig.getFetchProvider();
    }

    /**
     * Returns fetch provider consisting only of 'tooltip properties': properties that are used as tooltips for other properties.
     *
     * @return
     */
    public Optional<IFetchProvider<T>> getAdditionalFetchProviderForTooltipProperties() {
        final Set<String> tooltipProps = new LinkedHashSet<>();
        final Optional<List<ResultSetProp>> resultSetProps = dslDefaultConfig.getResultSetProperties();
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

    /**
     * Creates generic {@link IGenerator} instance from injector based on assumption that <code>generatorType</code> is of appropriate type (such checks are performed on API implementation level).
     *
     * @param generatorType
     * @return
     */
    @SuppressWarnings("rawtypes")
    public IGenerator createGeneratorInstance(final Class<?> generatorType) {
        return (IGenerator) injector.getInstance(generatorType);
    }

    /**
     * Injects custom JavaScript code into centre implementation. This code will be executed after
     * centre component creation.
     *
     * @param customCode
     * @return
     */
    public EntityCentre<T> injectCustomCode(final JsCode customCode) {
        this.customCode = Optional.of(customCode);
        return this;
    }

    /**
     * Injects custom JavaScript code into centre implementation. This code will be executed every time
     * centre component is attached to client application's DOM.
     *
     * @param customCode
     * @return
     */
    public EntityCentre<T> injectCustomCodeOnAttach(final JsCode customCode) {
        this.customCodeOnAttach = Optional.of(customCode);
        return this;
    }
}