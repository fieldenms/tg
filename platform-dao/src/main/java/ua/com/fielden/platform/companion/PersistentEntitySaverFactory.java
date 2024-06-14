package ua.com.fielden.platform.companion;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.EntityFetcher;
import ua.com.fielden.platform.entity.query.IDbVersionProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.utils.IUniversalConstants;

import java.util.List;
import java.util.function.*;

// This factory must be implemented by hand since com.google.inject.assistedinject.FactoryModuleBuilder
// doesn't support generic factory methods.
public final class PersistentEntitySaverFactory {

    private final IDbVersionProvider dbVersionProvider;
    private final EntityFetcher entityFetcher;
    private final IUserProvider userProvider;
    private final IUniversalConstants universalConstants;
    private final ICompanionObjectFinder coFinder;

    @Inject
    PersistentEntitySaverFactory(final IDbVersionProvider dbVersionProvider,
                                 final EntityFetcher entityFetcher,
                                 final IUserProvider userProvider,
                                 final IUniversalConstants universalConstants,
                                 final ICompanionObjectFinder coFinder) {
        this.dbVersionProvider = dbVersionProvider;
        this.entityFetcher = entityFetcher;
        this.userProvider = userProvider;
        this.universalConstants = universalConstants;
        this.coFinder = coFinder;
    }

    public <E extends AbstractEntity<?>> PersistentEntitySaver<E> create(
            final Supplier<Session> session,
            final Supplier<String> transactionGuid,
            final Class<E> entityType,
            final Class<? extends Comparable<?>> keyType,
            final BiConsumer<E, List<String>> processAfterSaveEvent,
            final Consumer<MetaProperty<?>> assignBeforeSave,
            final BiFunction<Long, fetch<E>, E> findById,
            final Function<EntityResultQueryModel<E>, Boolean> entityExists,
            final Logger logger)
    {
        return new PersistentEntitySaver<>(session, transactionGuid, entityType, keyType, processAfterSaveEvent,
                                           assignBeforeSave, findById, entityExists, logger,
                                           dbVersionProvider, entityFetcher, userProvider, universalConstants, coFinder);
    }

}
