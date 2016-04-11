package ua.com.fielden.platform.web.centre;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithCentreContext;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.IServerGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.CalculatedProperty;
import ua.com.fielden.platform.domaintree.impl.GlobalDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.types.Money;
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
import ua.com.fielden.platform.web.centre.api.insertion_points.InsertionPoints;
import ua.com.fielden.platform.web.centre.api.resultset.ICustomPropsAssignmentHandler;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.layout.FlexLayout;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.snappy.DateRangeConditionEnum;

import com.google.common.collect.ListMultimap;
import com.google.inject.Injector;

/**
 * Represents the entity centre.
 *
 * @author TG Team
 *
 */
public class EntityCentre<T extends AbstractEntity<?>> implements ICentre<T> {
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
        this.entityType = (Class<T>) CentreUtils.getEntityType(miType);
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
        if (resultSetProps.isPresent()) {
            for (final ResultSetProp property : resultSetProps.get()) {
                if (property.propName.isPresent()) {
                    final String propertyName = treeName(property.propName.get());
                    cdtmae.getSecondTick().check(entityType, propertyName, true);
                    cdtmae.getSecondTick().setWidth(entityType, propertyName, property.width);
                } else {
                    if (property.propDef.isPresent()) { // represents the 'custom' property
                        final String customPropName = CalculatedProperty.generateNameFrom(property.propDef.get().title);
                        enhanceCentreManagerWithCustomProperty(cdtmae, entityType, customPropName, property.propDef.get(), dslDefaultConfig.getResultSetCustomPropAssignmentHandlerType());
                        final String propertyName = treeName(customPropName);
                        cdtmae.getSecondTick().check(entityType, propertyName, true);
                        cdtmae.getSecondTick().setWidth(entityType, propertyName, property.width);
                    } else {
                        throw new IllegalStateException(String.format("The state of result-set property [%s] definition is not correct, need to exist either a 'propName' for the property or 'propDef'.", property));
                    }
                }
                if (property.tooltipProp.isPresent()) {
                    cdtmae.getSecondTick().check(entityType, treeName(property.tooltipProp.get()), true);
                }
            }
        }

