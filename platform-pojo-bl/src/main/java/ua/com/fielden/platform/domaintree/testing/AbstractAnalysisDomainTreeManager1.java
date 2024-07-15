package ua.com.fielden.platform.domaintree.testing;

import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.entity.factory.EntityFactory;

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
     * @param entityFactory
     * @param rootTypes
     */
    public AbstractAnalysisDomainTreeManager1(final EntityFactory entityFactory, final Set<Class<?>> rootTypes) {
        this(entityFactory, new AbstractAnalysisDomainTreeRepresentation1(entityFactory, rootTypes), null, new AbstractAnalysisAddToDistributionTickManager1(), new AbstractAnalysisAddToAggregationTickManager1());
    }

    /**
     * A <i>manager</i> constructor.
     * 
     * @param entityFactory
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected AbstractAnalysisDomainTreeManager1(final EntityFactory entityFactory, final AbstractAnalysisDomainTreeRepresentation dtr, final Boolean visible, final AbstractAnalysisAddToDistributionTickManager1 firstTick, final AbstractAnalysisAddToAggregationTickManager1 secondTick) {
        super(entityFactory, dtr, visible, firstTick, secondTick);
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
}
