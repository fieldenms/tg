package ua.com.fielden.platform.entity.validation.custom;

import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.reflection.AnnotationReflector.isAnnotationPresent;
import static ua.com.fielden.platform.reflection.Finder.getKeyMembers;
import static ua.com.fielden.platform.utils.EntityUtils.getRealProperties;
import static ua.com.fielden.platform.utils.EntityUtils.isOneToOne;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.validation.custom.DomainEntityDependencies.DomainEntityDependency;

public class DomainEntitiesDependenciesUtils {
    public static final String PARAM = "ENTITY_VALUE";
    public static final String ENTITY_TYPE_NAME = "entity_type_name";
    public static final String ENTITY_TYPE_TITLE = "entity_type_title";
    public static final String DEPENDENT_PROP_NAME = "dependent_prop_name";
    public static final String DEPENDENT_PROP_TITLE = "dependent_prop_title";
    public static final String COUNT = "KOUNT";
    
    public static AggregatedResultQueryModel generateQuery(final Set<DomainEntityDependency> dependencies, final boolean deactivationOnly) {
        
        final List<AggregatedResultQueryModel> models = new ArrayList<>();
        
        for (final DomainEntityDependency dependency : dependencies) { //
            final ConditionModel firstCondition = cond().prop(dependency.propName).eq().param(PARAM).model();
            final ConditionModel finalCondition =  deactivationOnly ? cond().condition(firstCondition).and().prop(ACTIVE).eq().val(true).model() : firstCondition;

            models.add(select(dependency.entityType). //
                    where().condition(finalCondition). //
                    // wrapped into CASE-WHEN just to make H2 happy.
                    yield().caseWhen().val(1).eq().val(1).then().val(dependency.entityType.getName()).endAsStr(255).as(ENTITY_TYPE_NAME). //
                    yield().caseWhen().val(1).eq().val(1).then().val(dependency.entityTitle).endAsStr(255).as(ENTITY_TYPE_TITLE). //
                    yield().caseWhen().val(1).eq().val(1).then().val(dependency.propName).endAsStr(255).as(DEPENDENT_PROP_NAME). //
                    yield().caseWhen().val(1).eq().val(1).then().val(dependency.propTitle).endAsStr(255).as(DEPENDENT_PROP_TITLE). //
                    modelAsAggregate());
        }

        return select(models.toArray(new AggregatedResultQueryModel[] {})). //
                groupBy().prop(ENTITY_TYPE_NAME). //
                groupBy().prop(DEPENDENT_PROP_NAME). //
                groupBy().prop(ENTITY_TYPE_TITLE). //
                groupBy().prop(DEPENDENT_PROP_TITLE). //
                yield().prop(ENTITY_TYPE_TITLE).as(ENTITY_TYPE_TITLE). //
                yield().prop(DEPENDENT_PROP_TITLE).as(DEPENDENT_PROP_TITLE). //
                yield().countAll().as(COUNT). //
                modelAsAggregate();
    }

    public static Map<Class<? extends AbstractEntity<?>>, DomainEntityDependencies> getEntityDependantsMap(final Collection<Class<? extends AbstractEntity<?>>> domainEntityTypes) {
        final Map<Class<? extends AbstractEntity<?>>, DomainEntityDependencies> map = new HashMap<>();

        for (final Class<? extends AbstractEntity<?>> entType : domainEntityTypes) {
            if (isPersistedEntityType(entType)) {
                for (final Field field : getRealProperties(entType)) {
                    if (isAnnotationPresent(field, MapTo.class) && isPersistedEntityType(field.getType())) {
                        if (!map.containsKey(field.getType())) {
                            map.put(((Class<? extends AbstractEntity<?>>) field.getType()), new DomainEntityDependencies(((Class<? extends AbstractEntity<?>>) field.getType())));
                        }
                        map.get(field.getType()).addDependency(new DomainEntityDependency(entType, field));
                    }
                }

                if (isOneToOne(entType)) {
                    final Class<? extends Comparable<?>> keyType = getKeyType(entType);
                    if (!map.containsKey(keyType)) {
                        map.put(((Class<? extends AbstractEntity<?>>) keyType), new DomainEntityDependencies(((Class<? extends AbstractEntity<?>>) keyType)));
                    }
                    map.get(keyType).addDependency(new DomainEntityDependency(entType, getKeyMembers(entType).get(0)));
                }
            }
        }

        return map;
    }
}