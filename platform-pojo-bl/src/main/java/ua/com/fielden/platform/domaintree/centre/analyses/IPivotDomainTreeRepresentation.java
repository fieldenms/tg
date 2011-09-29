package ua.com.fielden.platform.domaintree.centre.analyses;

import ua.com.fielden.platform.domaintree.centre.IWidthRepresentation;

/**
 * This interface defines how domain tree can be represented for <b>pivot analyses</b> (multiple property distribution). <br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * @author TG Team
 *
 */
public interface IPivotDomainTreeRepresentation extends IAbstractAnalysisDomainTreeRepresentation {
    IPivotAddToDistributionTickRepresentation getFirstTick();
    IPivotAddToAggregationTickRepresentation getSecondTick();

    /**
     * This interface defines how domain tree can be represented for <b>pivot analyses</b> specific ("add to distribution").
     * (Should manage property widths also)
     *
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
     *
     * @author TG Team
     *
     */
    public interface IPivotAddToDistributionTickRepresentation extends IAbstractAnalysisAddToDistributionTickRepresentation, IWidthRepresentation {
    }

    /**
     * This interface defines how domain tree can be represented for <b>pivot analyses</b> specific ("add to distribution").
     * (Should manage property widths also)
     *
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
     *
     * @author TG Team
     *
     */
    public interface IPivotAddToAggregationTickRepresentation extends IAbstractAnalysisAddToAggregationTickRepresentation, IWidthRepresentation {
    }
}
