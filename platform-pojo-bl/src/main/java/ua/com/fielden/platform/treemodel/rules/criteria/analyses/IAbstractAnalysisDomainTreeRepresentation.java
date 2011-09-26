package ua.com.fielden.platform.treemodel.rules.criteria.analyses;

import ua.com.fielden.platform.treemodel.rules.IDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.criteria.IOrderingRepresentation;

/**
 * This interface defines how domain tree can be represented for <b>analyses</b> (base interface). <br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * @author TG Team
 *
 */
public interface IAbstractAnalysisDomainTreeRepresentation extends IDomainTreeRepresentation {
    IAbstractAnalysisAddToDistributionTickRepresentation getFirstTick();
    IAbstractAnalysisAddToAggregationTickRepresentation getSecondTick();

    /**
     * This interface defines how domain tree can be represented for <b>analyses</b> specific ("add to distribution").
     *
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
     *
     * @author TG Team
     *
     */
    public interface IAbstractAnalysisAddToDistributionTickRepresentation extends ITickRepresentation {
    }

    /**
     * This interface defines how domain tree can be represented for <b>analyses</b> specific ("add to aggregation").
     *
     * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
     *
     * @author TG Team
     * @see IOrderingRepresentation
     *
     */
    public interface IAbstractAnalysisAddToAggregationTickRepresentation extends IOrderingRepresentation, ITickRepresentation {
    }
}
