package ua.com.fielden.platform.domaintree.centre.analyses;

/**
 * This interface defines how domain tree can be managed for special <b>sentinel analyses</b> (single "status" property distribution by "countOfSelf" aggregation). <br>
 * <br>
 * 
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br>
 * <br>
 * 
 * @author TG Team
 * 
 */
public interface ISentinelDomainTreeManager extends IAnalysisDomainTreeManager {
    ISentinelAddToDistributionTickManager getFirstTick();

    ISentinelAddToAggregationTickManager getSecondTick();

    ISentinelDomainTreeRepresentation getRepresentation();

    /**
     * This interface defines how domain tree can be managed for <b>sentinel analyses</b> specific ("add to distribution"). (Should return single used property -- "status" property
     * e.g. "RED", "GREEN"). <br>
     * <br>
     * 
     * @author TG Team
     * @see IUsageManager
     * 
     */
    public interface ISentinelAddToDistributionTickManager extends IAnalysisAddToDistributionTickManager {
    }

    /**
     * This interface defines how domain tree can be managed for <b>sentinel analyses</b> specific ("add to aggregation"). (Should return single used property -- "countOfSelf"
     * aggregation property).<br>
     * <br>
     * 
     * @author TG Team
     * 
     */
    public interface ISentinelAddToAggregationTickManager extends IAnalysisAddToAggregationTickManager {
    }
}