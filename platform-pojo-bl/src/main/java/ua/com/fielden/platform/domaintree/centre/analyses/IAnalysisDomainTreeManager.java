package ua.com.fielden.platform.domaintree.centre.analyses;

import java.util.List;

/**
 * This interface defines how domain tree can be managed for simple <b>analyses</b> (single property distribution). <br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * @author TG Team
 *
 */
public interface IAnalysisDomainTreeManager extends IAbstractAnalysisDomainTreeManager {
    IAnalysisAddToDistributionTickManager getFirstTick();
    IAnalysisAddToAggregationTickManager getSecondTick();
    IAnalysisDomainTreeRepresentation getRepresentation();

    /**
     * Gets a number of visible distributed values (chart columns) for simple analysis report.
     *
     * @return
     */
    int getVisibleDistributedValuesNumber();

    /**
     * Sets a number of visible distributed values (chart columns) for simple analysis report.
     *
     * @param visibleDistributedValuesNumber -- a value to set
     * @return -- an analysis manager
     */
    IAnalysisDomainTreeManager setVisibleDistributedValuesNumber(final int visibleDistributedValuesNumber);

    /**
     * This interface defines how domain tree can be managed for <b>analyses</b> specific ("add to distribution"). (Should return single used property). <br><br>
     *
     * @author TG Team
     * @see IUsageManager
     *
     */
    public interface IAnalysisAddToDistributionTickManager extends IAbstractAnalysisAddToDistributionTickManager {
        /**
         * Returns a <b>single</b> used property for concrete <code>root</code> type. (Should not return multiple used properties)
         *
         * @param root -- a root type that contains an used property.
         * @return
         */
        List<String> usedProperties(final Class<?> root);
    }

    /**
     * This interface defines how domain tree can be managed for <b>analyses</b> specific ("add to aggregation"). (no special enhancements).<br><br>
     *
     * @author TG Team
     *
     */
    public interface IAnalysisAddToAggregationTickManager extends IAbstractAnalysisAddToAggregationTickManager {
    }
}
