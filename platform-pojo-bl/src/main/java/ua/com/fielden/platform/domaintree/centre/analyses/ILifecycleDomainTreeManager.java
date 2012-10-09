package ua.com.fielden.platform.domaintree.centre.analyses;

import java.util.Date;
import java.util.List;

import ua.com.fielden.platform.types.ICategorizer;
import ua.com.fielden.platform.types.ICategory;
import ua.com.fielden.platform.utils.Pair;

/**
 * This interface defines how domain tree can be managed for <b>lifecycle analyses</b>. <br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * @author TG Team
 *
 */
public interface ILifecycleDomainTreeManager extends IAbstractAnalysisDomainTreeManager {
    ILifecycleAddToDistributionTickManager getFirstTick();
    ILifecycleAddToCategoriesTickManager getSecondTick();
    ILifecycleDomainTreeRepresentation getRepresentation();

    /**
     * This interface defines how domain tree can be managed for <b>lyfecycle analyses</b> specific ("add to distribution").
     *
     * @author TG Team
     *
     */
    public interface ILifecycleAddToDistributionTickManager extends IAbstractAnalysisAddToDistributionTickManager {
        /**
         * Returns a <b>single</b> used property for concrete <code>root</code> type. (Should not return multiple used properties)
         *
         * @param root -- a root type that contains an used property.
         * @return
         */
        List<String> usedProperties(final Class<?> root);
    }

    /**
     * This interface defines how domain tree can be managed for <b>lifecycle analyses</b> specific ("add to categories").
     * <p>
     * Note that, there are also enabled @Monitoring properties in this tick, which will adjust a list of categories by its own categories (defined in property {@link ICategorizer}).
     *
     * @author TG Team
     *
     */
    public interface ILifecycleAddToCategoriesTickManager extends IAbstractAnalysisAddToAggregationTickManager {
	/** TODO */
	List<ICategory> allCategories(final Class<?> root);

	/** TODO */
	List<ICategory> currentCategories(final Class<?> root);
    }

    /**
     * Gets a property to build lifecycle report.
     *
     * @return
     */
    Pair<Class<?>, String> getLifecycleProperty();

    /**
     * Gets a "from" date (left period boundary).
     *
     * @return
     */
    Date getFrom();

    /**
     * Sets a "from" date (left period boundary).
     *
     * @param from -- a value to set
     * @return -- an lifecycle analysis manager
     */
    ILifecycleDomainTreeManager setFrom(final Date from);

    /**
     * Gets a "to" date (right period boundary).
     *
     * @return
     */
    Date getTo();

    /**
     * Sets a "to" date (right period boundary).
     *
     * @param to -- a value to set
     * @return -- an lifecycle analysis manager
     */
    ILifecycleDomainTreeManager setTo(final Date to);

    /**
     * Gets an <i>total</i> flag for lifecycle analysis manager.<br><br>
     *
     * @return
     */
    boolean isTotal();

    /**
     * Sets an <i>total</i> flag for lifecycle analysis manager. <br><br>
     *
     * @param total -- a flag to set
     * @return -- an analysis manager
     */
    IAbstractAnalysisDomainTreeManager setTotal(final boolean total);
}