        final Optional<ListMultimap<String, SummaryPropDef>> summaryExpressions = dslDefaultConfig.getSummaryExpressions();
        if (summaryExpressions.isPresent()) {
            for (final Entry<String, Collection<SummaryPropDef>> entry : summaryExpressions.get().asMap().entrySet()) {
                final String originationProperty = treeName(entry.getKey());
                for (final SummaryPropDef summaryProp : entry.getValue()) {
                    cdtmae.getEnhancer().addCalculatedProperty(entityType, "", summaryProp.alias, summaryProp.expression, summaryProp.title, summaryProp.desc, CalculatedPropertyAttribute.NO_ATTR, "".equals(originationProperty) ? "SELF"
                            : originationProperty);
                    cdtmae.getEnhancer().apply();
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
    public Optional<IRenderingCustomiser<T, ?>> getRenderingCustomiser() {
        if (dslDefaultConfig.getResultSetRenderingCustomiserType().isPresent()) {
            return (Optional<IRenderingCustomiser<T, ?>>) Optional.of(injector.getInstance(dslDefaultConfig.getResultSetRenderingCustomiserType().get()));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public IRenderable build() {
        logger.debug("Initiating fresh centre...");
        return createRenderableRepresentation(getAssociatedEntityCentreManager());
    }

    public IRenderable buildEgi() {
        return createRenderableEgiRepresentaton(getAssociatedEntityCentreManager());
    }

    private final ICentreDomainTreeManagerAndEnhancer getAssociatedEntityCentreManager() {
        final IGlobalDomainTreeManager userSpecificGlobalManager = getUserSpecificGlobalManager();
        if (userSpecificGlobalManager == null) {
            return createUserUnspecificDefaultCentre(dslDefaultConfig, injector.getInstance(ISerialiser.class), postCentreCreated);
        } else {
            return CentreUtils.getFreshCentre(userSpecificGlobalManager, this.menuItemType);
        }
    }

    private IRenderable createRenderableEgiRepresentaton(final ICentreDomainTreeManagerAndEnhancer centre) {
        final String simpleValueString = "<div class='data-entry layout vertical' property='@calc-property-name'>" +
                "<div class='data-label truncate' tooltip-text='@column-desc'>@column-title</div>" +
                "<div class='data-value relative' on-tap='_tapAction' tooltip-text$='[[_getTooltip(egiEntity.entity, columns.@column-index)]]'>" +
                "<div style$='[[_calcRenderingHintsStyle(egiEntity, entityIndex, \"@property-name\")]]' class='fit'></div>" +
                "<div class='truncate relative'>[[_getValue(egiEntity.entity, '@property-name', '@property-type')]]</div>" +
                "</div>" +
                "</div>";

        final String booleanValueString = "<div class='data-entry layout vertical' property='@calc-property-name'>" +
                "<div class='data-label truncate' tooltip-text='@column-desc'>@column-title</div>" +
                "<div class='data-value relative' on-tap='_tapAction' tooltip-text$='[[_getTooltip(egiEntity.entity, columns.@column-index)]]'>" +
                "<div style$='[[_calcRenderingHintsStyle(egiEntity, entityIndex, \"@property-name\")]]' class='fit'></div>" +
                "<iron-icon class='card-icon' icon='[[_getBooleanIcon(egiEntity.entity, \"@property-name\")]]'></iron-icon>" +
                "</div>" +
                "</div>";
        final DomContainer domContainer = new DomContainer();
        final Optional<List<ResultSetProp>> resultProps = getCustomPropertiesDefinitions();
        final Class<?> managedType = centre.getEnhancer().getManagedType(getEntityType());
        if (resultProps.isPresent()) {
            for (int columnIndex = 0; columnIndex < resultProps.get().size(); columnIndex++) {
                final ResultSetProp resultProp = resultProps.get().get(columnIndex);
                final String propertyName = resultProp.propDef.isPresent() ? CalculatedProperty.generateNameFrom(resultProp.propDef.get().title) : resultProp.propName.get();
                final String resultPropName = propertyName.equals("this") ? "" : propertyName;
                final Class<?> propertyType = "".equals(resultPropName) ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, resultPropName);
                final String typeTemplate = EntityUtils.isBoolean(propertyType) ? booleanValueString : simpleValueString;
                final Pair<String, String> columnTitileAndDesc = CriteriaReflector.getCriteriaTitleAndDesc(managedType, resultPropName);
                domContainer.add(
                        new InnerTextElement(typeTemplate
                                .replaceAll("@calc-property-name", propertyName)
                                .replaceAll("@property-name", resultPropName)
                                .replace("@column-title", columnTitileAndDesc.getKey())
                                .replace("@column-desc", columnTitileAndDesc.getValue())
                                .replaceAll("@column-index", Integer.toString(columnIndex))
                                .replaceAll("@property-type", Matcher.quoteReplacement(egiRepresentationFor(propertyType).toString()))));
            }
        }
        final String text = ResourceLoader.getText("ua/com/fielden/platform/web/egi/tg-entity-grid-inspector-template.html");
        final String egiStr = text.
                replace("@toolbarVisible", !dslDefaultConfig.shouldHideToolbar() + "").
                replace("@checkboxVisible", !dslDefaultConfig.shouldHideCheckboxes() + "").
                replace("@checkboxesFixed", dslDefaultConfig.getScrollConfig().isCheckboxesFixed() + "").
                replace("@checkboxesWithPrimaryActionsFixed", dslDefaultConfig.getScrollConfig().isCheckboxesWithPrimaryActionsFixed() + "").
                replace("@numOfFixedCols", Integer.toString(dslDefaultConfig.getScrollConfig().getNumberOfFixedColumns())).
                replace("@secondaryActionsFixed", dslDefaultConfig.getScrollConfig().isSecondaryActionsFixed() + "").
                replace("@headerFixed", dslDefaultConfig.getScrollConfig().isHeaderFixed() + "").
                replace("@summaryFixed", dslDefaultConfig.getScrollConfig().isSummaryFixed() + "").
                replace("@visibleRowCount", dslDefaultConfig.getVisibleRowsCount() + "").
                replaceAll("@miType", getMenuItemType().getSimpleName()).
                replaceAll("@gridCardDom", Matcher.quoteReplacement(domContainer.toString()));

        return new IRenderable() {

            @Override
            public DomElement render() {
                return new InnerTextElement(egiStr);
            }
        };
    }

    private Object egiRepresentationFor(final Class<?> propertyType) {
        final Class<?> type = DynamicEntityClassLoader.getOriginalType(propertyType);
        return EntityUtils.isEntityType(type) ? type.getName() : (EntityUtils.isBoolean(type) ? "Boolean" : type.getSimpleName());
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

        final DomElement editorContainer = layout.render();

        importPaths.add(layout.importPath());

        final Class<?> root = this.entityType;

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
        final Optional<List<ResultSetProp>> resultProps = dslDefaultConfig.getResultSetProperties();
        final Optional<ListMultimap<String, SummaryPropDef>> summaryProps = dslDefaultConfig.getSummaryExpressions();
        final Class<?> managedType = centre.getEnhancer().getManagedType(root);
        if (resultProps.isPresent()) {
            int actionIndex = 0;
            for (final ResultSetProp resultProp : resultProps.get()) {
                final String propertyName = resultProp.propDef.isPresent() ? CalculatedProperty.generateNameFrom(resultProp.propDef.get().title) : resultProp.propName.get();
                final String tooltipProp = resultProp.tooltipProp.isPresent() ? resultProp.tooltipProp.get() : null;

                final String resultPropName = propertyName.equals("this") ? "" : propertyName;
                final boolean isEntityItself = "".equals(resultPropName); // empty property means "entity itself"
                final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, resultPropName);

                final Optional<FunctionalActionElement> action;
                if (resultProp.propAction.isPresent()) {
                    action = Optional.of(new FunctionalActionElement(resultProp.propAction.get(), actionIndex, resultPropName));
                    actionIndex += 1;
                } else {
                    action = Optional.empty();
                }

                final PropertyColumnElement el = new PropertyColumnElement(resultPropName, null, centre.getSecondTick().getWidth(root, resultPropName), resultProp.isFlexible, tooltipProp, egiRepresentationFor(propertyType), CriteriaReflector.getCriteriaTitleAndDesc(managedType, resultPropName), action);
                if (summaryProps.isPresent() && summaryProps.get().containsKey(propertyName)) {
                    final List<SummaryPropDef> summaries = summaryProps.get().get(propertyName);
                    summaries.forEach(summary -> el.addSummary(summary.alias, PropertyTypeDeterminator.determinePropertyType(managedType, summary.alias), new Pair<>(summary.title, summary.desc)));
                }
                propertyColumns.add(el);
            }
            calculteGrowFactor(propertyColumns);
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
            final FunctionalActionElement el = new FunctionalActionElement(resultSetPrimaryEntityAction.get(), 0, FunctionalActionKind.PRIMARY_RESULT_SET);

            importPaths.add(el.importPath());
            primaryActionDom.add(el.render().clazz("primary-action").attr("hidden", null));
            primaryActionObject.append(prefix + createActionObject(el));
        }

        //////////////////// Primary result-set action [END] //////////////
        logger.debug("Initiating secondary actions...");

        final List<FunctionalActionElement> secondaryActionElements = new ArrayList<>();
        final Optional<List<EntityActionConfig>> resultSetSecondaryEntityActions = this.dslDefaultConfig.getResultSetSecondaryEntityActions();
        if (resultSetSecondaryEntityActions.isPresent()) {
            for (int i = 0; i < resultSetSecondaryEntityActions.get().size(); i++) {
                final FunctionalActionElement el = new FunctionalActionElement(resultSetSecondaryEntityActions.get().get(i), i, FunctionalActionKind.SECONDARY_RESULT_SET);
                secondaryActionElements.add(el);
            }
        }

        final DomContainer secondaryActionsDom = new DomContainer();
        final StringBuilder secondaryActionsObjects = new StringBuilder();
        for (final FunctionalActionElement el : secondaryActionElements) {
            importPaths.add(el.importPath());
            secondaryActionsDom.add(el.render().clazz("secondary-action").attr("hidden", null));
            secondaryActionsObjects.append(prefix + createActionObject(el));
        }

        logger.debug("Initiating insertion point actions...");

        final List<FunctionalActionElement> insertionPointActionsElements = new ArrayList<>();
        final Optional<List<EntityActionConfig>> insertionPointActions = this.dslDefaultConfig.getInsertionPointActions();
        if (insertionPointActions.isPresent()) {
            for (int index = 0; index < insertionPointActions.get().size(); index++) {
                final FunctionalActionElement el = new FunctionalActionElement(insertionPointActions.get().get(index), index, FunctionalActionKind.INSERTION_POINT);
                insertionPointActionsElements.add(el);
            }
        }

        final DomContainer insertionPointActionsDom = new DomContainer();
        final StringBuilder insertionPointActionsObjects = new StringBuilder();
        for (final FunctionalActionElement el : insertionPointActionsElements) {
            importPaths.add(el.importPath());
            insertionPointActionsDom.add(el.render().clazz("insertion-point-action").attr("hidden", null));
            insertionPointActionsObjects.append(prefix + createActionObject(el));
        }
        importPaths.add(dslDefaultConfig.getToolbarConfig().importPath());

        final DomContainer leftInsertionPointsDom = new DomContainer();
        final DomContainer rightInsertionPointsDom = new DomContainer();
        final DomContainer bottomInsertionPointsDom = new DomContainer();
        for (final FunctionalActionElement el : insertionPointActionsElements) {
            if (el.entityActionConfig.whereToInsertView.get() == InsertionPoints.LEFT) {
                leftInsertionPointsDom.add(new DomElement("tg-entity-centre-insertion-point").attr("id", "ip" + el.numberOfAction).attr("retrieved-entities", "{{retrievedEntities}}").attr("retrieved-totals", "{{retrievedTotals}}"));
            } else if (el.entityActionConfig.whereToInsertView.get() == InsertionPoints.RIGHT) {
                rightInsertionPointsDom.add(new DomElement("tg-entity-centre-insertion-point").attr("id", "ip" + el.numberOfAction).attr("retrieved-entities", "{{retrievedEntities}}").attr("retrieved-totals", "{{retrievedTotals}}"));
            } else if (el.entityActionConfig.whereToInsertView.get() == InsertionPoints.BOTTOM) {
                bottomInsertionPointsDom.add(new DomElement("tg-entity-centre-insertion-point").attr("id", "ip" + el.numberOfAction).attr("retrieved-entities", "{{retrievedEntities}}").attr("retrieved-totals", "{{retrievedTotals}}"));
            } else {
                throw new IllegalArgumentException("Unexpected insertion point type.");
            }
        }

        final String funcActionString = functionalActionsObjects.toString();
        final String secondaryActionString = secondaryActionsObjects.toString();
        final String insertionPointActionsString = insertionPointActionsObjects.toString();
        final String primaryActionObjectString = primaryActionObject.toString();
        final String propActionsString = propActionsObject.toString();
        final Pair<String, String> gridLayoutConfig = generateGridLayoutConfig();
        final int prefixLength = prefix.length();
        logger.debug("Initiating template...");
        final String text = ResourceLoader.getText("ua/com/fielden/platform/web/centre/tg-entity-centre-template.html");
        logger.debug("Replacing some parts...");
        final String entityCentreStr = text.
                replace("<!--@imports-->", SimpleMasterBuilder.createImports(importPaths)).
                //TODO It looks like that is not needed any longer.
                //replace("@entity_type", entityType.getSimpleName()).
                replace("@gridLayout", gridLayoutConfig.getKey()).
                replace("@full_entity_type", entityType.getName()).
                replace("@mi_type", miType.getSimpleName()).
                replace("<!--@toolbar-->", dslDefaultConfig.getToolbarConfig().render().toString()).
                replace("//toolbarGeneratedFunction", dslDefaultConfig.getToolbarConfig().code(entityType).toString()).
                replace("/*toolbarStyles*/", dslDefaultConfig.getToolbarConfig().styles().toString()).
                replace("@full_mi_type", miType.getName()).
                replace("@pageCapacity", Integer.toString(dslDefaultConfig.getPageCapacity())).
                replace("@queryEnhancerContextConfig", queryEnhancerContextConfigString()).
                replace("<!--@criteria_editors-->", editorContainer.toString()).
                replace("<!--@egi_columns-->", egiColumns.toString()).
                replace("//generatedActionObjects", funcActionString.length() > prefixLength ? funcActionString.substring(prefixLength) : funcActionString).
                replace("//generatedSecondaryActions", secondaryActionString.length() > prefixLength ? secondaryActionString.substring(prefixLength) : secondaryActionString).
                replace("//generatedInsertionPointActions", insertionPointActionsString.length() > prefixLength ? insertionPointActionsString.substring(prefixLength)
                        : insertionPointActionsString).
                replace("//generatedPrimaryAction", primaryActionObjectString.length() > prefixLength ? primaryActionObjectString.substring(prefixLength)
                        : primaryActionObjectString).
                replace("//generatedPropActions", propActionsString.length() > prefixLength ? propActionsString.substring(prefixLength)
                        : propActionsString).
                replace("//@layoutConfig", layout.code().toString()).
                replace("//gridLayoutConfig", gridLayoutConfig.getValue()).
                replace("<!--@functional_actions-->", functionalActionsDom.toString()).
                replace("<!--@primary_action-->", primaryActionDom.toString()).
                replace("<!--@secondary_actions-->", secondaryActionsDom.toString()).
                replace("<!--@insertion_point_actions-->", insertionPointActionsDom.toString()).
                replace("<!--@left_insertion_points-->", leftInsertionPointsDom.toString()).
                replace("<!--@right_insertion_points-->", rightInsertionPointsDom.toString()).
                replace("<!--@bottom_insertion_points-->", bottomInsertionPointsDom.toString());
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
     * Calculates the relative grow factor for all columns.
     */
    private void calculteGrowFactor(final List<PropertyColumnElement> propertyColumns) {
        int minWidth = 0;
        for (final PropertyColumnElement column : propertyColumns) {
            if (minWidth == 0 && column.width > 0 && column.isFlexible) {
                minWidth = column.width;
            } else if (minWidth > 0 && column.width > 0 && column.isFlexible && column.width < minWidth) {
                minWidth = column.width;
            }
        }
        for (final PropertyColumnElement column : propertyColumns) {
            if (column.isFlexible) {
                column.setGrowFactor(minWidth > 0 ? Math.round((float) column.width / minWidth) : 0);
            }
        }
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
    private void enhanceCentreManagerWithCustomProperty(final ICentreDomainTreeManagerAndEnhancer centre, final Class<?> root, final String propName, final PropDef<?> propDef, final Optional<Class<? extends ICustomPropsAssignmentHandler<? extends AbstractEntity<?>>>> resultSetCustomPropAssignmentHandlerType) {
        centre.getEnhancer().addCustomProperty(root, "" /* this is the contextPath */, propName, propDef.title, propDef.desc, propDef.type);
        centre.getEnhancer().apply();
    }

    /**
     * Creates the widgets for criteria.
     *
     * @param centre
     * @param root
     * @return
     */
    private List<AbstractCriterionWidget> createCriteriaWidgets(final ICentreDomainTreeManagerAndEnhancer centre, final Class<?> root) {
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

    /**
     * Return DSL representation for property name.
     *
     * @param name
     * @return
     */
    private static String dslName(final String name) {
        return name.equals("") ? "this" : name;
    }

    /**
     * Return domain tree representation for property name.
     *
     * @param name
     * @return
     */
    private static String treeName(final String name) {
        return name.equals("this") ? "" : name;
    }

    private CentreContextConfig getCentreContextConfigFor(final String critProp) {
        final String dslProp = dslName(critProp);
        return dslDefaultConfig.getValueMatchersForSelectionCriteria().isPresent()
                && dslDefaultConfig.getValueMatchersForSelectionCriteria().get().get(dslProp) != null
                && dslDefaultConfig.getValueMatchersForSelectionCriteria().get().get(dslProp).getValue() != null
                && dslDefaultConfig.getValueMatchersForSelectionCriteria().get().get(dslProp).getValue().isPresent()
                ? dslDefaultConfig.getValueMatchersForSelectionCriteria().get().get(dslProp).getValue().get()
                : new CentreContextConfig(false, false, false, false);
    }

    /**
     * Creates value matcher instance.
     *
     * @param injector
     * @return
     */
    public <V extends AbstractEntity<?>> Pair<IValueMatcherWithCentreContext<V>, Optional<CentreContextConfig>> createValueMatcherAndContextConfig(final Class<? extends AbstractEntity<?>> criteriaType, final String criterionPropertyName) {
        final Optional<Map<String, Pair<Class<? extends IValueMatcherWithCentreContext<? extends AbstractEntity<?>>>, Optional<CentreContextConfig>>>> matchers = dslDefaultConfig.getValueMatchersForSelectionCriteria();

        final String originalPropertyName = CentreUtils.getOriginalPropertyName(criteriaType, criterionPropertyName);
        final String dslProp = dslName(originalPropertyName);
        logger.debug("createValueMatcherAndContextConfig: propertyName = " + criterionPropertyName + " originalPropertyName = " + dslProp);
        final Class<? extends IValueMatcherWithCentreContext<V>> matcherType = matchers.isPresent() && matchers.get().containsKey(dslProp) ?
                (Class<? extends IValueMatcherWithCentreContext<V>>) matchers.get().get(dslProp).getKey() : null;
        final Pair<IValueMatcherWithCentreContext<V>, Optional<CentreContextConfig>> matcherAndContextConfig;
        if (matcherType != null) {
            matcherAndContextConfig = new Pair<>(injector.getInstance(matcherType), matchers.get().get(dslProp).getValue());
        } else {
            matcherAndContextConfig = createDefaultValueMatcherAndContextConfig(CentreUtils.getOriginalType(criteriaType), originalPropertyName, coFinder);
        }
        return matcherAndContextConfig;
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
        final boolean isEntityItself = "".equals(originalPropertyName); // empty property means "entity itself"
        final Class<V> propertyType = (Class<V>) (isEntityItself ? originalType : PropertyTypeDeterminator.determinePropertyType(originalType, originalPropertyName));
        final IEntityDao<V> co = coFinder.find(propertyType);
        return new Pair<>(new FallbackValueMatcherWithCentreContext<V>(co), Optional.empty());
    }

    public Optional<Class<? extends ICustomPropsAssignmentHandler<? extends AbstractEntity<?>>>> getCustomPropertiesAsignmentHandler() {
        return dslDefaultConfig.getResultSetCustomPropAssignmentHandlerType();
    }

    public Optional<List<ResultSetProp>> getCustomPropertiesDefinitions() {
        return dslDefaultConfig.getResultSetProperties();
    }

    public ICustomPropsAssignmentHandler<T> createAssignmentHandlerInstance(final Class<? extends ICustomPropsAssignmentHandler<T>> assignmentHandlerType) {
        return injector.getInstance(assignmentHandlerType);
    }

    public Optional<IFetchProvider<T>> getAdditionalFetchProvider() {
        return dslDefaultConfig.getFetchProvider();
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
}
