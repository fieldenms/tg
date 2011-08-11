package ua.com.fielden.platform.treemodel.rules.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.treemodel.rules.IGlobalDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.ILocatorManager;
import ua.com.fielden.platform.treemodel.rules.criteria.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * A locator manager mixin implementation (save, init, discard locators etc.).
 *
 * @author TG Team
 *
 */
public class LocatorManager extends AbstractDomainTree implements ILocatorManager {
    // this instance should be initialised using Reflection when GlobalDomainTreeManager creates/deserialises the instance of LocatorManager
    private final transient IGlobalDomainTreeRepresentation globalRepresentation;
    private final Set<Class<?>> rootTypes;
    private final Map<Pair<Class<?>, String>, ILocatorDomainTreeManagerAndEnhancer> persistentLocators;
    private final transient Map<Pair<Class<?>, String>, ILocatorDomainTreeManagerAndEnhancer> currentLocators;

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
	this.rootTypes = new LinkedHashSet<Class<?>>(rootTypes) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public boolean contains(final Object o) {
		final Class<?> root = (Class<?>) o;
		return super.contains(DynamicEntityClassLoader.getOriginalType(root));
	    };
	};

	this.persistentLocators = createPropertiesMap();
	this.persistentLocators.putAll(persistentLocators);

	currentLocators = createPropertiesMap();
	for (final Entry<Pair<Class<?>, String>, ILocatorDomainTreeManagerAndEnhancer> entry : this.persistentLocators.entrySet()) {
	    currentLocators.put(entry.getKey(), EntityUtils.deepCopy(entry.getValue(), getSerialiser())); // should be initialised with copies of persistent locators
	}
    }

    @Override
    public ILocatorDomainTreeManagerAndEnhancer produceLocatorManagerByDefault(final Class<?> root, final String property) {
	AbstractDomainTree.illegalType(root, property, "Could not init a locator for 'non-AE' property [" + property + "] in type [" + root.getSimpleName() + "].", AbstractEntity.class);
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, property);
	return globalRepresentation.getLocatorManagerByDefault(propertyType);
    }

    private void init(final Class<?> root, final String property, final ILocatorDomainTreeManagerAndEnhancer mgr) {
	// create a new instance and put to "current" map
	currentLocators.put(key(root, property), mgr);
	// the init method fully accepts a new instance of manager. After that it should be 'unchanged'.
	acceptLocatorManager(root, property);
    }

    @Override
    public void initLocatorManagerByDefault(final Class<?> root, final String property) {
	init(root, property, produceLocatorManagerByDefault(root, property));
    }

    @Override
    public void resetLocatorManager(final Class<?> root, final String property) {
	init(root, property, null);
    }

    @Override
    public void discardLocatorManager(final Class<?> root, final String property) {
	AbstractDomainTree.illegalType(root, property, "Could not discard a locator for 'non-AE' property [" + property + "] in type [" + root.getSimpleName() + "].", AbstractEntity.class);
	currentLocators.put(key(root, property), EntityUtils.deepCopy(persistentLocators.get(key(root, property)), getSerialiser()));
    }

    @Override
    public void acceptLocatorManager(final Class<?> root, final String property) {
	AbstractDomainTree.illegalType(root, property, "Could not save a locator for 'non-AE' property [" + property + "] in type [" + root.getSimpleName() + "].", AbstractEntity.class);
	persistentLocators.put(key(root, property), EntityUtils.deepCopy(currentLocators.get(key(root, property)), getSerialiser()));
    }

    @Override
    public void saveLocatorManagerGlobally(final Class<?> root, final String property) {
	AbstractDomainTree.illegalType(root, property, "Could not save globally a locator for 'non-AE' property [" + property + "] in type [" + root.getSimpleName() + "].", AbstractEntity.class);
        final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, property);
	globalRepresentation.setLocatorManagerByDefault(propertyType, getLocatorManager(root, property));
    }

    @Override
    public ILocatorDomainTreeManagerAndEnhancer getLocatorManager(final Class<?> root, final String property) {
	AbstractDomainTree.illegalType(root, property, "Could not retrieve a locator for 'non-AE' property [" + property + "] in type [" + root.getSimpleName() + "].", AbstractEntity.class);
	return currentLocators.get(key(root, property));
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
	    final Set<Class<?>> rootTypes = readValue(buffer, HashSet.class);
	    final Map<Pair<Class<?>, String>, ILocatorDomainTreeManagerAndEnhancer> persistentLocators = readValue(buffer, HashMap.class);
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
