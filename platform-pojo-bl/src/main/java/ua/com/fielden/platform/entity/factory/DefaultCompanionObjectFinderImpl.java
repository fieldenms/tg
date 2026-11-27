package ua.com.fielden.platform.entity.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.companion.ICanReadUninstrumented;
import ua.com.fielden.platform.companion.ICompanionGenerator;
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
/// 2. If an entity type is annotated with [CompanionIsGenerated], the finder uses a generated companion object produced by [ICompanionGenerator].
///
/// If none of the above cases hold, the finder returns `null`.
///
@Singleton
final class DefaultCompanionObjectFinderImpl implements ICompanionObjectFinder {

    private static final Logger LOGGER = getLogger(DefaultCompanionObjectFinderImpl.class);
    public static final String ERR_CO_IS_MISSING = "Could not locate companion for entity of type [%s].";
    public static final String ERR_UNINSTRUMENTED_NOT_SUPPORTED_BY_CO = "Cannot produce uninstrumented companion of type [%s].";

    private final Injector injector;
    private final ICompanionGenerator companionGenerator;

    @Inject
    public DefaultCompanionObjectFinderImpl(
            final Injector injector,
            final ICompanionGenerator companionGenerator)
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
    @SuppressWarnings("unchecked")
    public <T extends IEntityDao<E>, E extends AbstractEntity<?>> T find(final Class<E> type, final boolean uninstrumented) {
        final T co;
        if (type.isAnnotationPresent(CompanionIsGenerated.class)) {
            co = (T) companionGenerator.generateCompanion(type);
        }
        else {
            final Class<T> coType = type.isAnnotationPresent(CompanionObject.class) ? (Class<T>) type.getAnnotation(CompanionObject.class).value() : null;
        if (coType != null) {
            try {
                    co = injector.getInstance(coType);
            } catch (final EntityCompanionException ex) {
                throw ex;
            } catch (final Exception ex) {
                LOGGER.warn(() -> ERR_CO_IS_MISSING.formatted(type.getName()), ex);
                // If a companion could not be instantiated for whatever reason, it can be considered as non-existing.
                // Thus, returning null.
                return null;
            }
        }
        else {
                co = null;
        }
    }
        return co == null ? null : decideUninstrumentation(uninstrumented, co);
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
