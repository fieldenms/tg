package ua.com.fielden.platform.domaintree.centre.analyses;

import ua.com.fielden.platform.types.ICategorizer;


/**
 * This interface defines how domain tree can be represented for <b>lifecycle analyses</b>. <br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * @author TG Team
 *
 */
public interface ILifecycleDomainTreeRepresentation extends IAbstractAnalysisDomainTreeRepresentation {
    ILifecycleAddToDistributionTickRepresentation getFirstTick();
    ILifecycleAddToCategoriesTickRepresentation getSecondTick();

    /**
     * Here the logic should include "date period" properties, like "weeks", "days", etc. <p>
     * Also the "category" properties should be included for concrete Lifecycle property (that has been chosen).
     *
     */
    @Override
    public boolean isExcludedImmutably(Class<?> root, String property);

    /**
     * This interface defines how domain tree can be represented for <b>lifecycle analyses</b> specific ("add to distribution").
     *
     * @author TG Team
     *
     */
    public interface ILifecycleAddToDistributionTickRepresentation extends IAbstractAnalysisAddToDistributionTickRepresentation {
    }

    /**
     * This interface defines how domain tree can be represented for <b>lifecycle analyses</b> specific ("add to categories").
     * <p>
     * Note that, there are also enabled @Monitoring properties in this tick, which will adjust a list of categories by its own categories (defined in property {@link ICategorizer}).
     *
     * @author TG Team
     *
     */
    public interface ILifecycleAddToCategoriesTickRepresentation extends IAbstractAnalysisAddToAggregationTickRepresentation {
    }
}
