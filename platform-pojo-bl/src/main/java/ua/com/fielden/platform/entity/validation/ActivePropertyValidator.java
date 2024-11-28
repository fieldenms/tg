package ua.com.fielden.platform.entity.validation;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.EntityUtils;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.lang.Math.max;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.apache.commons.lang3.StringUtils.rightPad;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.REF_COUNT;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.entity.validation.custom.DomainEntitiesDependenciesUtils.*;
import static ua.com.fielden.platform.error.Result.*;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isNotSpecialActivatableToBeSkipped;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.utils.MessageUtils.singleOrPlural;


/**
 * A validator for property {@code active} on class {@link ActivatableAbstractEntity} to prevent deactivation of entities with active dependencies.
 *
 * @author TG Team
 *
 */
public class ActivePropertyValidator extends AbstractBeforeChangeEventHandler<Boolean> {
    private static final Logger LOGGER = getLogger(ActivePropertyValidator.class);
    private static final String PAD_STR = "\u00A0";
    public static final String WARN_ENTITY_HAS_NO_ACTIVATABLE_DEPENDENCIES = "Entity [%s] has no activatable dependencies at the domain level, but has refCount > 0.";
    public static final String INFO_ENTITY_HAS_ACTIVE_DEPENDENCIES = "%s [%s] has %s active %s:%n%n<br><br>%s<hr>%n<br>%s";
    public static final String INFO_DEPENDENCY = "<tt>%s%s\u00A0%s</tt>";
    public static final String ERR_INACTIVE_REFERENCES = "Property [%s] in %s [%s] references inactive %s [%s].";

    private final IApplicationDomainProvider applicationDomainProvider;

