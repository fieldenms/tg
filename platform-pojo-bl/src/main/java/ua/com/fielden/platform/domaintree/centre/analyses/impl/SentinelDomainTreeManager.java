package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.ISentinelDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.ISentinelDomainTreeRepresentation;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;

/**
 * A domain tree manager for sentinel analyses.
 *
 * @author TG Team
 *
 */
public class SentinelDomainTreeManager extends AnalysisDomainTreeManager implements ISentinelDomainTreeManager {
    /**
     * A <i>manager</i> constructor for the first time instantiation.
     *
     * @param serialiser
     * @param rootTypes
     */
    public SentinelDomainTreeManager(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, new SentinelDomainTreeRepresentation(serialiser, rootTypes), null, new SentinelAddToDistributionTickManager(), new SentinelAddToAggregationTickManager(), null);
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected SentinelDomainTreeManager(final ISerialiser serialiser, final AnalysisDomainTreeRepresentation dtr, final Boolean visible, final AnalysisAddToDistributionTickManager firstTick, final AnalysisAddToAggregationTickManager secondTick, final Integer visibleDistributedValuesNumber) {
	super(serialiser, dtr, visible, firstTick, secondTick, visibleDistributedValuesNumber);
    }

    @Override
    public ISentinelAddToDistributionTickManager getFirstTick() {
	return (ISentinelAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public ISentinelAddToAggregationTickManager getSecondTick() {
	return (ISentinelAddToAggregationTickManager) super.getSecondTick();
    }

    @Override
    public ISentinelDomainTreeRepresentation getRepresentation() {
	return (ISentinelDomainTreeRepresentation) super.getRepresentation();
    }

    public static class SentinelAddToDistributionTickManager extends AnalysisAddToDistributionTickManager implements ISentinelAddToDistributionTickManager {
	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public SentinelAddToDistributionTickManager() {
	}

	@Override
	public void check(final Class<?> root, final String property, final boolean check) {
	    if (check) {
		// remove previously checked property. It should be the only one, but remove them all to be sure:
		while (!checkedPropertiesMutable(root).isEmpty()) {
		    check(root, checkedPropertiesMutable(root).get(0), false);
		}
	    }
	    super.check(root, property, check);
	}
    }

    public static class SentinelAddToAggregationTickManager extends AnalysisAddToAggregationTickManager implements ISentinelAddToAggregationTickManager {
	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public SentinelAddToAggregationTickManager() {
	}
    }

    /**
     * A specific Kryo serialiser for {@link SentinelDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    public static class SentinelDomainTreeManagerSerialiser extends AbstractAnalysisDomainTreeManagerSerialiser<SentinelDomainTreeManager> {
	public SentinelDomainTreeManagerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public SentinelDomainTreeManager read(final ByteBuffer buffer) {
	    final SentinelDomainTreeRepresentation dtr = readValue(buffer, SentinelDomainTreeRepresentation.class);
	    final SentinelAddToDistributionTickManager firstTick = readValue(buffer, SentinelAddToDistributionTickManager.class);
	    final SentinelAddToAggregationTickManager secondTick = readValue(buffer, SentinelAddToAggregationTickManager.class);
	    final Boolean visible = readValue(buffer, Boolean.class);
	    final Integer visibleDistributedValuesNumber = readValue(buffer, Integer.class);
	    return new SentinelDomainTreeManager(kryo(), dtr, visible, firstTick, secondTick, visibleDistributedValuesNumber);
	}

	@Override
	public void write(final ByteBuffer buffer, final SentinelDomainTreeManager manager) {
	    super.write(buffer, manager);
	    writeValue(buffer, manager.visibleDistributedValuesNumber());
	}
    }
}
