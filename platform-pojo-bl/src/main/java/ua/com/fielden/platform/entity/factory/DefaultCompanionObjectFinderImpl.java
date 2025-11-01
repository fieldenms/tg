package ua.com.fielden.platform.entity.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.companion.ICanReadUninstrumented;
import ua.com.fielden.platform.companion.IEntityCompanionGenerator;
import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionIsGenerated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;

import static org.apache.logging.log4j.LogManager.getLogger;

/// Default implementation for [ICompanionObjectFinder], which utilises injector (thread-safe) for creating Companion Object (CO) instances.
///
/// There are two cases:
/// 1. If an entity type is annotated with [CompanionObject], the finder uses a companion object type, specified by this annotation.
/// 2. If an entity type is annotated with [CompanionIsGenerated], the finder generates a new type that represents the default companion object implementation.
///    The companion type generation is delegated to [IEntityCompanionGenerator].
///
/// If none of the above cases hold, the finder returns `null`.
///
///
@Singleton
final class DefaultCompanionObjectFinderImpl implements ICompanionObjectFinder {

    private static final Logger LOGGER = getLogger(DefaultCompanionObjectFinderImpl.class);
    public static final String ERR_CO_IS_MISSING = "Could not locate companion for entity of type [%s].";
    public static final String ERR_UNINSTRUMENTED_NOT_SUPPORTED_BY_CO = "Cannot produce uninstrumented companion of type [%s].";

    private final Injector injector;
    private final IEntityCompanionGenerator companionGenerator;

    @Inject
    public DefaultCompanionObjectFinderImpl(
            final Injector injector,
            final IEntityCompanionGenerator companionGenerator)
    {
        this.injector = injector;
        this.companionGenerator = companionGenerator;
    }

    @Override
    public <T extends IEntityDao<E>, E extends AbstractEntity<?>> T find(final Class<E> type) {
       return find(type, false);
    }

    @Override
    public <T extends IEntityReader<E>, E extends AbstractEntity<?>> T findAsReader(final Class<E> type, final boolean uninstrumented) {
        return find(type, uninstrumented);
    }

    @Override
    public <T extends IEntityDao<E>, E extends AbstractEntity<?>> T find(final Class<E> type, final boolean uninstrumented) {
        final Class<T> coType;
        if (type.isAnnotationPresent(CompanionObject.class)) {
            coType = (Class<T>) type.getAnnotation(CompanionObject.class).value();
        }
        else if (type.isAnnotationPresent(CompanionIsGenerated.class)) {
            coType = (Class<T>) companionGenerator.generateCompanion(type);
        }
        else {
            coType = null;
        }

        if (coType != null) {
            try {
                final T co = injector.getInstance(coType);
                return decideUninstrumentation(uninstrumented, co);
            } catch (final EntityCompanionException e) {
                throw e;
            } catch (final Exception ex) {
                LOGGER.warn(() -> ERR_CO_IS_MISSING.formatted(type.getName()), ex);
                // If a companion could not be instantiated for whatever reason, it can be considered as non-existing.
                // Thus, returning null.
                return null;
            }
        }
        else {
            return null;
        }
    }

    /// A helper method to decide whether the instantiated companion should read instrumented or uninstrumented entities.
    ///
    private <T extends IEntityDao<E>, E extends AbstractEntity<?>> T decideUninstrumentation(final boolean uninstrumented, final T co) {
        if (uninstrumented) {
            if (co instanceof ICanReadUninstrumented) {
                ((ICanReadUninstrumented) co).readUninstrumented();
            } else {
                throw new EntityCompanionException(ERR_UNINSTRUMENTED_NOT_SUPPORTED_BY_CO.formatted(co.getClass().getSimpleName()));
            }
        }
        
        return co;
    }

}
