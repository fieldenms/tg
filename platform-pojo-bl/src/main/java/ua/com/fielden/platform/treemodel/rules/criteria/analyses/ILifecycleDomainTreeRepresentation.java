package ua.com.fielden.platform.treemodel.rules.criteria.analyses;


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
     * This interface defines how domain tree can be represented for <b>lifecycle analyses</b> specific ("add to categories").
     *
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
     *
     * @author TG Team
     *
     */
    public interface ILifecycleAddToCategoriesTickRepresentation extends IAbstractAnalysisAddToAggregationTickRepresentation {
    }

    /**
     * This interface defines how domain tree can be represented for <b>lifecycle analyses</b> specific ("add to distribution").
     *
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
     *
     * @author TG Team
     *
     */
    public interface ILifecycleAddToDistributionTickRepresentation extends IAbstractAnalysisAddToDistributionTickRepresentation {
    }
}
