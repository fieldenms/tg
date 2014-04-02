package ua.com.fielden.platform.domaintree.centre.analyses;

/**
 * This interface defines how domain tree can be represented for special <b>sentinel analyses</b> (single "status" property distribution by "countOfSelf" aggregation). <br>
 * <br>
 * 
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br>
 * <br>
 * 
 * @author TG Team
 * 
 */
public interface ISentinelDomainTreeRepresentation extends IAnalysisDomainTreeRepresentation {
    ISentinelAddToDistributionTickRepresentation getFirstTick();

    ISentinelAddToAggregationTickRepresentation getSecondTick();

    /**
     * Overridden in order to show only properties "", "countOfSelf" (aggregation) and all "status"-related calculated "sentinel" properties (distribution).
     */
    @Override
    public boolean isExcludedImmutably(Class<?> root, String property);

    /**
     * This interface defines how domain tree can be represented for <b>sentinel analyses</b> specific ("add to distribution").
     * 
     * @author TG Team
     * 
     */
    public interface ISentinelAddToDistributionTickRepresentation extends IAnalysisAddToDistributionTickRepresentation {
        /**
         * Overridden in order to enable only all "status"-related calculated "sentinel" properties.
         */
        @Override
        public boolean isDisabledImmutably(Class<?> root, String property);
    }

    /**
     * This interface defines how domain tree can be represented for <b>sentinel analyses</b> specific ("add to aggregation").
     * 
     * @author TG Team
     * 
     */
    public interface ISentinelAddToAggregationTickRepresentation extends IAnalysisAddToAggregationTickRepresentation {
        /**
         * Overridden in order to enable only "countOfSelf" property.
         */
        @Override
        public boolean isDisabledImmutably(Class<?> root, String property);
    }
}
