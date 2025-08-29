package ua.com.fielden.platform.entity.validation;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.EntityUtils;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.lang.Math.max;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.apache.commons.lang3.StringUtils.rightPad;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.exceptions.InvalidArgumentException.requireNonNull;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.validation.custom.DomainEntitiesDependenciesUtils.*;
import static ua.com.fielden.platform.error.Result.*;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isActivatableReferenceWithoutPreconditions;
import static ua.com.fielden.platform.reflection.Reflector.isPropertyProxied;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.types.tuples.T3.t3;
import static ua.com.fielden.platform.utils.CollectionUtil.mapOf;
import static ua.com.fielden.platform.utils.EntityUtils.isEntityType;
import static ua.com.fielden.platform.utils.MessageUtils.singleOrPlural;
import static ua.com.fielden.platform.utils.StreamUtils.foldLeft;

/// A validator for property `active` on class [ActivatableAbstractEntity] to prevent deactivation of entities with active dependencies.
///
@Singleton
public class ActivePropertyValidator extends AbstractBeforeChangeEventHandler<Boolean> {

    private static final String PAD_STR = "\u00A0";
    public static final String INFO_DEPENDENCY = "<span style='font-family:monospace'>%s%s\u00A0%s</span>";
    public static final String ERR_SHORT_ENTITY_HAS_ACTIVE_DEPENDENCIES = "%s [%s] has %s active %s.";
    public static final String ERR_ENTITY_HAS_ACTIVE_DEPENDENCIES = "%s [%s] has %s active %s:%n%n<br><br>%s<hr>%n<br>%s";
    public static final String ERR_INACTIVE_REFERENCES = "Property [%s] in %s [%s] references inactive %s [%s].";
    public static final String ERR_UNEXPECTED_ENTITY_TYPE = "Unexpected entity type for an activatable reference: [%s]";

    public static final Predicate<Class<? extends AbstractEntity<?>>> PREDICATE_ACTIVATABLE_AND_PERSISTENT_ENTITY_TYPE = EntityUtils::isActivatablePersistentEntityType;

    private final IApplicationDomainProvider applicationDomainProvider;
    private final ICompanionObjectFinder coFinder;

    @Inject
    ActivePropertyValidator(
            final IApplicationDomainProvider applicationDomainProvider,
            final ICompanionObjectFinder coFinder)
    {
        this.applicationDomainProvider = applicationDomainProvider;
        this.coFinder = coFinder;
    }

