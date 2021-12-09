package ua.com.fielden.platform.utils;

import static java.util.Arrays.asList;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.utils.EntityUtils.isDateOnly;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere1;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.filtering.ActivatableEntityHasPropertyOfTypePredicate;
import ua.com.fielden.platform.reflection.filtering.TypeFilter;

public final class Validators {
    private Validators() {
    }


    /**
     * Identifies whether the provided entity overlaps with any of the existing entities. <em>fromDate</em> and <em>toDate</em> values are extracted from the <em>entity</em>.
     *
     * @param entity
     *            -- entity which should be tested for overlapping.
     * @param co
     *            -- entity companion used for executing the query.
     * @param fromDateProperty
     *            -- name of the entity property serving as the from date.
     * @param toDateProperty
     *            -- name of the entity property serving as the to date.
     * @param matchProperties
     *            -- names of properties used for matching entity by, for example person in case of Timesheet entity. Could be empty, which would mean that overlapping happens
     *            against all existing entities of this type.
     * @return <i>true</i> if there is at least one overlapped entity.
     */
    public static <T extends AbstractEntity<?>> boolean overlaps(//
    /*    */final T entity, //
            final IEntityDao<T> co, //
            final String fromDateProperty, //
            final String toDateProperty, //
            final String... matchProperties) {
        final EntityResultQueryModel<T> model = composeOverlappingCheckQueryModel(entity, fromDateProperty, toDateProperty, matchProperties);
        return co.exists(model);
    }

    /**
     * Identifies whether the provided entity overlaps with any of the existing entities.
     * <em>fromDateValue</em> and <em>toDateValue</em> are passed in as separate parameters -
     * this is especially useful if overlap check is performed as part of the validation of fromDateProperty or toDateProperty.
     *
     * @param entity
     *            -- entity which should be tested for overlapping.
     * @param co
     *            -- entity companion used for executing the query.
     * @param fromDateProperty
     *            -- name of the entity property serving as the from date.
     * @param toDateProperty
     *            -- name of the entity property serving as the to date.
     * @param fromDateValue
     *            -- value serving as the from date.
     * @param toDateValue
     *            -- value serving as the to date.
     * @param matchProperties
     *            -- names of properties used for matching entity by, for example person in case of Timesheet entity. Could be empty, which would mean that overlapping happens
     *            against all existing entities of this type.
     * @return <i>true</i> if there is at least one overlapped entity.
     */
    public static <T extends AbstractEntity<?>> boolean overlaps(//
    /*    */final T entity, //
            final IEntityDao<T> co, //
            final String fromDateProperty, //
            final String toDateProperty, //
            final Date fromDateValue, //
            final Date toDateValue, //
            final String... matchProperties) {
        final EntityResultQueryModel<T> model = composeOverlappingCheckQueryModel(entity, fromDateProperty, toDateProperty, fromDateValue, toDateValue, matchProperties);
        return co.exists(model);
    }

    /**
     * Returns the first overlapping entity if any, <code>null</code> value otherwise.
     *
     * @param entity
     * @param fetchModel
     *            -- the fetch model is used to initialise the overlapped entity
     * @param co
     * @param fromDateProperty
     * @param toDateProperty
     * @param matchProperties
     * @return
     */
    public static <T extends AbstractEntity<?>> T findFirstOverlapping(//
    /*    */final T entity, //
            final fetch<T> fetchModel,//
            final IEntityDao<T> co, //
            final String fromDateProperty, //
            final String toDateProperty, //
            final String... matchProperties) {
        final EntityResultQueryModel<T> model = composeOverlappingCheckQueryModel(entity, fromDateProperty, toDateProperty, matchProperties);
        final OrderingModel orderBy = orderBy().prop(fromDateProperty).asc().model();

        final fetch<T> runFetch = fetchModel != null ? fetchModel : fetch((Class<T>) entity.getType());
        final List<T> result = co.getFirstEntities(from(model).with(runFetch).with(orderBy).model(), 1);
        return !result.isEmpty() ? result.get(0) : null;
    }

