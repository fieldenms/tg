package ua.com.fielden.platform.dao;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.companion.IEntityInstantiator;
import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.companion.IPersistentEntityMutator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.user.User;

/**
 * The contract for any Entity Companion object to implement. It extends both {@link IEntityReader} and {@link IPersistentEntityMutator} contracts.
 *
 * @author TG Team
 *
 */
public interface IEntityDao<T extends AbstractEntity<?>> extends IEntityReader<T>, IPersistentEntityMutator<T>, IEntityInstantiator<T>, IComputationMonitor {
    static final int DEFAULT_PAGE_CAPACITY = 25;

    /**
     * Should return an entity type the DAO is managing.
     *
     * @return
     */
    Class<T> getEntityType();

    /**
     * Should return entity's key type.
     *
     * @return
     */
    Class<? extends Comparable<?>> getKeyType();

    /**
     * A factory method that creates an instance of a companion object for the specified entity type.
     * The reader methods of such companion return <code>uninstrumented</code> entities.
     *
     * @return
     */
    default <C extends IEntityDao<E>, E extends AbstractEntity<?>> C co(final Class<E> type) {
        throw new UnsupportedOperationException("This method should be overriden by descendants.");
    }

    /**
     * A factory method that creates an instance of a companion object for the specified entity type.
     * The reader methods of such companion return <code>instrumented</code> entities, which are suitable for mutation and saving.
     *
     * @return
     */
    default <C extends IEntityDao<E>, E extends AbstractEntity<?>> C co$(final Class<E> type) {
        throw new UnsupportedOperationException("This method should be overriden by descendants.");
    }

    /**
     * Returns provided name.
     *
     * @return
     */
    String getUsername();

    /**
     * Should return the current application user.
     *
     * @return
     */
    abstract User getUser();

    /**
     * Should return a byte array representation the exported data in a format envisaged by the specific implementation.
     * <p>
     * For example it could be a byte array of GZipped Excel data.
     *
     * @param query
     *            -- query result of which should be exported.
     * @param propertyNames
     *            -- names of properties, including dot notated properties, which should be used in the export.
     * @param propertyTitles
     *            -- titles corresponding to the properties being exported, which are used as headers of columns.
     * @return
     */
    byte[] export(final QueryExecutionModel<T, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException;

    /**
     * Returns all entities produced by the provided query.
     *
     * @param quert
     * @return
     * @deprecated Streaming API must be used instead.
     */
    @Deprecated
    List<T> getAllEntities(final QueryExecutionModel<T, ?> query);

    /**
     * Returns first entities produced by the provided query.
     *
     * @param quert
     * @return
     * @deprecated Streaming API must be used instead.
     */
    @Deprecated
    List<T> getFirstEntities(final QueryExecutionModel<T, ?> query, final int numberOfEntities);

}