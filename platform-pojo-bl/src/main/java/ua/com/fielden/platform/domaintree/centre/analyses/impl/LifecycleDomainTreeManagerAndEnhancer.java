package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeManager.ILifecycleDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.ILifecycleDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeManager.LifecycleAddToCategoriesTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeManager.LifecycleAddToDistributionTickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.TickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.AbstractTickRepresentation;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;
import ua.com.fielden.platform.utils.Pair;

public class LifecycleDomainTreeManagerAndEnhancer extends AbstractAnalysisDomainTreeManagerAndEnhancer implements ILifecycleDomainTreeManagerAndEnhancer {
    public LifecycleDomainTreeManagerAndEnhancer(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(new LifecycleDomainTreeManager(serialiser, AbstractDomainTree.validateRootTypes(rootTypes)), new DomainTreeEnhancer(serialiser, AbstractDomainTree.validateRootTypes(rootTypes)));
    }

    protected LifecycleDomainTreeManagerAndEnhancer(final LifecycleDomainTreeManager base, final IDomainTreeEnhancer enhancer) {
	super(base, enhancer);
    }

    @Override
    protected LifecycleDomainTreeManager base() {
        return (LifecycleDomainTreeManager) super.base();
    }

    @Override
    protected LifecycleAddToDistributionTickManagerAndEnhancer createFirstTick(final TickManager base) {
	return new LifecycleAddToDistributionTickManagerAndEnhancer(base);
    }

    @Override
    protected LifecycleAddToCategoriesTickManagerAndEnhancer createSecondTick(final TickManager base) {
	return new LifecycleAddToCategoriesTickManagerAndEnhancer(base);
    }

    @Override
    protected LifecycleDomainTreeRepresentationAndEnhancer createRepresentation(final AbstractDomainTreeRepresentation base) {
	return new LifecycleDomainTreeRepresentationAndEnhancer(base);
    }

    @Override
    public ILifecycleDomainTreeRepresentation getRepresentation() {
	return (ILifecycleDomainTreeRepresentation) super.getRepresentation();
    }

    public class LifecycleAddToDistributionTickManagerAndEnhancer extends AbstractAnalysisAddToDistributionTickManagerAndEnhancer implements ILifecycleAddToDistributionTickManager {
	protected LifecycleAddToDistributionTickManagerAndEnhancer(final TickManager base) {
	    super(base);
	}

	@Override
	protected LifecycleAddToDistributionTickManager base() {
	    return (LifecycleAddToDistributionTickManager) super.base();
	}
    }

    public class LifecycleAddToCategoriesTickManagerAndEnhancer extends AbstractAnalysisAddToAggregationTickManagerAndEnhancer implements ILifecycleAddToCategoriesTickManager {
	protected LifecycleAddToCategoriesTickManagerAndEnhancer(final TickManager base) {
	    super(base);
	}

	@Override
	protected LifecycleAddToCategoriesTickManager base() {
	    return (LifecycleAddToCategoriesTickManager) super.base();
	}
    }

    public class LifecycleDomainTreeRepresentationAndEnhancer extends AbstractAnalysisDomainTreeRepresentationAndEnhancer implements ILifecycleDomainTreeRepresentation {
	protected LifecycleDomainTreeRepresentationAndEnhancer(final AbstractDomainTreeRepresentation base) {
	    super(base);
	}

	@Override
	public ILifecycleAddToDistributionTickRepresentation getFirstTick() {
	    return (ILifecycleAddToDistributionTickRepresentation) super.getFirstTick();
	}

	@Override
	public ILifecycleAddToCategoriesTickRepresentation getSecondTick() {
	    return (ILifecycleAddToCategoriesTickRepresentation) super.getSecondTick();
	}

	@Override
	protected ILifecycleAddToDistributionTickRepresentation createFirstTick(final AbstractTickRepresentation base) {
	    return new LifecycleAddToDistributionTickRepresentationAndEnhancer(base);
	}