    /**
     * Returns the first overlapping entity if any, <code>null</code> value otherwise. Default entity query fetch model is used for initialising the overlapped entity.
     *
     * @param entity
     * @param co
     * @param fromDateProperty
     * @param toDateProperty
     * @param matchProperties
     * @return
     */
    public static <T extends AbstractEntity<?>> T findFirstOverlapping(//
    /*    */final T entity, //
            final IEntityDao<T> co, //
            final String fromDateProperty, //
            final String toDateProperty, //
            final String... matchProperties) {
        return findFirstOverlapping(entity, null, co, fromDateProperty, toDateProperty, matchProperties);
    }

    /**
     * Analyses all entity types on the subject of having an active dependency on the specified value. This implementation assumes that all involved entities have boolean property
     * "active".
     *
     * @param entityTypes
     * @param entityType
     * @return
     */
    public static <T extends AbstractEntity<?>> long countActiveDependencies(final List<Class<? extends AbstractEntity<?>>> entityTypes, final T entity, final IEntityAggregatesOperations coAggregate) {
        // there should be exactly 0 active and persisted dependencies to not yet persisted entity
        if (!entity.isPersisted()) {
            return 0;
        }

        // otherwise let's count
        final Class<? extends AbstractEntity<?>> entityType = entity.getType();
        // first analyse the domain tree
        final List<Class<? extends AbstractEntity<?>>> relevantTypes = TypeFilter.filter(entityTypes, new ActivatableEntityHasPropertyOfTypePredicate(entityType));
        // if there are no dependent entity types then there is no reason to check the actual persisted entity instances
        if (relevantTypes.isEmpty()) {
            return 0;
        } else {
            // otherwise, need to compose a complex query that would count active dependencies
            // one important thing here is not to count self-references...
            final Iterator<Class<? extends AbstractEntity<?>>> iter = relevantTypes.iterator();
            final Class<? extends AbstractEntity<?>> firstDependentEntityType = iter.next();

            // let's start with making counting query for the first dependent entity type -- there is at least one if this code was reached
            // first obtain list of properties of the type that matches the entity type of interest
            final List<Field> propsForFirstEntityType = Finder.findPropertiesOfSpecifiedType(firstDependentEntityType, entityType, MapTo.class);
            // then actually make a counting query based on the obtained list
            IStandAloneExprOperationAndClose expressionModelInProgress = expr().model(mkQueryToCountReferencesInType(firstDependentEntityType, propsForFirstEntityType));

            // need to do the same operation with other dependent types in case there are more than one
            while (iter.hasNext()) {
                final Class<? extends AbstractEntity<?>> nextDependentEntityType = iter.next();
                final List<Field> propsForNextEntityType = Finder.findPropertiesOfSpecifiedType(nextDependentEntityType, entityType, MapTo.class);
                expressionModelInProgress = expressionModelInProgress.add().model(mkQueryToCountReferencesInType(nextDependentEntityType, propsForNextEntityType));
            }

            final AggregatedResultQueryModel query = select(entityType).where().prop("id").eq().val(entity).yield().expr(expressionModelInProgress.model()).as("kount").modelAsAggregate();
            final QueryExecutionModel<EntityAggregates, AggregatedResultQueryModel> qem = from(query).model();

            final Number kount = (Number) coAggregate.getEntity(qem).get("kount");
            return kount.longValue();
        }
    }

