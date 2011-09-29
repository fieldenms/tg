package ua.com.fielden.platform.domaintree.centre.analyses;

import ua.com.fielden.platform.domaintree.centre.IWidthManager;

/**
 * This interface defines how domain tree can be managed for <b>pivot analyses</b> (multiple property distribution). <br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * @author TG Team
 *
 */
public interface IPivotDomainTreeManager extends IAbstractAnalysisDomainTreeManager {
    /**
     * A <i>domain tree manager<i> with <i>enhancer</i> inside.
     *
     * @author TG Team
     *
     */
    public interface IPivotDomainTreeManagerAndEnhancer extends IAbstractAnalysisDomainTreeManagerAndEnhancer, IPivotDomainTreeManager {
    }

    IPivotAddToDistributionTickManager getFirstTick();
    IPivotAddToAggregationTickManager getSecondTick();
    IPivotDomainTreeRepresentation getRepresentation();

    /**
     * This interface defines how domain tree can be managed for <b>pivot analyses</b> specific ("add to distribution").
     * (Should manage property widths also)
     *
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
     *
     * @author TG Team
     *
     */
    public interface IPivotAddToDistributionTickManager extends IAbstractAnalysisAddToDistributionTickManager, IWidthManager {
    }

    /**
     * This interface defines how domain tree can be managed for <b>pivot analyses</b> specific ("add to distribution").
     * (Should manage property widths also)
     *
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
     *
     * @author TG Team
     *
     */
    public interface IPivotAddToAggregationTickManager extends IAbstractAnalysisAddToAggregationTickManager, IWidthManager {
    }
}
