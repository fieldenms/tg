package ua.com.fielden.platform.domaintree.centre.analyses;

import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingManager;

/**
 * This interface defines how domain tree can be managed for <b>analyses</b>. <br>
 * <br>
 * 
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br>
 * <br>
 * 
 * @author TG Team
 * 
 */
public interface IAbstractAnalysisDomainTreeManager extends IDomainTreeManager {
    @Override
    IAbstractAnalysisAddToDistributionTickManager getFirstTick();

    @Override
    IAbstractAnalysisAddToAggregationTickManager getSecondTick();

    /**
     * Returns a domain representation that is able to change domain representation rules. See {@link IDomainTreeRepresentation} documentation for more details.
     * 
     * @return
     */
    @Override
    IAbstractAnalysisDomainTreeRepresentation getRepresentation();

    /**
     * Returns a parent centre manager which this analysis belongs to.
     * 
     * @return
     */
    ICentreDomainTreeManagerAndEnhancer parentCentreDomainTreeManager();

    /**
     * Gets an <i>visible</i> flag for analysis manager.<br>
     * <br>
     * 
     * @return
     */
    boolean isVisible();

    /**
     * Sets an <i>visible</i> flag for analysis manager. <br>
     * <br>
     * 
     * @param visible
     *            -- a flag to set
     * @return -- an analysis manager
     */
    IAbstractAnalysisDomainTreeManager setVisible(final boolean visible);

    /**
     * This interface defines how domain tree can be managed for <b>analyses</b> specific ("add to aggregation").
     * 
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br>
     * <br>
     * 
     * @author TG Team
     * @see IUsageManager
     * @see IOrderingManager
     * 
     */
    public interface IAbstractAnalysisAddToAggregationTickManager extends IOrderingManager, ITickManager {
    }

    /**
     * This interface defines how domain tree can be managed for <b>analyses</b> specific ("add to distribution").
     * 
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br>
     * <br>
     * 
     * @author TG Team
     * @see IUsageManager
     * 
     */
    public interface IAbstractAnalysisAddToDistributionTickManager extends ITickManager {
    }
}