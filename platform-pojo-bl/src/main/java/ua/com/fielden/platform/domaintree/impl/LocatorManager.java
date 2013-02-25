package ua.com.fielden.platform.domaintree.impl;

import static ua.com.fielden.platform.domaintree.ILocatorManager.Phase.EDITING_PHASE;
import static ua.com.fielden.platform.domaintree.ILocatorManager.Phase.FREEZED_EDITING_PHASE;
import static ua.com.fielden.platform.domaintree.ILocatorManager.Phase.USAGE_PHASE;
import static ua.com.fielden.platform.domaintree.ILocatorManager.Type.GLOBAL;
import static ua.com.fielden.platform.domaintree.ILocatorManager.Type.LOCAL;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.IGlobalDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.ILocatorManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;


/**
 * A locator manager mixin implementation (save, init, discard locators etc.).
 *
 * @author TG Team
 *
 */
public class LocatorManager extends AbstractDomainTree implements ILocatorManager {
    private final transient Logger logger = Logger.getLogger(getClass());
    // this instance should be initialised using Reflection when GlobalDomainTreeManager creates/deserialises the instance of LocatorManager
    private final transient IGlobalDomainTreeRepresentation globalRepresentation;
    private final EnhancementLinkedRootsSet rootTypes;
    private final EnhancementPropertiesMap<ILocatorDomainTreeManagerAndEnhancer> persistentLocators;
    /** Do <b>NOT</b> use this field directly! Please use currentAnalyses() method instead. */
    private final transient EnhancementPropertiesMap<ILocatorDomainTreeManagerAndEnhancer> currentLocators;
    private final transient EnhancementPropertiesMap<ILocatorDomainTreeManagerAndEnhancer> freezedLocators;
    private final transient EnhancementSet locatorsInEditingMode;
    /** Do <b>NOT</b> use this field directly! Please use currentAnalyses() method instead. */
    private final transient EnhancementSet locatorsWithLocalType;

    /**
     * Returns a current locators for locator manager. It is lazily loaded by the very first invocation from "persistentLocators" by copying them.
     *
     * @return
     */
    private EnhancementPropertiesMap<ILocatorDomainTreeManagerAndEnhancer> currentLocators() {
	if (currentLocators == null) {
	    try {
		final Field currentLocatorsField = Finder.findFieldByName(LocatorManager.class, "currentLocators");
		final boolean isAccessible = currentLocatorsField.isAccessible();
		currentLocatorsField.setAccessible(true);
		currentLocatorsField.set(this, AbstractDomainTree.<ILocatorDomainTreeManagerAndEnhancer>createPropertiesMap());
		currentLocatorsField.setAccessible(isAccessible);
	    } catch (final Exception e) {
		e.printStackTrace();
		throw new IllegalStateException(e);
	    }
	    for (final Entry<Pair<Class<?>, String>, ILocatorDomainTreeManagerAndEnhancer> entry : this.persistentLocators.entrySet()) {
		persistent_to_current(entry.getKey().getKey(), entry.getKey().getValue()); // should be initialised with copies of persistent locators
	    }
	}
	return currentLocators;
    }

    private EnhancementSet locatorsWithLocalType() {
	if (locatorsWithLocalType == null) {
	    try {
		final Field locatorsWithLocalTypeField = LocatorManager.class.getDeclaredField("locatorsWithLocalType");
		final boolean isAccessible = locatorsWithLocalTypeField.isAccessible();
		locatorsWithLocalTypeField.setAccessible(true);
		locatorsWithLocalTypeField.set(this, createSet());
		locatorsWithLocalTypeField.setAccessible(isAccessible);
	    } catch (final Exception e) {
		e.printStackTrace();
		throw new IllegalStateException(e);
	    }
	    locatorsWithLocalType.addAll(locatorKeys()); // all non-null locators are LOCAL
	}
	return locatorsWithLocalType;
    }

