package ua.com.fielden.platform.domaintree.centre.analyses;

import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisAddToDistributionTickManager;

/**
 * This interface defines how domain tree can be managed for multiple dec <b>analyses</b> and single distribution property. <br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * @author TG Team
 *
 */
public interface IMultipleDecDomainTreeManager extends IAbstractAnalysisDomainTreeManager{
    IAnalysisAddToDistributionTickManager getFirstTick();
    IAnalysisAddToAggregationTickManager getSecondTick();
    IMultipleDecDomainTreeRepresentation getRepresentation();
}
