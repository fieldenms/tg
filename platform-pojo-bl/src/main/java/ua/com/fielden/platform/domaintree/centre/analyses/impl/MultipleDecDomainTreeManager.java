package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisAddToDistributionTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IMultipleDecDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IMultipleDecDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeManager.AnalysisAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeManager.AnalysisAddToDistributionTickManager;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;

/**
 * A domain tree manager for multiple dec analyis.
 *
 * @author TG Team
 *
 */
public class MultipleDecDomainTreeManager extends AbstractAnalysisDomainTreeManager implements IMultipleDecDomainTreeManager {

    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public MultipleDecDomainTreeManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, new MultipleDecDomainTreeRepresentation(serialiser, rootTypes), null, new AnalysisAddToDistributionTickManager(), new AnalysisAddToAggregationTickManager());
    }

    /**
     * A <i>manager</i> constructor for 'restoring from the cloud' process.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected MultipleDecDomainTreeManager(final ISerialiser serialiser, final MultipleDecDomainTreeRepresentation dtr, final Boolean visible, final AnalysisAddToDistributionTickManager firstTick, final AnalysisAddToAggregationTickManager secondTick) {
	super(serialiser, dtr, visible, firstTick, secondTick);
    }

    @Override
    public IAnalysisAddToDistributionTickManager getFirstTick() {
	return (IAnalysisAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public IAnalysisAddToAggregationTickManager getSecondTick() {
	return (IAnalysisAddToAggregationTickManager) super.getSecondTick();
    }

    @Override
    public IMultipleDecDomainTreeRepresentation getRepresentation() {
	return (IMultipleDecDomainTreeRepresentation) super.getRepresentation();
    }

    /**
     * A specific Kryo serialiser for {@link MultipleDecDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    public static class MultipleDecDomainTreeManagerSerialiser extends AbstractAnalysisDomainTreeManagerSerialiser<MultipleDecDomainTreeManager> {
	public MultipleDecDomainTreeManagerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public MultipleDecDomainTreeManager read(final ByteBuffer buffer) {
	    final MultipleDecDomainTreeRepresentation dtr = readValue(buffer, MultipleDecDomainTreeRepresentation.class);
	    final AnalysisAddToDistributionTickManager firstTick = readValue(buffer, AnalysisAddToDistributionTickManager.class);
	    final AnalysisAddToAggregationTickManager secondTick = readValue(buffer, AnalysisAddToAggregationTickManager.class);
	    final Boolean visible = readValue(buffer, Boolean.class);
	    return new MultipleDecDomainTreeManager(kryo(), dtr, visible, firstTick, secondTick);
	}
    }
}
