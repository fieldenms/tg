package ua.com.fielden.platform.entity.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.audit.AbstractAuditEntity;
import ua.com.fielden.platform.companion.ICanReadUninstrumented;
import ua.com.fielden.platform.companion.IEntityCompanionGenerator;
import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionIsGenerated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.reflection.AnnotationReflector;

import static java.lang.String.format;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.audit.AuditUtils.isAuditEntityType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresentForClass;

/**
 * Default implementation for {@link ICompanionObjectFinder}, which utilises injector (thread-safe) for creating Companion Object (CO) instances.
 * 
 * @author TG Team
 */
@Singleton
final class DefaultCompanionObjectFinderImpl implements ICompanionObjectFinder {

    private static final Logger LOGGER = getLogger(DefaultCompanionObjectFinderImpl.class);
    
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
                return decideUninstrumentation(uninstrumented, coType, co);
            } catch (final EntityCompanionException e) {
                throw e;
            } catch (final Exception e) {
                LOGGER.warn(format("Could not locate companion for type [%s].", type.getName()), e);
                // if controller could not be instantiated for whatever reason it can be considered non-existent
                // thus, returning null
                return null;
            }
        }
        else {
            return null;
        }
    }

    /**
     * A helper method to decide whether the instantiated companion should read instrumented or uninstrumented entities.
     *  
     * @param uninstrumented
     * @param coType
     * @param co
     * @return
     */
    private <T extends IEntityDao<E>, E extends AbstractEntity<?>> T decideUninstrumentation(final boolean uninstrumented, final Class<T> coType, final T co) {
        if (uninstrumented) {
            if (co instanceof ICanReadUninstrumented) {
                ((ICanReadUninstrumented) co).readUninstrumented();
            } else {
                throw new EntityCompanionException(format("Cannot produce uninstrumented companion of type [%s].", coType.getName()));
            }
        }
        
        return co;
    }

}
