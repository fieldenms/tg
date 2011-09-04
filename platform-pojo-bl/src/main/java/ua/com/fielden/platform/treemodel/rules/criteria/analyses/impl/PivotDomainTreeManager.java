package ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.IPivotDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.IPivotDomainTreeRepresentation;

/**
 * TODO just a stub. Should be replaced on actual implementation.
 *
 * @author jhou
 *
 */
public class PivotDomainTreeManager extends AbstractAnalysisDomainTreeManager implements IPivotDomainTreeManager {
    private static final long serialVersionUID = -4155274305648154329L;

    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public PivotDomainTreeManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, new PivotDomainTreeRepresentation(serialiser, rootTypes),null, new PivotAddToDistributionTickManager(), new PivotAddToAggregationTickManager());
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected PivotDomainTreeManager(final ISerialiser serialiser, final IPivotDomainTreeRepresentation dtr, final Boolean visible, final PivotAddToDistributionTickManager firstTick, final PivotAddToAggregationTickManager secondTick) {
	super(serialiser, dtr, visible, firstTick, secondTick);
    }

    @Override
    public IPivotAddToDistributionTickManager getFirstTick() {
	return (IPivotAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public IPivotAddToAggregationTickManager getSecondTick() {
	return (IPivotAddToAggregationTickManager) super.getSecondTick();
    }

    @Override
    public IPivotDomainTreeRepresentation getRepresentation() {
	return (IPivotDomainTreeRepresentation) super.getRepresentation();
    }

    public static class PivotAddToDistributionTickManager extends AbstractAnalysisAddToDistributionTickManager implements IPivotAddToDistributionTickManager {
	private static final long serialVersionUID = 4659406246345595522L;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public PivotAddToDistributionTickManager() {
	    super();
	}

	@Override
	public int getWidth(final Class<?> root, final String property) {
	    // TODO Auto-generated method stub
	    return 0;
	}

	@Override
	public void setWidth(final Class<?> root, final String property, final int width) {
	    // TODO Auto-generated method stub
	}
    }

    public static class PivotAddToAggregationTickManager extends AbstractAnalysisAddToAggregationTickManager implements IPivotAddToAggregationTickManager {
	private static final long serialVersionUID = -4025471910983945279L;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public PivotAddToAggregationTickManager() {
	    super();
	}

	@Override
	public int getWidth(final Class<?> root, final String property) {
	    // TODO Auto-generated method stub
	    return 0;
	}

	@Override
	public void setWidth(final Class<?> root, final String property, final int width) {
	    // TODO Auto-generated method stub
	}
    }

    /**
     * A specific Kryo serialiser for {@link PivotDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    public static class PivotDomainTreeManagerSerialiser extends AbstractAnalysisDomainTreeManagerSerialiser<PivotDomainTreeManager> {
	public PivotDomainTreeManagerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public PivotDomainTreeManager read(final ByteBuffer buffer) {
	    final PivotDomainTreeRepresentation dtr = readValue(buffer, PivotDomainTreeRepresentation.class);
	    final PivotAddToDistributionTickManager firstTick = readValue(buffer, PivotAddToDistributionTickManager.class);
	    final PivotAddToAggregationTickManager secondTick = readValue(buffer, PivotAddToAggregationTickManager.class);
	    final Boolean visible = readValue(buffer, Boolean.class);
	    return new PivotDomainTreeManager(kryo(), dtr, visible, firstTick, secondTick);
	}
    }
}
