package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.util.List;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AbstractAnalysisDomainTreeManager.AbstractAnalysisAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AbstractAnalysisDomainTreeManager.AbstractAnalysisAddToDistributionTickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.TickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.AbstractTickRepresentation;
import ua.com.fielden.platform.utils.Pair;

/**
 * The abstract base implementation for all types of analyses.
 *
 * @author TG Team
 *
 */
public abstract class AbstractAnalysisDomainTreeManagerAndEnhancer extends AbstractDomainTreeManagerAndEnhancer implements IAbstractAnalysisDomainTreeManagerAndEnhancer {
    /**
     * A <i>manager with enhancer</i> constructor.
     */
    public AbstractAnalysisDomainTreeManagerAndEnhancer(final AbstractAnalysisDomainTreeManager base, final IDomainTreeEnhancer enhancer) {
	super(base, enhancer);
    }

    @Override
    protected IAbstractAnalysisDomainTreeManager base() {
	return (IAbstractAnalysisDomainTreeManager) super.base();
    }

    @Override
    public IAbstractAnalysisAddToDistributionTickManager getFirstTick() {
	return (IAbstractAnalysisAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public IAbstractAnalysisAddToAggregationTickManager getSecondTick() {
	return (IAbstractAnalysisAddToAggregationTickManager) super.getSecondTick();
    }

    @Override
    public IAbstractAnalysisDomainTreeRepresentation getRepresentation() {
	return (IAbstractAnalysisDomainTreeRepresentation) super.getRepresentation();
    }

    @Override
    protected abstract AbstractAnalysisAddToDistributionTickManagerAndEnhancer createFirstTick(final TickManager base);

    @Override
    protected abstract AbstractAnalysisAddToAggregationTickManagerAndEnhancer createSecondTick(final TickManager base);

    @Override
    protected abstract AbstractAnalysisDomainTreeRepresentationAndEnhancer createRepresentation(final AbstractDomainTreeRepresentation base);

    /**
     * Overridden to take into account calculated properties.
     *
     * @author TG Team
     *
     */
    protected abstract class AbstractAnalysisAddToDistributionTickManagerAndEnhancer extends TickManagerAndEnhancer implements IAbstractAnalysisAddToDistributionTickManager {
	private static final long serialVersionUID = 5845028563069886027L;

	protected AbstractAnalysisAddToDistributionTickManagerAndEnhancer(final TickManager base) {
	    super(base);
	}

	@Override
	protected AbstractAnalysisAddToDistributionTickManager base() {
	    return (AbstractAnalysisAddToDistributionTickManager) super.base();
	}

	@Override
	public boolean isUsed(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().isUsed(enhancer().getManagedType(root), property);
	}

	@Override
	public void use(final Class<?> root, final String property, final boolean check) {
	    // inject an enhanced type into method implementation
	    base().use(enhancer().getManagedType(root), property, check);
	}

	@Override
	public List<String> usedProperties(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    return base().usedProperties(enhancer().getManagedType(root));
	}

	@Override
	public boolean addPropertyUsageListener(final IPropertyUsageListener listener) {
	    return base().addPropertyUsageListener(listener);
	}

	@Override
	public boolean removePropertyUsageListener(final IPropertyUsageListener listener) {
	    return base().removePropertyUsageListener(listener);
	}
    }

    /**
     * Overridden to take into account calculated properties.
     *
     * @author TG Team
     *
     */
    protected abstract class AbstractAnalysisAddToAggregationTickManagerAndEnhancer extends TickManagerAndEnhancer implements IAbstractAnalysisAddToAggregationTickManager {
	private static final long serialVersionUID = 5845028563069886027L;

	protected AbstractAnalysisAddToAggregationTickManagerAndEnhancer(final TickManager base) {
	    super(base);
	}

	@Override
	protected AbstractAnalysisAddToAggregationTickManager base() {
	    return (AbstractAnalysisAddToAggregationTickManager) super.base();
	}

	@Override
	public boolean isUsed(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().isUsed(enhancer().getManagedType(root), property);
	}

	@Override
	public void use(final Class<?> root, final String property, final boolean check) {
	    // inject an enhanced type into method implementation
	    base().use(enhancer().getManagedType(root), property, check);
	}

	@Override
	public List<String> usedProperties(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    return base().usedProperties(enhancer().getManagedType(root));
	}

	@Override
	public List<Pair<String, Ordering>> orderedProperties(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    return base().orderedProperties(enhancer().getManagedType(root));
	}

	@Override
	public void toggleOrdering(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().toggleOrdering(enhancer().getManagedType(root), property);
	}

	@Override
	public boolean addPropertyUsageListener(final IPropertyUsageListener listener) {
	    return base().addPropertyUsageListener(listener);
	}

	@Override
	public boolean removePropertyUsageListener(final IPropertyUsageListener listener) {
	    return base().removePropertyUsageListener(listener);
	}

	@Override
	public boolean addPropertyOrderingListener(final IPropertyOrderingListener listener) {
	    return base().addPropertyOrderingListener(listener);
	}

	@Override
	public boolean removePropertyOrderingListener(final IPropertyOrderingListener listener) {
	    return base().removePropertyOrderingListener(listener);
	}
    }

    /**
     * Overridden to take into account calculated properties.
     *
     * @author TG Team
     *
     */
    protected abstract class AbstractAnalysisDomainTreeRepresentationAndEnhancer extends DomainTreeRepresentationAndEnhancer implements IAbstractAnalysisDomainTreeRepresentation {
	private static final long serialVersionUID = -5345869657944629725L;

	protected AbstractAnalysisDomainTreeRepresentationAndEnhancer(final AbstractDomainTreeRepresentation base) {
	    super(base);
	}

	@Override
	public IAbstractAnalysisAddToDistributionTickRepresentation getFirstTick() {
	    return (IAbstractAnalysisAddToDistributionTickRepresentation) super.getFirstTick();
	}

	@Override
	public IAbstractAnalysisAddToAggregationTickRepresentation getSecondTick() {
	    return (IAbstractAnalysisAddToAggregationTickRepresentation) super.getSecondTick();
	}

	@Override
	protected abstract IAbstractAnalysisAddToDistributionTickRepresentation createFirstTick(final AbstractTickRepresentation base);

	@Override
	protected abstract IAbstractAnalysisAddToAggregationTickRepresentation createSecondTick(final AbstractTickRepresentation base);

	//	@Override
	//	protected IAbstractAnalysisAddToDistributionTickRepresentation createFirstTick(final ITickRepresentation base) {
	//	    return new AbstractAnalysisAddToDistributionTickRepresentationAndEnhancer(base);
	//	}
	//
	//	@Override
	//	protected IAbstractAnalysisAddToAggregationTickRepresentation createSecondTick(final ITickRepresentation base) {
	//	    return new AbstractAnalysisAddToAggregationTickRepresentationAndEnhancer(base);
	//	}

	/**
	 * Overridden to take into account calculated properties.
	 *
	 * @author TG Team
	 *
	 */
	protected abstract class AbstractAnalysisAddToDistributionTickRepresentationAndEnhancer extends TickRepresentationAndEnhancer implements IAbstractAnalysisAddToDistributionTickRepresentation {
	    private static final long serialVersionUID = -8143739289123268471L;

	    protected AbstractAnalysisAddToDistributionTickRepresentationAndEnhancer(final AbstractTickRepresentation base) {
		super(base);
	    }

	    @Override
	    protected IAbstractAnalysisAddToDistributionTickRepresentation base() {
		return (IAbstractAnalysisAddToDistributionTickRepresentation) super.base();
	    }
	}

	/**
	 * Overridden to take into account calculated properties.
	 *
	 * @author TG Team
	 *
	 */
	protected abstract class AbstractAnalysisAddToAggregationTickRepresentationAndEnhancer extends TickRepresentationAndEnhancer implements IAbstractAnalysisAddToAggregationTickRepresentation {
	    private static final long serialVersionUID = -8143739289123268471L;

	    protected AbstractAnalysisAddToAggregationTickRepresentationAndEnhancer(final AbstractTickRepresentation base) {
		super(base);
	    }

	    @Override
	    protected IAbstractAnalysisAddToAggregationTickRepresentation base() {
		return (IAbstractAnalysisAddToAggregationTickRepresentation) super.base();
	    }

	    @Override
	    public boolean isOrderingDisabledImmutably(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		return base().isOrderingDisabledImmutably(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public void disableOrderingImmutably(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		base().disableOrderingImmutably(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public List<Pair<String, Ordering>> orderedPropertiesByDefault(final Class<?> root) {
		// inject an enhanced type into method implementation
		return base().orderedPropertiesByDefault(enhancer().getManagedType(root));
	    }

	    @Override
	    public void setOrderedPropertiesByDefault(final Class<?> root, final List<Pair<String, Ordering>> orderedPropertiesByDefault) {
		// inject an enhanced type into method implementation
		base().setOrderedPropertiesByDefault(enhancer().getManagedType(root), orderedPropertiesByDefault);
	    }
	}
    }

    @Override
    public boolean isVisible() {
	return base().isVisible();
    }

    @Override
    public IAbstractAnalysisDomainTreeManager setVisible(final boolean visible) {
	base().setVisible(visible);
	return this;
    }
}
