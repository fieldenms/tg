package ua.com.fielden.platform.companion;

import com.google.inject.Inject;
import org.hibernate.Session;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.eql.meta.EqlTables;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.utils.IDates;

import java.util.function.Supplier;

// This factory must be implemented by hand since com.google.inject.assistedinject.FactoryModuleBuilder
// doesn't support generic factory methods.
public final class DeleteOperationsFactory {

    private final IDomainMetadata domainMetadata;
    private final IDbVersionProvider dbVersionProvider;
    private final EqlTables eqlTables;
    private final QuerySourceInfoProvider querySourceInfoProvider;
    private final IDates dates;

    @Inject
    DeleteOperationsFactory(final IDomainMetadata domainMetadata,
                            final IDbVersionProvider dbVersionProvider,
                            final EqlTables eqlTables,
                            final QuerySourceInfoProvider querySourceInfoProvider,
                            final IDates dates) {
        this.domainMetadata = domainMetadata;
        this.dbVersionProvider = dbVersionProvider;
        this.eqlTables = eqlTables;
        this.querySourceInfoProvider = querySourceInfoProvider;
        this.dates = dates;
    }

    public <E extends AbstractEntity<?>> DeleteOperations<E> create(final IEntityReader<E> reader,
                                                                    final Supplier<Session> session,
                                                                    final Class<E> entityType) {
        return new DeleteOperations<>(reader, session, entityType,
                                      domainMetadata, dbVersionProvider, eqlTables,
                                      querySourceInfoProvider, dates);
    }

}
