package ua.com.fielden.platform.domaintree.testing;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.serialisation.api.ISerialiser;

/**
 * Test implementation of abstract analysis manager.
 * 
 * @author TG Team
 * 
 */
public class AbstractAnalysisDomainTreeManager1 extends AbstractAnalysisDomainTreeManager implements IAbstractAnalysisDomainTreeManager {
    /**
     * A <i>manager</i> constructor for the first time instantiation.
     * 
     * @param serialiser
     * @param rootTypes
     */
    public AbstractAnalysisDomainTreeManager1(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
        this(serialiser, new AbstractAnalysisDomainTreeRepresentation1(serialiser, rootTypes), null, new AbstractAnalysisAddToDistributionTickManager1(), new AbstractAnalysisAddToAggregationTickManager1());
    }

    /**
     * A <i>manager</i> constructor.
     * 
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected AbstractAnalysisDomainTreeManager1(final ISerialiser serialiser, final AbstractAnalysisDomainTreeRepresentation dtr, final Boolean visible, final AbstractAnalysisAddToDistributionTickManager1 firstTick, final AbstractAnalysisAddToAggregationTickManager1 secondTick) {
        super(serialiser, dtr, visible, firstTick, secondTick);
    }

    @Override
    public AbstractAnalysisAddToDistributionTickManager1 getFirstTick() {
        return (AbstractAnalysisAddToDistributionTickManager1) super.getFirstTick();
    }

    @Override
    public AbstractAnalysisAddToAggregationTickManager1 getSecondTick() {
        return (AbstractAnalysisAddToAggregationTickManager1) super.getSecondTick();
    }

    @Override
    public AbstractAnalysisDomainTreeRepresentation1 getRepresentation() {
        return (AbstractAnalysisDomainTreeRepresentation1) super.getRepresentation();
    }

    public static class AbstractAnalysisAddToDistributionTickManager1 extends AbstractAnalysisAddToDistributionTickManager implements IAbstractAnalysisAddToDistributionTickManager {
        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
         * fields.
         */
        public AbstractAnalysisAddToDistributionTickManager1() {
            super();
        }
    }

    public static class AbstractAnalysisAddToAggregationTickManager1 extends AbstractAnalysisAddToAggregationTickManager implements IAbstractAnalysisAddToAggregationTickManager {
        /**
         * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
         * fields.
         */
        public AbstractAnalysisAddToAggregationTickManager1() {
            super();
        }
    }

    /**
     * A specific Kryo serialiser for {@link AbstractAnalysisDomainTreeManager1}.
     * 
     * @author TG Team
     * 
     */
    public static class AbstractAnalysisDomainTreeManager1Serialiser extends AbstractAnalysisDomainTreeManagerSerialiser<AbstractAnalysisDomainTreeManager1> {
        public AbstractAnalysisDomainTreeManager1Serialiser(final ISerialiser kryo) {
            super(kryo);
        }

        @Override
        public AbstractAnalysisDomainTreeManager1 read(final ByteBuffer buffer) {
            final AbstractAnalysisDomainTreeRepresentation1 dtr = readValue(buffer, AbstractAnalysisDomainTreeRepresentation1.class);
            final AbstractAnalysisAddToDistributionTickManager1 firstTick = readValue(buffer, AbstractAnalysisAddToDistributionTickManager1.class);
            final AbstractAnalysisAddToAggregationTickManager1 secondTick = readValue(buffer, AbstractAnalysisAddToAggregationTickManager1.class);
            final Boolean visible = readValue(buffer, Boolean.class);
            return new AbstractAnalysisDomainTreeManager1(kryo(), dtr, visible, firstTick, secondTick);
        }
    }
}
