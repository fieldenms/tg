package ua.com.fielden.platform.treemodel.rules.criteria.impl;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeEnhancer;
import ua.com.fielden.platform.treemodel.rules.criteria.ILocatorDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.criteria.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.criteria.ILocatorDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.impl.DomainTreeEnhancer;

/**
 * Criteria (entity-centre) domain tree manager with "power" of managing domain with calculated properties. The calculated properties can be managed exactly as simple properties.<br>
 *
 * @author TG Team
 *
 */
public class LocatorDomainTreeManagerAndEnhancer extends CriteriaDomainTreeManagerAndEnhancer implements ILocatorDomainTreeManagerAndEnhancer {
    public LocatorDomainTreeManagerAndEnhancer(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(new LocatorDomainTreeManager(serialiser, rootTypes), new DomainTreeEnhancer(rootTypes));
    }

    protected LocatorDomainTreeManagerAndEnhancer(final LocatorDomainTreeManager base, final IDomainTreeEnhancer enhancer) {
	super(base, enhancer);
    }

    @Override
    protected DomainTreeRepresentationAndEnhancer createRepresentation(final AbstractDomainTreeRepresentation base) {
	return new LocatorDomainTreeRepresentationAndEnhancer(base);
    }

    @Override
    protected ILocatorDomainTreeManager base() {
	return (ILocatorDomainTreeManager) super.base();
    }

    @Override
    public ILocatorDomainTreeRepresentation getRepresentation() {
        return (ILocatorDomainTreeRepresentation) super.getRepresentation();
    }

    @Override
    public SearchBy getSearchBy() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public ILocatorDomainTreeManager setSearchBy(final SearchBy searchBy) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public boolean isUseForAutocompletion() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public ILocatorDomainTreeManager setUseForAutocompletion(final boolean useForAutocompletion) {
	// TODO Auto-generated method stub
	return null;
    }

    /**
     * Overridden to take into account calculated properties.
     *
     * @author TG Team
     *
     */
    protected class LocatorDomainTreeRepresentationAndEnhancer extends CriteriaDomainTreeRepresentationAndEnhancer implements ILocatorDomainTreeRepresentation {
	private static final long serialVersionUID = -5345869657944629725L;

	protected LocatorDomainTreeRepresentationAndEnhancer(final AbstractDomainTreeRepresentation base) {
	    super(base);
	}
    }

    /**
     * A specific Kryo serialiser for {@link LocatorDomainTreeManagerAndEnhancer}.
     *
     * @author TG Team
     *
     */
    public static class LocatorDomainTreeManagerAndEnhancerSerialiser extends TgSimpleSerializer<LocatorDomainTreeManagerAndEnhancer> {
	public LocatorDomainTreeManagerAndEnhancerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public LocatorDomainTreeManagerAndEnhancer read(final ByteBuffer buffer) {
	    final LocatorDomainTreeManager base = readValue(buffer, LocatorDomainTreeManager.class);
	    final DomainTreeEnhancer enhancer = readValue(buffer, DomainTreeEnhancer.class);
	    return new LocatorDomainTreeManagerAndEnhancer(base, enhancer);
	}

	@Override
	public void write(final ByteBuffer buffer, final LocatorDomainTreeManagerAndEnhancer manager) {
	    writeValue(buffer, manager.base());
	    writeValue(buffer, manager.enhancer());
	}
    }
}
