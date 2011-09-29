package ua.com.fielden.platform.domaintree.master.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.LocatorManager;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.utils.Pair;

/**
 * Entity master domain tree manager. Includes locators management logic. <br><br>
 *
 * @author TG Team
 *
 */
public class MasterDomainTreeManager extends AbstractDomainTree implements IMasterDomainTreeManager {
    private static final long serialVersionUID = 7832625541851145438L;

    private final LocatorManager locatorManager;

    public LocatorManager locatorManager() {
	return locatorManager;
    }

    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public MasterDomainTreeManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, new LocatorManager(serialiser, rootTypes));
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param locatorManager
     */
    protected MasterDomainTreeManager(final ISerialiser serialiser, final LocatorManager locatorManager) {
	super(serialiser);
	this.locatorManager = locatorManager;
    }

    @Override
    public void initLocatorManagerByDefault(final Class<?> root, final String property) {
	locatorManager.initLocatorManagerByDefault(root, property);
    }

    @Override
    public void resetLocatorManager(final Class<?> root, final String property) {
	locatorManager.resetLocatorManager(root, property);
    }

    @Override
    public ILocatorDomainTreeManagerAndEnhancer produceLocatorManagerByDefault(final Class<?> root, final String property) {
	return locatorManager.produceLocatorManagerByDefault(root, property);
    }

    @Override
    public void discardLocatorManager(final Class<?> root, final String property) {
	locatorManager.discardLocatorManager(root, property);
    }

    @Override
    public void acceptLocatorManager(final Class<?> root, final String property) {
	locatorManager.acceptLocatorManager(root, property);
    }

    @Override
    public void saveLocatorManagerGlobally(final Class<?> root, final String property) {
	locatorManager.saveLocatorManagerGlobally(root, property);
    }

    @Override
    public ILocatorDomainTreeManagerAndEnhancer getLocatorManager(final Class<?> root, final String property) {
	return locatorManager.getLocatorManager(root, property);
    }

    @Override
    public boolean isChangedLocatorManager(final Class<?> root, final String property) {
	return locatorManager.isChangedLocatorManager(root, property);
    }

    @Override
    public List<Pair<Class<?>, String>> locatorKeys() {
	return locatorManager.locatorKeys();
    }

    /**
     * A specific Kryo serialiser for {@link MasterDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    public static class MasterDomainTreeManagerSerialiser extends AbstractDomainTreeSerialiser<MasterDomainTreeManager> {
	public MasterDomainTreeManagerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public MasterDomainTreeManager read(final ByteBuffer buffer) {
	    final LocatorManager locatorManager = readValue(buffer, LocatorManager.class);
	    return new MasterDomainTreeManager(kryo(), locatorManager);
	}

	@Override
	public void write(final ByteBuffer buffer, final MasterDomainTreeManager manager) {
	    writeValue(buffer, manager.locatorManager);
	}
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((locatorManager == null) ? 0 : locatorManager.hashCode());
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
	final MasterDomainTreeManager other = (MasterDomainTreeManager) obj;
	if (locatorManager == null) {
	    if (other.locatorManager != null)
		return false;
	} else if (!locatorManager.equals(other.locatorManager))
	    return false;
	return true;
    }

    @Override
    public Set<Class<?>> rootTypes() {
	return locatorManager.rootTypes();
    }
}
