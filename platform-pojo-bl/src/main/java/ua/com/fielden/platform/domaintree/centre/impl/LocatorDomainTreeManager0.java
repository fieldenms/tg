package ua.com.fielden.platform.domaintree.centre.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager.AddToResultTickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap;
import ua.com.fielden.platform.domaintree.impl.LocatorManager0;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * WARNING: this is an OLD version!
 *
 * @author TG Team
 *
 */
@Deprecated
public class LocatorDomainTreeManager0 extends CentreDomainTreeManager0 implements ILocatorDomainTreeManager {
    private Boolean useForAutocompletion;
    private SearchBy searchBy;

    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public LocatorDomainTreeManager0(final ISerialiser0 serialiser, final Set<Class<?>> rootTypes) {
        this(serialiser, new LocatorDomainTreeRepresentation(serialiser, rootTypes), new AddToCriteriaTickManagerForLocator0(serialiser, rootTypes), new AddToResultTickManager(), null, null, SearchBy.KEY);
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected LocatorDomainTreeManager0(final ISerialiser serialiser, final LocatorDomainTreeRepresentation dtr, final AddToCriteriaTickManagerForLocator0 firstTick, final AddToResultTickManager secondTick, final Boolean runAutomatically, final Boolean useForAutocompletion, final SearchBy searchBy) {
        super(serialiser, dtr, firstTick, secondTick, runAutomatically);

        this.useForAutocompletion = useForAutocompletion;
        this.searchBy = searchBy;
    }

    @Override
    public ILocatorDomainTreeRepresentation getRepresentation() {
        return (ILocatorDomainTreeRepresentation) super.getRepresentation();
    }

    @Override
    public AddToCriteriaTickManagerForLocator0 getFirstTick() {
        return (AddToCriteriaTickManagerForLocator0) super.getFirstTick();
    }

    @Override
    public SearchBy getSearchBy() {
        return searchBy;
    }

    @Override
    public ILocatorDomainTreeManager setSearchBy(final SearchBy searchBy) {
        this.searchBy = searchBy;
        return this;
    }

    @Override
    public boolean isRunAutomatically() {
        return isRunAutomatically1() != null ? isRunAutomatically1() : true; // should be enabled by default
    }

    @Override
    public boolean isUseForAutocompletion() {
        return useForAutocompletion != null ? useForAutocompletion : false; // should be disabled by default
    }

    @Override
    public ILocatorDomainTreeManager setUseForAutocompletion(final boolean useForAutocompletion) {
        this.useForAutocompletion = useForAutocompletion;
        return this;
    }

    /**
     * WARNING: this is an OLD version!
     *
     * @author TG Team
     *
     */
    @Deprecated
    public static class AddToCriteriaTickManagerForLocator0 extends AddToCriteriaTickManager0 implements IAddToCriteriaTickManager {
        /**
         * Used for the first time instantiation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr", "tr" and "serialiser"
         * fields.
         */
        public AddToCriteriaTickManagerForLocator0(final ISerialiser0 serialiser, final Set<Class<?>> rootTypes) {
            this(AbstractDomainTree.<List<String>> createRootsMap(), serialiser, AbstractDomainTree.<Object> createPropertiesMap(), AbstractDomainTree.<Object> createPropertiesMap(), AbstractDomainTree.<Boolean> createPropertiesMap(), AbstractDomainTree.<Boolean> createPropertiesMap(), AbstractDomainTree.<DateRangePrefixEnum> createPropertiesMap(), AbstractDomainTree.<MnemonicEnum> createPropertiesMap(), AbstractDomainTree.<Boolean> createPropertiesMap(), AbstractDomainTree.<Boolean> createPropertiesMap(), AbstractDomainTree.<Boolean> createPropertiesMap(), null, new LocatorManager0(serialiser, rootTypes), AbstractDomainTree.<Set<MetaValueType>> createPropertiesMap());
        }

        /**
         * A tick <i>manager</i> constructor.
         *
         * @param serialiser
         */
        protected AddToCriteriaTickManagerForLocator0(final Map<Class<?>, List<String>> checkedProperties, final ISerialiser0 serialiser, final Map<Pair<Class<?>, String>, Object> propertiesValues1, final Map<Pair<Class<?>, String>, Object> propertiesValues2, final Map<Pair<Class<?>, String>, Boolean> propertiesExclusive1, final Map<Pair<Class<?>, String>, Boolean> propertiesExclusive2, final Map<Pair<Class<?>, String>, DateRangePrefixEnum> propertiesDatePrefixes, final Map<Pair<Class<?>, String>, MnemonicEnum> propertiesDateMnemonics, final Map<Pair<Class<?>, String>, Boolean> propertiesAndBefore, final Map<Pair<Class<?>, String>, Boolean> propertiesOrNulls, final Map<Pair<Class<?>, String>, Boolean> propertiesNots, final Integer columnsNumber, final LocatorManager0 locatorManager, final Map<Pair<Class<?>, String>, Set<MetaValueType>> propertiesMetaValuePresences) {
            super(checkedProperties, serialiser, propertiesValues1, propertiesValues2, propertiesExclusive1, propertiesExclusive2, propertiesDatePrefixes, propertiesDateMnemonics, propertiesAndBefore, propertiesOrNulls, propertiesNots, columnsNumber, locatorManager, propertiesMetaValuePresences);
        }

        @Override
        public int getColumnsNumber() {
            return columnsNumber() == null ? 1 : columnsNumber();
        }

        /**
         * WARNING: this is an OLD version!
         *
         * @author TG Team
         *
         */
        @Deprecated
        public static class AddToCriteriaTickManagerForLocator0Serialiser extends AddToCriteriaTickManager0Serialiser {
            /**
             * WARNING: this is an OLD version!
             *
             * @author TG Team
             *
             */
            @Deprecated
            public AddToCriteriaTickManagerForLocator0Serialiser(final ISerialiser0 serialiser) {
                super(serialiser);
            }

            @Override
            public AddToCriteriaTickManagerForLocator0 read(final ByteBuffer buffer) {
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
                final LocatorManager0 locatorManager = readValue(buffer, LocatorManager0.class);
                final EnhancementPropertiesMap<Set<MetaValueType>> propertiesMetaValuePresences = readValue(buffer, EnhancementPropertiesMap.class);
                return new AddToCriteriaTickManagerForLocator0(checkedProperties, serialiser(), propertiesValues1, propertiesValues2, propertiesExclusive1, propertiesExclusive2, propertiesDatePrefixes, propertiesDateMnemonics, propertiesAndBefore, propertiesOrNulls, propertiesNots, columnsNumber, locatorManager, propertiesMetaValuePresences);
            }
        }
    }

    /**
     * WARNING: this is an OLD version!
     *
     * @author TG Team
     *
     */
    @Deprecated
    public static class LocatorDomainTreeManager0Serialiser extends AbstractDomainTreeManagerSerialiser<LocatorDomainTreeManager0> {
        /**
         * WARNING: this is an OLD version!
         *
         * @author TG Team
         *
         */
        @Deprecated
        public LocatorDomainTreeManager0Serialiser(final ISerialiser0 serialiser) {
            super(serialiser);
        }

        @Override
        public LocatorDomainTreeManager0 read(final ByteBuffer buffer) {
            final LocatorDomainTreeRepresentation dtr = readValue(buffer, LocatorDomainTreeRepresentation.class);
            final AddToCriteriaTickManagerForLocator0 firstTick = readValue(buffer, AddToCriteriaTickManagerForLocator0.class);
            final AddToResultTickManager secondTick = readValue(buffer, AddToResultTickManager.class);
            final Boolean runAutomatically = readValue(buffer, Boolean.class);
            final Boolean useForAutocompletion = readValue(buffer, Boolean.class);
            final SearchBy searchBy = readValue(buffer, SearchBy.class);
            return new LocatorDomainTreeManager0(serialiser(), dtr, firstTick, secondTick, runAutomatically, useForAutocompletion, searchBy);
        }

        @Override
        public void write(final ByteBuffer buffer, final LocatorDomainTreeManager0 manager) {
            //	    super.write(buffer, manager);
            writeValue(buffer, manager.getRepresentation());
            writeValue(buffer, manager.getFirstTick());
            writeValue(buffer, manager.getSecondTick());

            writeValue(buffer, manager.runAutomatically());

            writeValue(buffer, manager.useForAutocompletion);
            writeValue(buffer, manager.searchBy);
        }

        @Override
        protected ISerialiser0 serialiser() {
            return (ISerialiser0) super.serialiser();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((searchBy == null) ? 0 : searchBy.hashCode());
        result = prime * result + ((useForAutocompletion == null) ? 0 : useForAutocompletion.hashCode());
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
        final LocatorDomainTreeManager0 other = (LocatorDomainTreeManager0) obj;
        if (searchBy != other.searchBy) {
            return false;
        }
        if (useForAutocompletion == null) {
            if (other.useForAutocompletion != null) {
                return false;
            }
        } else if (!useForAutocompletion.equals(other.useForAutocompletion)) {
            return false;
        }
        return true;
    }
}
