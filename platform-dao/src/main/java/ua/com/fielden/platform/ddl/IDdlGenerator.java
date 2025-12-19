package ua.com.fielden.platform.ddl;

import com.google.inject.ImplementedBy;
import org.hibernate.dialect.Dialect;
import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.Collection;
import java.util.List;

@ImplementedBy(DdlGeneratorImpl.class)
public interface IDdlGenerator {

    /**
     * Generates DDL statements for creating tables, primary keys, indices and foreign keys for all persistent entity types,
     * which includes domain entities and auxiliary platform entities.
     */
    default List<String> generateDatabaseDdl(final Dialect dialect) {
        return generateDatabaseDdl(dialect, true);
    }

    /// Returns a list of DDL statements that will create the complete schema for the application.
    /// This includes tables for persistent entity types, column constraints and indices.
    ///
    /// @param withFk controls whether foreign keys will be created.
    ///               Specify `false` to skip foreign keys.
    ///
    List<String> generateDatabaseDdl(Dialect dialect, boolean withFk);

    /**
     * Generates DDL statements for creating tables, primary keys, indices, and foreign keys for the given entity types.
     */
    default List<String> generateDatabaseDdl(final Dialect dialect,
                                             final Class<? extends AbstractEntity<?>> type,
                                             final Class<? extends AbstractEntity<?>>... types)
    {
        return generateDatabaseDdl(dialect, true, type, types);
    }

    /// Returns a list of DDL statements that will create a schema for the application using only the specified entity types.
    /// This includes tables for persistent entity types, column constraints and indices.
    ///
    /// @param withFk controls whether foreign keys will be created.
    ///               Specify `false` to skip foreign keys.
    ///
    List<String> generateDatabaseDdl(Dialect dialect,
                                     boolean withFk,
                                     Class<? extends AbstractEntity<?>> type,
                                     Class<? extends AbstractEntity<?>>... types);

    /**
     * Generates DDL statements for creating tables, primary keys, indices, and foreign keys for the given entity types.
     */
    default List<String> generateDatabaseDdl(Dialect dialect, Collection<Class<? extends AbstractEntity<?>>> types) {
        return generateDatabaseDdl(dialect, true, types);
    }

    /// Returns a list of DDL statements that will create a schema for the application using only the specified entity types.
    /// This includes tables for persistent entity types, column constraints and indices.
    ///
    /// @param withFk controls whether foreign keys will be created.
    ///               Specify `false` to skip foreign keys.
    ///
    List<String> generateDatabaseDdl(Dialect dialect, boolean withFk, Collection<Class<? extends AbstractEntity<?>>> types);

}
