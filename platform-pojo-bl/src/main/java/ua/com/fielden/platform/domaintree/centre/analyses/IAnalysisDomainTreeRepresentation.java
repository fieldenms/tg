package ua.com.fielden.platform.domaintree.centre.analyses;


/**
 * This interface defines how domain tree can be represented for simple <b>analyses</b> (single property distribution). <br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * @author TG Team
 *
 */
public interface IAnalysisDomainTreeRepresentation extends IAbstractAnalysisDomainTreeRepresentation {
    IAnalysisAddToDistributionTickRepresentation getFirstTick();
    IAnalysisAddToAggregationTickRepresentation getSecondTick();

    /**
     * This interface defines how domain tree can be represented for <b>analyses</b> specific ("add to distribution").
     *
     * @author TG Team
     *
     */
    public interface IAnalysisAddToDistributionTickRepresentation extends IAbstractAnalysisAddToDistributionTickRepresentation {
    }

    /**
     * This interface defines how domain tree can be represented for <b>pivot analyses</b> specific ("add to distribution").
     *
     * @author TG Team
     *
     */
    public interface IAnalysisAddToAggregationTickRepresentation extends IAbstractAnalysisAddToAggregationTickRepresentation {
    }
}
