package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.IPivotDomainTreeManager.IPivotDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.PivotDomainTreeManager.PivotAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.PivotDomainTreeManager.PivotAddToDistributionTickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.TickManager;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;

public class PivotDomainTreeManagerAndEnhancer extends AbstractAnalysisDomainTreeManagerAndEnhancer implements IPivotDomainTreeManagerAndEnhancer {
    public PivotDomainTreeManagerAndEnhancer(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(new PivotDomainTreeManager(serialiser, rootTypes), new DomainTreeEnhancer(rootTypes));
    }

    protected PivotDomainTreeManagerAndEnhancer(final AbstractAnalysisDomainTreeManager base, final IDomainTreeEnhancer enhancer) {
	super(base, enhancer);
    }

    @Override
    protected PivotAddToDistributionTickManagerAndEnhancer createFirstTick(final TickManager base) {
	return new PivotAddToDistributionTickManagerAndEnhancer(base);
    }

    @Override
    protected PivotAddToAggregationTickManagerAndEnhancer createSecondTick(final TickManager base) {
	return new PivotAddToAggregationTickManagerAndEnhancer(base);
    }

    @Override
    protected PivotDomainTreeRepresentationAndEnhancer createRepresentation(final AbstractDomainTreeRepresentation base) {
	return new PivotDomainTreeRepresentationAndEnhancer(base);
    }

    @Override
    public IPivotDomainTreeRepresentation getRepresentation() {
	return (IPivotDomainTreeRepresentation)super.getRepresentation();
    }

    public class PivotAddToDistributionTickManagerAndEnhancer extends AbstractAnalysisAddToDistributionTickManagerAndEnhancer implements IPivotAddToDistributionTickManager {
	protected PivotAddToDistributionTickManagerAndEnhancer(final TickManager base) {
	    super(base);
	}

	@Override
	protected PivotAddToDistributionTickManager base() {
	    return (PivotAddToDistributionTickManager) super.base();
	}

	@Override
	public int getWidth(final Class<?> root, final String property) {
	    return base().getWidth(enhancer().getManagedType(root), property);
	}

	@Override
	public void setWidth(final Class<?> root, final String property, final int width) {
	    base().setWidth(enhancer().getManagedType(root), property, width);
	}
    }

    public class PivotAddToAggregationTickManagerAndEnhancer extends AbstractAnalysisAddToAggregationTickManagerAndEnhancer implements IPivotAddToAggregationTickManager {
	protected PivotAddToAggregationTickManagerAndEnhancer(final TickManager base) {
	    super(base);
	}

	@Override
	protected PivotAddToAggregationTickManager base() {
	    return (PivotAddToAggregationTickManager) super.base();
	}

	@Override
	public int getWidth(final Class<?> root, final String property) {
	    return base().getWidth(enhancer().getManagedType(root), property);
	}

	@Override
	public void setWidth(final Class<?> root, final String property, final int width) {
	    base().setWidth(enhancer().getManagedType(root), property, width);
	}
    }

    public class PivotDomainTreeRepresentationAndEnhancer extends AbstractAnalysisDomainTreeRepresentationAndEnhancer implements IPivotDomainTreeRepresentation {
	protected PivotDomainTreeRepresentationAndEnhancer(final AbstractDomainTreeRepresentation base) {
	    super(base);
	}

	@Override
	public IPivotAddToDistributionTickRepresentation getFirstTick() {
	    return (IPivotAddToDistributionTickRepresentation) super.getFirstTick();
	}

	@Override
	public IPivotAddToAggregationTickRepresentation getSecondTick() {
	    return (IPivotAddToAggregationTickRepresentation) super.getSecondTick();
	}

	@Override
	protected IPivotAddToDistributionTickRepresentation createFirstTick(final ITickRepresentation base) {
	    return new PivotAddToDistributionTickRepresentationAndEnhancer(base);
	}

	@Override
	protected IPivotAddToAggregationTickRepresentation createSecondTick(final ITickRepresentation base) {
	    return new PivotAddToAggregationTickRepresentationAndEnhancer(base);
	}

	/**
	 * Overridden to take into account calculated properties.
	 *
	 * @author TG Team
	 *
	 */
	public class PivotAddToDistributionTickRepresentationAndEnhancer extends AbstractAnalysisAddToDistributionTickRepresentationAndEnhancer implements IPivotAddToDistributionTickRepresentation {
	    private static final long serialVersionUID = -8143739289123268471L;

	    protected PivotAddToDistributionTickRepresentationAndEnhancer(final ITickRepresentation base) {
		super(base);
	    }

	    @Override
	    protected IPivotAddToDistributionTickRepresentation base() {
		return (IPivotAddToDistributionTickRepresentation) super.base();
	    }

	    @Override
	    public int getWidthByDefault(final Class<?> root, final String property) {
		return base().getWidthByDefault(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public void setWidthByDefault(final Class<?> root, final String property, final int width) {
		base().setWidthByDefault(enhancer().getManagedType(root), property, width);
	    }
	}

	/**
	 * Overridden to take into account calculated properties.
	 *
	 * @author TG Team
	 *
	 */
	public class PivotAddToAggregationTickRepresentationAndEnhancer extends AbstractAnalysisAddToAggregationTickRepresentationAndEnhancer implements IPivotAddToAggregationTickRepresentation {
	    private static final long serialVersionUID = -8143739289123268471L;

	    protected PivotAddToAggregationTickRepresentationAndEnhancer(final ITickRepresentation base) {
		super(base);
	    }

	    @Override
	    protected IPivotAddToAggregationTickRepresentation base() {
		return (IPivotAddToAggregationTickRepresentation) super.base();
	    }

	    @Override
	    public int getWidthByDefault(final Class<?> root, final String property) {
		return base().getWidthByDefault(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public void setWidthByDefault(final Class<?> root, final String property, final int width) {
		base().setWidthByDefault(enhancer().getManagedType(root), property, width);
	    }
	}
    }

    @Override
    public IPivotAddToDistributionTickManager getFirstTick() {
	return (IPivotAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public IPivotAddToAggregationTickManager getSecondTick() {
	return (IPivotAddToAggregationTickManager) super.getSecondTick();
    };

    /**
     * A specific Kryo serialiser for {@link AbstractDomainTreeManagerAndEnhancer}.
     *
     * @author TG Team
     *
     */
    public static class PivotDomainTreeManagerAndEnhancerSerialiser extends TgSimpleSerializer<PivotDomainTreeManagerAndEnhancer> {
	public PivotDomainTreeManagerAndEnhancerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public PivotDomainTreeManagerAndEnhancer read(final ByteBuffer buffer) {
	    final PivotDomainTreeManager base = readValue(buffer, PivotDomainTreeManager.class);
	    final DomainTreeEnhancer enhancer = readValue(buffer, DomainTreeEnhancer.class);
	    return new PivotDomainTreeManagerAndEnhancer(base, enhancer);
	}

	@Override
	public void write(final ByteBuffer buffer, final PivotDomainTreeManagerAndEnhancer manager) {
	    writeValue(buffer, manager.base());
	    writeValue(buffer, manager.enhancer());
	}
    }
}
