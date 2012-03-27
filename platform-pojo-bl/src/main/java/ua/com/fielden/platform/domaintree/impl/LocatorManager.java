package ua.com.fielden.platform.domaintree.impl;

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
    private final transient EnhancementPropertiesMap<ILocatorDomainTreeManagerAndEnhancer> currentLocators;
    private final transient EnhancementPropertiesMap<ILocatorDomainTreeManagerAndEnhancer> freezedLocators;

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

	this.persistentLocators = createPropertiesMap();
	this.persistentLocators.putAll(persistentLocators);

	currentLocators = createPropertiesMap();
	for (final Entry<Pair<Class<?>, String>, ILocatorDomainTreeManagerAndEnhancer> entry : this.persistentLocators.entrySet()) {
	    currentLocators.put(entry.getKey(), EntityUtils.deepCopy(entry.getValue(), getSerialiser())); // should be initialised with copies of persistent locators
	}
	freezedLocators = createPropertiesMap();
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
    public ILocatorDomainTreeManagerAndEnhancer produceLocatorManagerByDefault(final Class<?> root, final String property) {
	if (isFreezedLocatorManager(root, property)) {
	    error("Unable to Produce locator instance if it is freezed for type [" + root + "] and property [" + property + "].");
	}
	AbstractDomainTree.illegalType(root, property, "Could not init a locator for 'non-AE' property [" + property + "] in type [" + root.getSimpleName() + "].", AbstractEntity.class);
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, property);
        final Class<?> notEnhancedPropertyType = DynamicEntityClassLoader.getOriginalType(propertyType);
	return globalRepresentation.getLocatorManagerByDefault(notEnhancedPropertyType);
    }

    private void init(final Class<?> root, final String property, final ILocatorDomainTreeManagerAndEnhancer mgr) {
	// create a new instance and put to "current" map
	currentLocators.put(key(root, property), mgr);
	// the init method fully accepts a new instance of manager. After that it should be 'unchanged'.
	acceptLocatorManager(root, property);
    }

    @Override
    public void initLocatorManagerByDefault(final Class<?> root, final String property) {
	if (isFreezedLocatorManager(root, property)) {
	    error("Unable to Init locator instance if it is freezed for type [" + root + "] and property [" + property + "].");
	}
	init(root, property, produceLocatorManagerByDefault(root, property));
    }

    @Override
    public void resetLocatorManager(final Class<?> root, final String property) {
	if (isFreezedLocatorManager(root, property)) {
	    error("Unable to Reset locator instance if it is freezed for type [" + root + "] and property [" + property + "].");
	}
	init(root, property, null);
    }

    @Override
    public void discardLocatorManager(final Class<?> root, final String property) {
	AbstractDomainTree.illegalType(root, property, "Could not discard a locator for 'non-AE' property [" + property + "] in type [" + root.getSimpleName() + "].", AbstractEntity.class);
	currentLocators.put(key(root, property), EntityUtils.deepCopy(persistentLocators.get(key(root, property)), getSerialiser()));

	if (isFreezedLocatorManager(root, property)) {
	    unfreeze(root, property);
	}
    }

    @Override
    public void acceptLocatorManager(final Class<?> root, final String property) {
	if (isFreezedLocatorManager(root, property)) {
	    unfreeze(root, property);
	} else {
	    AbstractDomainTree.illegalType(root, property, "Could not save a locator for 'non-AE' property [" + property + "] in type [" + root.getSimpleName() + "].", AbstractEntity.class);
	    persistentLocators.put(key(root, property), EntityUtils.deepCopy(currentLocators.get(key(root, property)), getSerialiser()));
	}
    }

    @Override
    public void saveLocatorManagerGlobally(final Class<?> root, final String property) {
	if (isFreezedLocatorManager(root, property)) {
	    error("Unable to Save locator instance Globally if it is freezed for type [" + root + "] and property [" + property + "].");
	}
	AbstractDomainTree.illegalType(root, property, "Could not save globally a locator for 'non-AE' property [" + property + "] in type [" + root.getSimpleName() + "].", AbstractEntity.class);
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, property);
        final Class<?> notEnhancedPropertyType = DynamicEntityClassLoader.getOriginalType(propertyType);
	globalRepresentation.setLocatorManagerByDefault(notEnhancedPropertyType, getLocatorManager(root, property));
    }

    @Override
    public ILocatorDomainTreeManagerAndEnhancer getLocatorManager(final Class<?> root, final String property) {
	AbstractDomainTree.illegalType(root, property, "Could not retrieve a locator for 'non-AE' property [" + property + "] in type [" + root.getSimpleName() + "].", AbstractEntity.class);
	return currentLocators.get(key(root, property));
    }

    /**
     * Throws an error when the instance is <code>null</code> (not initialised).
     *
     * @param mgr
     * @param root
     * @param name
     */
    private void notInitiliasedError(final ILocatorDomainTreeManagerAndEnhancer mgr, final Class<?> root, final String property) {
	if (mgr == null) {
	    error("Unable to perform this operation on the locator instance, that wasn't initialised, for type [" + root + "] and property [" + property + "].");
	}
    }

    @Override
    public void freezeLocatorManager(final Class<?> root, final String property) {
	if (isFreezedLocatorManager(root, property)) {
	    error("Unable to freeze the locator instance more than once for type [" + root + "] and property [" + property + "].");
	}
	notInitiliasedError(persistentLocators.get(key(root, property)), root, property);
	notInitiliasedError(currentLocators.get(key(root, property)), root, property);
	final ILocatorDomainTreeManagerAndEnhancer persistentLocator = persistentLocators.remove(key(root, property));
	freezedLocators.put(key(root, property), persistentLocator);
	persistentLocators.put(key(root, property), EntityUtils.deepCopy(currentLocators.get(key(root, property)), getSerialiser()));
    }

    /**
     * Returns <code>true</code> if the locator instance is in 'freezed' state, <code>false</code> otherwise.
     *
     * @param root
     * @param property
     * @return
     */
    @Override
    public boolean isFreezedLocatorManager(final Class<?> root, final String property) {
	return freezedLocators.get(key(root, property)) != null;
    }

    /**
     * Unfreezes the locator instance that is currently freezed.
     *
     * @param root
     * @param property
     */
    protected void unfreeze(final Class<?> root, final String property) {
	if (!isFreezedLocatorManager(root, property)) {
	    error("Unable to unfreeze the locator instance that is not 'freezed' for type [" + root + "] and property [" + property + "].");
	}
	final ILocatorDomainTreeManagerAndEnhancer persistentLocator = freezedLocators.remove(key(root, property));
	persistentLocators.put(key(root, property), persistentLocator);
    }

    @Override
    public boolean isChangedLocatorManager(final Class<?> root, final String property) {
	AbstractDomainTree.illegalType(root, property, "Could not ask whether a locator has been changed for 'non-AE' property [" + property + "] in type [" + root.getSimpleName() + "].", AbstractEntity.class);
	return !EntityUtils.equalsEx(currentLocators.get(key(root, property)), persistentLocators.get(key(root, property)));
    }

    @Override
    public List<Pair<Class<?>, String>> locatorKeys() {
	return new ArrayList<Pair<Class<?>, String>>(currentLocators.keySet());
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
}
