package ua.com.fielden.platform.ddl;

import com.google.common.collect.ImmutableSet;
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
    List<String> generateDatabaseDdl(Dialect dialect);

    /**
     * Generates DDL statements for creating tables, primary keys, indices, and foreign keys for the given entity types.
     */
    default List<String> generateDatabaseDdl(final Dialect dialect,
                                             final Class<? extends AbstractEntity<?>> type,
                                             final Class<? extends AbstractEntity<?>>... types) {
        return generateDatabaseDdl(dialect,
                                   ImmutableSet.<Class<? extends AbstractEntity<?>>>builder()
                                           .add(type).add(types)
                                           .build());
    }

    /**
     * Generates DDL statements for creating tables, primary keys, indices, and foreign keys for the given entity types.
     */
    List<String> generateDatabaseDdl(Dialect dialect, Collection<? extends Class<? extends AbstractEntity<?>>> types);

}
