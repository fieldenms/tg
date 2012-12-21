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
	/**
	 * A bunch of used properties for {@link IPivotAddToDistributionTickManager} should be treated as single "entity" (e.g. UI column)
	 * in terms of {@link IWidthManager}. So, please use one of "used" properties to determine the width of that single "column entity".
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public int getWidth(final Class<?> root, final String property);

	/**
	 * A bunch of used properties for {@link IPivotAddToDistributionTickManager} should be treated as single "entity" (e.g. UI column)
	 * in terms of {@link IWidthManager}. So, please use one of "used" properties to set the width of that single "column entity".
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public IPivotAddToDistributionTickManager setWidth(Class<?> root, String property, int width);
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
