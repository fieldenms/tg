package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.summary.IWithSummary;

/**
 * This contract provides a way for specifying result set ordering.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder3Ordering<T extends AbstractEntity<?>> extends IWithSummary<T> {


    /**
     * Specifies the result set to be order by the current property, where argument <code>orderSeq</code> defines a sequential number
     * of the property in the ordering sequence.
     * This argument is provided to enable ordering by properties out of the sequence in which they are added to the result set.
     *
     * @param orderSeq
     * @return
     */
    IResultSetBuilder4OrderingDirection<T> order(final int orderSeq);
}