    /**
     * Finds active deactivatable dependencies for the specified entity.
     *
     * @param entity
     * @return
     */
    public static <T extends ActivatableAbstractEntity<?>> List<? extends ActivatableAbstractEntity<?>> findActiveDeactivatableDependencies(final T entity, final ICompanionObjectFinder coFinder) {
        final List<? extends ActivatableAbstractEntity<?>> result = new ArrayList<>();
        final DeactivatableDependencies annotation = entity.getType().getAnnotation(DeactivatableDependencies.class);
        if (annotation == null) {
            return result;
        }
        final Class<? extends AbstractEntity<?>> entityType = entity.getType();
        final List<Class<? extends ActivatableAbstractEntity<?>>> relevantTypes = asList(annotation.value());

        for (final Class<? extends ActivatableAbstractEntity<?>> dependentType : relevantTypes) {
            // only those properties that are key members are of interest
            final List<Field> props = new ArrayList<>();
            final List<Field> keyMembers = Finder.getKeyMembers(dependentType);
            for(final Field keyMember : keyMembers) {
                if (entityType.isAssignableFrom(keyMember.getType())) {
                    props.add(keyMember);
                }
            }
            if (!props.isEmpty()) { // most likely this means an invalid dependency
                IWhere1<? extends ActivatableAbstractEntity<?>> inProgress = select(dependentType).where().prop(ActivatableAbstractEntity.ACTIVE).eq().val(true).and().begin();
                for (int index  = 0; index < props.size() - 1; index++) {
                    final String propName = props.get(index).getName();
                    inProgress = inProgress.prop(propName).eq().val(entity).or();
                }
                final String propName = props.get(props.size() - 1).getName();
                final EntityResultQueryModel<? extends ActivatableAbstractEntity<?>> query = inProgress.prop(propName).eq().val(entity).end().model();

                final IEntityDao co = coFinder.find(dependentType, false);
                result.addAll(co.getAllEntities(from(query).with(co.getFetchProvider().fetchModel()).model()));
            }
        }
        return result;
    }

    /**
     * Makes an EQL model that counts all instances of the specified entity <code>entityType</code> that contain references to the entity of interest that are expressed as
     * properties <code>props</code>.
     *
     * @param entityType
     * @param props
     * @return
     */
    private static <T extends AbstractEntity<?>> PrimitiveResultQueryModel mkQueryToCountReferencesInType(
            final Class<? extends AbstractEntity<?>> entityType, // property owner
            final List<Field> props // properties of the owning entity
    ) {
        final Iterator<Field> iter = props.iterator();
        if (props.size() == 1) {
            final Field firstProp = iter.next();
            final ICompoundCondition0<? extends AbstractEntity<?>> cond =
                    select(entityType).
                            where().
                            prop("id").ne().extProp("id").and(). // this is to prevent counting self-references, relies on throughout ID
                            prop("active").eq().val(true).and().
                            prop(firstProp.getName()).eq().extProp("id");

            return cond.yield().countAll().modelAsPrimitive();

        } else {
            // need to add conditions to cover all properties of the referenced type using OR operation in case there are more than one
            final Field firstProp = iter.next();
            IWhere1<? extends AbstractEntity<?>> where =
                    select(entityType).
                            where().
                            prop("id").ne().extProp("id").and(). // this is to prevent counting self-references, relies on throughout ID
                            prop("active").eq().val(true).and().
                            begin().
                                prop(firstProp.getName()).eq().extProp("id").or();


            ICompoundCondition0<? extends AbstractEntity<?>> cond = null;
            do {
                final Field prop = iter.next();
                if (iter.hasNext()) {
                    where = where.prop(prop.getName()).eq().extProp("id").or();
                } else {
                    cond = where.prop(prop.getName()).eq().extProp("id").end();
                }

            } while (iter.hasNext());

            // yield count
            return cond.yield().countAll().modelAsPrimitive();
        }
    }

    /**
     * A helper method, which produces query model for overlapping validation. <em>fromDate</em> and <em>toDate</em> values are extracted from the <em>entity</em>.
     *
     * @param entity
     * @param fromDateProperty
     * @param toDateProperty
     * @param matchProperties
     * @return
     */
    private static <T extends AbstractEntity<?>> EntityResultQueryModel<T> composeOverlappingCheckQueryModel(final T entity, final String fromDateProperty, final String toDateProperty, final String... matchProperties) {
        // check preconditions
        final Date fromDateValue = entity.get(fromDateProperty);
        if (fromDateValue == null) {
            throw new IllegalArgumentException("Property \"" + fromDateProperty + "\" should have a value.");
        }

        return composeOverlappingCheckQueryModel(entity, fromDateProperty, toDateProperty, fromDateValue, entity.get(toDateProperty), matchProperties);
    }


