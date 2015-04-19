package ua.com.fielden.platform.web.centre;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithCentreContext;
import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dom.DomContainer;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.OrderDirection;
import ua.com.fielden.platform.web.centre.api.EntityCentreConfig.ResultSetProp;
import ua.com.fielden.platform.web.centre.api.ICentre;
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
import ua.com.fielden.platform.web.centre.api.crit.impl.StringCriterionWidget;
import ua.com.fielden.platform.web.centre.api.crit.impl.StringSingleCriterionWidget;
import ua.com.fielden.platform.web.centre.api.resultset.impl.PropertyColumnElement;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.layout.FlexLayout;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.snappy.DateRangeConditionEnum;

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
     * Generates default centre from DSL config and postCentreCreated callback.
     *
     * @param dslDefaultConfig
     * @param postCentreCreated
     * @return
     */
    private ICentreDomainTreeManagerAndEnhancer createDefaultCentre(final EntityCentreConfig<T> dslDefaultConfig, final ISerialiser serialiser, final UnaryOperator<ICentreDomainTreeManagerAndEnhancer> postCentreCreated) {
        final ICentreDomainTreeManagerAndEnhancer cdtmae = createEmptyCentre(serialiser);

        final Optional<List<String>> selectionCriteria = dslDefaultConfig.getSelectionCriteria();
        if (selectionCriteria.isPresent()) {
            for (final String property : selectionCriteria.get()) {
                cdtmae.getFirstTick().check(entityType, treeName(property), true);

                provideDefaultsFor(property, cdtmae, dslDefaultConfig);
            }
        }

        final Optional<List<ResultSetProp>> resultSetProps = dslDefaultConfig.getResultSetProperties();
        if (resultSetProps.isPresent()) {
            for (final ResultSetProp property : resultSetProps.get()) {
                if (property.propName.isPresent()) {
                    cdtmae.getSecondTick().check(entityType, treeName(property.propName.get()), true);
                } else {
                    // TODO handle 'custom property definitions' here (property.propDef)
                }
            }
        }

        final Optional<Map<String, OrderDirection>> propOrdering = dslDefaultConfig.getResultSetOrdering();
        if (propOrdering.isPresent()) {
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
                provideAssignerDefaultsSingle(assignerSupplier.get().get().get(dslProperty), cdtmae, property);
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
     * TODO this method has derived from GDTM.
     *
     * @param serialiser
     * @return
     */
    private ICentreDomainTreeManagerAndEnhancer createEmptyCentre(final ISerialiser serialiser) {
        // TODO next line of code must take in to account that the menu item is for association centre.
        final CentreDomainTreeManagerAndEnhancer c = new CentreDomainTreeManagerAndEnhancer(serialiser, new HashSet<Class<?>>() {
            {
                add(entityType);
            }
        });
        // initialise checkedProperties tree to make it more predictable in getting meta-info from "checkedProperties"
        c.getFirstTick().checkedProperties(entityType);
        c.getSecondTick().checkedProperties(entityType);

        return c;
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
     * Returns the entity centre name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    @Override
    public IRenderable build() {
        return createRenderableRepresentation();
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

    private IRenderable createRenderableRepresentation() {
        final ICentreDomainTreeManagerAndEnhancer centre = CentreUtils.getFreshCentre(getUserSpecificGdtm(), this.menuItemType);
        logger.debug("Building renderable for cdtmae:" + centre);

        final LinkedHashSet<String> importPaths = new LinkedHashSet<>();
        importPaths.add("polymer/polymer/polymer");
        importPaths.add("master/tg-entity-master");

        final FlexLayout layout = this.dslDefaultConfig.getSelectionCriteriaLayout();

        final DomElement editorContainer = layout.render();

        importPaths.add(layout.importPath());

        final Class<?> root = this.entityType;

        final List<AbstractCriterionWidget> criteriaWidgets = new ArrayList<>();
        final Class<?> managedType = centre.getEnhancer().getManagedType(root);
        for (final String critProp : centre.getFirstTick().checkedProperties(root)) {
            if (!AbstractDomainTree.isPlaceholder(critProp)) {
                final boolean isEntityItself = "".equals(critProp); // empty property means "entity itself"
                final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, critProp);

                final AbstractCriterionWidget criterionWidget;
                if (AbstractDomainTree.isCritOnlySingle(managedType, critProp)) {
                    if (EntityUtils.isEntityType(propertyType)) {
                        criterionWidget = new EntitySingleCriterionWidget(root, managedType, critProp, getCentreContextConfigFor(critProp));
                    } else if (EntityUtils.isString(propertyType)) {
                        criterionWidget = new StringSingleCriterionWidget(root, managedType, critProp);
                    } else if (EntityUtils.isBoolean(propertyType)) {
                        criterionWidget = new BooleanSingleCriterionWidget(root, managedType, critProp);
                    } else if (Integer.class.isAssignableFrom(propertyType) || Long.class.isAssignableFrom(propertyType)) {
                        criterionWidget = new IntegerSingleCriterionWidget(root, managedType, critProp);
                    } else if (BigDecimal.class.isAssignableFrom(propertyType)) { // TODO do not forget about Money later (after Money widget will be available)
                        criterionWidget = new DecimalSingleCriterionWidget(root, managedType, critProp);
                    } else if (EntityUtils.isDate(propertyType)) {
                        criterionWidget = new DateSingleCriterionWidget(root, managedType, critProp);
                    } else {
                        throw new UnsupportedOperationException(String.format("The crit-only single editor type [%s] is currently unsupported.", propertyType));
                    }
                } else {
                    if (EntityUtils.isEntityType(propertyType)) {
                        criterionWidget = new EntityCriterionWidget(root, managedType, critProp, getCentreContextConfigFor(critProp));
                    } else if (EntityUtils.isString(propertyType)) {
                        criterionWidget = new StringCriterionWidget(root, managedType, critProp);
                    } else if (EntityUtils.isBoolean(propertyType)) {
                        criterionWidget = new BooleanCriterionWidget(root, managedType, critProp);
                    } else if (Integer.class.isAssignableFrom(propertyType) || Long.class.isAssignableFrom(propertyType)) {
                        criterionWidget = new IntegerCriterionWidget(root, managedType, critProp);
                    } else if (BigDecimal.class.isAssignableFrom(propertyType)) { // TODO do not forget about Money later (after Money widget will be available)
                        criterionWidget = new DecimalCriterionWidget(root, managedType, critProp);
                    } else if (EntityUtils.isDate(propertyType)) {
                        criterionWidget = new DateCriterionWidget(root, managedType, critProp);
                    } else {
                        throw new UnsupportedOperationException(String.format("The multi / range editor type [%s] is currently unsupported.", propertyType));
                    }
                }
                criteriaWidgets.add(criterionWidget);
            }
        }
        criteriaWidgets.forEach(widget -> {
            importPaths.add(widget.importPath());
            importPaths.addAll(widget.editorsImportPaths());
            editorContainer.add(widget.render());
        });

        final List<PropertyColumnElement> propertyColumns = new ArrayList<>();
        for (final String resultProp : centre.getSecondTick().checkedProperties(root)) {
            final boolean isEntityItself = "".equals(resultProp); // empty property means "entity itself"
            final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, resultProp);

            final PropertyColumnElement el = new PropertyColumnElement(resultProp, centre.getSecondTick().getWidth(root, resultProp), propertyType, CriteriaReflector.getCriteriaTitleAndDesc(managedType, resultProp));
            propertyColumns.add(el);
        }
        final DomContainer egiColumns = new DomContainer();
        propertyColumns.forEach(column -> {
            importPaths.add(column.importPath());
            egiColumns.add(column.render());
        });

        final String entityCentreStr = ResourceLoader.getText("ua/com/fielden/platform/web/centre/tg-entity-centre-template.html").
                replace("<!--@imports-->", SimpleMasterBuilder.createImports(importPaths)).
                replace("@entity_type", entityType.getSimpleName()).
                replace("@full_entity_type", entityType.getName()).
                replace("@mi_type", miType.getName()).
                replace("<!--@criteria_editors-->", editorContainer.toString()).
                replace("<!--@egi_columns-->", egiColumns.toString());

        final IRenderable representation = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityCentreStr);
            }
        };
        return representation;
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
     * Returns the global manager for the user on this concrete thread, on which {@link #build()} was invoked.
     *
     * @return
     */
    private IGlobalDomainTreeManager getUserSpecificGdtm() {
        return injector.getInstance(IGlobalDomainTreeManager.class);
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
        logger.error("createValueMatcherAndContextConfig: propertyName = " + criterionPropertyName + " originalPropertyName = " + dslProp);
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
}
