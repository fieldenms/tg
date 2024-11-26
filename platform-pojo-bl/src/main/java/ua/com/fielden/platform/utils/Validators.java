package ua.com.fielden.platform.utils;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneExprOperationAndClose;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.filtering.ActivatableEntityHasPropertyOfTypePredicate;
import ua.com.fielden.platform.reflection.filtering.TypeFilter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.utils.EntityUtils.isDateOnly;

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
     * Analyses all {@code entityTypes} on the subject of having an active dependency of {@code entity}.
     * This implementation assumes that all involved entities have boolean property {@code active}.
     */
    public static <T extends AbstractEntity<?>> long countActiveDependencies(
            final List<Class<? extends AbstractEntity<?>>> entityTypes,
            final T entity,
            final IEntityAggregatesOperations coAggregate)
    {
        // There should be exactly 0 active and persisted dependencies to not yet persisted entity
        if (!entity.isPersisted()) {
            return 0;
        }

        // Otherwise, count dependencies.
        final var entityType = entity.getType();
        // First analyse the domain tree.
        final var relevantTypes = TypeFilter.filter(entityTypes, new ActivatableEntityHasPropertyOfTypePredicate(entityType));
        // If there are no dependent entity types, then there is no reason to check the actual persisted entity instances.
        if (relevantTypes.isEmpty()) {
            return 0;
        }
        // Otherwise, need to compose a query that would count active dependencies.
        // Self-references should be excluded.
        else {
            final var iter = relevantTypes.iterator();
            final var firstDependentEntityType = iter.next();

            // Start with making the counting query for the first dependent entity type -- there is at least one such type, if this code was reached.
            // First find a list of properties matching the entity type of interest.
            final var propsForFirstEntityType = Finder.findPropertiesOfSpecifiedType(firstDependentEntityType, entityType, MapTo.class);
            // Make a counting query based on the obtained list.
            IStandAloneExprOperationAndClose expressionModelInProgress = expr().model(mkQueryToCountReferencesInType(firstDependentEntityType, propsForFirstEntityType));

            // Need to repeat the same operation with other dependent types in case there are more than one.
            while (iter.hasNext()) {
                final Class<? extends AbstractEntity<?>> nextDependentEntityType = iter.next();
                final List<Field> propsForNextEntityType = Finder.findPropertiesOfSpecifiedType(nextDependentEntityType, entityType, MapTo.class);
                expressionModelInProgress = expressionModelInProgress.add().model(mkQueryToCountReferencesInType(nextDependentEntityType, propsForNextEntityType));
            }

            final var propCount = "kount";
            final var query = select(entityType).where().prop(ID).eq().val(entity).yield().expr(expressionModelInProgress.model()).as(propCount).modelAsAggregate();
            final var qem = from(query).model();

            final Number count = coAggregate.getEntity(qem).get(propCount);
            return count.longValue();
        }
    }

    /**
     * Finds active deactivatable dependencies for {@code entity}.
     */
    public static <T extends ActivatableAbstractEntity<?>> List<? extends ActivatableAbstractEntity<?>> findActiveDeactivatableDependencies(
            final T entity,
            final ICompanionObjectFinder coFinder)
    {
        final List<? extends ActivatableAbstractEntity<?>> result = new ArrayList<>();
        final DeactivatableDependencies annotation = entity.getType().getAnnotation(DeactivatableDependencies.class);
        if (annotation == null) {
            return result;
        }
        final var entityType = entity.getType();
        final var relevantTypes = ImmutableList.copyOf(annotation.value());

        for (final var dependentType : relevantTypes) {
            // Only those properties that are key members are of interest.
            final var props = new ArrayList<Field>();
            final var keyMembers = Finder.getKeyMembers(dependentType);
            for(final var keyMember : keyMembers) {
                if (entityType.isAssignableFrom(keyMember.getType())) {
                    props.add(keyMember);
                }
            }
            if (!props.isEmpty()) { // empty props likely indicate an invalid dependency
                var partialQ = select(dependentType).where().prop(ACTIVE).eq().val(true).and().begin();
                for (int index  = 0; index < props.size() - 1; index++) {
                    final var propName = props.get(index).getName();
                    partialQ = partialQ.prop(propName).eq().val(entity).or();
                }
                final var propName = props.getLast().getName();
                final var query = partialQ.prop(propName).eq().val(entity).end().model();

                final IEntityDao co = coFinder.find(dependentType, false);
                result.addAll(co.getAllEntities(from(query).with(co.getFetchProvider().fetchModel()).model()));
            }
        }
        return result;
    }

    /**
     * Makes an EQL query to count all instances of {@code entityType} that contain references to an entity of interest,
     * expressed as properties {@code props} of that entity type.
     *
     * @param entityType  the owner of properties.
     * @param props  properties in {@code entityType} of the same entity type (i.e., "entity of interest").
     * @return  a primitive EQL query model to count dependencies.
     */
    private static <T extends AbstractEntity<?>> PrimitiveResultQueryModel mkQueryToCountReferencesInType(
            final Class<? extends AbstractEntity<?>> entityType,
            final List<Field> props)
    {
        if (props.isEmpty()) {
            throw new InvalidArgumentException("At least one property is expected.");
        }

        // Add conditions to cover all properties of the referenced type using OR operation in case there are more than one
        var partialQ = select(entityType).
                           where().
                           prop(ID).ne().extProp(ID).and(). // this is to prevent counting self-references, relies on throughout ID
                           prop(ACTIVE).eq().val(true).and().
                           begin();
        for (int index = 0; index < props.size() - 1; index++) {
            final var propName = props.get(index).getName();
            partialQ = partialQ.prop(propName).eq().extProp(ID).or();
        }
        final var propName = props.getLast().getName();
        final var endQ = partialQ.prop(propName).eq().extProp(ID).end();
        return endQ.yield().countAll().modelAsPrimitive();
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