    @Override
    public Result handle(final MetaProperty<Boolean> property, final Boolean newValue, final Set<Annotation> mutatorAnnotations) {
        final ActivatableAbstractEntity<?> entity = property.getEntity();
        // A persisted entity is being deactivated, but it may still be referenced.
        if (!newValue && entity.isPersisted()) {
            // Consider only activatable persistent entities as dependencies.
            // As it stands now, every activatable entity is also persistent.
            // However, this may change in the future, which is why the type's persistence should also be tested.
            final var domainDependencies = entityDependencyMap(applicationDomainProvider.entityTypes(), PREDICATE_ACTIVATABLE_AND_PERSISTENT_ENTITY_TYPE);
            final var dependencies = domainDependencies.get(entity.getType()).getAllDependenciesThatCanPreventDeactivation(domainDependencies).collect(toSet());
            if (dependencies.isEmpty()) {
                return successful();
            } else {
                // Count active dependencies.
                // Excluding inactive dependencies guarantees that only records with dependency COUNT > 0 are returned.
                final var query = dependencyCountQuery(dependencies, true);
                final var orderBy = orderBy().yield(COUNT).desc().yield(ENTITY_TYPE_TITLE).asc().yield(DEPENDENT_PROP_TITLE).asc().model();
                final var dependencyStats = co(EntityAggregates.class).getAllEntities(from(query).with(orderBy).with(mapOf(t2(PARAM, entity))).model());
                final var count = dependencyStats.stream().mapToInt(eg -> eg.get(COUNT)).sum();
                if (count > 0) {
                    final String entityTitle = getEntityTitleAndDesc(entity.getType()).getKey();
                    final var shortErrMsg = ERR_SHORT_ENTITY_HAS_ACTIVE_DEPENDENCIES.formatted(entityTitle, entity, count, singleOrPlural(count, "dependency", "dependencies"));
                    return failureEx(shortErrMsg, mkErrorMsg(entity, count, dependencyStats));
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

            // Need to check if already referenced activatables are active and thus may be referenced by this entity, which is being activated.
            for (final var prop : activatableNotNullNotProxyProperties(entity)) {
                final var value = extractActivatable(prop.getValue());
                if (value != null && !isPropertyProxied(value, ACTIVE) && !value.isActive()) {
                    final var entityTitle = getEntityTitleAndDesc(entity.getType()).getKey();
                    final var propTitle = getTitleAndDesc(prop.getName(), entity.getType()).getKey();
                    final var valueEntityTitle = getEntityTitleAndDesc(value.getType()).getKey();
                    return failuref(ERR_INACTIVE_REFERENCES, propTitle, entityTitle, entity, valueEntityTitle, value);
                }
            }
        }
        // Otherwise...
        return successful();
    }

    private static @Nullable ActivatableAbstractEntity<?> extractActivatable(final AbstractEntity<?> entity) {
        requireNonNull(entity, "entity");

        return switch (entity) {
            case ActivatableAbstractEntity<?> it -> it;
            case AbstractUnionEntity union -> union.activeEntity() instanceof ActivatableAbstractEntity<?> it ? it : null;
            default -> throw new InvalidStateException(ERR_UNEXPECTED_ENTITY_TYPE.formatted(entity.getType().getSimpleName()));
        };
    }

    private String mkErrorMsg(final ActivatableAbstractEntity<?> entity, final int count, final List<EntityAggregates> dependencies) {
        final var lengths = foldLeft(
                dependencies.stream()
                        .map(dep -> t3(dep.get(ENTITY_TYPE_TITLE).toString().length(), dep.get(DEPENDENT_PROP_TITLE).toString().length(), dep.get(COUNT).toString().length())),
                t3(0, 0, 0),
                (accum, val) -> t3(max(accum._1, val._1), max(accum._2, val._2), max(accum._3, val._3)));
        final var deps = dependencies.stream()
                .map(dep -> depMsg(dep.get(ENTITY_TYPE_TITLE), dep.get(DEPENDENT_PROP_TITLE), dep.get(COUNT).toString(), lengths))
                .collect(joining("\n<br>"));
        final var columns = depMsg("Entity", "Property", "Qty", lengths);
        final var entityTitle = getEntityTitleAndDesc(entity.getType()).getKey();
        return ERR_ENTITY_HAS_ACTIVE_DEPENDENCIES.formatted(entityTitle, entity, count, singleOrPlural(count, "dependency", "dependencies"), columns, deps);
    }

    private static String depMsg(final String val1, final String val2, final String val3, final T3<Integer, Integer, Integer> lengths) {
        return INFO_DEPENDENCY.formatted(rightPad(val1, lengths._1 + 2, PAD_STR), leftPad(val3, lengths._3 + 2, PAD_STR), rightPad(val2, lengths._2 + 2, PAD_STR));
    }

    /// Collects properties that represent persistent, non-null, non-proxy, and non-self-referenced activatable properties for `entity`.
    ///
    @SuppressWarnings("unchecked")
    private List<? extends MetaProperty<? extends AbstractEntity<?>>> activatableNotNullNotProxyProperties(final ActivatableAbstractEntity<?> entity) {
        return entity.nonProxiedProperties()
                .filter(mp -> {
                    final Object value = mp.getValue();
                    if (value == null || !isEntityType(mp.getType())) {
                        return false;
                    }
                    final var propValue = (AbstractEntity<?>) value;
                    if (propValue.isIdOnlyProxy() || entity.equals(propValue)) {
                        return false;
                    }

                    final var entityType = (Class<? extends ActivatableAbstractEntity<?>>) entity.getType();
                    final var propName = mp.getName();
                    return isActivatableReferenceWithoutPreconditions(entityType, propName, propValue, coFinder);
                })
                .map(mp -> (MetaProperty<? extends AbstractEntity<?>>) mp)
                .toList();
    }

}
