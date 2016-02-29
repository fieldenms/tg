package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;

import com.google.inject.Inject;


/**
 * A validator for property <code>active</code> on class {@link ActivatableAbstractEntity} to prevent deactivation of entities with active dependencies.
 *
 * @author TG Team
 *
 */

public class ActivePropertyValidator implements IBeforeChangeEventHandler<Boolean> {
    private final ICompanionObjectFinder coFinder;

    @Inject
    public ActivePropertyValidator(final ICompanionObjectFinder coFinder) {
        this.coFinder = coFinder;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Result handle(final MetaProperty<Boolean> property, final Boolean newValue, final Boolean oldValue, final Set<Annotation> mutatorAnnotations) {
        final ActivatableAbstractEntity<?> entity = (ActivatableAbstractEntity<?>) property.getEntity();
        if (!entity.isPersisted()) { // a brand new entity is being created
            return Result.successful(newValue);
        } else if (!newValue) { // entity is being deactivated, but could still be referenced
            // let's check refCount... it could potentially be stale...
            final IEntityDao<?> co = coFinder.find(entity.getType());
            final long count;
            if (!co.isStale(entity.getId(), entity.getVersion())) {
                count = entity.getRefCount();
            } else {
                // need to retireve the latest refCount
                final fetch fetch = fetchOnly(entity.getType()).with("refCount");
                final ActivatableAbstractEntity<?> updatedEntity = (ActivatableAbstractEntity<?>) co.findById(entity.getId(), fetch);
                count = updatedEntity.getRefCount();
            }

            if (count == 0) {
                return Result.successful(newValue);
            } else {
                final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey();
                return Result.failure(count, format("Entity %s has active dependencies (%s).", entityTitle, count));
            }
        } else { // entity is being activated, but could be referencing inactive activatables
            // we could not rely on the fact that all activatable are fetched
            // so, we should only perform so-called soft validation
            // where validation would occur strictly against fetched values
            // later during saving all activatable properties would get checked anyway
            final Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> activatableProps = collectActivatableNotNullNotProxyProperties(entity);

            // need to check if already referenced activatables are active and thus may be referenced by this entity, whic is being activated
            for (final MetaProperty<? extends ActivatableAbstractEntity<?>> prop : activatableProps) {
                final ActivatableAbstractEntity<?> value = prop.getValue();
                if (!value.isActive()) {
                    return Result.failure(format("Entity %s has a reference to already inactive entity %s (type %s)", entity, value, prop.getType()));
                }
            }

            return Result.successful(null);
        }
    }

    /**
     * Collects properties that represent non-null, non-proxy and non-self-referenced activatable properties.
     *
     * @param entity
     * @return
     */
    private Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> collectActivatableNotNullNotProxyProperties(final ActivatableAbstractEntity<?> entity) {
        final Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> result = new HashSet<>();
        
        entity.nonProxiedProperties()
        .forEach(mp -> {
            final Object value = mp.getValue();
            if (value != null && 
                ActivatableAbstractEntity.class.isAssignableFrom(mp.getType()) &&
                !entity.equals(value)) {
                result.add((MetaProperty<? extends ActivatableAbstractEntity<?>>) mp);
            }
        });
        
        return result;
    }
}
