package ua.com.fielden.platform.dao;

import java.util.List;

import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;

/**
 * Defines a contract that should be implemented by any lifecycle data access object (an eQuery or REST driven implementation). Lifecycle information could be retrieved only for
 * those entities that have at least one <code>@Monitoring</code> property.
 *
 * Business logic and UI should strictly depend only on DAO interfaces -- not the concrete implementations. This will ensure implementation flexibility of the concrete way to
 * access data.
 *
 * @author Jhou
 *
 */
public interface ILifecycleDao<T extends AbstractEntity<?>> {

    /**
     * Retrieves information about entities lifecycle (<code>model</code> specifies a criteria for that entities) for concrete property (specified by <code>propertyName</code>) for
     * period [<code>from; to</code>].
     *
     * @param model
     *            - to restrict entities for analysis.
     * @param binaryTypes
     * @param propertyName
     *            - a property for which lifecycle information should be retrieved.
     * @param from
     *            - left period boundary.
     * @param to
     *            - right period boundary.
     * @return
     */
    LifecycleModel<T> getLifecycleInformation(final SingleResultQueryModel<? extends AbstractEntity<?>> model, final List<byte[]> binaryTypes, final String propertyName, final DateTime from, final DateTime to);

    /**
     * Username should be provided for every DAO instance in order to support data filtering and auditing.
     */
    void setUsername(final String username);
    String getUsername();
}
