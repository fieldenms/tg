package ua.com.fielden.platform.domaintree.centre.analyses;

import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.ITickRepresentationWithMutability;
import ua.com.fielden.platform.domaintree.impl.IDomainTreeRepresentationWithMutability;

/**
 * This interface defines how domain tree can be represented for <b>analyses</b> (base interface). <br><br>
 *
 * <b>Important:</b> it is necessary to override {@link #equals(Object)} and {@link #hashCode()} methods in implementors to provide logical comparison of instances. <br><br>
 *
 * @author TG Team
 *
 */
public interface IAbstractAnalysisDomainTreeRepresentation extends IDomainTreeRepresentationWithMutability {
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
    public interface IAbstractAnalysisAddToDistributionTickRepresentation extends ITickRepresentationWithMutability {
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
    public interface IAbstractAnalysisAddToAggregationTickRepresentation extends IOrderingRepresentation, ITickRepresentationWithMutability {
    }
}