	@Override
	protected ILifecycleAddToCategoriesTickRepresentation createSecondTick(final AbstractTickRepresentation base) {
	    return new LifecycleAddToCategoriesTickRepresentationAndEnhancer(base);
	}

	/**
	 * Overridden to take into account calculated properties.
	 *
	 * @author TG Team
	 *
	 */
	public class LifecycleAddToDistributionTickRepresentationAndEnhancer extends AbstractAnalysisAddToDistributionTickRepresentationAndEnhancer implements ILifecycleAddToDistributionTickRepresentation {
	    private static final long serialVersionUID = -8143739289123268471L;

	    protected LifecycleAddToDistributionTickRepresentationAndEnhancer(final AbstractTickRepresentation base) {
		super(base);
	    }

	    @Override
	    protected ILifecycleAddToDistributionTickRepresentation base() {
		return (ILifecycleAddToDistributionTickRepresentation) super.base();
	    }
	}

	/**
	 * Overridden to take into account calculated properties.
	 *
	 * @author TG Team
	 *
	 */
	public class LifecycleAddToCategoriesTickRepresentationAndEnhancer extends AbstractAnalysisAddToAggregationTickRepresentationAndEnhancer implements ILifecycleAddToCategoriesTickRepresentation {
	    private static final long serialVersionUID = -8143739289123268471L;

	    protected LifecycleAddToCategoriesTickRepresentationAndEnhancer(final AbstractTickRepresentation base) {
		super(base);
	    }

	    @Override
	    protected ILifecycleAddToCategoriesTickRepresentation base() {
		return (ILifecycleAddToCategoriesTickRepresentation) super.base();
	    }
	}
    }

    @Override
    public ILifecycleAddToDistributionTickManager getFirstTick() {
	return (ILifecycleAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public ILifecycleAddToCategoriesTickManager getSecondTick() {
	return (ILifecycleAddToCategoriesTickManager) super.getSecondTick();
    };

    /**
     * A specific Kryo serialiser for {@link LifecycleDomainTreeManagerAndEnhancer}.
     *
     * @author TG Team
     *
     */
    public static class LifecycleDomainTreeManagerAndEnhancerSerialiser extends TgSimpleSerializer<LifecycleDomainTreeManagerAndEnhancer> {
	public LifecycleDomainTreeManagerAndEnhancerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public LifecycleDomainTreeManagerAndEnhancer read(final ByteBuffer buffer) {
	    final LifecycleDomainTreeManager base = readValue(buffer, LifecycleDomainTreeManager.class);
	    final DomainTreeEnhancer enhancer = readValue(buffer, DomainTreeEnhancer.class);
	    return new LifecycleDomainTreeManagerAndEnhancer(base, enhancer);
	}

	@Override
	public void write(final ByteBuffer buffer, final LifecycleDomainTreeManagerAndEnhancer manager) {
	    writeValue(buffer, manager.base());
	    writeValue(buffer, manager.enhancer());
	}
    }

    @Override
    public Pair<Class<?>, String> getLifecycleProperty() {
	return base().getLifecycleProperty();
    }

    @Override
    public ILifecycleDomainTreeManager setLifecycleProperty(final Pair<Class<?>, String> lifecycleProperty) {
	base().setLifecycleProperty(lifecycleProperty);
	return this;
    }

    @Override
    public Date getFrom() {
	return base().getFrom();
    }

    @Override
    public ILifecycleDomainTreeManager setFrom(final Date from) {
	base().setFrom(from);
	return this;
    }

    @Override
    public Date getTo() {
	return base().getTo();
    }

    @Override
    public ILifecycleDomainTreeManager setTo(final Date to) {
	base().setTo(to);
	return this;
    }

    @Override
    public boolean isTotal() {
	return base().isTotal();
    }

    @Override
    public IAbstractAnalysisDomainTreeManager setTotal(final boolean total) {
	base().setTotal(total);
	return this;
    }
}
