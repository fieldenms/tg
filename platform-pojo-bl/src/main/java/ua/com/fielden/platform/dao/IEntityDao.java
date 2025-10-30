package ua.com.fielden.platform.dao;

import java.io.IOException;
import java.util.List;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.companion.IEntityInstantiator;
import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.companion.IPersistentEntityMutator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.security.user.User;

/// A contract representing an Entity Companion object.
/// It extends both [IEntityReader] and [IPersistentEntityMutator].
///
public interface IEntityDao<T extends AbstractEntity<?>> extends IEntityReader<T>, IPersistentEntityMutator<T>, IEntityInstantiator<T>, IComputationMonitor {

    String ERR_UNSUPPORTED_BY_DEFAULT = "This method should be overridden by descendants.";

    int DEFAULT_PAGE_CAPACITY = 25;

    /// A factory method that creates an instance of a companion object for the specified entity type as [IEntityReader].
    ///
    /// The reader methods return *uninstrumented* entities, which are not suitable for mutation.
    ///
    default <C extends IEntityReader<E>, E extends AbstractEntity<?>> C co(final Class<E> type) {
        throw new UnsupportedOperationException(ERR_UNSUPPORTED_BY_DEFAULT);
    }

    /// A factory method that creates an instance of a companion object for the specified entity type as [IEntityDao].
    ///
    /// The reader methods of such companion return *instrumented* entities, which are suitable for mutation and saving.
    ///
    default <C extends IEntityDao<E>, E extends AbstractEntity<?>> C co$(final Class<E> type) {
        throw new UnsupportedOperationException(ERR_UNSUPPORTED_BY_DEFAULT);
    }

    /// Returns a name of the current user or `null`, if no user was identified.
    ///
    @Nullable String getUsername();

    /// Returns an instance of [User] for the current user or `null`, if no user was identified.
    ///
    @Nullable User getUser();

    /// Returns the exported data as a byte array, formatted according to the rules of the specific implementation.
    ///
    /// For example, the result may be a GZipped Excel file represented as a byte array.
    ///
    /// @param query          the query used to retrieve the data for export
    /// @param propertyNames  the properties (including dot-expressions) to include in the export
    /// @param propertyTitles the titles corresponding to the exported properties, used as column headers
    ///
    byte[] export(final QueryExecutionModel<T, ?> query, final String[] propertyNames, final String[] propertyTitles) throws IOException;

}