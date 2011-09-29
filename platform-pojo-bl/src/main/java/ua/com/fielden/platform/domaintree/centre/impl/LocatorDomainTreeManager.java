package ua.com.fielden.platform.domaintree.centre.impl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap;
import ua.com.fielden.platform.domaintree.impl.LocatorManager;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * Locator domain tree manager. <br><br>
 *
 * Includes implementation of "checking" logic, that contain: <br>
 * a) default mutable state management; <br>
 * a) manual state management; <br>
 * b) resolution of conflicts with excluded, disabled etc. properties; <br>
 *
 * @author TG Team
 *
 */
public class LocatorDomainTreeManager extends CentreDomainTreeManager implements ILocatorDomainTreeManager {
    private static final long serialVersionUID = 7832625541851145438L;

    private Boolean useForAutocompletion;
    private SearchBy searchBy;

    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public LocatorDomainTreeManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, new LocatorDomainTreeRepresentation(serialiser, rootTypes), new AddToCriteriaTickManagerForLocator(serialiser, rootTypes), new AddToResultTickManager(), new HashMap<String, IAbstractAnalysisDomainTreeManagerAndEnhancer>(), null, null, SearchBy.KEY);
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected LocatorDomainTreeManager(final ISerialiser serialiser, final ILocatorDomainTreeRepresentation dtr, final AddToCriteriaTickManagerForLocator firstTick, final AddToResultTickManager secondTick, final Map<String, IAbstractAnalysisDomainTreeManagerAndEnhancer> persistentAnalyses, final Boolean runAutomatically, final Boolean useForAutocompletion, final SearchBy searchBy) {
	super(serialiser, dtr, firstTick, secondTick, persistentAnalyses, runAutomatically);

	this.useForAutocompletion = useForAutocompletion;
	this.searchBy = searchBy;
    }

    @Override
    public ILocatorDomainTreeRepresentation getRepresentation() {
        return (ILocatorDomainTreeRepresentation) super.getRepresentation();
    }

    @Override
    public AddToCriteriaTickManagerForLocator getFirstTick() {
        return (AddToCriteriaTickManagerForLocator) super.getFirstTick();
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
     * A first tick manager for entity locator specific. <br><br>
     *
     * @author TG Team
     *
     */
    public static class AddToCriteriaTickManagerForLocator extends AddToCriteriaTickManager implements IAddToCriteriaTickManager {
	private static final long serialVersionUID = 2723287339828318955L;

	/**
	 * Used for the first time instantiation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr", "tr" and "serialiser" fields.
	 */
	public AddToCriteriaTickManagerForLocator(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	    this(AbstractDomainTree.<List<String>>createRootsMap(), serialiser, AbstractDomainTree.<Object>createPropertiesMap(), AbstractDomainTree.<Object>createPropertiesMap(), AbstractDomainTree.<Boolean>createPropertiesMap(), AbstractDomainTree.<Boolean>createPropertiesMap(), AbstractDomainTree.<DateRangePrefixEnum>createPropertiesMap(), AbstractDomainTree.<MnemonicEnum>createPropertiesMap(), AbstractDomainTree.<Boolean>createPropertiesMap(), AbstractDomainTree.<Boolean>createPropertiesMap(), AbstractDomainTree.<Boolean>createPropertiesMap(), null, new LocatorManager(serialiser, rootTypes));
	}

	/**
	 * A tick <i>manager</i> constructor.
	 *
	 * @param serialiser
	 */
	protected AddToCriteriaTickManagerForLocator(final Map<Class<?>, List<String>> checkedProperties, final ISerialiser serialiser, final Map<Pair<Class<?>, String>, Object> propertiesValues1, final Map<Pair<Class<?>, String>, Object> propertiesValues2, final Map<Pair<Class<?>, String>, Boolean> propertiesExclusive1, final Map<Pair<Class<?>, String>, Boolean> propertiesExclusive2, final Map<Pair<Class<?>, String>, DateRangePrefixEnum> propertiesDatePrefixes, final Map<Pair<Class<?>, String>, MnemonicEnum> propertiesDateMnemonics, final Map<Pair<Class<?>, String>, Boolean> propertiesAndBefore, final Map<Pair<Class<?>, String>, Boolean> propertiesOrNulls, final Map<Pair<Class<?>, String>, Boolean> propertiesNots, final Integer columnsNumber, final LocatorManager locatorManager) {
	    super(checkedProperties, serialiser, propertiesValues1, propertiesValues2, propertiesExclusive1, propertiesExclusive2, propertiesDatePrefixes, propertiesDateMnemonics, propertiesAndBefore, propertiesOrNulls, propertiesNots, columnsNumber, locatorManager);
	}

	@Override
	public int getColumnsNumber() {
	    return columnsNumber() == null ? 1 : columnsNumber();
	}

	/**
	 * A specific Kryo serialiser for {@link AddToCriteriaTickManagerForLocator}.
	 *
	 * @author TG Team
	 *
	 */
	public static class AddToCriteriaTickManagerForLocatorSerialiser extends AddToCriteriaTickManagerSerialiser {
	    public AddToCriteriaTickManagerForLocatorSerialiser(final TgKryo kryo) {
		super(kryo);
	    }

	    @Override
	    public AddToCriteriaTickManagerForLocator read(final ByteBuffer buffer) {
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
		return new AddToCriteriaTickManagerForLocator(checkedProperties, kryo(), propertiesValues1, propertiesValues2, propertiesExclusive1, propertiesExclusive2, propertiesDatePrefixes, propertiesDateMnemonics, propertiesAndBefore, propertiesOrNulls, propertiesNots, columnsNumber, locatorManager);
	    }
	}
    }

    /**
     * A specific Kryo serialiser for {@link LocatorDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    public static class LocatorDomainTreeManagerSerialiser extends AbstractDomainTreeManagerSerialiser<LocatorDomainTreeManager> {
	public LocatorDomainTreeManagerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public LocatorDomainTreeManager read(final ByteBuffer buffer) {
	    final LocatorDomainTreeRepresentation dtr = readValue(buffer, LocatorDomainTreeRepresentation.class);
	    final AddToCriteriaTickManagerForLocator firstTick = readValue(buffer, AddToCriteriaTickManagerForLocator.class);
	    final AddToResultTickManager secondTick = readValue(buffer, AddToResultTickManager.class);
	    final Map<String, IAbstractAnalysisDomainTreeManagerAndEnhancer> persistentAnalyses = readValue(buffer, HashMap.class);
	    final Boolean runAutomatically = readValue(buffer, Boolean.class);
	    final Boolean useForAutocompletion = readValue(buffer, Boolean.class);
	    final SearchBy searchBy = readValue(buffer, SearchBy.class);
	    return new LocatorDomainTreeManager(kryo(), dtr, firstTick, secondTick, persistentAnalyses, runAutomatically, useForAutocompletion, searchBy);
	}

	@Override
	public void write(final ByteBuffer buffer, final LocatorDomainTreeManager manager) {
//	    super.write(buffer, manager);
	    writeValue(buffer, manager.getRepresentation());
	    writeValue(buffer, manager.getFirstTick());
	    writeValue(buffer, manager.getSecondTick());

	    writeValue(buffer, manager.persistentAnalyses());
	    writeValue(buffer, manager.runAutomatically());

	    writeValue(buffer, manager.useForAutocompletion);
	    writeValue(buffer, manager.searchBy);
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
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final LocatorDomainTreeManager other = (LocatorDomainTreeManager) obj;
	if (searchBy != other.searchBy)
	    return false;
	if (useForAutocompletion == null) {
	    if (other.useForAutocompletion != null)
		return false;
	} else if (!useForAutocompletion.equals(other.useForAutocompletion))
	    return false;
	return true;
    }
}
