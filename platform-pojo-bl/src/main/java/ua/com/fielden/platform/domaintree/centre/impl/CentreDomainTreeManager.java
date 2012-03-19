package ua.com.fielden.platform.domaintree.centre.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation.IAddToCriteriaTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation.IAddToResultTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.PivotDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap;
import ua.com.fielden.platform.domaintree.impl.LocatorManager;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * Criteria (entity centre) domain tree manager. Includes support for checking (from base {@link AbstractDomainTreeManager}). <br><br>
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
    private static final long serialVersionUID = 7832625541851145438L;
    private final transient Logger logger = Logger.getLogger(getClass());

    private final LinkedHashMap<String, IAbstractAnalysisDomainTreeManagerAndEnhancer> persistentAnalyses;
    private final transient LinkedHashMap<String, IAbstractAnalysisDomainTreeManagerAndEnhancer> currentAnalyses;
    private final transient LinkedHashMap<String, IAbstractAnalysisDomainTreeManagerAndEnhancer> freezedAnalyses;
    private Boolean runAutomatically;

    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public CentreDomainTreeManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, new CentreDomainTreeRepresentation(serialiser, rootTypes), new AddToCriteriaTickManager(serialiser, rootTypes), new AddToResultTickManager(), new HashMap<String, IAbstractAnalysisDomainTreeManagerAndEnhancer>(), null);
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected CentreDomainTreeManager(final ISerialiser serialiser, final CentreDomainTreeRepresentation dtr, final AddToCriteriaTickManager firstTick, final AddToResultTickManager secondTick, final Map<String, IAbstractAnalysisDomainTreeManagerAndEnhancer> persistentAnalyses, final Boolean runAutomatically) {
	super(serialiser, dtr, firstTick, secondTick);
	this.persistentAnalyses = new LinkedHashMap<String, IAbstractAnalysisDomainTreeManagerAndEnhancer>();
	this.persistentAnalyses.putAll(persistentAnalyses);
	this.runAutomatically = runAutomatically;

	currentAnalyses = new LinkedHashMap<String, IAbstractAnalysisDomainTreeManagerAndEnhancer>();
	for (final Entry<String, IAbstractAnalysisDomainTreeManagerAndEnhancer> entry : this.persistentAnalyses.entrySet()) {
	    currentAnalyses.put(entry.getKey(), EntityUtils.deepCopy(entry.getValue(), getSerialiser())); // should be initialised with copies of persistent analyses
	}
	freezedAnalyses = new LinkedHashMap<String, IAbstractAnalysisDomainTreeManagerAndEnhancer>();
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

    /**
     * A first tick manager for entity centres specific. <br><br>
     *
     * @author TG Team
     *
     */
    public static class AddToCriteriaTickManager extends TickManager implements IAddToCriteriaTickManager {

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

	/**
	 * Used for the first time instantiation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr", "tr" and "serialiser" fields.
	 */
	public AddToCriteriaTickManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	    this(AbstractDomainTree.<List<String>>createRootsMap(), serialiser, AbstractDomainTree.<Object>createPropertiesMap(), AbstractDomainTree.<Object>createPropertiesMap(), AbstractDomainTree.<Boolean>createPropertiesMap(), AbstractDomainTree.<Boolean>createPropertiesMap(), AbstractDomainTree.<DateRangePrefixEnum>createPropertiesMap(), AbstractDomainTree.<MnemonicEnum>createPropertiesMap(), AbstractDomainTree.<Boolean>createPropertiesMap(), AbstractDomainTree.<Boolean>createPropertiesMap(), AbstractDomainTree.<Boolean>createPropertiesMap(), null, new LocatorManager(serialiser, rootTypes));
	}

	/**
	 * A tick <i>manager</i> constructor.
	 *
	 * @param serialiser
	 */
	protected AddToCriteriaTickManager(final Map<Class<?>, List<String>> checkedProperties, final ISerialiser serialiser, final Map<Pair<Class<?>, String>, Object> propertiesValues1, final Map<Pair<Class<?>, String>, Object> propertiesValues2, final Map<Pair<Class<?>, String>, Boolean> propertiesExclusive1, final Map<Pair<Class<?>, String>, Boolean> propertiesExclusive2, final Map<Pair<Class<?>, String>, DateRangePrefixEnum> propertiesDatePrefixes, final Map<Pair<Class<?>, String>, MnemonicEnum> propertiesDateMnemonics, final Map<Pair<Class<?>, String>, Boolean> propertiesAndBefore, final Map<Pair<Class<?>, String>, Boolean> propertiesOrNulls, final Map<Pair<Class<?>, String>, Boolean> propertiesNots, final Integer columnsNumber, final LocatorManager locatorManager) {
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
	}

	@Override
	protected IAddToCriteriaTickRepresentation tr() {
	    return (IAddToCriteriaTickRepresentation) super.tr();
	}

	@Override
	public Set<Class<?>> rootTypes() {
	    return locatorManager.rootTypes();
	}

	@Override
	public ILocatorDomainTreeManagerAndEnhancer produceLocatorManagerByDefault(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not init a locator for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return locatorManager.produceLocatorManagerByDefault(root, property);
	}

	@Override
	public void initLocatorManagerByDefault(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not init a locator for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    locatorManager.initLocatorManagerByDefault(root, property);
	}

	@Override
	public void resetLocatorManager(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not reset a locator for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    locatorManager.resetLocatorManager(root, property);
	}

	@Override
	public void discardLocatorManager(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not discard a locator for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    locatorManager.discardLocatorManager(root, property);
	}

	@Override
	public void acceptLocatorManager(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not accept a locator for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    locatorManager.acceptLocatorManager(root, property);
	}

	@Override
	public void saveLocatorManagerGlobally(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not save globally a locator for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    locatorManager.saveLocatorManagerGlobally(root, property);
	}

	@Override
	public ILocatorDomainTreeManagerAndEnhancer getLocatorManager(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not retrieve a locator for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return locatorManager.getLocatorManager(root, property);
	}

	@Override
	public void freezeLocatorManager(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not freeze a locator for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    locatorManager.freezeLocatorManager(root, property);
	}

	@Override
	public boolean isChangedLocatorManager(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not ask whether a locator has been changed for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
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
	    this.columnsNumber = Integer.valueOf(columnsNumber);
	    return this;
	}

	@Override
	public Object getValue(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get a 'value' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return (propertiesValues1.containsKey(key(root, property))) ? propertiesValues1.get(key(root, property)) : tr().getValueByDefault(root, property);
	}

	@Override
	public boolean isValueEmpty(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not ask whether 'value' is empty for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    final Object value = (propertiesValues1.containsKey(key(root, property))) ? propertiesValues1.get(key(root, property)) : tr().getValueByDefault(root, property);
	    return EntityUtils.equalsEx(value, tr().getEmptyValueFor(root, property));
	}

	@Override
	public IAddToCriteriaTickManager setValue(final Class<?> root, final String property, final Object value) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set a 'value' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    propertiesValues1.put(key(root, property), value);
	    return this;
	}

	@Override
	public Object getValue2(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get a 'value 2' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return (propertiesValues2.containsKey(key(root, property))) ? propertiesValues2.get(key(root, property)) : tr().getValue2ByDefault(root, property);
	}

	@Override
	public boolean is2ValueEmpty(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not ask whether 'value 2' is empty for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    final Object value = (propertiesValues2.containsKey(key(root, property))) ? propertiesValues2.get(key(root, property)) : tr().getValue2ByDefault(root, property);
	    return EntityUtils.equalsEx(value, tr().get2EmptyValueFor(root, property));
	}

	@Override
	public IAddToCriteriaTickManager setValue2(final Class<?> root, final String property, final Object value2) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set a 'value' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    propertiesValues2.put(key(root, property), value2);
	    return this;
	}

	@Override
	public Boolean getExclusive(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get an 'exclusive' flag for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    AbstractDomainTree.illegalType(root, property, "Could not get an 'exclusive' flag for 'non-range' property [" + property + "] in type [" + root.getSimpleName() + "].", Number.class, Money.class, Date.class);
	    return (propertiesExclusive1.containsKey(key(root, property))) ? propertiesExclusive1.get(key(root, property)) : null;
	}

	@Override
	public IAddToCriteriaTickManager setExclusive(final Class<?> root, final String property, final Boolean exclusive) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set an 'exclusive' flag for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    AbstractDomainTree.illegalType(root, property, "Could not set an 'exclusive' flag for 'non-range' property [" + property + "] in type [" + root.getSimpleName() + "].", Number.class, Money.class, Date.class);
	    propertiesExclusive1.put(key(root, property), exclusive);
	    return this;
	}

	@Override
	public Boolean getExclusive2(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get an 'exclusive 2' flag for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    AbstractDomainTree.illegalType(root, property, "Could not get an 'exclusive 2' flag for 'non-range' property [" + property + "] in type [" + root.getSimpleName() + "].", Number.class, Money.class, Date.class);
	    return (propertiesExclusive2.containsKey(key(root, property))) ? propertiesExclusive2.get(key(root, property)) : null;
	}

	@Override
	public IAddToCriteriaTickManager setExclusive2(final Class<?> root, final String property, final Boolean exclusive2) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set an 'exclusive 2' flag for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    AbstractDomainTree.illegalType(root, property, "Could not set an 'exclusive 2' flag for 'non-range' property [" + property + "] in type [" + root.getSimpleName() + "].", Number.class, Money.class, Date.class);
	    propertiesExclusive2.put(key(root, property), exclusive2);
	    return this;
	}

	@Override
	public DateRangePrefixEnum getDatePrefix(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get a 'date prefix' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    AbstractDomainTree.illegalType(root, property, "Could not get a 'date prefix' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
	    return (propertiesDatePrefixes.containsKey(key(root, property))) ? propertiesDatePrefixes.get(key(root, property)) : null;
	}

	@Override
	public IAddToCriteriaTickManager setDatePrefix(final Class<?> root, final String property, final DateRangePrefixEnum datePrefix) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set a 'date prefix' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    AbstractDomainTree.illegalType(root, property, "Could not set a 'date prefix' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
	    propertiesDatePrefixes.put(key(root, property), datePrefix);
	    return this;
	}

	@Override
	public MnemonicEnum getDateMnemonic(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get a 'date mnemonic' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    AbstractDomainTree.illegalType(root, property, "Could not get a 'date mnemonic' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
	    return (propertiesDateMnemonics.containsKey(key(root, property))) ? propertiesDateMnemonics.get(key(root, property)) : null;
	}

	@Override
	public IAddToCriteriaTickManager setDateMnemonic(final Class<?> root, final String property, final MnemonicEnum dateMnemonic) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set a 'date mnemonic' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    AbstractDomainTree.illegalType(root, property, "Could not set a 'date mnemonic' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
	    propertiesDateMnemonics.put(key(root, property), dateMnemonic);
	    return this;
	}

	@Override
	public Boolean getAndBefore(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get an 'and before' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    AbstractDomainTree.illegalType(root, property, "Could not get an 'and before' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
	    return (propertiesAndBefore.containsKey(key(root, property))) ? propertiesAndBefore.get(key(root, property)) : null;
	}

	@Override
	public IAddToCriteriaTickManager setAndBefore(final Class<?> root, final String property, final Boolean andBefore) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set an 'and before' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    AbstractDomainTree.illegalType(root, property, "Could not set an 'and before' for 'non-date' property [" + property + "] in type [" + root.getSimpleName() + "].", Date.class);
	    propertiesAndBefore.put(key(root, property), andBefore);
	    return this;
	}

	@Override
	public Boolean getOrNull(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get an 'or null' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return (propertiesOrNulls.containsKey(key(root, property))) ? propertiesOrNulls.get(key(root, property)) : null;
	}

	@Override
	public IAddToCriteriaTickManager setOrNull(final Class<?> root, final String property, final Boolean orNull) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set an 'or null' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    propertiesOrNulls.put(key(root, property), orNull);
	    return this;
	}

	@Override
	public Boolean getNot(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get a 'not' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return (propertiesNots.containsKey(key(root, property))) ? propertiesNots.get(key(root, property)) : null;
	}

	@Override
	public IAddToCriteriaTickManager setNot(final Class<?> root, final String property, final Boolean not) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set a 'not' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    propertiesNots.put(key(root, property), not);
	    return this;
	}

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
	    private final TgKryo kryo;

	    public AddToCriteriaTickManagerSerialiser(final TgKryo kryo) {
		super(kryo);
		this.kryo = kryo;
	    }

	    protected TgKryo kryo() {
		return kryo;
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
		return new AddToCriteriaTickManager(checkedProperties, kryo(), propertiesValues1, propertiesValues2, propertiesExclusive1, propertiesExclusive2, propertiesDatePrefixes, propertiesDateMnemonics, propertiesAndBefore, propertiesOrNulls, propertiesNots, columnsNumber, locatorManager);
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
	    if (this == obj)
		return true;
	    if (!super.equals(obj))
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    final AddToCriteriaTickManager other = (AddToCriteriaTickManager) obj;
	    if (columnsNumber == null) {
		if (other.columnsNumber != null)
		    return false;
	    } else if (!columnsNumber.equals(other.columnsNumber))
		return false;
	    if (locatorManager == null) {
		if (other.locatorManager != null)
		    return false;
	    } else if (!locatorManager.equals(other.locatorManager))
		return false;
	    if (propertiesAndBefore == null) {
		if (other.propertiesAndBefore != null)
		    return false;
	    } else if (!propertiesAndBefore.equals(other.propertiesAndBefore))
		return false;
	    if (propertiesDateMnemonics == null) {
		if (other.propertiesDateMnemonics != null)
		    return false;
	    } else if (!propertiesDateMnemonics.equals(other.propertiesDateMnemonics))
		return false;
	    if (propertiesDatePrefixes == null) {
		if (other.propertiesDatePrefixes != null)
		    return false;
	    } else if (!propertiesDatePrefixes.equals(other.propertiesDatePrefixes))
		return false;
	    if (propertiesExclusive1 == null) {
		if (other.propertiesExclusive1 != null)
		    return false;
	    } else if (!propertiesExclusive1.equals(other.propertiesExclusive1))
		return false;
	    if (propertiesExclusive2 == null) {
		if (other.propertiesExclusive2 != null)
		    return false;
	    } else if (!propertiesExclusive2.equals(other.propertiesExclusive2))
		return false;
	    if (propertiesNots == null) {
		if (other.propertiesNots != null)
		    return false;
	    } else if (!propertiesNots.equals(other.propertiesNots))
		return false;
	    if (propertiesOrNulls == null) {
		if (other.propertiesOrNulls != null)
		    return false;
	    } else if (!propertiesOrNulls.equals(other.propertiesOrNulls))
		return false;
	    if (propertiesValues1 == null) {
		if (other.propertiesValues1 != null)
		    return false;
	    } else if (!propertiesValues1.equals(other.propertiesValues1))
		return false;
	    if (propertiesValues2 == null) {
		if (other.propertiesValues2 != null)
		    return false;
	    } else if (!propertiesValues2.equals(other.propertiesValues2))
		return false;
	    return true;
	}

	public LocatorManager locatorManager() {
	    return locatorManager;
	}
    }

    /**
     * A second tick manager for entity centres specific. <br><br>
     *
     * @author TG Team
     *
     */
    protected static class AddToResultTickManager extends TickManager implements IAddToResultTickManager {
	private static final long serialVersionUID = -5840622913992787411L;
	private final EnhancementPropertiesMap<Integer> propertiesWidths;
	private final EnhancementRootsMap<List<Pair<String, Ordering>>> rootsListsOfOrderings;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr" fields.
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
		return tr().orderedPropertiesByDefault(root);
	    }
	}

	@Override
	public void toggleOrdering(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not toggle 'ordering' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    if (!rootsListsOfOrderings.containsKey(root)) {
		rootsListsOfOrderings.put(root, new ArrayList<Pair<String, Ordering>>(tr().orderedPropertiesByDefault(root)));
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
		    return;
		}
	    } // if the property does not have an Ordering assigned -- put a ASC ordering to it (into the end of the list)
	    rootsListsOfOrderings.get(root).add(new Pair<String, Ordering>(property, Ordering.ASCENDING));
	}

	@Override
	public boolean addPropertyOrderingListener(final IPropertyOrderingListener listener) {
	    // TODO Auto-generated method stub
	    throw new UnsupportedOperationException("IPropertyOrderingListener is currently unsupported for CentreDomainTreeManager's second tick.");
	}

	@Override
	public boolean removePropertyOrderingListener(final IPropertyOrderingListener listener) {
	    // TODO Auto-generated method stub
	    throw new UnsupportedOperationException("IPropertyOrderingListener is currently unsupported for CentreDomainTreeManager's second tick.");
	}

	@Override
	public int getWidth(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not get a 'width' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return (propertiesWidths.containsKey(key(root, property))) ? propertiesWidths.get(key(root, property)) : tr().getWidthByDefault(root, property);
	}

	@Override
	public void setWidth(final Class<?> root, final String property, final int width) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not set a 'width' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    propertiesWidths.put(key(root, property), width);
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
	    if (this == obj)
		return true;
	    if (!super.equals(obj))
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    final AddToResultTickManager other = (AddToResultTickManager) obj;
	    if (propertiesWidths == null) {
		if (other.propertiesWidths != null)
		    return false;
	    } else if (!propertiesWidths.equals(other.propertiesWidths))
		return false;
	    if (rootsListsOfOrderings == null) {
		if (other.rootsListsOfOrderings != null)
		    return false;
	    } else if (!rootsListsOfOrderings.equals(other.rootsListsOfOrderings))
		return false;
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
    public void initAnalysisManagerByDefault(final String name, final AnalysisType analysisType) {
	if (isFreezed(name)) {
	    error("Unable to Init analysis instance if it is freezed for title [" + name + "].");
	}
	if (getAnalysisManager(name) != null) {
	    throw new IllegalArgumentException("The analysis with name [" + name + "] already exists.");
	}
	// create a new instance and put to "current" map
	if (AnalysisType.PIVOT.equals(analysisType)) {
	    currentAnalyses.put(name, new PivotDomainTreeManagerAndEnhancer(getSerialiser(), getRepresentation().rootTypes()));
	} if (AnalysisType.SIMPLE.equals(analysisType)) {
	    currentAnalyses.put(name, new AnalysisDomainTreeManagerAndEnhancer(getSerialiser(), getRepresentation().rootTypes()));
	} if (AnalysisType.LIFECYCLE.equals(analysisType)) {
	    currentAnalyses.put(name, new LifecycleDomainTreeManagerAndEnhancer(getSerialiser(), getRepresentation().rootTypes()));
	}
    }

    @Override
    public void discardAnalysisManager(final String name) {
	final IAbstractAnalysisDomainTreeManagerAndEnhancer dtm = EntityUtils.deepCopy(persistentAnalyses.get(name), getSerialiser());
	if (dtm != null) {
	    currentAnalyses.put(name, dtm);
	} else {
	    currentAnalyses.remove(name);
	}

	if (isFreezed(name)) {
	    unfreeze(name);
	}
    }

    @Override
    public void acceptAnalysisManager(final String name) {
	if (isFreezed(name)) {
	    unfreeze(name);
	} else {
	    final IAbstractAnalysisDomainTreeManagerAndEnhancer dtm = EntityUtils.deepCopy(currentAnalyses.get(name), getSerialiser());
	    if (dtm != null) {
		persistentAnalyses.put(name, dtm);
	    } else {
		persistentAnalyses.remove(name);
	    }
	}
    }

    @Override
    public boolean isChangedAnalysisManager(final String name) {
	return !EntityUtils.equalsEx(currentAnalyses.get(name), persistentAnalyses.get(name));
    }

    @Override
    public void removeAnalysisManager(final String name) {
	if (isFreezed(name)) {
	    error("Unable to remove analysis instance if it is freezed for title [" + name + "].");
	}
	final IAbstractAnalysisDomainTreeManager mgr = getAnalysisManager(name);
	if (mgr == null) {
	    throw new IllegalArgumentException("The unknown analysis with name [" + name + "] can not be removed.");
	}
	currentAnalyses.remove(name);
	acceptAnalysisManager(name);
    }

    @Override
    public IAbstractAnalysisDomainTreeManagerAndEnhancer getAnalysisManager(final String name) {
	return currentAnalyses.get(name);
    }

    @Override
    public void freezeAnalysisManager(final String name) {
	if (isFreezed(name)) {
	    error("Unable to freeze the analysis instance more than once for title [" + name + "].");
	}
	notInitiliasedError(persistentAnalyses.get(name), name);
	notInitiliasedError(currentAnalyses.get(name), name);
	final IAbstractAnalysisDomainTreeManagerAndEnhancer persistentAnalysis = persistentAnalyses.remove(name);
	freezedAnalyses.put(name, persistentAnalysis);
	persistentAnalyses.put(name, EntityUtils.deepCopy(currentAnalyses.get(name), getSerialiser()));
    }

    /**
     * Returns <code>true</code> if the analysis instance is in 'freezed' state, <code>false</code> otherwise.
     *
     * @param name
     * @return
     */
    protected boolean isFreezed(final String name) {
	return freezedAnalyses.get(name) != null;
    }

    /**
     * Unfreezes the centre instance that is currently freezed.
     *
     * @param root
     * @param name
     */
    protected void unfreeze(final String name) {
	if (!isFreezed(name)) {
	    error("Unable to unfreeze the analysis instance that is not 'freezed' for title [" + name + "].");
	}
	final IAbstractAnalysisDomainTreeManagerAndEnhancer persistentAnalysis = freezedAnalyses.remove(name);
	persistentAnalyses.put(name, persistentAnalysis);
    }

    /**
     * Throws an error when the instance is <code>null</code> (not initialised).
     *
     * @param mgr
     * @param root
     * @param name
     */
    private void notInitiliasedError(final IAbstractAnalysisDomainTreeManagerAndEnhancer mgr, final String name) {
	if (mgr == null) {
	    error("Unable to perform this operation on the analysis instance, that wasn't initialised, for title [" + name + "].");
	}
    }

    /**
     * Logs and throws an {@link IllegalArgumentException} error with specified message.
     *
     * @param message
     */
    private void error(final String message) {
	logger.error(message);
	throw new IllegalArgumentException(message);
    }

    @Override
    public List<String> analysisKeys() {
	return new ArrayList<String>(currentAnalyses.keySet());
    }

    /**
     * A specific Kryo serialiser for {@link CentreDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    public static class CentreDomainTreeManagerSerialiser extends AbstractDomainTreeManagerSerialiser<CentreDomainTreeManager> {
	public CentreDomainTreeManagerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public CentreDomainTreeManager read(final ByteBuffer buffer) {
	    final CentreDomainTreeRepresentation dtr = readValue(buffer, CentreDomainTreeRepresentation.class);
	    final AddToCriteriaTickManager firstTick = readValue(buffer, AddToCriteriaTickManager.class);
	    final AddToResultTickManager secondTick = readValue(buffer, AddToResultTickManager.class);
	    final Map<String, IAbstractAnalysisDomainTreeManagerAndEnhancer> persistentAnalyses = readValue(buffer, LinkedHashMap.class);
	    final Boolean runAutomatically = readValue(buffer, Boolean.class);
	    return new CentreDomainTreeManager(kryo(), dtr, firstTick, secondTick, persistentAnalyses, runAutomatically);
	}

	@Override
	public void write(final ByteBuffer buffer, final CentreDomainTreeManager manager) {
	    super.write(buffer, manager);
	    writeValue(buffer, manager.persistentAnalyses);
	    writeValue(buffer, manager.runAutomatically);
	}
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((persistentAnalyses == null) ? 0 : persistentAnalyses.hashCode());
	result = prime * result + ((runAutomatically == null) ? 0 : runAutomatically.hashCode());
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
	final CentreDomainTreeManager other = (CentreDomainTreeManager) obj;
	if (persistentAnalyses == null) {
	    if (other.persistentAnalyses != null)
		return false;
	} else if (!persistentAnalyses.equals(other.persistentAnalyses))
	    return false;
	if (runAutomatically == null) {
	    if (other.runAutomatically != null)
		return false;
	} else if (!runAutomatically.equals(other.runAutomatically))
	    return false;
	return true;
    }

    protected Map<String, IAbstractAnalysisDomainTreeManagerAndEnhancer> persistentAnalyses() {
	return persistentAnalyses;
    }

    protected Boolean runAutomatically() {
	return runAutomatically;
    }
}
