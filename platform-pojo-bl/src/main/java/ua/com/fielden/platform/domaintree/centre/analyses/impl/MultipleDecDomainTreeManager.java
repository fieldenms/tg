package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisAddToDistributionTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IMultipleDecDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IMultipleDecDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeManager.AnalysisAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeManager.AnalysisAddToDistributionTickManager;
import ua.com.fielden.platform.entity.factory.EntityFactory;

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
     * @param entityFactory
     * @param rootTypes
     */
    public MultipleDecDomainTreeManager(final EntityFactory entityFactory, final Set<Class<?>> rootTypes) {
        this(entityFactory, new MultipleDecDomainTreeRepresentation(entityFactory, rootTypes), null, new AnalysisAddToDistributionTickManager(), new AnalysisAddToAggregationTickManager());
    }

    /**
     * A <i>manager</i> constructor for 'restoring from the cloud' process.
     * 
     * @param entityFactory
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected MultipleDecDomainTreeManager(final EntityFactory entityFactory, final MultipleDecDomainTreeRepresentation dtr, final Boolean visible, final AnalysisAddToDistributionTickManager firstTick, final AnalysisAddToAggregationTickManager secondTick) {
        super(entityFactory, dtr, visible, firstTick, secondTick);
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

}
