package ua.com.fielden.platform.domaintree.centre.analyses;

import ua.com.fielden.platform.domaintree.centre.IWidthManager;
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
	/**
	 * A bunch of used properties for {@link IPivotAddToDistributionTickRepresentation} should be treated as single "entity" (e.g. UI column)
	 * in terms of {@link IWidthManager}. So, please use one of "used" properties to determine default width for that single "column entity".
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public int getWidthByDefault(final Class<?> root, final String property);

	/**
	 * A bunch of used properties for {@link IPivotAddToDistributionTickRepresentation} should be treated as single "entity" (e.g. UI column)
	 * in terms of {@link IWidthManager}. So, please use one of "used" properties to set the default width for that single "column entity".
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public IPivotAddToDistributionTickRepresentation setWidthByDefault(Class<?> root, String property, int width);
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
