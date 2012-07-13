package ua.com.fielden.platform.domaintree.centre.impl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
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
	this(serialiser, new LocatorDomainTreeManager(serialiser, AbstractDomainTree.validateRootTypes(rootTypes)), new DomainTreeEnhancer(serialiser, AbstractDomainTree.validateRootTypes(rootTypes)), new HashMap<String, IAbstractAnalysisDomainTreeManager>(), new HashMap<String, IAbstractAnalysisDomainTreeManager>(), new HashMap<String, IAbstractAnalysisDomainTreeManager>());
    }

    protected LocatorDomainTreeManagerAndEnhancer(final ISerialiser serialiser, final LocatorDomainTreeManager base, final IDomainTreeEnhancer enhancer, final Map<String, IAbstractAnalysisDomainTreeManager> persistentAnalyses, final Map<String, IAbstractAnalysisDomainTreeManager> currentAnalyses, final Map<String, IAbstractAnalysisDomainTreeManager> freezedAnalyses) {
	super(serialiser, base, enhancer, persistentAnalyses, currentAnalyses, freezedAnalyses);
    }

    @Override
    protected DomainTreeRepresentationAndEnhancer createRepresentation(final AbstractDomainTreeRepresentation base) {
	return new LocatorDomainTreeRepresentationAndEnhancer(base);
    }

    @Override
    public ILocatorDomainTreeManager base() {
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
    public static class LocatorDomainTreeManagerAndEnhancerWithTransientAnalysesSerialiser extends TgSimpleSerializer<LocatorDomainTreeManagerAndEnhancer> {
	public LocatorDomainTreeManagerAndEnhancerWithTransientAnalysesSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public LocatorDomainTreeManagerAndEnhancer read(final ByteBuffer buffer) {
	    final LocatorDomainTreeManager base = readValue(buffer, LocatorDomainTreeManager.class);
	    final DomainTreeEnhancer enhancer = readValue(buffer, DomainTreeEnhancer.class);
	    final Map<String, IAbstractAnalysisDomainTreeManager> persistentAnalyses = readValue(buffer, HashMap.class);
	    final Map<String, IAbstractAnalysisDomainTreeManager> currentAnalyses = readValue(buffer, HashMap.class);
	    final Map<String, IAbstractAnalysisDomainTreeManager> freezedAnalyses = readValue(buffer, HashMap.class);
	    return new LocatorDomainTreeManagerAndEnhancer(kryo, base, enhancer, persistentAnalyses, currentAnalyses, freezedAnalyses);
	}

	@Override
	public void write(final ByteBuffer buffer, final LocatorDomainTreeManagerAndEnhancer manager) {
	    writeValue(buffer, manager.base());
	    writeValue(buffer, manager.enhancer());
	    writeValue(buffer, manager.persistentAnalyses());
	    writeValue(buffer, manager.currentAnalyses());
	    writeValue(buffer, manager.freezedAnalyses());
	}
    }
}
