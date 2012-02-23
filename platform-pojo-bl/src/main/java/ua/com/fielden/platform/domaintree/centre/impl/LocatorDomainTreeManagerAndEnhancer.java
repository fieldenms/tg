package ua.com.fielden.platform.domaintree.centre.impl;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;

/**
 * Criteria (entity-centre) domain tree manager with "power" of managing domain with calculated properties. The calculated properties can be managed exactly as simple properties.<br>
 *
 * @author TG Team
 *
 */
public class LocatorDomainTreeManagerAndEnhancer extends CentreDomainTreeManagerAndEnhancer implements ILocatorDomainTreeManagerAndEnhancer {
    public LocatorDomainTreeManagerAndEnhancer(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(new LocatorDomainTreeManager(serialiser, rootTypes), new DomainTreeEnhancer(serialiser, rootTypes));
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
	return base().getSearchBy();
    }

    @Override
    public ILocatorDomainTreeManager setSearchBy(final SearchBy searchBy) {
	base().setSearchBy(searchBy);
	return this;
    }

    @Override
    public boolean isUseForAutocompletion() {
	return base().isUseForAutocompletion();
    }

    @Override
    public ILocatorDomainTreeManager setUseForAutocompletion(final boolean useForAutocompletion) {
	base().setUseForAutocompletion(useForAutocompletion);
	return this;
    }

    /**
     * Overridden to take into account calculated properties.
     *
     * @author TG Team
     *
     */
    protected class LocatorDomainTreeRepresentationAndEnhancer extends CentreDomainTreeRepresentationAndEnhancer implements ILocatorDomainTreeRepresentation {
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
