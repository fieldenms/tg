package ua.com.fielden.platform.entity.validation.custom;

import com.google.common.collect.ImmutableSet;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isNotSpecialActivatableToBeSkipped;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;

/**
 * Identifies activatable dependencies for an entity of the specified type.
 *
 * @author TG Team
 *
 */
public class DomainEntityDependencies {
    public final Class<? extends AbstractEntity<?>> entityType;
    public final boolean activatable;
    private final Set<Class<? extends ActivatableAbstractEntity<?>>> automaticallyDeactivatedDependencies;
    private final Set<DomainEntityDependency> dependencies = new HashSet<>();

    public DomainEntityDependencies(final Class<? extends AbstractEntity<?>> entityType) {
        this.entityType = entityType;
        this.activatable = isActivatableEntityType(entityType);

        if (activatable && entityType.isAnnotationPresent(DeactivatableDependencies.class)) {
            final DeactivatableDependencies annotation = ((Class<? extends ActivatableAbstractEntity<?>>) entityType).getAnnotation(DeactivatableDependencies.class);
            automaticallyDeactivatedDependencies = ImmutableSet.copyOf(annotation.value());
        }
        else {
            automaticallyDeactivatedDependencies = ImmutableSet.of();
        }
    }

    /**
     * Returns a subset of activatable dependencies.
     */
    public Set<DomainEntityDependency> getActivatableDependencies() {
        return dependencies.stream()
               .filter(dep -> dep.shouldBeCheckedDuringDeactivation && !(automaticallyDeactivatedDependencies.contains(dep.entityType) && dep.belongsToEntityKey))
               .collect(toSet());
    }

    /**
     * Returns a subset of deactivatable dependencies.
     */
    public Set<DomainEntityDependency> getDeactivatableDependencies() {
        return dependencies.stream()
                .filter(dep -> dep.shouldBeCheckedDuringDeactivation && automaticallyDeactivatedDependencies.contains(dep.entityType))
                .collect(toSet());
    }

    public void addDependency(final Class<? extends AbstractEntity<?>> entityType, final Field propField) {
        dependencies.add(new DomainEntityDependency(entityType, propField));
    }

    /**
     * A convenient struct to represent a single property that is an activatable dependency.
     */
    public class DomainEntityDependency {
        public final Class<? extends AbstractEntity<?>> entityType;
        public final String entityTitle;
        public final String propName;
        public final String propPath;
        public final String propTitle;
        public final boolean shouldBeCheckedDuringDeactivation;
        public final boolean belongsToEntityKey;

        private DomainEntityDependency(
                Class<? extends AbstractEntity<?>> entityType,
                String entityTitle,
                String propName,
                String propPath,
                String propTitle,
                boolean shouldBeCheckedDuringDeactivation,
                boolean belongsToEntityKey)
        {
            this.entityType = entityType;
            this.entityTitle = entityTitle;
            this.propName = propName;
            this.propPath = propPath;
            this.propTitle = propTitle;
            this.shouldBeCheckedDuringDeactivation = shouldBeCheckedDuringDeactivation;
            this.belongsToEntityKey = belongsToEntityKey;


        }

        private DomainEntityDependency(final Class<? extends AbstractEntity<?>> entityType, final Field propField) {
            this(entityType,
                 getEntityTitleAndDesc(entityType).getKey(),
                 propField.getName(),
                 propField.getName(),
                 getTitleAndDesc(propField.getName(), entityType).getKey(),
                 checkDuringDeactivation(entityType, propField),
                 getKeyMembers(entityType).contains(propField));

        }

        public DomainEntityDependency updatePropPath(final CharSequence propPathSuffix) {
            return  new DomainEntityDependency(entityType, entityTitle, propName, propPath + "." + propPathSuffix, propTitle, belongsToEntityKey, shouldBeCheckedDuringDeactivation);
        }

        private static boolean checkDuringDeactivation(final Class<? extends AbstractEntity<?>> entityType, final Field propField) {
            final SkipEntityExistsValidation seevAnnotation = propField.getAnnotation(SkipEntityExistsValidation.class);
            final boolean skipActiveOnly = seevAnnotation != null && seevAnnotation.skipActiveOnly();
            return isActivatableEntityType(entityType) && isNotSpecialActivatableToBeSkipped(propField) && !skipActiveOnly;

        }

        public static final String INFO_ENTITY_DEPENDENCIES = "Entity [%s] has dependency in entity [%s] as property [%s] (full path [%s], checked during deactivation [%s], belongs to entity key [%s].";
        @Override
        public String toString() {
            return INFO_ENTITY_DEPENDENCIES.formatted(entityType.getName(), entityType.getName(), propName, propPath, shouldBeCheckedDuringDeactivation, belongsToEntityKey);
        }

    }
}