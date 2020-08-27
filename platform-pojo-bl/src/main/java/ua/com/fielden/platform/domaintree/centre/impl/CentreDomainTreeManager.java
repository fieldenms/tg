package ua.com.fielden.platform.domaintree.centre.impl;

import static java.lang.String.format;
import static ua.com.fielden.platform.criteria.generator.impl.SynchroniseCriteriaWithModelHandler.areDifferent;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation.IAddToCriteriaTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation.IAddToResultTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.IOrderingManager;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.IWidthManager;
import ua.com.fielden.platform.domaintree.exceptions.DomainTreeException;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.EnhancementLinkedRootsSet;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * Criteria (entity centre) domain tree manager. Includes support for checking (from base {@link AbstractDomainTreeManager}). <br>
 * <br>
 *
 * Includes implementation of "checking" logic, that contain: <br>
 * a) default mutable state management; <br>
 * a) manual state management; <br>
 * b) resolution of conflicts with excluded, disabled etc. properties; <br>
 *
 * @author TG Team
 *
 */
public class CentreDomainTreeManager extends AbstractDomainTreeManager implements ICentreDomainTreeManager {
    private Boolean runAutomatically;

    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param entityFactory
     * @param rootTypes
     */
    public CentreDomainTreeManager(final EntityFactory entityFactory, final Set<Class<?>> rootTypes) {
        this(entityFactory, new CentreDomainTreeRepresentation(entityFactory, rootTypes), new AddToCriteriaTickManager(entityFactory, rootTypes), new AddToResultTickManager(), null);
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param entityFactory
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    public CentreDomainTreeManager(final EntityFactory entityFactory, final CentreDomainTreeRepresentation dtr, final AddToCriteriaTickManager firstTick, final AddToResultTickManager secondTick, final Boolean runAutomatically) {
        super(entityFactory, dtr, firstTick, secondTick);
        this.runAutomatically = runAutomatically;
    }

    @Override
    public ICentreDomainTreeRepresentation getRepresentation() {
        return (ICentreDomainTreeRepresentation) super.getRepresentation();
    }

    @Override
    public IAddToCriteriaTickManager getFirstTick() {
        return (IAddToCriteriaTickManager) super.getFirstTick();
    }

    @Override
    public IAddToResultTickManager getSecondTick() {
        return (IAddToResultTickManager) super.getSecondTick();
    }

    /**
     * A first tick manager for entity centres specific. <br>
     * <br>
     *
     * @author TG Team
     *
     */
    public static class AddToCriteriaTickManager extends TickManager implements IAddToCriteriaTickManager {

        private final transient EntityFactory entityFactory;
        private final EnhancementPropertiesMap<Object> propertiesValues1;
        private final EnhancementPropertiesMap<Object> propertiesValues2;

        private final EnhancementPropertiesMap<Boolean> propertiesExclusive1;
        private final EnhancementPropertiesMap<Boolean> propertiesExclusive2;

        private final EnhancementPropertiesMap<DateRangePrefixEnum> propertiesDatePrefixes;
        private final EnhancementPropertiesMap<MnemonicEnum> propertiesDateMnemonics;
        private final EnhancementPropertiesMap<Boolean> propertiesAndBefore;

        private final EnhancementPropertiesMap<Boolean> propertiesOrNulls;
        private final EnhancementPropertiesMap<Boolean> propertiesNots;
        private final EnhancementPropertiesMap<Integer> propertiesOrGroups;

        private Integer columnsNumber;

        private final EnhancementLinkedRootsSet rootTypes;

        /**
         * Used for the first time instantiation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr", "tr" and "serialiser"
         * fields.
         */
        public AddToCriteriaTickManager(final EntityFactory entityFactory, final Set<Class<?>> rootTypes) {
            this(AbstractDomainTree.<List<String>> createRootsMap(), entityFactory, AbstractDomainTree.<Object> createPropertiesMap(), AbstractDomainTree.<Object> createPropertiesMap(), AbstractDomainTree.<Boolean> createPropertiesMap(), AbstractDomainTree.<Boolean> createPropertiesMap(), AbstractDomainTree.<DateRangePrefixEnum> createPropertiesMap(), AbstractDomainTree.<MnemonicEnum> createPropertiesMap(), AbstractDomainTree.<Boolean> createPropertiesMap(), AbstractDomainTree.<Boolean> createPropertiesMap(), AbstractDomainTree.<Boolean> createPropertiesMap(), AbstractDomainTree.<Integer> createPropertiesMap(), null, rootTypes);
        }

        /**
         * A tick <i>manager</i> constructor.
         *
         * @param serialiser
         */
        public AddToCriteriaTickManager(final Map<Class<?>, List<String>> checkedProperties, final EntityFactory entityFactory, final Map<Pair<Class<?>, String>, Object> propertiesValues1, final Map<Pair<Class<?>, String>, Object> propertiesValues2, final Map<Pair<Class<?>, String>, Boolean> propertiesExclusive1, final Map<Pair<Class<?>, String>, Boolean> propertiesExclusive2, final Map<Pair<Class<?>, String>, DateRangePrefixEnum> propertiesDatePrefixes, final Map<Pair<Class<?>, String>, MnemonicEnum> propertiesDateMnemonics, final Map<Pair<Class<?>, String>, Boolean> propertiesAndBefore, final Map<Pair<Class<?>, String>, Boolean> propertiesOrNulls, final Map<Pair<Class<?>, String>, Boolean> propertiesNots, final Map<Pair<Class<?>, String>, Integer> propertiesOrGroups, final Integer columnsNumber, final Set<Class<?>> rootTypes) {
            super(checkedProperties);
            this.entityFactory = entityFactory;

            this.propertiesValues1 = createPropertiesMap();
            this.propertiesValues1.putAll(propertiesValues1);
            this.propertiesValues2 = createPropertiesMap();
            this.propertiesValues2.putAll(propertiesValues2);
            this.propertiesExclusive1 = createPropertiesMap();
            this.propertiesExclusive1.putAll(propertiesExclusive1);
            this.propertiesExclusive2 = createPropertiesMap();
            this.propertiesExclusive2.putAll(propertiesExclusive2);
            this.propertiesDatePrefixes = createPropertiesMap();
            this.propertiesDatePrefixes.putAll(propertiesDatePrefixes);
            this.propertiesDateMnemonics = createPropertiesMap();
            this.propertiesDateMnemonics.putAll(propertiesDateMnemonics);
            this.propertiesAndBefore = createPropertiesMap();
            this.propertiesAndBefore.putAll(propertiesAndBefore);
            this.propertiesOrNulls = createPropertiesMap();
            this.propertiesOrNulls.putAll(propertiesOrNulls);
            this.propertiesNots = createPropertiesMap();
            this.propertiesNots.putAll(propertiesNots);
            this.propertiesOrGroups = createPropertiesMap();
            this.propertiesOrGroups.putAll(propertiesOrGroups);

            this.columnsNumber = columnsNumber;

            this.rootTypes = new EnhancementLinkedRootsSet();
            this.rootTypes.addAll(rootTypes);
        }

        @Override
        protected IAddToCriteriaTickRepresentation tr() {
            return (IAddToCriteriaTickRepresentation) super.tr();
        }

        @Override
        protected boolean isCheckedMutably(final Class<?> root, final String property) {
            return super.isCheckedMutably(root, property) || property.endsWith("mutablyCheckedProp");
        }

        @Override
        public Set<Class<?>> rootTypes() {
            return rootTypes;
        }

        @Override
        public IAddToCriteriaTickManager check(final Class<?> root, final String property, final boolean check) {
            super.check(root, property, check);
            // TODO need to encapsulate logic for removing values as well as metadata associated with an unchecked property into a separate method.
            if (!check) {
                propertiesValues1.remove(key(root, property));
                propertiesValues2.remove(key(root, property));
            }
            return this;
        }

        protected Integer columnsNumber() {
            return columnsNumber;
        }

        @Override
        public int getColumnsNumber() {
            return columnsNumber == null ? 2 : columnsNumber;
        }

        @Override
        public IAddToCriteriaTickManager setColumnsNumber(final int columnsNumber) {
            if (columnsNumber <= 0) {
                throw new DomainTreeException("Columns number cannot be <= 0. Please change columns number [" + columnsNumber + "] to some more appropriate value.");
            }
            this.columnsNumber = Integer.valueOf(columnsNumber);

            for (final Class<?> root : rootTypes()) { //
                if (checkedProperties().get(root) != null) { // not yet loaded
                    supplementTheMatrixWithPlaceholders(root);
                    cropEmptyRows(root);
                }
            }

            return this;
        }

        @Override
        public Object getValue(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "Could not get a 'value' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            return (propertiesValues1.containsKey(key(root, property))) ? propertiesValues1.get(key(root, property)) : tr().getValueByDefault(root, property);
        }

        @Override
        public boolean isValueEmpty(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "Could not ask whether 'value' is empty for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            final Object value = (propertiesValues1.containsKey(key(root, property))) ? propertiesValues1.get(key(root, property)) : tr().getValueByDefault(root, property);
            return EntityUtils.equalsEx(value, tr().getEmptyValueFor(root, property));
        }

        @Override
        public IAddToCriteriaTickManager setValue(final Class<?> root, final String propertyName, final Object value) {
            illegalUncheckedProperties(this, root, propertyName, "Could not set a 'value' for 'unchecked' property [" + propertyName + "] in type ["
                    + root.getSimpleName() + "].");
            final Object oldValue = getValue(root, propertyName);
            final Object defaultValue = tr().getValueByDefault(root, propertyName);
            if (EntityUtils.equalsEx(value, defaultValue)) {
                propertiesValues1.remove(key(root, propertyName));
            } else {
                propertiesValues1.put(key(root, propertyName), value);
            }
            return this;
        }

        @Override
        public Object getValue2(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "Could not get a 'value 2' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            illegalNonDoubleEditorProperties(root, property, "Could not get a 'value 2' for 'non-double (or boolean) editor' property [" + property
                    + "] in type [" + root.getSimpleName() + "].");
            return (propertiesValues2.containsKey(key(root, property))) ? propertiesValues2.get(key(root, property)) : tr().getValue2ByDefault(root, property);
        }

        @Override
        public boolean is2ValueEmpty(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "Could not ask whether 'value 2' is empty for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            illegalNonDoubleEditorProperties(root, property, "Could not ask whether 'value 2' is empty for 'non-double (or boolean) editor' property ["
                    + property + "] in type [" + root.getSimpleName() + "].");
            final Object value = (propertiesValues2.containsKey(key(root, property))) ? propertiesValues2.get(key(root, property)) : tr().getValue2ByDefault(root, property);
            return EntityUtils.equalsEx(value, tr().get2EmptyValueFor(root, property));
        }

        @Override
        public IAddToCriteriaTickManager setValue2(final Class<?> root, final String propertyName, final Object value2) {
            illegalUncheckedProperties(this, root, propertyName, "Could not set a 'value 2' for 'unchecked' property [" + propertyName + "] in type ["
                    + root.getSimpleName() + "].");
            illegalNonDoubleEditorProperties(root, propertyName, "Could not set a 'value 2' for 'non-double (or boolean) editor' property [" + propertyName
                    + "] in type [" + root.getSimpleName() + "].");
            final Object oldValue2 = getValue2(root, propertyName);

            final Object defaultValue2 = tr().getValue2ByDefault(root, propertyName);
            if (EntityUtils.equalsEx(value2, defaultValue2)) {
                propertiesValues2.remove(key(root, propertyName));
            } else {
                propertiesValues2.put(key(root, propertyName), value2);
            }
            return this;
        }

        @Override
        public Boolean getExclusive(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "Could not get an 'exclusive' flag for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            illegalNonDoubleEditorOrBooleanProperties(root, property, "Could not get an 'exclusive' flag for 'non-double editor' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            return (propertiesExclusive1.containsKey(key(root, property))) ? propertiesExclusive1.get(key(root, property)) : null;
        }

        @Override
        public IAddToCriteriaTickManager setExclusive(final Class<?> root, final String property, final Boolean exclusive) {
            illegalUncheckedProperties(this, root, property, "Could not set an 'exclusive' flag for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            illegalNonDoubleEditorOrBooleanProperties(root, property, "Could not set an 'exclusive' flag for 'non-double editor' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            if (exclusive == null) {
                propertiesExclusive1.remove(key(root, property));
            } else {
                propertiesExclusive1.put(key(root, property), exclusive);
            }
            return this;
        }

        @Override
        public Boolean getExclusive2(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "Could not get an 'exclusive 2' flag for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            illegalNonDoubleEditorOrBooleanProperties(root, property, "Could not get an 'exclusive 2' flag for 'non-double editor' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            return (propertiesExclusive2.containsKey(key(root, property))) ? propertiesExclusive2.get(key(root, property)) : null;
        }

        @Override
        public IAddToCriteriaTickManager setExclusive2(final Class<?> root, final String property, final Boolean exclusive2) {
            illegalUncheckedProperties(this, root, property, "Could not set an 'exclusive 2' flag for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            illegalNonDoubleEditorOrBooleanProperties(root, property, "Could not set an 'exclusive 2' flag for 'non-double editor' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            if (exclusive2 == null) {
                propertiesExclusive2.remove(key(root, property));
            } else {
                propertiesExclusive2.put(key(root, property), exclusive2);
            }
            return this;
        }

        @Override
        public DateRangePrefixEnum getDatePrefix(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "Could not get a 'date prefix' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            illegalType(root, property, "Could not get a 'date prefix' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
            return (propertiesDatePrefixes.containsKey(key(root, property))) ? propertiesDatePrefixes.get(key(root, property)) : null;
        }

        @Override
        public IAddToCriteriaTickManager setDatePrefix(final Class<?> root, final String property, final DateRangePrefixEnum datePrefix) {
            illegalUncheckedProperties(this, root, property, "Could not set a 'date prefix' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            illegalType(root, property, "Could not set a 'date prefix' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
            if (datePrefix == null) {
                propertiesDatePrefixes.remove(key(root, property));
            } else {
                propertiesDatePrefixes.put(key(root, property), datePrefix);
            }
            return this;
        }

        @Override
        public MnemonicEnum getDateMnemonic(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "Could not get a 'date mnemonic' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            illegalType(root, property, "Could not get a 'date mnemonic' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
            return (propertiesDateMnemonics.containsKey(key(root, property))) ? propertiesDateMnemonics.get(key(root, property)) : null;
        }

        @Override
        public IAddToCriteriaTickManager setDateMnemonic(final Class<?> root, final String property, final MnemonicEnum dateMnemonic) {
            illegalUncheckedProperties(this, root, property, "Could not set a 'date mnemonic' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            illegalType(root, property, "Could not set a 'date mnemonic' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
            if (dateMnemonic == null) {
                propertiesDateMnemonics.remove(key(root, property));
            } else {
                propertiesDateMnemonics.put(key(root, property), dateMnemonic);
            }
            return this;
        }

        @Override
        public Boolean getAndBefore(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "Could not get an 'and before' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            illegalType(root, property, "Could not get an 'and before' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
            return (propertiesAndBefore.containsKey(key(root, property))) ? propertiesAndBefore.get(key(root, property)) : null;
        }

        @Override
        public IAddToCriteriaTickManager setAndBefore(final Class<?> root, final String property, final Boolean andBefore) {
            illegalUncheckedProperties(this, root, property, "Could not set an 'and before' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            illegalType(root, property, "Could not set an 'and before' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
            if (andBefore == null) {
                propertiesAndBefore.remove(key(root, property));
            } else {
                propertiesAndBefore.put(key(root, property), andBefore);
            }
            return this;
        }

        @Override
        public Boolean getOrNull(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "Could not get an 'or null' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            return (propertiesOrNulls.containsKey(key(root, property))) ? propertiesOrNulls.get(key(root, property)) : null;
        }

        @Override
        public IAddToCriteriaTickManager setOrNull(final Class<?> root, final String property, final Boolean orNull) {
            illegalUncheckedProperties(this, root, property, "Could not set an 'or null' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            if (orNull == null) {
                propertiesOrNulls.remove(key(root, property));
            } else {
                propertiesOrNulls.put(key(root, property), orNull);
            }
            return this;
        }

        @Override
        public Boolean getNot(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "Could not get a 'not' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            return (propertiesNots.containsKey(key(root, property))) ? propertiesNots.get(key(root, property)) : null;
        }

        @Override
        public IAddToCriteriaTickManager setNot(final Class<?> root, final String property, final Boolean not) {
            illegalUncheckedProperties(this, root, property, "Could not set a 'not' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            if (not == null) {
                propertiesNots.remove(key(root, property));
            } else {
                propertiesNots.put(key(root, property), not);
            }
            return this;
        }
        
        @Override
        public Integer getOrGroup(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, format("Could not get an 'or group' for 'unchecked' property [%s] in type [%s].", property, root.getSimpleName()));
            return (propertiesOrGroups.containsKey(key(root, property))) ? propertiesOrGroups.get(key(root, property)) : null;
        }
        
        @Override
        public IAddToCriteriaTickManager setOrGroup(final Class<?> root, final String property, final Integer orGroup) {
            illegalUncheckedProperties(this, root, property, format("Could not set an 'or group' for 'unchecked' property [%s] in type [%s].", property, root.getSimpleName()));
            if (orGroup == null) {
                propertiesOrGroups.remove(key(root, property));
            } else {
                propertiesOrGroups.put(key(root, property), orGroup);
            }
            return this;
        }
        
        /////////////////// Checked properties with placeholders ///////////////////
        @Override
        public IAddToCriteriaTickManager swap(final Class<?> root, final String property1, final String property2) {
            super.swap(root, property1, property2);

            cropEmptyRows(root);
            return this;
        }

        @Override
        public IAddToCriteriaTickManager move(final Class<?> root, final String what, final String beforeWhat) {
            throw new UnsupportedOperationException("Move operation is not supported for Centre domain tree manager's first tick. Please use perhaps 'swap' operation.");
        }

        @Override
        public IAddToCriteriaTickManager moveToTheEnd(final Class<?> root, final String what) {
            throw new UnsupportedOperationException("MoveToTheEnd operation is not supported for Centre domain tree manager's first tick. Please use perhaps 'swap' operation.");
        }

        @Override
        protected void removeCheckedProperty(final Class<?> root, final String property) {
            final int removalIndex = checkedPropertiesMutable(root).indexOf(property);
            super.removeCheckedProperty(root, property);
            super.insertCheckedProperty(root, generatePlaceholderName(root, removalIndex), removalIndex);

            cropEmptyRows(root);
        }

        /**
         * Removes the rows of placeholders in the matrix of checked properties to form a matrix without empty rows.
         *
         * @param root
         */
        protected final void cropEmptyRows(final Class<?> root) {
            cropEmptyRows(root, checkedPropertiesMutable(root).size() - 1);
        }

        private final void cropEmptyRows(final Class<?> root, final int index) {
            if (index < 0) {
                return;
            }
            boolean isEmptyRow = true;
            for (int i = index; i >= index - getColumnsNumber() + 1; i--) {
                if (!isPlaceholder(checkedPropertiesMutable(root).get(i))) {
                    isEmptyRow = false;
                    break;
                }
            }
            if (isEmptyRow) {
                for (int i = index; i >= index - getColumnsNumber() + 1; i--) {
                    super.removeCheckedProperty(root, checkedPropertiesMutable(root).get(i));
                }
            }
            cropEmptyRows(root, index - getColumnsNumber());
        }

        @Override
        protected void insertCheckedProperty(final Class<?> root, final String property, final int index) {
            final int firstPlaceholderIndex = findFirstPlaceholder(root);
            if (firstPlaceholderIndex < checkedPropertiesMutable(root).size()) { // there is at least one placeholder in checked properties matrix
                super.removeCheckedProperty(root, checkedPropertiesMutable(root).get(firstPlaceholderIndex));
                super.insertCheckedProperty(root, property, firstPlaceholderIndex);
            } else {
                super.insertCheckedProperty(root, property, checkedPropertiesMutable(root).size());
                supplementTheMatrixWithPlaceholders(root);
            }
        }

        /**
         * Adds the placeholders to the end of the checked properties list to form a full matrix with a columns number defined in {@link #getColumnsNumber()}.
         *
         * @param root
         */
        protected final void supplementTheMatrixWithPlaceholders(final Class<?> root) {
            while (checkedPropertiesMutable(root).size() % getColumnsNumber() != 0) {
                final int newPlaceholderIndex = checkedPropertiesMutable(root).size();
                super.insertCheckedProperty(root, generatePlaceholderName(root, newPlaceholderIndex), newPlaceholderIndex);
            }
        }

        private String generatePlaceholderName(final Class<?> root, final int newPlaceholderIndex) {
            int max = -1;
            final List<String> checkedProperties = checkedPropertiesMutable(root);
            for (int i = 0; i < checkedProperties.size(); i++) {
                final String name = checkedProperties.get(i);
                if (isPlaceholder(name)) {
                    final int placeholderNumber = Integer.valueOf(name.substring(0, name.indexOf(PLACEHOLDER)));
                    if (placeholderNumber > max) {
                        max = placeholderNumber;
                    }
                }
            }
            return (max + 1) + PLACEHOLDER + (newPlaceholderIndex / getColumnsNumber()) + "-" + (newPlaceholderIndex % getColumnsNumber());
        }

        private int findFirstPlaceholder(final Class<?> root) {
            final List<String> checkedProperties = checkedPropertiesMutable(root);
            for (int i = 0; i < checkedProperties.size(); i++) {
                if (isPlaceholder(checkedProperties.get(i))) {
                    return i;
                }
            }
            return checkedProperties.size();
        }

        /////////////////// Checked properties with placeholders (END) ///////////////////

        protected EntityFactory getEntityFactory() {
            return entityFactory;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((columnsNumber == null) ? 0 : columnsNumber.hashCode());
            result = prime * result + (rootTypes == null ? 0 : rootTypes.hashCode());
            result = prime * result + propertiesAndBefore.hashCode();
            result = prime * result + propertiesDateMnemonics.hashCode();
            result = prime * result + propertiesDatePrefixes.hashCode();
            result = prime * result + propertiesExclusive1.hashCode();
            result = prime * result + propertiesExclusive2.hashCode();
            result = prime * result + propertiesOrGroups.hashCode();
            result = prime * result + propertiesNots.hashCode();
            result = prime * result + propertiesOrNulls.hashCode();
            result = prime * result + propertiesValues1.hashCode();
            result = prime * result + propertiesValues2.hashCode();
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AddToCriteriaTickManager other = (AddToCriteriaTickManager) obj;
            if (columnsNumber == null) {
                if (other.columnsNumber != null) {
                    return false;
                }
            } else if (!columnsNumber.equals(other.columnsNumber)) {
                return false;
            }
            if (rootTypes == null) {
                if (other.rootTypes != null) {
                    return false;
                }
            } else if (!rootTypes.equals(other.rootTypes)) {
                return false;
            }
            if (!propertiesAndBefore.equals(other.propertiesAndBefore)) {
                return false;
            }
            if (!propertiesDateMnemonics.equals(other.propertiesDateMnemonics)) {
                return false;
            }
            if (!propertiesDatePrefixes.equals(other.propertiesDatePrefixes)) {
                return false;
            }
            if (!propertiesExclusive1.equals(other.propertiesExclusive1)) {
                return false;
            }
            if (!propertiesExclusive2.equals(other.propertiesExclusive2)) {
                return false;
            }
            if (!propertiesOrGroups.equals(other.propertiesOrGroups)) {
                return false;
            }
            if (!propertiesNots.equals(other.propertiesNots)) {
                return false;
            }
            if (!propertiesOrNulls.equals(other.propertiesOrNulls)) {
                return false;
            }
            if (propertyValuesDifferent(propertiesValues1, other.propertiesValues1)) {
                return false;
            }
            if (!propertiesValues2.equals(other.propertiesValues2)) {
                return false;
            }
            return true;
        }

    }
    
    /**
     * Compares <code>propertiesValues1</code> with <code>propertiesValues2</code>.
     * If they are equal using standard logic then we need to compare their values one by one with the check on 'not found mock' entities.
     * <p>
     * This logic is condensed to only {@link AddToCriteriaTickManager#propertiesValues1} due to the fact that this is the only place where 'not found mocks' can reside.
     * We don't need to override 'hashCode' because we do not place {@link AddToCriteriaTickManager} and its wrappers into hash-sets or maps as a keys.
     * 
     * @param propertiesValues1
     * @param propertiesValues2
     * @return
     */
    private static boolean propertyValuesDifferent(final EnhancementPropertiesMap<Object> propertiesValues1, final EnhancementPropertiesMap<Object> propertiesValues2) {
        final boolean different = !propertiesValues1.equals(propertiesValues2);
        if (!different) { // there is a chance that inside some entity-typed crit-only single property we will have two mocks; in that case we should compare their 'desc'
            return propertiesValues1.entrySet().stream().anyMatch(entry -> areDifferent(entry.getValue(), propertiesValues2.get(entry.getKey())));
        }
        return different;
    }
    
    /**
     * A second tick manager for entity centres specific. <br>
     * <br>
     *
     * @author TG Team
     *
     */
    public static class AddToResultTickManager extends TickManager implements IAddToResultTickManager {
        private final EnhancementPropertiesMap<Integer> propertiesWidths;
        private final EnhancementPropertiesMap<Integer> propertiesGrowFactors;
        private final EnhancementRootsMap<List<Pair<String, Ordering>>> rootsListsOfOrderings;
        private int pageCapacity;
        private int maxPageCapacity;
        private int visibleRowsCount;
        private int numberOfHeaderLines;

        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
         * fields.
         */
        public AddToResultTickManager() {
            super();
            propertiesWidths = createPropertiesMap();
            propertiesGrowFactors = createPropertiesMap();
            rootsListsOfOrderings = createRootsMap();
        }

        @Override
        protected IAddToResultTickRepresentation tr() {
            return (IAddToResultTickRepresentation) super.tr();
        }

        @Override
        public List<Pair<String, Ordering>> orderedProperties(final Class<?> root) {
            if (rootsListsOfOrderings.containsKey(root)) {
                return rootsListsOfOrderings.get(root);
            } else {
                final List<Pair<String, Ordering>> orderedPropertiesByDefault = new ArrayList<>(tr().orderedPropertiesByDefault(root));
                final List<Pair<String, Ordering>> orderedPropertiesByDefaultWithoutUnchecked = new ArrayList<>(orderedPropertiesByDefault);
                for (final Pair<String, Ordering> propAndOrdering : orderedPropertiesByDefault) {
                    if (!isChecked(root, propAndOrdering.getKey())) {
                        orderedPropertiesByDefaultWithoutUnchecked.remove(propAndOrdering);
                    }
                }
                return orderedPropertiesByDefaultWithoutUnchecked;
            }
        }

        @Override
        public IOrderingManager toggleOrdering(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "Could not toggle 'ordering' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            if (!rootsListsOfOrderings.containsKey(root)) {
                rootsListsOfOrderings.put(root, new ArrayList<>(orderedProperties(root)));
            }
            final List<Pair<String, Ordering>> list = new ArrayList<>(rootsListsOfOrderings.get(root));
            for (final Pair<String, Ordering> pair : list) {
                if (pair.getKey().equals(property)) {
                    final int index = rootsListsOfOrderings.get(root).indexOf(pair);
                    if (Ordering.ASCENDING.equals(pair.getValue())) {
                        rootsListsOfOrderings.get(root).get(index).setValue(Ordering.DESCENDING);
                    } else { // Ordering.DESCENDING
                        rootsListsOfOrderings.get(root).remove(index);
                    }
                    return this;
                }
            } // if the property does not have an Ordering assigned -- put a ASC ordering to it (into the end of the list)
            rootsListsOfOrderings.get(root).add(new Pair<>(property, Ordering.ASCENDING));
            return this;
        }

        @Override
        protected void removeCheckedProperty(final Class<?> root, final String property) {
            super.removeCheckedProperty(root, property);

            if (rootsListsOfOrderings.containsKey(root)) {
                final List<Pair<String, Ordering>> list = new ArrayList<>(rootsListsOfOrderings.get(root));
                for (final Pair<String, Ordering> pair : list) {
                    if (pair.getKey().equals(property)) {
                        final int index = rootsListsOfOrderings.get(root).indexOf(pair);
                        rootsListsOfOrderings.get(root).remove(index); // removes an ordering associated with just unchecked property
                        return;
                    }
                }
            }
        }

        @Override
        public int getWidth(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "Could not get a 'width' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            return (propertiesWidths.containsKey(key(root, property))) ? propertiesWidths.get(key(root, property)) : tr().getWidthByDefault(root, property);
        }

        @Override
        public IWidthManager setWidth(final Class<?> root, final String property, final int width) {
            illegalUncheckedProperties(this, root, property, "Could not set a 'width' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            propertiesWidths.put(key(root, property), width);
            return this;
        }

        @Override
        public int getGrowFactor(final Class<?> root, final String property) {
            illegalUncheckedProperties(this, root, property, "Could not get a 'grow factor' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            return (propertiesGrowFactors.containsKey(key(root, property))) ? propertiesGrowFactors.get(key(root, property)) : 0;
        }

        @Override
        public IAddToResultTickManager setGrowFactor(final Class<?> root, final String property, final int width) {
            illegalUncheckedProperties(this, root, property, "Could not set a 'grow factor' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            propertiesGrowFactors.put(key(root, property), width);
            return this;
        }

        @Override
        public T2<EnhancementPropertiesMap<Integer>, EnhancementPropertiesMap<Integer>> getWidthsAndGrowFactors() {
            return t2(propertiesWidths, propertiesGrowFactors);
        }

        @Override
        public void setWidthsAndGrowFactors(final T2<EnhancementPropertiesMap<Integer>, EnhancementPropertiesMap<Integer>> widthsAndGrowFactors) {
            propertiesWidths.clear();
            propertiesWidths.putAll(widthsAndGrowFactors._1);
            propertiesGrowFactors.clear();
            propertiesGrowFactors.putAll(widthsAndGrowFactors._2);
        }

        @Override
        public int getPageCapacity() {
            return pageCapacity;
        }

        @Override
        public IAddToResultTickManager setPageCapacity(final int pageCapacity) {
            this.pageCapacity = pageCapacity;
            return this;
        }

        @Override
        public int getMaxPageCapacity() {
            return maxPageCapacity;
        }

        @Override
        public IAddToResultTickManager setMaxPageCapacity(final int maxPageCapacity) {
            this.maxPageCapacity = maxPageCapacity;
            return this;
        }

        @Override
        public int getVisibleRowsCount() {
            return visibleRowsCount;
        }

        @Override
        public IAddToResultTickManager setVisibleRowsCount(final int visibleRowsCount) {
            this.visibleRowsCount = visibleRowsCount;
            return this;
        }

        @Override
        public int getNumberOfHeaderLines() {
            return numberOfHeaderLines;
        }

        @Override
        public IAddToResultTickManager setNumberOfHeaderLines(final int numberOfHeaderLines) {
            this.numberOfHeaderLines = numberOfHeaderLines;
            return this;
        }

        @Override
        public int hashCode() {
            return 31 * super.hashCode() + Objects.hash(propertiesGrowFactors, propertiesWidths, rootsListsOfOrderings, pageCapacity, maxPageCapacity, visibleRowsCount, numberOfHeaderLines);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this != obj) {
                if (super.equals(obj) && getClass() == obj.getClass()) {
                    final AddToResultTickManager other = (AddToResultTickManager) obj;
                    return Objects.equals(propertiesGrowFactors, other.propertiesGrowFactors) &&
                            Objects.equals(propertiesWidths, other.propertiesWidths) &&
                            Objects.equals(rootsListsOfOrderings, other.rootsListsOfOrderings) &&
                            pageCapacity == other.pageCapacity &&
                            maxPageCapacity == other.maxPageCapacity &&
                            numberOfHeaderLines == other.numberOfHeaderLines &&
                            visibleRowsCount == other.visibleRowsCount;
                }
                return false;
            }
            return true;
        }
    }

    protected Boolean isRunAutomatically1() {
        return runAutomatically;
    }

    @Override
    public boolean isRunAutomatically() {
        return runAutomatically != null ? runAutomatically : false; // should be disabled by default
    }

    @Override
    public ICentreDomainTreeManager setRunAutomatically(final boolean runAutomatically) {
        this.runAutomatically = runAutomatically;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((runAutomatically == null) ? 0 : runAutomatically.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CentreDomainTreeManager other = (CentreDomainTreeManager) obj;
        if (runAutomatically == null) {
            if (other.runAutomatically != null) {
                return false;
            }
        } else if (!runAutomatically.equals(other.runAutomatically)) {
            return false;
        }
        return true;
    }

    protected Boolean runAutomatically() {
        return runAutomatically;
    }
}
