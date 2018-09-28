package ua.com.fielden.platform.entity.validation;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.validation.custom.DomainEntitiesDependenciesUtils.DEPENDENT_PROP_TITLE;
import static ua.com.fielden.platform.entity.validation.custom.DomainEntitiesDependenciesUtils.ENTITY_TYPE_TITLE;
import static ua.com.fielden.platform.entity.validation.custom.DomainEntitiesDependenciesUtils.PARAM;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isNotSpecialActivatableToBeSkipped;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.validation.custom.DomainEntitiesDependenciesUtils;
import ua.com.fielden.platform.entity.validation.custom.DomainEntityDependencies;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;


/**
 * A validator for property <code>active</code> on class {@link ActivatableAbstractEntity} to prevent deactivation of entities with active dependencies.
 *
 * @author TG Team
 *
 */

public class ActivePropertyValidator implements IBeforeChangeEventHandler<Boolean> {
    private final ICompanionObjectFinder coFinder;
    private final IEntityAggregatesOperations aggregatesOperations;
    private final IApplicationDomainProvider applicationDomainProvider;

    @Inject
    public ActivePropertyValidator(final ICompanionObjectFinder coFinder, final IEntityAggregatesOperations aggregatesOperations, final IApplicationDomainProvider applicationDomainProvider) {
        this.coFinder = coFinder;
        this.aggregatesOperations = aggregatesOperations;
        this.applicationDomainProvider = applicationDomainProvider;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Result handle(final MetaProperty<Boolean> property, final Boolean newValue, final Set<Annotation> mutatorAnnotations) {
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

                final Map<Class<? extends AbstractEntity<?>>, DomainEntityDependencies> a = DomainEntitiesDependenciesUtils.getEntityDependantsMap(applicationDomainProvider.entityTypes());
                final AggregatedResultQueryModel query = DomainEntitiesDependenciesUtils.generateQuery(a.get(entity.getType()).getActivatableDependencies(), true);
                final Map<String, Object> savingsParams1 = new HashMap<>();
                savingsParams1.put(PARAM, entity);
                final List<EntityAggregates> deps = aggregatesOperations.getAllEntities(from(query).with(savingsParams1).model());
                for (final EntityAggregates dep : deps) {
                    System.out.println(String.format("%25s%25s%25s", dep.get(ENTITY_TYPE_TITLE), dep.get(DEPENDENT_PROP_TITLE), dep.get("KOUNT")));
                }
                return Result.failure(count, format("%s [%s] has active dependencies (%s).", entityTitle, entity, count));
            }
        } else { 
            // entity is being activated, but could be referencing inactive activatables
            // we could not rely on the fact that all activatable are fetched
            // so, we should only perform so-called soft validation
            // where validation would occur strictly against fetched values
            // later during saving all activatable properties would get checked anyway
            final Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> activatableProps = collectActivatableNotNullNotProxyProperties(entity);

            // need to check if already referenced activatables are active and thus may be referenced by this entity, whic is being activated
            for (final MetaProperty<? extends ActivatableAbstractEntity<?>> prop : activatableProps) {
                final ActivatableAbstractEntity<?> value = prop.getValue();
                if (!value.isActive()) {
                    final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entity.getType()).getKey();
                    final String propTitle = TitlesDescsGetter.getTitleAndDesc(prop.getName(), entity.getType()).getKey();
                    final String valueEntityTitle = TitlesDescsGetter.getEntityTitleAndDesc(value.getType()).getKey();
                    return Result.failure(format("Property [%s] in %s [%s] references inactive %s [%s].", propTitle, entityTitle, entity, valueEntityTitle, value));
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
        return entity.nonProxiedProperties()
               .filter(mp -> mp.getValue() != null &&                           
                             mp.isActivatable() &&
                             isNotSpecialActivatableToBeSkipped(mp) &&
                             !((AbstractEntity<?>) mp.getValue()).isIdOnlyProxy() &&
                             !entity.equals(mp.getValue()))
               .map(mp -> (MetaProperty<? extends ActivatableAbstractEntity<?>>) mp)
               .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
