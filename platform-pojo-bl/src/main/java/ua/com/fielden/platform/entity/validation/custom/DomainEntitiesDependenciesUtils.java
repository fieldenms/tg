package ua.com.fielden.platform.entity.validation.custom;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.validation.custom.DomainEntityDependencies.DomainEntityDependency;
import ua.com.fielden.platform.utils.EntityUtils;

import java.lang.reflect.Field;
import java.util.*;

import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresent;
import static ua.com.fielden.platform.reflection.Finder.findRealProperties;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

public class DomainEntitiesDependenciesUtils {
    public static final String PARAM = "ENTITY_VALUE";
    public static final String ENTITY_TYPE_NAME = "entity_type_name";
    public static final String ENTITY_TYPE_TITLE = "entity_type_title";
    public static final String DEPENDENT_PROP_NAME = "dependent_prop_name";
    public static final String DEPENDENT_PROP_TITLE = "dependent_prop_title";
    public static final String COUNT = "KOUNT";
    
    public static AggregatedResultQueryModel generateQuery(final Set<DomainEntityDependency> dependencies, final boolean deactivationOnly) {
        
        final List<AggregatedResultQueryModel> models = new ArrayList<>();
        
        for (final DomainEntityDependency dependency : dependencies) {
            final ConditionModel firstCondition = cond().prop(dependency.propName).eq().param(PARAM).model();
            final ConditionModel finalCondition =  deactivationOnly ? cond().condition(firstCondition).and().prop(ACTIVE).eq().val(true).model() : firstCondition;

            models.add(select(dependency.entityType).
                    where().condition(finalCondition).
                    yield().val(dependency.entityType.getName()).as(ENTITY_TYPE_NAME).
                    yield().val(dependency.entityTitle).as(ENTITY_TYPE_TITLE).
                    yield().val(dependency.propName).as(DEPENDENT_PROP_NAME).
                    yield().val(dependency.propTitle).as(DEPENDENT_PROP_TITLE).
                    modelAsAggregate());
        }

        return select(models.toArray(new AggregatedResultQueryModel[] {})).
                groupBy().prop(ENTITY_TYPE_NAME).
                groupBy().prop(DEPENDENT_PROP_NAME).
                groupBy().prop(ENTITY_TYPE_TITLE).
                groupBy().prop(DEPENDENT_PROP_TITLE).
                yield().prop(ENTITY_TYPE_TITLE).as(ENTITY_TYPE_TITLE).
                yield().prop(DEPENDENT_PROP_TITLE).as(DEPENDENT_PROP_TITLE).
                yield().countAll().as(COUNT).
                modelAsAggregate();
    }

    public static Map<Class<? extends AbstractEntity<?>>, DomainEntityDependencies> getEntityDependantsMap(final Collection<Class<? extends AbstractEntity<?>>> domainEntityTypes) {
        final var map = new HashMap<Class<? extends AbstractEntity<?>>, DomainEntityDependencies>();
        domainEntityTypes.stream().filter(EntityUtils::isPersistedEntityType)
        .forEach(entType -> {
            for (final Field field : findRealProperties(entType)) {
                if (isAnnotationPresent(field, MapTo.class) && isPersistedEntityType(field.getType())) {
                    if (!map.containsKey(field.getType())) {
                        final var propType = (Class<? extends AbstractEntity<?>>) field.getType();
                        map.put(propType, new DomainEntityDependencies(propType));
                    }
                    map.get(field.getType()).addDependency(entType, field);
                }
            }

            if (isOneToOne(entType)) {
                final Class<? extends Comparable<?>> keyType = getKeyType(entType);
                if (!map.containsKey(keyType)) {
                    final var keyPropType = (Class<? extends AbstractEntity<?>>) keyType;
                    map.put(keyPropType, new DomainEntityDependencies(keyPropType));
                }
                map.get(keyType).addDependency(entType, getKeyMembers(entType).getFirst());
            }
        });

        return map;
    }
}