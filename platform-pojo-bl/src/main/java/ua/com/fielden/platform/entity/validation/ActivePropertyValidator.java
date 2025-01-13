package ua.com.fielden.platform.entity.validation;

import com.google.inject.Inject;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.tuples.T3;
import ua.com.fielden.platform.utils.EntityUtils;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.lang.Math.max;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.apache.commons.lang3.StringUtils.rightPad;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
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
 */
public class ActivePropertyValidator extends AbstractBeforeChangeEventHandler<Boolean> {
    private static final Logger LOGGER = getLogger(ActivePropertyValidator.class);
    private static final String PAD_STR = "\u00A0";
    public static final String ERR_SHORT_ENTITY_HAS_ACTIVE_DEPENDENCIES = "%s [%s] has %s active %s.";
    public static final String ERR_ENTITY_HAS_ACTIVE_DEPENDENCIES = "%s [%s] has %s active %s:%n%n<br><br>%s<hr>%n<br>%s";
    public static final String INFO_DEPENDENCY = "<tt>%s%s\u00A0%s</tt>";
    public static final String ERR_INACTIVE_REFERENCES = "Property [%s] in %s [%s] references inactive %s [%s].";

    public static final Predicate<Class<? extends AbstractEntity<?>>> PREDICATE_ACTIVATABLE_ENTITY_TYPE = EntityUtils::isActivatableEntityType;
    public static final Predicate<Class<? extends AbstractEntity<?>>> PREDICATE_ACTIVATABLE_AND_PERSISTENT_ENTITY_TYPE = PREDICATE_ACTIVATABLE_ENTITY_TYPE.and(EntityUtils::isPersistentEntityType);


    private final IApplicationDomainProvider applicationDomainProvider;

    @Inject
    public ActivePropertyValidator(final ICompanionObjectFinder coFinder, final IApplicationDomainProvider applicationDomainProvider) {
        this.applicationDomainProvider = applicationDomainProvider;
    }

    @Override
    public Result handle(final MetaProperty<Boolean> property, final Boolean newValue, final Set<Annotation> mutatorAnnotations) {
        final ActivatableAbstractEntity<?> entity = property.getEntity();
        // A persisted entity is being deactivated, but it may still be referenced.
        if (!newValue && entity.isPersisted()) {
            // Consider only activatable persistent entities as dependencies.
            // Hypothetically speaking, every activatable entity is also persistent.
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
            final var activatableProps = collectActivatableNotNullNotProxyProperties(entity);

            // Need to check if already referenced activatables are active and thus may be referenced by this entity, which is being activated.
            for (final MetaProperty<? extends ActivatableAbstractEntity<?>> prop : activatableProps) {
                final ActivatableAbstractEntity<?> value = prop.getValue();
                if (!value.isActive()) {
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

    private String mkErrorMsg(final ActivatableAbstractEntity<?> entity, final int count, final List<EntityAggregates> dependencies) {
        final var lengths = dependencies.stream()
                .map(dep -> t3(dep.get(ENTITY_TYPE_TITLE).toString().length(), dep.get(DEPENDENT_PROP_TITLE).toString().length(), dep.get(COUNT).toString().length()))
                .reduce(t3(0, 0, 0), (accum, val) -> t3(max(accum._1, val._1), max(accum._2, val._2), max(accum._3, val._3)), (v1, v2) -> {throw failure("Should not happen");}); 
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