    /**
     * A locator <i>manager</i> constructor (save, int, discard locators, etc.) for the first time instantiation.
     */
    public LocatorManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, rootTypes, AbstractDomainTree.<ILocatorDomainTreeManagerAndEnhancer>createPropertiesMap());
    }

    /**
     * A locator <i>manager</i> constructor (save, int, discard locators, etc.).
     *
     * @param serialiser
     */
    protected LocatorManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Map<Pair<Class<?>, String>, ILocatorDomainTreeManagerAndEnhancer> persistentLocators) {
	super(serialiser);

	// this instance should be initialised using Reflection when GlobalDomainTreeManager creates/deserialises the instance of LocatorManager
	this.globalRepresentation = null;
	this.rootTypes = createLinkedRootsSet();
	this.rootTypes.addAll(rootTypes);

	this.persistentLocators = createPropertiesMap();
	this.persistentLocators.putAll(persistentLocators);

	// currentLocators = createPropertiesMap();

	// VERY IMPORTANT : Please note that deepCopy operation is not applicable here, because deserialisation process cannot be mixed with serialisation.
	// This constructor is explicitly used in deserialisation. That is why "currentAnalyses" initialisation (by copying "persistentAnalyses")
	// should be performed after ALL deserialisation has been completed. In this case -- we will use lazy initialisation.
	currentLocators = null; // this stuff will be initialised during the first invocation of currentAnalyses().

	freezedLocators = createPropertiesMap();

	locatorsInEditingMode = createSet();

	locatorsWithLocalType = null; // createSet();
	// locatorsWithLocalType.addAll(locatorKeys()); // all non-null locators are LOCAL
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

    /**
     * Logs and throws an {@link RuntimeException} error with specified message to indicate inner implementation errors.
     *
     * @param message
     */
    private void implementationError(final String message) {
	logger.error(message);
	throw new RuntimeException(message);
    }

    private void nonEntityTypedPropertyError(final Class<?> root, final String property) {
	AbstractDomainTree.illegalType(root, property, "Could not do any locator-related action for 'non-AE' property [" + property + "] in type [" + root.getSimpleName() + "].", AbstractEntity.class);
    }

    @Override
    public ILocatorDomainTreeManagerAndEnhancer getLocatorManager(final Class<?> root, final String property) {
	nonEntityTypedPropertyError(root, property);
	return currentLocators().get(key(root, property));
    }

    @Override
    public Pair<Phase, Type> phaseAndTypeOfLocatorManager(final Class<?> root, final String property) {
	nonEntityTypedPropertyError(root, property);
        return new Pair<Phase, Type>(phase(root, property), type(root, property));
    }

    private Phase phase(final Class<?> root, final String property) {
	return freezedLocators.get(key(root, property)) != null ? FREEZED_EDITING_PHASE //
		: locatorsInEditingMode.contains(key(root, property)) ? EDITING_PHASE : USAGE_PHASE;
    }

    private Type type(final Class<?> root, final String property) {
	return locatorsWithLocalType().contains(key(root, property)) ? LOCAL : GLOBAL;
    }

    @Override
    public boolean isChangedLocatorManager(final Class<?> root, final String property) {
	nonEntityTypedPropertyError(root, property);
	final boolean isChanged = !EntityUtils.equalsEx(currentLocators().get(key(root, property)), persistentLocators.get(key(root, property)));
	if (USAGE_PHASE == phase(root, property) && isChanged) {
	    implementationError("Inner implementation error : locator isChanged == true in USAGE phase for some reason for property [" + property + "] in type [" + root.getSimpleName() + "].");
	}
	return isChanged;
    }

    @Override
    public List<Pair<Class<?>, String>> locatorKeys() {
	return new ArrayList<Pair<Class<?>, String>>(currentLocators().keySet());
    }

    private Class<?> propertyTypeForGlobalRepresentationLocator(final Class<?> root, final String property) {
	final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, property);
        return DynamicEntityClassLoader.getOriginalType(propertyType);
    }

    private ILocatorDomainTreeManagerAndEnhancer produceByDefault(final Class<?> root, final String property) {
	final Class<?> pType = propertyTypeForGlobalRepresentationLocator(root, property);
	final ILocatorDomainTreeManagerAndEnhancer l = globalRepresentation.getLocatorManagerByDefault(pType);
	// initialise checkedProperties tree to make it more predictable in getting meta-info from "checkedProperties"
	l.getFirstTick().checkedProperties(pType);
	l.getSecondTick().checkedProperties(pType);
	return l;
    }

    private void checkEmptinessOfGlobalLocator(final Class<?> root, final String property) {
	if (getLocatorManager(root, property) != null) {
	    implementationError("Inner implementation error : not 'null' locator with GLOBAL type in USAGE phase for type [" + root + "] and property [" + property + "].");
	}
    }

    private static void moveMgrCopy(final ISerialiser serialiser, final EnhancementPropertiesMap<ILocatorDomainTreeManagerAndEnhancer> from, final EnhancementPropertiesMap<ILocatorDomainTreeManagerAndEnhancer> to, final Class<?> root, final String property) {
	if (from.containsKey(key(root, property))) {
	    to.put(key(root, property), EntityUtils.deepCopy(from.get(key(root, property)), serialiser));
	} else {
	    to.remove(key(root, property));
	}
    }

    private void current_to_current(final Class<?> root, final String property) {
	moveMgrCopy(getSerialiser(), currentLocators(), currentLocators(), root, property);
    }

    private void current_to_persistent(final Class<?> root, final String property) {
	moveMgrCopy(getSerialiser(), currentLocators(), persistentLocators, root, property);
    }

    private void persistent_to_current(final Class<?> root, final String property) {
	moveMgrCopy(getSerialiser(), persistentLocators, currentLocators(), root, property);
    }

    private void moveToUSAGE_PHASE(final Class<?> root, final String property) {
	locatorsInEditingMode.remove(key(root, property));
    }

    private void moveToEDITING_PHASE(final Class<?> root, final String property) {
	locatorsInEditingMode.add(key(root, property));
    }

    private void makeGLOBAL(final Class<?> root, final String property) {
	locatorsWithLocalType().remove(key(root, property));
    }

    private void makeLOCAL(final Class<?> root, final String property) {
	locatorsWithLocalType().add(key(root, property));
    }

    @Override
    public ILocatorManager refreshLocatorManager(final Class<?> root, final String property) {
	nonEntityTypedPropertyError(root, property);
	if (USAGE_PHASE == phase(root, property)) {
	    if (GLOBAL == type(root, property)) {
		checkEmptinessOfGlobalLocator(root, property);
		currentLocators().put(key(root, property), produceByDefault(root, property));
		current_to_persistent(root, property);
	    } else { // LOCAL_PHASE
		current_to_current(root, property);
	    }
	    moveToEDITING_PHASE(root, property);
	} else { // not applicable
	    error("Could not Refresh locator while it is editing. Please Accept or Discard it before Refresh (maybe multiple times in case of freezed locator). Property [" + property + "] in type [" + root.getSimpleName() + "].");
	}
	return this;
    }

    @Override
    public ILocatorManager resetLocatorManagerToDefault(final Class<?> root, final String property) {
	nonEntityTypedPropertyError(root, property);
	if (USAGE_PHASE == phase(root, property)) {
	    if (LOCAL == type(root, property)) {
		currentLocators().remove(key(root, property));
		current_to_persistent(root, property);
		makeGLOBAL(root, property);
	    } else {
		checkEmptinessOfGlobalLocator(root, property);
		// do nothing
	    }
	} else { // not applicable
	    error("Could not Reset locator to Default while it is editing. Please Accept or Discard it before ResetToDefault (maybe multiple times in case of freezed locator). Property [" + property + "] in type [" + root.getSimpleName() + "].");
	}
	return this;
    }


    @Override
    public ILocatorManager acceptLocatorManager(final Class<?> root, final String property) {
	nonEntityTypedPropertyError(root, property);
	if (USAGE_PHASE == phase(root, property)) { // USAGE_PHASE -- not applicable
	    error("Could not Accept locator while it is in Usage phase. Please Refresh it (that will move it to Editing phase) before Accept. Property [" + property + "] in type [" + root.getSimpleName() + "].");
	}
	if (EDITING_PHASE == phase(root, property)) {
	    if (GLOBAL == type(root, property)) {
		makeLOCAL(root, property);
	    }
	    current_to_persistent(root, property);
	    moveToUSAGE_PHASE(root, property);
	} else if (FREEZED_EDITING_PHASE == phase(root, property)) {
	    unfreeze(root, property);
	}
	current_to_current(root, property);
	return this;
    }

    @Override
    public ILocatorManager discardLocatorManager(final Class<?> root, final String property) {
	nonEntityTypedPropertyError(root, property);
	if (EDITING_PHASE == phase(root, property)) {
	    if (GLOBAL == type(root, property)) {
		persistentLocators.remove(key(root, property));
	    }
	    persistent_to_current(root, property);
	    moveToUSAGE_PHASE(root, property);
	} else if (FREEZED_EDITING_PHASE == phase(root, property)) {
	    persistent_to_current(root, property);
	    unfreeze(root, property);
	} else { // USAGE_PHASE -- not applicable
	    error("Could not Discard locator while it is in Usage phase. Please Refresh it (that will move it to Editing phase) before Discard. Property [" + property + "] in type [" + root.getSimpleName() + "].");
	}
	return this;
    }

    @Override
    public ILocatorManager saveLocatorManagerGlobally(final Class<?> root, final String property) {
	nonEntityTypedPropertyError(root, property);
	if (EDITING_PHASE == phase(root, property)) {
	    globalRepresentation.setLocatorManagerByDefault(propertyTypeForGlobalRepresentationLocator(root, property), getLocatorManager(root, property));
	} else { // not applicable
	    error("Could not SaveGlobally locator while it is not in Editing phase. Property [" + property + "] in type [" + root.getSimpleName() + "].");
	}
	return this;
    }

    @Override
    public ILocatorManager freezeLocatorManager(final Class<?> root, final String property) {
	nonEntityTypedPropertyError(root, property);
	if (EDITING_PHASE == phase(root, property)) {
	    freezedLocators.put(key(root, property), persistentLocators.remove(key(root, property)));
	    current_to_persistent(root, property);
	    current_to_current(root, property);
	} else { // not applicable
	    error("Could not Freeze locator while it is not in Editing phase (e.g. double freezing is not permitted). Property [" + property + "] in type [" + root.getSimpleName() + "].");
	}
	return this;
    }

    /**
     * Unfreezes the locator instance that is currently freezed.
     *
     * @param root
     * @param property
     */
    private void unfreeze(final Class<?> root, final String property) {
	persistentLocators.put(key(root, property), freezedLocators.remove(key(root, property)));
    }

    /**
     * A specific Kryo serialiser for {@link LocatorManager}.
     *
     * @author TG Team
     *
     */
    public static class LocatorManagerSerialiser extends AbstractDomainTreeSerialiser<LocatorManager> {
	public LocatorManagerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public LocatorManager read(final ByteBuffer buffer) {
	    final EnhancementLinkedRootsSet rootTypes = readValue(buffer, EnhancementLinkedRootsSet.class);
	    final EnhancementPropertiesMap<ILocatorDomainTreeManagerAndEnhancer> persistentLocators = readValue(buffer, EnhancementPropertiesMap.class);

//	    for (final ILocatorDomainTreeManagerAndEnhancer loc : persistentLocators.values()) {
//		EntityUtils.deepCopy(loc, kryo());
//	    }

	    return new LocatorManager(kryo(), rootTypes, persistentLocators);
	}

	@Override
	public void write(final ByteBuffer buffer, final LocatorManager manager) {
	    writeValue(buffer, manager.rootTypes);
	    writeValue(buffer, manager.persistentLocators);
	}
    }

    @Override
    public Set<Class<?>> rootTypes() {
	return rootTypes;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((persistentLocators == null) ? 0 : persistentLocators.hashCode());
	result = prime * result + ((rootTypes == null) ? 0 : rootTypes.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final LocatorManager other = (LocatorManager) obj;
	if (persistentLocators == null) {
	    if (other.persistentLocators != null)
		return false;
	} else if (!persistentLocators.equals(other.persistentLocators))
	    return false;
	if (rootTypes == null) {
	    if (other.rootTypes != null)
		return false;
	} else if (!rootTypes.equals(other.rootTypes))
	    return false;
	return true;
    }

    public IGlobalDomainTreeRepresentation getGlobalRepresentation() {
	return globalRepresentation;
    }
}
