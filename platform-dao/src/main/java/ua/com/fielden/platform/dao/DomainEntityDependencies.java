package ua.com.fielden.platform.dao;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.reflection.ActivatableEntityRetrospectionHelper.isNotSpecialActivatableToBeSkipped;
import static ua.com.fielden.platform.utils.EntityUtils.isActivatableEntityType;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;

public class DomainEntityDependencies {
    private final Class<? extends AbstractEntity<?>> entityType;
    private final boolean activatable;
    private final Set<Class<? extends ActivatableAbstractEntity<?>>> automaticallyDeactivatedDependencies = new HashSet<>();
    private final Set<DomainEntityDependency> dependencies = new HashSet<>();

    public Set<DomainEntityDependency> getDependencies() {
        return dependencies;
    }

    public Set<DomainEntityDependency> getActivatableDependencies() {
        return dependencies.stream().filter(d -> d.shouldBeCheckedDuringDeactivation() && !automaticallyDeactivatedDependencies.contains(d.getEntityType())).collect(toSet());
    }

    public DomainEntityDependencies(final Class<? extends AbstractEntity<?>> javaType) {
        this.entityType = javaType;
        this.activatable = isActivatableEntityType(javaType);

        if (activatable && javaType.isAnnotationPresent(DeactivatableDependencies.class)) {
            final DeactivatableDependencies annotation = ((Class<? extends ActivatableAbstractEntity<?>>) javaType).getAnnotation(DeactivatableDependencies.class);
            automaticallyDeactivatedDependencies.addAll(asList(annotation.value()));
        }
    }

    public Class<? extends AbstractEntity<?>> getEntityType() {
        return entityType;
    }

    public boolean isActivatable() {
        return activatable;
    }

    public Set<Class<? extends ActivatableAbstractEntity<?>>> getDeactivatableDependencies() {
        return automaticallyDeactivatedDependencies;
    }

    public void addDependency(final DomainEntityDependency dependency) {
        dependencies.add(dependency);
    }

    public static class DomainEntityDependency {
        private final Class<? extends AbstractEntity<?>> entityType;
        private final String entityTitle;
        private final String propName;
        private final String propTitle;
        private final boolean shouldBeCheckedDuringDeactivation;

        public DomainEntityDependency(final Class<? extends AbstractEntity<?>> javaType, final Field propField) {
            this.entityType = javaType;
            this.entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(javaType).getKey();
            this.propName = propField.getName();
            this.propTitle = TitlesDescsGetter.getTitleAndDesc(propName, entityType).getKey();
            
            final SkipEntityExistsValidation seevAnnotation = propField.getAnnotation(SkipEntityExistsValidation.class);
            final boolean skipActiveOnly = seevAnnotation != null ? seevAnnotation.skipActiveOnly() : false;        
            this.shouldBeCheckedDuringDeactivation = isActivatableEntityType(javaType) && isNotSpecialActivatableToBeSkipped(propField) && !skipActiveOnly;

        }

        public Class<? extends AbstractEntity<?>> getEntityType() {
            return entityType;
        }

        public String getPropName() {
            return propName;
        }

        public boolean shouldBeCheckedDuringDeactivation() {
            return shouldBeCheckedDuringDeactivation;
        }
        
        public String getEntityTitle() {
            return entityTitle;
        }
        
        public String getPropTitle() {
            return propTitle;
        }
    }
}