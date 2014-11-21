package ua.com.fielden.platform.domaintree.centre.impl;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.domaintree.ILocatorManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager.IPropertyValueListener;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation.IAddToCriteriaTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation.IAddToResultTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingManager;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.IWidthManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap;
import ua.com.fielden.platform.domaintree.impl.LocatorManager;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.kryo.serialisers.TgSimpleSerializer;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

import com.esotericsoftware.kryo.Kryo;

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
     * @param serialiser
     * @param rootTypes
     */
    public CentreDomainTreeManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
        this(serialiser, new CentreDomainTreeRepresentation(serialiser, rootTypes), new AddToCriteriaTickManager(serialiser, rootTypes), new AddToResultTickManager(), null);
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    public CentreDomainTreeManager(final ISerialiser serialiser, final CentreDomainTreeRepresentation dtr, final AddToCriteriaTickManager firstTick, final AddToResultTickManager secondTick, final Boolean runAutomatically) {
        super(serialiser, dtr, firstTick, secondTick);
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

    @Override
    protected ISerialiser getSerialiser() {
        return super.getSerialiser();
    }

    private static class WeakPropertyValueListener implements IPropertyValueListener {

        private final IAddToCriteriaTickManager tickManager;
        private final WeakReference<IPropertyValueListener> ref;
        private final boolean isFirst;

        public WeakPropertyValueListener(final IAddToCriteriaTickManager tickManager, final IPropertyValueListener listener, final boolean isFirst) {
            this.tickManager = tickManager;
            this.ref = new WeakReference<IPropertyValueListener>(listener);
            this.isFirst = isFirst;
        }

        @Override
        public void propertyStateChanged(final Class<?> root, final String property, final Object newValue, final Object oldState) {
            if (ref.get() != null) {
                ref.get().propertyStateChanged(root, property, newValue, oldState);
            } else {
                if (isFirst) {
                    tickManager.removePropertyValueListener(this);
                } else {
                    tickManager.removePropertyValue2Listener(this);
                }
            }
        }

        public IPropertyValueListener getRef() {
            return ref.get();
        }

    }

    /**
     * A first tick manager for entity centres specific. <br>
     * <br>
     *
     * @author TG Team
     *
     */
    public static class AddToCriteriaTickManager extends TickManager implements IAddToCriteriaTickManager, ILocatorManager {

        private final transient ISerialiser serialiser;
        private final EnhancementPropertiesMap<Object> propertiesValues1;
        private final EnhancementPropertiesMap<Object> propertiesValues2;

        private final EnhancementPropertiesMap<Boolean> propertiesExclusive1;
        private final EnhancementPropertiesMap<Boolean> propertiesExclusive2;

        private final EnhancementPropertiesMap<DateRangePrefixEnum> propertiesDatePrefixes;
        private final EnhancementPropertiesMap<MnemonicEnum> propertiesDateMnemonics;
        private final EnhancementPropertiesMap<Boolean> propertiesAndBefore;

        private final EnhancementPropertiesMap<Boolean> propertiesOrNulls;
        private final EnhancementPropertiesMap<Boolean> propertiesNots;

        private Integer columnsNumber;

        private final LocatorManager locatorManager;

        private final transient EventListenerList propertyValueListeners, propertyValue2Listeners;

        /**
         * Used for the first time instantiation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr", "tr" and "serialiser"
         * fields.
         */
        public AddToCriteriaTickManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
            this(AbstractDomainTree.<List<String>> createRootsMap(), serialiser, AbstractDomainTree.<Object> createPropertiesMap(), AbstractDomainTree.<Object> createPropertiesMap(), AbstractDomainTree.<Boolean> createPropertiesMap(), AbstractDomainTree.<Boolean> createPropertiesMap(), AbstractDomainTree.<DateRangePrefixEnum> createPropertiesMap(), AbstractDomainTree.<MnemonicEnum> createPropertiesMap(), AbstractDomainTree.<Boolean> createPropertiesMap(), AbstractDomainTree.<Boolean> createPropertiesMap(), AbstractDomainTree.<Boolean> createPropertiesMap(), null, new LocatorManager(serialiser, rootTypes));
        }

        /**
         * A tick <i>manager</i> constructor.
         *
         * @param serialiser
         */
        public AddToCriteriaTickManager(final Map<Class<?>, List<String>> checkedProperties, final ISerialiser serialiser, final Map<Pair<Class<?>, String>, Object> propertiesValues1, final Map<Pair<Class<?>, String>, Object> propertiesValues2, final Map<Pair<Class<?>, String>, Boolean> propertiesExclusive1, final Map<Pair<Class<?>, String>, Boolean> propertiesExclusive2, final Map<Pair<Class<?>, String>, DateRangePrefixEnum> propertiesDatePrefixes, final Map<Pair<Class<?>, String>, MnemonicEnum> propertiesDateMnemonics, final Map<Pair<Class<?>, String>, Boolean> propertiesAndBefore, final Map<Pair<Class<?>, String>, Boolean> propertiesOrNulls, final Map<Pair<Class<?>, String>, Boolean> propertiesNots, final Integer columnsNumber, final LocatorManager locatorManager) {
            super(checkedProperties);
            this.serialiser = serialiser;

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

            this.columnsNumber = columnsNumber;

            this.locatorManager = locatorManager;

            this.propertyValueListeners = new EventListenerList();
            this.propertyValue2Listeners = new EventListenerList();
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
            return locatorManager.rootTypes();
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

        @Override
        public ILocatorManager refreshLocatorManager(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not refresh a locator for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            locatorManager.refreshLocatorManager(root, property);
            return this;
        }

        @Override
        public ILocatorManager resetLocatorManagerToDefault(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not reset a locator for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            locatorManager.resetLocatorManagerToDefault(root, property);
            return this;
        }

        @Override
        public ILocatorManager acceptLocatorManager(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not accept a locator for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            locatorManager.acceptLocatorManager(root, property);
            return this;
        }

        @Override
        public ILocatorManager discardLocatorManager(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not discard a locator for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            locatorManager.discardLocatorManager(root, property);
            return this;
        }

        @Override
        public ILocatorManager saveLocatorManagerGlobally(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not save globally a locator for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            locatorManager.saveLocatorManagerGlobally(root, property);
            return this;
        }

        @Override
        public ILocatorManager freezeLocatorManager(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not freeze a locator for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            locatorManager.freezeLocatorManager(root, property);
            return this;
        }

        @Override
        public ILocatorDomainTreeManagerAndEnhancer getLocatorManager(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get a locator for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            return locatorManager.getLocatorManager(root, property);
        }

        @Override
        public Pair<Phase, Type> phaseAndTypeOfLocatorManager(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not ask a locator state for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            return locatorManager.phaseAndTypeOfLocatorManager(root, property);
        }

        @Override
        public boolean isChangedLocatorManager(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not ask whether a locator has been changed for 'unchecked' property [" + property
                    + "] in type [" + root.getSimpleName() + "].");
            return locatorManager.isChangedLocatorManager(root, property);
        }

        @Override
        public List<Pair<Class<?>, String>> locatorKeys() {
            return locatorManager.locatorKeys();
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
                throw new IllegalArgumentException("Columns number cannot be <= 0. Please change columns number [" + columnsNumber + "] to some more appropriate value.");
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
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get a 'value' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            return (propertiesValues1.containsKey(key(root, property))) ? propertiesValues1.get(key(root, property)) : tr().getValueByDefault(root, property);
        }

        @Override
        public boolean isValueEmpty(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not ask whether 'value' is empty for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            final Object value = (propertiesValues1.containsKey(key(root, property))) ? propertiesValues1.get(key(root, property)) : tr().getValueByDefault(root, property);
            return EntityUtils.equalsEx(value, tr().getEmptyValueFor(root, property));
        }

        @Override
        public IAddToCriteriaTickManager setValue(final Class<?> root, final String propertyName, final Object value) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, propertyName, "Could not set a 'value' for 'unchecked' property [" + propertyName + "] in type ["
                    + root.getSimpleName() + "].");
            final Object oldValue = getValue(root, propertyName);
            final Object defaultValue = tr().getValueByDefault(root, propertyName);
            if (EntityUtils.equalsEx(value, defaultValue)) {
                propertiesValues1.remove(key(root, propertyName));
            } else {
                propertiesValues1.put(key(root, propertyName), value);
            }
            for (final IPropertyValueListener listener : propertyValueListeners.getListeners(IPropertyValueListener.class)) {
                listener.propertyStateChanged(root, propertyName, value, oldValue);
            }
            return this;
        }

        @Override
        public Object getValue2(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get a 'value 2' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            AbstractDomainTree.illegalNonDoubleEditorAndNonBooleanProperties(root, property, "Could not get a 'value 2' for 'non-double (or boolean) editor' property [" + property
                    + "] in type [" + root.getSimpleName() + "].");
            return (propertiesValues2.containsKey(key(root, property))) ? propertiesValues2.get(key(root, property)) : tr().getValue2ByDefault(root, property);
        }

        @Override
        public boolean is2ValueEmpty(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not ask whether 'value 2' is empty for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            AbstractDomainTree.illegalNonDoubleEditorAndNonBooleanProperties(root, property, "Could not ask whether 'value 2' is empty for 'non-double (or boolean) editor' property ["
                    + property + "] in type [" + root.getSimpleName() + "].");
            final Object value = (propertiesValues2.containsKey(key(root, property))) ? propertiesValues2.get(key(root, property)) : tr().getValue2ByDefault(root, property);
            return EntityUtils.equalsEx(value, tr().get2EmptyValueFor(root, property));
        }

        @Override
        public IAddToCriteriaTickManager setValue2(final Class<?> root, final String propertyName, final Object value2) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, propertyName, "Could not set a 'value 2' for 'unchecked' property [" + propertyName + "] in type ["
                    + root.getSimpleName() + "].");
            illegalNonDoubleEditorAndNonBooleanProperties(root, propertyName, "Could not set a 'value 2' for 'non-double (or boolean) editor' property [" + propertyName
                    + "] in type [" + root.getSimpleName() + "].");
            final Object oldValue2 = getValue2(root, propertyName);

            final Object defaultValue2 = tr().getValue2ByDefault(root, propertyName);
            if (EntityUtils.equalsEx(value2, defaultValue2)) {
                propertiesValues2.remove(key(root, propertyName));
            } else {
                propertiesValues2.put(key(root, propertyName), value2);
            }

            for (final IPropertyValueListener listener : propertyValue2Listeners.getListeners(IPropertyValueListener.class)) {
                listener.propertyStateChanged(root, propertyName, value2, oldValue2);
            }
            return this;
        }

        @Override
        public void addPropertyValueListener(final IPropertyValueListener listener) {
            removeEmptyPropertyValueListenersFrom(propertyValueListeners);
            propertyValueListeners.add(IPropertyValueListener.class, listener);
        }

        @Override
        public void addWeakPropertyValueListener(final IPropertyValueListener listener) {
            removeEmptyPropertyValueListenersFrom(propertyValueListeners);
            propertyValueListeners.add(IPropertyValueListener.class, new WeakPropertyValueListener(this, listener, true));
        }

        @Override
        public void removePropertyValueListener(final IPropertyValueListener listener) {
            for (final IPropertyValueListener obj : propertyValueListeners.getListeners(IPropertyValueListener.class)) {
                if (listener == obj) {
                    propertyValueListeners.remove(IPropertyValueListener.class, listener);
                } else if (obj instanceof WeakPropertyValueListener) {
                    final IPropertyValueListener weakRef = ((WeakPropertyValueListener) obj).getRef();
                    if (weakRef == listener || weakRef == null) {
                        propertyValueListeners.remove(IPropertyValueListener.class, obj);
                    }
                }
            }
        }

        @Override
        public void addPropertyValue2Listener(final IPropertyValueListener listener) {
            removeEmptyPropertyValueListenersFrom(propertyValue2Listeners);
            propertyValue2Listeners.add(IPropertyValueListener.class, listener);
        }

        @Override
        public void addWeakPropertyValue2Listener(final IPropertyValueListener listener) {
            removeEmptyPropertyValueListenersFrom(propertyValue2Listeners);
            propertyValue2Listeners.add(IPropertyValueListener.class, new WeakPropertyValueListener(this, listener, false));
        }

        @Override
        public void removePropertyValue2Listener(final IPropertyValueListener listener) {
            for (final IPropertyValueListener obj : propertyValue2Listeners.getListeners(IPropertyValueListener.class)) {
                if (listener == obj) {
                    propertyValue2Listeners.remove(IPropertyValueListener.class, listener);
                } else if (obj instanceof WeakPropertyValueListener) {
                    final IPropertyValueListener weakRef = ((WeakPropertyValueListener) obj).getRef();
                    if (weakRef == listener || weakRef == null) {
                        propertyValue2Listeners.remove(IPropertyValueListener.class, obj);
                    }
                }
            }
        }

        private void removeEmptyPropertyValueListenersFrom(final EventListenerList listeners) {
            for (final IPropertyValueListener obj : listeners.getListeners(IPropertyValueListener.class)) {
                if (obj instanceof WeakPropertyValueListener && ((WeakPropertyValueListener) obj).getRef() == null) {
                    listeners.remove(IPropertyValueListener.class, obj);
                }
            }
        }

        @Override
        public Boolean getExclusive(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get an 'exclusive' flag for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            AbstractDomainTree.illegalNonDoubleEditorProperties(root, property, "Could not get an 'exclusive' flag for 'non-double editor' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            return (propertiesExclusive1.containsKey(key(root, property))) ? propertiesExclusive1.get(key(root, property)) : null;
        }

        @Override
        public IAddToCriteriaTickManager setExclusive(final Class<?> root, final String property, final Boolean exclusive) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set an 'exclusive' flag for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            AbstractDomainTree.illegalNonDoubleEditorProperties(root, property, "Could not set an 'exclusive' flag for 'non-double editor' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            propertiesExclusive1.put(key(root, property), exclusive);
            return this;
        }

        @Override
        public Boolean getExclusive2(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get an 'exclusive 2' flag for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            AbstractDomainTree.illegalNonDoubleEditorProperties(root, property, "Could not get an 'exclusive 2' flag for 'non-double editor' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            return (propertiesExclusive2.containsKey(key(root, property))) ? propertiesExclusive2.get(key(root, property)) : null;
        }

        @Override
        public IAddToCriteriaTickManager setExclusive2(final Class<?> root, final String property, final Boolean exclusive2) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set an 'exclusive 2' flag for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            AbstractDomainTree.illegalNonDoubleEditorProperties(root, property, "Could not set an 'exclusive 2' flag for 'non-double editor' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            propertiesExclusive2.put(key(root, property), exclusive2);
            return this;
        }

        @Override
        public DateRangePrefixEnum getDatePrefix(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get a 'date prefix' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            AbstractDomainTree.illegalType(root, property, "Could not get a 'date prefix' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
            return (propertiesDatePrefixes.containsKey(key(root, property))) ? propertiesDatePrefixes.get(key(root, property)) : null;
        }

        @Override
        public IAddToCriteriaTickManager setDatePrefix(final Class<?> root, final String property, final DateRangePrefixEnum datePrefix) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set a 'date prefix' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            AbstractDomainTree.illegalType(root, property, "Could not set a 'date prefix' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
            propertiesDatePrefixes.put(key(root, property), datePrefix);
            return this;
        }

        @Override
        public MnemonicEnum getDateMnemonic(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get a 'date mnemonic' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            AbstractDomainTree.illegalType(root, property, "Could not get a 'date mnemonic' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
            return (propertiesDateMnemonics.containsKey(key(root, property))) ? propertiesDateMnemonics.get(key(root, property)) : null;
        }

        @Override
        public IAddToCriteriaTickManager setDateMnemonic(final Class<?> root, final String property, final MnemonicEnum dateMnemonic) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set a 'date mnemonic' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            AbstractDomainTree.illegalType(root, property, "Could not set a 'date mnemonic' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
            propertiesDateMnemonics.put(key(root, property), dateMnemonic);
            return this;
        }

        @Override
        public Boolean getAndBefore(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get an 'and before' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            AbstractDomainTree.illegalType(root, property, "Could not get an 'and before' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
            return (propertiesAndBefore.containsKey(key(root, property))) ? propertiesAndBefore.get(key(root, property)) : null;
        }

        @Override
        public IAddToCriteriaTickManager setAndBefore(final Class<?> root, final String property, final Boolean andBefore) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set an 'and before' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            AbstractDomainTree.illegalType(root, property, "Could not set an 'and before' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
            propertiesAndBefore.put(key(root, property), andBefore);
            return this;
        }

        @Override
        public Boolean getOrNull(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get an 'or null' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            return (propertiesOrNulls.containsKey(key(root, property))) ? propertiesOrNulls.get(key(root, property)) : null;
        }

        @Override
        public IAddToCriteriaTickManager setOrNull(final Class<?> root, final String property, final Boolean orNull) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set an 'or null' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            propertiesOrNulls.put(key(root, property), orNull);
            return this;
        }

        @Override
        public Boolean getNot(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get a 'not' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            return (propertiesNots.containsKey(key(root, property))) ? propertiesNots.get(key(root, property)) : null;
        }

        @Override
        public IAddToCriteriaTickManager setNot(final Class<?> root, final String property, final Boolean not) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set a 'not' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            propertiesNots.put(key(root, property), not);
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
            for (int i = 0; i < checkedPropertiesMutable(root).size(); i++) {
                final String name = checkedPropertiesMutable(root).get(i);
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
            for (int i = 0; i < checkedPropertiesMutable(root).size(); i++) {
                if (isPlaceholder(checkedPropertiesMutable(root).get(i))) {
                    return i;
                }
            }
            return checkedPropertiesMutable(root).size();
        }

        /////////////////// Checked properties with placeholders (END) ///////////////////

        protected ISerialiser getSerialiser() {
            return serialiser;
        }

        /**
         * A specific Kryo serialiser for {@link AddToCriteriaTickManager}.
         *
         * @author TG Team
         *
         */
        public static class AddToCriteriaTickManagerSerialiser extends TgSimpleSerializer<AddToCriteriaTickManager> {
            private final ISerialiser serialiser;

            public AddToCriteriaTickManagerSerialiser(final ISerialiser serialiser) {
                super((Kryo) serialiser.getEngine(SerialiserEngines.KRYO));
                this.serialiser = serialiser;
            }

            protected ISerialiser serialiser() {
                return serialiser;
            }

            @Override
            public AddToCriteriaTickManager read(final ByteBuffer buffer) {
                final EnhancementRootsMap<List<String>> checkedProperties = readValue(buffer, EnhancementRootsMap.class);
                final EnhancementPropertiesMap<Object> propertiesValues1 = readValue(buffer, EnhancementPropertiesMap.class);
                final EnhancementPropertiesMap<Object> propertiesValues2 = readValue(buffer, EnhancementPropertiesMap.class);
                final EnhancementPropertiesMap<Boolean> propertiesExclusive1 = readValue(buffer, EnhancementPropertiesMap.class);
                final EnhancementPropertiesMap<Boolean> propertiesExclusive2 = readValue(buffer, EnhancementPropertiesMap.class);
                final EnhancementPropertiesMap<DateRangePrefixEnum> propertiesDatePrefixes = readValue(buffer, EnhancementPropertiesMap.class);
                final EnhancementPropertiesMap<MnemonicEnum> propertiesDateMnemonics = readValue(buffer, EnhancementPropertiesMap.class);
                final EnhancementPropertiesMap<Boolean> propertiesAndBefore = readValue(buffer, EnhancementPropertiesMap.class);
                final EnhancementPropertiesMap<Boolean> propertiesOrNulls = readValue(buffer, EnhancementPropertiesMap.class);
                final EnhancementPropertiesMap<Boolean> propertiesNots = readValue(buffer, EnhancementPropertiesMap.class);
                final Integer columnsNumber = readValue(buffer, Integer.class);
                final LocatorManager locatorManager = readValue(buffer, LocatorManager.class);
                return new AddToCriteriaTickManager(checkedProperties, serialiser(), propertiesValues1, propertiesValues2, propertiesExclusive1, propertiesExclusive2, propertiesDatePrefixes, propertiesDateMnemonics, propertiesAndBefore, propertiesOrNulls, propertiesNots, columnsNumber, locatorManager);
            }

            @Override
            public void write(final ByteBuffer buffer, final AddToCriteriaTickManager manager) {
                writeValue(buffer, manager.checkedProperties());
                writeValue(buffer, manager.propertiesValues1);
                writeValue(buffer, manager.propertiesValues2);
                writeValue(buffer, manager.propertiesExclusive1);
                writeValue(buffer, manager.propertiesExclusive2);
                writeValue(buffer, manager.propertiesDatePrefixes);
                writeValue(buffer, manager.propertiesDateMnemonics);
                writeValue(buffer, manager.propertiesAndBefore);
                writeValue(buffer, manager.propertiesOrNulls);
                writeValue(buffer, manager.propertiesNots);
                writeValue(buffer, manager.columnsNumber);
                writeValue(buffer, manager.locatorManager);
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((columnsNumber == null) ? 0 : columnsNumber.hashCode());
            result = prime * result + ((locatorManager == null) ? 0 : locatorManager.hashCode());
            result = prime * result + ((propertiesAndBefore == null) ? 0 : propertiesAndBefore.hashCode());
            result = prime * result + ((propertiesDateMnemonics == null) ? 0 : propertiesDateMnemonics.hashCode());
            result = prime * result + ((propertiesDatePrefixes == null) ? 0 : propertiesDatePrefixes.hashCode());
            result = prime * result + ((propertiesExclusive1 == null) ? 0 : propertiesExclusive1.hashCode());
            result = prime * result + ((propertiesExclusive2 == null) ? 0 : propertiesExclusive2.hashCode());
            result = prime * result + ((propertiesNots == null) ? 0 : propertiesNots.hashCode());
            result = prime * result + ((propertiesOrNulls == null) ? 0 : propertiesOrNulls.hashCode());
            result = prime * result + ((propertiesValues1 == null) ? 0 : propertiesValues1.hashCode());
            result = prime * result + ((propertiesValues2 == null) ? 0 : propertiesValues2.hashCode());
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
            if (locatorManager == null) {
                if (other.locatorManager != null) {
                    return false;
                }
            } else if (!locatorManager.equals(other.locatorManager)) {
                return false;
            }
            if (propertiesAndBefore == null) {
                if (other.propertiesAndBefore != null) {
                    return false;
                }
            } else if (!propertiesAndBefore.equals(other.propertiesAndBefore)) {
                return false;
            }
            if (propertiesDateMnemonics == null) {
                if (other.propertiesDateMnemonics != null) {
                    return false;
                }
            } else if (!propertiesDateMnemonics.equals(other.propertiesDateMnemonics)) {
                return false;
            }
            if (propertiesDatePrefixes == null) {
                if (other.propertiesDatePrefixes != null) {
                    return false;
                }
            } else if (!propertiesDatePrefixes.equals(other.propertiesDatePrefixes)) {
                return false;
            }
            if (propertiesExclusive1 == null) {
                if (other.propertiesExclusive1 != null) {
                    return false;
                }
            } else if (!propertiesExclusive1.equals(other.propertiesExclusive1)) {
                return false;
            }
            if (propertiesExclusive2 == null) {
                if (other.propertiesExclusive2 != null) {
                    return false;
                }
            } else if (!propertiesExclusive2.equals(other.propertiesExclusive2)) {
                return false;
            }
            if (propertiesNots == null) {
                if (other.propertiesNots != null) {
                    return false;
                }
            } else if (!propertiesNots.equals(other.propertiesNots)) {
                return false;
            }
            if (propertiesOrNulls == null) {
                if (other.propertiesOrNulls != null) {
                    return false;
                }
            } else if (!propertiesOrNulls.equals(other.propertiesOrNulls)) {
                return false;
            }
            if (propertiesValues1 == null) {
                if (other.propertiesValues1 != null) {
                    return false;
                }
            } else if (!propertiesValues1.equals(other.propertiesValues1)) {
                return false;
            }
            if (propertiesValues2 == null) {
                if (other.propertiesValues2 != null) {
                    return false;
                }
            } else if (!propertiesValues2.equals(other.propertiesValues2)) {
                return false;
            }
            return true;
        }

        public LocatorManager locatorManager() {
            return locatorManager;
        }
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
        private final EnhancementRootsMap<List<Pair<String, Ordering>>> rootsListsOfOrderings;

        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
         * fields.
         */
        public AddToResultTickManager() {
            super();
            propertiesWidths = createPropertiesMap();
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
                final List<Pair<String, Ordering>> orderedPropertiesByDefault = new ArrayList<Pair<String, Ordering>>(tr().orderedPropertiesByDefault(root));
                final List<Pair<String, Ordering>> orderedPropertiesByDefaultWithoutUnchecked = new ArrayList<Pair<String, Ordering>>(orderedPropertiesByDefault);
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
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not toggle 'ordering' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            if (!rootsListsOfOrderings.containsKey(root)) {
                rootsListsOfOrderings.put(root, new ArrayList<Pair<String, Ordering>>(orderedProperties(root)));
            }
            final List<Pair<String, Ordering>> list = new ArrayList<Pair<String, Ordering>>(rootsListsOfOrderings.get(root));
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
            rootsListsOfOrderings.get(root).add(new Pair<String, Ordering>(property, Ordering.ASCENDING));
            return this;
        }

        @Override
        protected void removeCheckedProperty(final Class<?> root, final String property) {
            super.removeCheckedProperty(root, property);

            if (rootsListsOfOrderings.containsKey(root)) {
                final List<Pair<String, Ordering>> list = new ArrayList<Pair<String, Ordering>>(rootsListsOfOrderings.get(root));
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
        public void addPropertyOrderingListener(final IPropertyOrderingListener listener) {
            throw new UnsupportedOperationException("IPropertyOrderingListener is currently unsupported for CentreDomainTreeManager's second tick.");
        }

        @Override
        public void addWeakPropertyOrderingListener(final IPropertyOrderingListener listener) {
            throw new UnsupportedOperationException("Weak IPropertyOrderingListener is currently unsupported for CentreDomainTreeManager's second tick.");
        }

        @Override
        public void removePropertyOrderingListener(final IPropertyOrderingListener listener) {
            throw new UnsupportedOperationException("IPropertyOrderingListener is currently unsupported for CentreDomainTreeManager's second tick.");
        }

        @Override
        public int getWidth(final Class<?> root, final String property) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get a 'width' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            return (propertiesWidths.containsKey(key(root, property))) ? propertiesWidths.get(key(root, property)) : tr().getWidthByDefault(root, property);
        }

        @Override
        public IWidthManager setWidth(final Class<?> root, final String property, final int width) {
            AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set a 'width' for 'unchecked' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            propertiesWidths.put(key(root, property), width);
            return this;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((propertiesWidths == null) ? 0 : propertiesWidths.hashCode());
            result = prime * result + ((rootsListsOfOrderings == null) ? 0 : rootsListsOfOrderings.hashCode());
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
            final AddToResultTickManager other = (AddToResultTickManager) obj;
            if (propertiesWidths == null) {
                if (other.propertiesWidths != null) {
                    return false;
                }
            } else if (!propertiesWidths.equals(other.propertiesWidths)) {
                return false;
            }
            if (rootsListsOfOrderings == null) {
                if (other.rootsListsOfOrderings != null) {
                    return false;
                }
            } else if (!rootsListsOfOrderings.equals(other.rootsListsOfOrderings)) {
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

    /**
     * A specific Kryo serialiser for {@link CentreDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    public static class CentreDomainTreeManagerSerialiser extends AbstractDomainTreeManagerSerialiser<CentreDomainTreeManager> {
        public CentreDomainTreeManagerSerialiser(final ISerialiser serialiser) {
            super(serialiser);
        }

        @Override
        public CentreDomainTreeManager read(final ByteBuffer buffer) {
            final CentreDomainTreeRepresentation dtr = readValue(buffer, CentreDomainTreeRepresentation.class);
            final AddToCriteriaTickManager firstTick = readValue(buffer, AddToCriteriaTickManager.class);
            final AddToResultTickManager secondTick = readValue(buffer, AddToResultTickManager.class);
            final Boolean runAutomatically = readValue(buffer, Boolean.class);
            return new CentreDomainTreeManager(serialiser(), dtr, firstTick, secondTick, runAutomatically);
        }

        @Override
        public void write(final ByteBuffer buffer, final CentreDomainTreeManager manager) {
            super.write(buffer, manager);
            writeValue(buffer, manager.runAutomatically);
        }
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
