package ua.com.fielden.platform.dao;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.companion.IPersistentEntityMutator;
import ua.com.fielden.platform.companion.IEntityInstantiator;
import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
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
     * This is a mixin method that should indicate whether data retrieval should follow the instrumented or uninstrumented strategy for entity instantiation during retrieval.
     * 
     * @return
     */
    default boolean instrumented() {
        return true;
    }
    
    /**
     * A factory method that creates an instance of the same companion object it is invoked on, but with method {@link #instrumented()} returning <code>false</code>.
     * 
     * @return
     */
    default <E extends IEntityDao<T>> E uninstrumented() {
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
     * Returns default {@link FetchProvider} for the entity.
     * <p>
     * This fetch provider represents the 'aggregated' variant of all fetch providers needed mainly for entity master actions (and potentially others): <br>
     * <br>
     * 1. visual representation of entity properties in entity master UI <br>
     * 2. validation / modification processes with BCE / ACE / conversions handling <br>
     * 3. autocompletion of entity-typed properties
     *
     * @return
     */
    IFetchProvider<T> getFetchProvider();
    
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