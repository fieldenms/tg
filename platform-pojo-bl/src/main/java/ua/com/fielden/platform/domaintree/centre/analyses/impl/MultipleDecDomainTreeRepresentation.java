package ua.com.fielden.platform.domaintree.centre.analyses.impl;

import java.util.Set;

import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeRepresentation.IAnalysisAddToAggregationTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeRepresentation.IAnalysisAddToDistributionTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.IMultipleDecDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeRepresentation.AnalysisAddToAggregationTickRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeRepresentation.AnalysisAddToDistributionTickRepresentation;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.utils.Pair;

/**
 * A domain tree representation for multiple dec analysis.
 * 
 * @author TG Team
 * 
 */
public class MultipleDecDomainTreeRepresentation extends AbstractAnalysisDomainTreeRepresentation implements IMultipleDecDomainTreeRepresentation {

    /**
     * A <i>representation</i> constructor for the first time instantiation.
     * 
     * @param serialiser
     * @param rootTypes
     */
    public MultipleDecDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
        this(serialiser, rootTypes, createSet(), new AnalysisAddToDistributionTickRepresentation(), new AnalysisAddToAggregationTickRepresentation());
    }

    /**
     * A <i>representation</i> constructor. Needed for 'restoring from the cloud' process. Initialises also children references on itself.
     */
    protected MultipleDecDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final AnalysisAddToDistributionTickRepresentation firstTick, final AnalysisAddToAggregationTickRepresentation secondTick) {
        super(serialiser, rootTypes, excludedProperties, firstTick, secondTick);
    }

    @Override
    public IAnalysisAddToDistributionTickRepresentation getFirstTick() {
        return (IAnalysisAddToDistributionTickRepresentation) super.getFirstTick();
    }

    @Override
    public IAnalysisAddToAggregationTickRepresentation getSecondTick() {
        return (IAnalysisAddToAggregationTickRepresentation) super.getSecondTick();
    }

}
