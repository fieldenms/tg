package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeManager.AnalysisAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeManager.AnalysisAddToDistributionTickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.TickManager;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;

public class AnalysisDomainTreeManagerAndEnhancer extends AbstractAnalysisDomainTreeManagerAndEnhancer implements IAnalysisDomainTreeManagerAndEnhancer {
    public AnalysisDomainTreeManagerAndEnhancer(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(new AnalysisDomainTreeManager(serialiser, rootTypes), new DomainTreeEnhancer(rootTypes));
    }

    protected AnalysisDomainTreeManagerAndEnhancer(final AnalysisDomainTreeManager base, final IDomainTreeEnhancer enhancer) {
	super(base, enhancer);
    }

    @Override
    protected AnalysisDomainTreeManager base() {
        return (AnalysisDomainTreeManager) super.base();
    }

    @Override
    protected AnalysisAddToDistributionTickManagerAndEnhancer createFirstTick(final TickManager base) {
	return new AnalysisAddToDistributionTickManagerAndEnhancer(base);
    }

    @Override
    protected AnalysisAddToAggregationTickManagerAndEnhancer createSecondTick(final TickManager base) {
	return new AnalysisAddToAggregationTickManagerAndEnhancer(base);
    }

    @Override
    protected AnalysisDomainTreeRepresentationAndEnhancer createRepresentation(final AbstractDomainTreeRepresentation base) {
	return new AnalysisDomainTreeRepresentationAndEnhancer(base);
    }

    @Override
    public IAnalysisDomainTreeRepresentation getRepresentation() {
	return (IAnalysisDomainTreeRepresentation) super.getRepresentation();
    }

    public class AnalysisAddToDistributionTickManagerAndEnhancer extends AbstractAnalysisAddToDistributionTickManagerAndEnhancer implements IAnalysisAddToDistributionTickManager {
	protected AnalysisAddToDistributionTickManagerAndEnhancer(final TickManager base) {
	    super(base);
	}

	@Override
	protected AnalysisAddToDistributionTickManager base() {
	    return (AnalysisAddToDistributionTickManager) super.base();
	}
    }

    public class AnalysisAddToAggregationTickManagerAndEnhancer extends AbstractAnalysisAddToAggregationTickManagerAndEnhancer implements IAnalysisAddToAggregationTickManager {
	protected AnalysisAddToAggregationTickManagerAndEnhancer(final TickManager base) {
	    super(base);
	}

	@Override
	protected AnalysisAddToAggregationTickManager base() {
	    return (AnalysisAddToAggregationTickManager) super.base();
	}
    }

    public class AnalysisDomainTreeRepresentationAndEnhancer extends AbstractAnalysisDomainTreeRepresentationAndEnhancer implements IAnalysisDomainTreeRepresentation {
	protected AnalysisDomainTreeRepresentationAndEnhancer(final AbstractDomainTreeRepresentation base) {
	    super(base);
	}

	@Override
	public IAnalysisAddToDistributionTickRepresentation getFirstTick() {
	    return (IAnalysisAddToDistributionTickRepresentation) super.getFirstTick();
	}

	@Override
	public IAnalysisAddToAggregationTickRepresentation getSecondTick() {
	    return (IAnalysisAddToAggregationTickRepresentation) super.getSecondTick();
	}

	@Override
	protected IAnalysisAddToDistributionTickRepresentation createFirstTick(final ITickRepresentation base) {
	    return new AnalysisAddToDistributionTickRepresentationAndEnhancer(base);
	}

	@Override
	protected IAnalysisAddToAggregationTickRepresentation createSecondTick(final ITickRepresentation base) {
	    return new AnalysisAddToAggregationTickRepresentationAndEnhancer(base);
	}

	/**
	 * Overridden to take into account calculated properties.
	 *
	 * @author TG Team
	 *
	 */
	public class AnalysisAddToDistributionTickRepresentationAndEnhancer extends AbstractAnalysisAddToDistributionTickRepresentationAndEnhancer implements IAnalysisAddToDistributionTickRepresentation {
	    private static final long serialVersionUID = -8143739289123268471L;

	    protected AnalysisAddToDistributionTickRepresentationAndEnhancer(final ITickRepresentation base) {
		super(base);
	    }

	    @Override
	    protected IAnalysisAddToDistributionTickRepresentation base() {
		return (IAnalysisAddToDistributionTickRepresentation) super.base();
	    }
	}

	/**
	 * Overridden to take into account calculated properties.
	 *
	 * @author TG Team
	 *
	 */
	public class AnalysisAddToAggregationTickRepresentationAndEnhancer extends AbstractAnalysisAddToAggregationTickRepresentationAndEnhancer implements IAnalysisAddToAggregationTickRepresentation {
	    private static final long serialVersionUID = -8143739289123268471L;

	    protected AnalysisAddToAggregationTickRepresentationAndEnhancer(final ITickRepresentation base) {
		super(base);
	    }

	    @Override
	    protected IAnalysisAddToAggregationTickRepresentation base() {
		return (IAnalysisAddToAggregationTickRepresentation) super.base();
	    }
	}
    }

    @Override
    public IAnalysisAddToDistributionTickManager getFirstTick() {
	return (IAnalysisAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public IAnalysisAddToAggregationTickManager getSecondTick() {
	return (IAnalysisAddToAggregationTickManager) super.getSecondTick();
    };

    /**
     * A specific Kryo serialiser for {@link AbstractDomainTreeManagerAndEnhancer}.
     *
     * @author TG Team
     *
     */
    public static class AnalysisDomainTreeManagerAndEnhancerSerialiser extends TgSimpleSerializer<AnalysisDomainTreeManagerAndEnhancer> {
	public AnalysisDomainTreeManagerAndEnhancerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public AnalysisDomainTreeManagerAndEnhancer read(final ByteBuffer buffer) {
	    final AnalysisDomainTreeManager base = readValue(buffer, AnalysisDomainTreeManager.class);
	    final DomainTreeEnhancer enhancer = readValue(buffer, DomainTreeEnhancer.class);
	    return new AnalysisDomainTreeManagerAndEnhancer(base, enhancer);
	}

	@Override
	public void write(final ByteBuffer buffer, final AnalysisDomainTreeManagerAndEnhancer manager) {
	    writeValue(buffer, manager.base());
	    writeValue(buffer, manager.enhancer());
	}
    }

    @Override
    public int getVisibleDistributedValuesNumber() {
	return base().getVisibleDistributedValuesNumber();
    }

    @Override
    public IAnalysisDomainTreeManager setVisibleDistributedValuesNumber(final int visibleDistributedValuesNumber) {
	base().setVisibleDistributedValuesNumber(visibleDistributedValuesNumber);
	return this;
    }
}
