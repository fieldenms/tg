package ua.com.fielden.platform.entity.validation.custom;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isNotSpecialActivatableToBeSkipped;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;

/**
 * Identifies activatable dependencies for an entity of the specified type.
 *
 * @author TG Team
 *
 */
public class DomainEntityDependencies {
    public final Class<? extends AbstractEntity<?>> entityType;
    public final boolean activatable;
    private final Set<Class<? extends ActivatableAbstractEntity<?>>> automaticallyDeactivatedDependencies = new HashSet<>();
    private final Set<DomainEntityDependency> dependencies = new HashSet<>();

    public DomainEntityDependencies(final Class<? extends AbstractEntity<?>> entityType) {
        this.entityType = entityType;
        this.activatable = isActivatableEntityType(entityType);

        if (activatable && entityType.isAnnotationPresent(DeactivatableDependencies.class)) {
            final DeactivatableDependencies annotation = ((Class<? extends ActivatableAbstractEntity<?>>) entityType).getAnnotation(DeactivatableDependencies.class);
            automaticallyDeactivatedDependencies.addAll(asList(annotation.value()));
        }
    }

    public Set<DomainEntityDependency> getDependencies() {
        return dependencies;
    }

    public Set<DomainEntityDependency> getActivatableDependencies() {
        return dependencies.stream().filter(d -> d.shouldBeCheckedDuringDeactivation && !(automaticallyDeactivatedDependencies.contains(d.entityType) && d.belongsToEntityKey)).collect(toSet());
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
        public final String propTitle;
        public final boolean shouldBeCheckedDuringDeactivation;
        public final boolean belongsToEntityKey;

        private DomainEntityDependency(final Class<? extends AbstractEntity<?>> entityType, final Field propField) {
            this.entityType = entityType;
            this.entityTitle = getEntityTitleAndDesc(entityType).getKey();
            this.propName = propField.getName();
            this.propTitle = getTitleAndDesc(propName, entityType).getKey();
            this.belongsToEntityKey = getKeyMembers(entityType).contains(propField);
            
            final SkipEntityExistsValidation seevAnnotation = propField.getAnnotation(SkipEntityExistsValidation.class);
            final boolean skipActiveOnly = seevAnnotation != null ? seevAnnotation.skipActiveOnly() : false;        
            this.shouldBeCheckedDuringDeactivation = isActivatableEntityType(entityType) && isNotSpecialActivatableToBeSkipped(propField) && !skipActiveOnly;
        }
        
        @Override
        public String toString() {
            return format("Entity [%s] has dependency in entity [%s] as property [%s], checked during deactivation [%s], belongs to entity key [%s].", DomainEntityDependencies.this.entityType.getName(), entityType.getName(), propName, shouldBeCheckedDuringDeactivation, belongsToEntityKey);
        }
    }
}