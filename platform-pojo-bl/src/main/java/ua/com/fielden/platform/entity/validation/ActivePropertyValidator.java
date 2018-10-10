package ua.com.fielden.platform.entity.validation;

import static java.lang.Math.max;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang.StringUtils.leftPad;
import static org.apache.commons.lang.StringUtils.rightPad;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.validation.custom.DomainEntitiesDependenciesUtils.COUNT;
import static ua.com.fielden.platform.entity.validation.custom.DomainEntitiesDependenciesUtils.DEPENDENT_PROP_TITLE;
import static ua.com.fielden.platform.entity.validation.custom.DomainEntitiesDependenciesUtils.ENTITY_TYPE_TITLE;
import static ua.com.fielden.platform.entity.validation.custom.DomainEntitiesDependenciesUtils.PARAM;
import static ua.com.fielden.platform.entity.validation.custom.DomainEntitiesDependenciesUtils.generateQuery;
import static ua.com.fielden.platform.entity.validation.custom.DomainEntitiesDependenciesUtils.getEntityDependantsMap;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isNotSpecialActivatableToBeSkipped;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.utils.MessageUtils.singleOrPlural;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.validation.custom.DomainEntityDependencies;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.types.tuples.T3;


/**
 * A validator for property <code>active</code> on class {@link ActivatableAbstractEntity} to prevent deactivation of entities with active dependencies.
 *
 * @author TG Team
 *
 */

public class ActivePropertyValidator extends AbstractBeforeChangeEventHandler<Boolean> {
    private final ICompanionObjectFinder coFinder;
    
    private final IApplicationDomainProvider applicationDomainProvider;

    @Inject
    public ActivePropertyValidator(final ICompanionObjectFinder coFinder, final IApplicationDomainProvider applicationDomainProvider) {
        this.coFinder = coFinder;
        this.applicationDomainProvider = applicationDomainProvider;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Result handle(final MetaProperty<Boolean> property, final Boolean newValue, final Set<Annotation> mutatorAnnotations) {
        final ActivatableAbstractEntity<?> entity = property.getEntity();
        if (!entity.isPersisted()) { // a brand new entity is being created
            return successful(newValue);
        } else if (!newValue) { // entity is being deactivated, but could still be referenced
            // let's check refCount... it could potentially be stale...
            final IEntityDao<?> co = coFinder.find(entity.getType());
            final int count;
            if (!co.isStale(entity.getId(), entity.getVersion())) {
                count = entity.getRefCount();
            } else {
                // need to retrieve the latest refCount
                final fetch fetch = fetchOnly(entity.getType()).with("refCount");
                final ActivatableAbstractEntity<?> updatedEntity = (ActivatableAbstractEntity<?>) co.findById(entity.getId(), fetch);
                count = updatedEntity.getRefCount();
            }

            if (count == 0) {
                return successful(newValue);
            } else {
                final Map<Class<? extends AbstractEntity<?>>, DomainEntityDependencies> domainDependency = getEntityDependantsMap(applicationDomainProvider.entityTypes());
                final AggregatedResultQueryModel query = generateQuery(domainDependency.get(entity.getType()).getActivatableDependencies(), true);
                final OrderingModel orderBy = orderBy().yield(COUNT).desc().yield(ENTITY_TYPE_TITLE).asc().yield(DEPENDENT_PROP_TITLE).asc().model();
                return failure(count, mkErrorMsg(entity, count, co(EntityAggregates.class).getAllEntities(from(query).with(orderBy).with(mapOf(t2(PARAM, entity))).model())));
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
                    return failure(format("Property [%s] in %s [%s] references inactive %s [%s].", propTitle, entityTitle, entity, valueEntityTitle, value));
                }
            }

            return successful(null);
        }
    }

    private String mkErrorMsg(final ActivatableAbstractEntity<?> entity, final int count, final List<EntityAggregates> dependencies) {
        final T3<Integer, Integer, Integer> lengths = dependencies.stream()
                .map(dep -> t3(dep.get(ENTITY_TYPE_TITLE).toString().length(), dep.get(DEPENDENT_PROP_TITLE).toString().length(), dep.get(COUNT).toString().length()))
                .reduce(t3(0, 0, 0), (accum, val) -> t3(max(accum._1, val._1), max(accum._2, val._2), max(accum._3, val._3)), (v1, v2) -> {throw failure("Should not happen");}); 
        final String deps = dependencies.stream()
                .map(dep -> depMsg(dep.get(ENTITY_TYPE_TITLE), dep.get(DEPENDENT_PROP_TITLE), dep.get(COUNT).toString(), lengths))
                .collect(joining("\n<br>"));
        final String columns = depMsg("Entity", "Property", "Qty", lengths);
        final String entityTitle = getEntityTitleAndDesc(entity.getType()).getKey();
        return format("%s [%s] has %s active %s:%n%n<br><br>%s<hr>%n<br>%s", entityTitle, entity, count, singleOrPlural(count, "dependency", "dependencies"), columns, deps);
    }

    private static String depMsg(final String val1, final String val2, final String val3, final T3<Integer, Integer, Integer> lengths) {
        final String padStr = "\u00A0";
        return format("<tt>%s%s\u00A0%s</tt>", rightPad(val1, lengths._1 + 2, padStr), leftPad(val3, lengths._3 + 2, padStr), rightPad(val2, lengths._2 + 2, padStr));
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