    /**
     * A helper method, which produces query model for overlapping validation. <em>fromDateValue</em> and <em>toDateValue</em> are passed in as separate parameters.
     *
     * @param entity
     * @param fromDateProperty
     * @param toDateProperty
     * @param fromDateValue
     * @param toDateValue
     * @param matchProperties
     * @return
     */
    private static <T extends AbstractEntity<?>> EntityResultQueryModel<T> composeOverlappingCheckQueryModel(
            final T entity,
            final String fromDateProperty,
            final String toDateProperty,
            final Date fromDateValue,
            final Date toDateValue,
            final String... matchProperties) {
        // check preconditions
        if (fromDateValue == null) {
            throw new IllegalArgumentException("fromDateValue should not be null.");
        }

        /////////////// start query composition ///////////////////
        // add matching conditions
        final IWhere0<T> where = select((Class<T>) entity.getType()).where();
        // if the entity being checked for overlapping has already been persisted it would most likely overlap itself
        // thus, need to exclude entity itself
        ICompoundCondition0<T> cc = where.prop("id").ne().iVal(entity.getId()); // should be ignore if id is null
        // now the matching properties
        for (final String matchProperty : matchProperties) {
            final Object value = entity.get(matchProperty);
            if (value == null) {
                throw new IllegalArgumentException("Entity \"" + entity + "\" should have a value for matching property \"" + matchProperty + "\".");
            }
            cc = cc.and().prop(matchProperty).eq().val(value);
        }

        // add overlapping test conditions
        // invariant: fromDateProperty always has a value.
        // there could be several cases:
        // 1. toDateProperty has a value (closed period).
        // 2. toDateProperty has no value (open period).

        // Condition for the end of the period for potentially overlapped existing entities
        condition_1: if (isDateOnly(entity.getType(), toDateProperty)) {
        //If to date property is annotated with DateOnly annotation then it should be greater or equal than fromDateValue
        /*        */cc = cc.and().
        /*              */begin().
        /*                  */prop(toDateProperty).isNull()./* the end of the potentially overlapped entity is OPEN and thus is after the fromDateProperty value of the entity under test */
        /*                  */or().
        /*                  */dateOf().prop(toDateProperty).ge().dateOf().val(fromDateValue)./* the end of the potentially overlapped entity is AFTER the fromDateValue */
        /*              */end();
        } else {
        //If to date property is not annotated with DateOnly annotation then it should be greater than fromDateValue
        /*        */cc = cc.and().
        /*              */begin().
        /*                  */prop(toDateProperty).isNull()./* the end of the potentially overlapped entity is OPEN and thus is after the fromDateProperty value of the entity under test */
        /*                  */or().
        /*                  */prop(toDateProperty).gt().val(fromDateValue)./* the end of the potentially overlapped entity is AFTER the fromDateValue */
        /*              */end();
        }

        // Condition for the beginning of the period for potentially overlapped existing entities
        // Open ended period does not require any condition, because any toDateProperty of the potentially overlapped entity would be BEFORE such an end.
        // Thus, if the condition_1 holds then there is an overlap and there is no reason to add any conditions
        // If condition_1 does not hold then there is no overlap and there is no reason to add any conditions too.
        // Closed ended period does require an additional condition to ensure the beginning of the potentially overlapped entity if BEFORE that end value of the entity under test
        condition_2: if (toDateValue != null) {
            //If from date property is annotated with DateOnly annotation then it should be less or equal than toDateValue
            if (isDateOnly(entity.getType(), fromDateProperty)) {
                cc = cc.and().//
                /*    */begin().//
                /*        */dateOf().prop(fromDateProperty).le().dateOf().val(toDateValue)./* the beginning of the potentially overlapped entity is BEFORE the toDateValue  */
                /*    */end();
           //If from date property is not annotated with DateOnly annotation then it should be less than toDateValue
            } else {
                cc = cc.and().//
                /*    */begin().//
                /*        */prop(fromDateProperty).lt().val(toDateValue)./* the beginning of the potentially overlapped entity is BEFORE the toDateValue  */
                /*    */end();
            }
        }

        // make a model with result ordered by fromDateProperty, which is only required if at some stage it would be used for selecting overlapped entities.
        return cc.model();
    }
}