    @Inject
    public ActivePropertyValidator(final ICompanionObjectFinder coFinder, final IApplicationDomainProvider applicationDomainProvider) {
        this.applicationDomainProvider = applicationDomainProvider;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Result handle(final MetaProperty<Boolean> property, final Boolean newValue, final Set<Annotation> mutatorAnnotations) {
        final ActivatableAbstractEntity<?> entity = property.getEntity();
        // A persisted entity is being deactivated, but it may still be referenced.
        if (!newValue && entity.isPersisted()) {
            // Check refCount... it could potentially be stale...
            final IEntityReader<?> co = co(entity.getType());
            final int count;
            if (!co.isStale(entity.getId(), entity.getVersion())) {
                count = entity.getRefCount();
            }
            // If an entity is stale, need to retrieve the latest refCount.
            else {
                final fetch fetch = fetchOnly(entity.getType()).with(REF_COUNT);
                final ActivatableAbstractEntity<?> updatedEntity = (ActivatableAbstractEntity<?>) co.findById(entity.getId(), fetch);
                count = updatedEntity.getRefCount();
            }

            // TODO: refCount excludes deactivatable dependencies.
            //       In light of issue #2316, this condition is no longer sufficient.
            //       Issue #1745 already touched on that, suggesting converting refCount to a function and removing all refCount-related computations.
            // If refCount indicates 0 references, return success, permitting deactivation.
            if (count == 0) {
                return successful();
            }
            // Otherwise, need to identify active dependencies, preventing deactivation.
            else {
                // Consider only activatable persistent entities as dependencies.
                // Hypothetically speaking, every activatable entity is also persistent.
                // However, this may change in the future, which is why the type's persistence should also be tested.
                final Predicate<Class<? extends AbstractEntity<?>>> activatableEntityType = EntityUtils::isActivatableEntityType;
                final var domainDependency = entityDependencyMap(applicationDomainProvider.entityTypes(), activatableEntityType.and(EntityUtils::isPersistentEntityType));
                // TODO: At the moment deactivatable entities, associated with the current entity are excluded not to count transitive references/dependencies.
                //       However, it is now required to count such transitive references, which in turn requires recursive analysis of dependencies.
                //       This is to cater for rare, but possible cases of having deactivatable dependencies on top of deactivatable dependencies.
                final var activatableDependencies = domainDependency.get(entity.getType()).getActivatableDependencies();
                // There can be cases where the domain model evolved and entity dependencies were relaxed/removed, but refCount has not been updated.
                // In such cases, there will be no dependencies identified in the domain model, and thus there is no reason to report any errors.
                if (activatableDependencies.isEmpty()) {
                    LOGGER.warn(WARN_ENTITY_HAS_NO_ACTIVATABLE_DEPENDENCIES.formatted(entity.getType().getSimpleName()));
                    return successful();
                } else {
                    // Exclude inactive dependencies to guarantee that only records with dependency COUNT > 0 are returned.
                    final var query = dependencyCountQuery(activatableDependencies, true);
                    // TODO: Ordering would need to take into account the presence of deactivatable dependencies somehow,
                    //       so that the output would be a linearised tree structure of dependencies.
                    //       It would potentially be useful to make deactivatable dependencies obvious in the resultant message.
                    final var orderBy = orderBy().yield(COUNT).desc().yield(ENTITY_TYPE_TITLE).asc().yield(DEPENDENT_PROP_TITLE).asc().model();
                    return failure(count, mkErrorMsg(entity, count, co(EntityAggregates.class).getAllEntities(from(query).with(orderBy).with(mapOf(t2(PARAM, entity))).model())));
                }
            }
        }
        // Either existing or new entity is being activated...
        else if (newValue) {
            // Entity is being activated, but could be referencing inactive activatables.
            // We cannot rely on the fact that all activatable are fetched.
            // Therefore, we should only perform a so-called soft validation,
            // where validation would occur strictly against fetched values.
            // Later during saving, all activatable properties would get checked anyway.
            final var activatableProps = collectActivatableNotNullNotProxyProperties(entity);

            // Need to check if already referenced activatables are active and thus may be referenced by this entity, which is being activated.
            for (final MetaProperty<? extends ActivatableAbstractEntity<?>> prop : activatableProps) {
                final ActivatableAbstractEntity<?> value = prop.getValue();
                if (!value.isActive()) {
                    final String entityTitle = getEntityTitleAndDesc(entity.getType()).getKey();
                    final String propTitle = getTitleAndDesc(prop.getName(), entity.getType()).getKey();
                    final String valueEntityTitle = getEntityTitleAndDesc(value.getType()).getKey();
                    return failuref(ERR_INACTIVE_REFERENCES, propTitle, entityTitle, entity, valueEntityTitle, value);
                }
            }
        }
        // Otherwise...
        return successful();
    }

    private String mkErrorMsg(final ActivatableAbstractEntity<?> entity, final int count, final List<EntityAggregates> dependencies) {
        final var lengths = dependencies.stream()
                .map(dep -> t3(dep.get(ENTITY_TYPE_TITLE).toString().length(), dep.get(DEPENDENT_PROP_TITLE).toString().length(), dep.get(COUNT).toString().length()))
                .reduce(t3(0, 0, 0), (accum, val) -> t3(max(accum._1, val._1), max(accum._2, val._2), max(accum._3, val._3)), (v1, v2) -> {throw failure("Should not happen");}); 
        final String deps = dependencies.stream()
                .map(dep -> depMsg(dep.get(ENTITY_TYPE_TITLE), dep.get(DEPENDENT_PROP_TITLE), dep.get(COUNT).toString(), lengths))
                .collect(joining("\n<br>"));
        final String columns = depMsg("Entity", "Property", "Qty", lengths);
        final String entityTitle = getEntityTitleAndDesc(entity.getType()).getKey();
        return INFO_ENTITY_HAS_ACTIVE_DEPENDENCIES.formatted(entityTitle, entity, count, singleOrPlural(count, "dependency", "dependencies"), columns, deps);
    }

    private static String depMsg(final String val1, final String val2, final String val3, final T3<Integer, Integer, Integer> lengths) {
        return INFO_DEPENDENCY.formatted(rightPad(val1, lengths._1 + 2, PAD_STR), leftPad(val3, lengths._3 + 2, PAD_STR), rightPad(val2, lengths._2 + 2, PAD_STR));
    }

    /**
     * Collects properties that represent non-null, non-proxy, and non-self-referenced activatable properties for {@code entity}.
     */
    private Set<MetaProperty<? extends ActivatableAbstractEntity<?>>> collectActivatableNotNullNotProxyProperties(final ActivatableAbstractEntity<?> entity) {
        return entity.nonProxiedProperties()
               .filter(mp -> mp.getValue() != null &&                           
                             mp.isActivatable() &&
                             isNotSpecialActivatableToBeSkipped(mp) &&
                             !((AbstractEntity<?>) mp.getValue()).isIdOnlyProxy() &&
                             !entity.equals(mp.getValue()))
               .map(mp -> (MetaProperty<? extends ActivatableAbstractEntity<?>>) mp)
               .collect(toCollection(LinkedHashSet::new));
    }

}
