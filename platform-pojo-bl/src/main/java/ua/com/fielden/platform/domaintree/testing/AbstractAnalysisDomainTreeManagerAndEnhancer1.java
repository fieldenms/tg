package ua.com.fielden.platform.domaintree.testing;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.TickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.AbstractTickRepresentation;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;

public class AbstractAnalysisDomainTreeManagerAndEnhancer1 extends AbstractAnalysisDomainTreeManagerAndEnhancer implements IAbstractAnalysisDomainTreeManagerAndEnhancer {
    public AbstractAnalysisDomainTreeManagerAndEnhancer1(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(new AbstractAnalysisDomainTreeManager1(serialiser, AbstractDomainTree.validateRootTypes(rootTypes)), new DomainTreeEnhancer(serialiser, AbstractDomainTree.validateRootTypes(rootTypes)));
    }

    protected AbstractAnalysisDomainTreeManagerAndEnhancer1(final AbstractAnalysisDomainTreeManager base, final IDomainTreeEnhancer enhancer) {
	super(base, enhancer);
    }

    @Override
    protected AbstractAnalysisAddToDistributionTickManagerAndEnhancer1 createFirstTick(final TickManager base) {
	return new AbstractAnalysisAddToDistributionTickManagerAndEnhancer1(base);
    }

    @Override
    protected AbstractAnalysisAddToAggregationTickManagerAndEnhancer1 createSecondTick(final TickManager base) {
	return new AbstractAnalysisAddToAggregationTickManagerAndEnhancer1(base);
    }

    @Override
    protected AbstractAnalysisDomainTreeRepresentationAndEnhancer1 createRepresentation(final AbstractDomainTreeRepresentation base) {
	return new AbstractAnalysisDomainTreeRepresentationAndEnhancer1(base);
    }

    public class AbstractAnalysisAddToDistributionTickManagerAndEnhancer1 extends AbstractAnalysisAddToDistributionTickManagerAndEnhancer implements IAbstractAnalysisAddToDistributionTickManager {
	protected AbstractAnalysisAddToDistributionTickManagerAndEnhancer1(final TickManager base) {
	    super(base);
	}
    }

    public class AbstractAnalysisAddToAggregationTickManagerAndEnhancer1 extends AbstractAnalysisAddToAggregationTickManagerAndEnhancer implements IAbstractAnalysisAddToAggregationTickManager {
	protected AbstractAnalysisAddToAggregationTickManagerAndEnhancer1(final TickManager base) {
	    super(base);
	}
    }

    public class AbstractAnalysisDomainTreeRepresentationAndEnhancer1 extends AbstractAnalysisDomainTreeRepresentationAndEnhancer implements IAbstractAnalysisDomainTreeRepresentation {
	protected AbstractAnalysisDomainTreeRepresentationAndEnhancer1(final AbstractDomainTreeRepresentation base) {
	    super(base);
	}

	@Override
	protected IAbstractAnalysisAddToDistributionTickRepresentation createFirstTick(final AbstractTickRepresentation base) {
	    return new AbstractAnalysisAddToDistributionTickRepresentationAndEnhancer1(base);
	}

	@Override
	protected IAbstractAnalysisAddToAggregationTickRepresentation createSecondTick(final AbstractTickRepresentation base) {
	    return new AbstractAnalysisAddToAggregationTickRepresentationAndEnhancer1(base);
	}

	/**
	 * Overridden to take into account calculated properties.
	 *
	 * @author TG Team
	 *
	 */
	public class AbstractAnalysisAddToDistributionTickRepresentationAndEnhancer1 extends AbstractAnalysisAddToDistributionTickRepresentationAndEnhancer implements IAbstractAnalysisAddToDistributionTickRepresentation {
	    private static final long serialVersionUID = -8143739289123268471L;

	    protected AbstractAnalysisAddToDistributionTickRepresentationAndEnhancer1(final AbstractTickRepresentation base) {
		super(base);
	    }
	}

	/**
	 * Overridden to take into account calculated properties.
	 *
	 * @author TG Team
	 *
	 */
	public class AbstractAnalysisAddToAggregationTickRepresentationAndEnhancer1 extends AbstractAnalysisAddToAggregationTickRepresentationAndEnhancer implements IAbstractAnalysisAddToAggregationTickRepresentation {
	    private static final long serialVersionUID = -8143739289123268471L;

	    protected AbstractAnalysisAddToAggregationTickRepresentationAndEnhancer1(final AbstractTickRepresentation base) {
		super(base);
	    }
	}
    }

    /**
     * A specific Kryo serialiser for {@link AbstractDomainTreeManagerAndEnhancer}.
     *
     * @author TG Team
     *
     */
    public static class AbstractAnalysisDomainTreeManagerAndEnhancer1Serialiser extends TgSimpleSerializer<AbstractAnalysisDomainTreeManagerAndEnhancer1> {
	public AbstractAnalysisDomainTreeManagerAndEnhancer1Serialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public AbstractAnalysisDomainTreeManagerAndEnhancer1 read(final ByteBuffer buffer) {
	    final AbstractAnalysisDomainTreeManager1 base = readValue(buffer, AbstractAnalysisDomainTreeManager1.class);
	    final DomainTreeEnhancer enhancer = readValue(buffer, DomainTreeEnhancer.class);
	    return new AbstractAnalysisDomainTreeManagerAndEnhancer1(base, enhancer);
	}

	@Override
	public void write(final ByteBuffer buffer, final AbstractAnalysisDomainTreeManagerAndEnhancer1 manager) {
	    writeValue(buffer, manager.base());
	    writeValue(buffer, manager.enhancer());
	}
    }
}
