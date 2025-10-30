package ua.com.fielden.platform.utils;

import com.google.common.collect.ImmutableList;
import ua.com.fielden.platform.companion.IEntityReader;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.Finder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ua.com.fielden.platform.entity.AbstractUnionEntity.isUnionMember;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.utils.EntityUtils.isDateOnly;
import static ua.com.fielden.platform.utils.EntityUtils.isUnionEntityType;

public final class Validators {
    private Validators() {
    }


    /**
     * Identifies whether a provided entity overlaps with any of existing entities.
     * Values `fromDate` and `toDate` are extracted from `entity`.
     *
     * @param entity
     *           An entity that is tested for overlapping.
     * @param co
     *           An entity companion used for executing a matching query.
     * @param fromDateProperty
     *           A property name representing a `from` date.
     * @param toDateProperty
     *           A property name representing a `to` date.
     * @param matchProperties
     *           Optional properties that can be used to fine-tune entity matching when identifying those that should be considered for overlapping.
     *           For example, property `person` can be specified when searching for overlaps of entities `Timesheet`
     *           to consider only the timesheets for the same person as in `entity` being tested.
     * @return `true` if there is at least one overlapped entity.
     */
    public static <T extends AbstractEntity<?>> boolean overlaps(
            final T entity,
            final IEntityReader<T> co,
            final String fromDateProperty,
            final String toDateProperty,
            final String... matchProperties)
    {
        return co.exists(composeOverlappingCheckQueryModel(entity, fromDateProperty, toDateProperty, matchProperties));
    }

    /**
     * Identifies whether the provided `entity` overlaps with any of the existing entities.
     * Values `fromDateValue` and `toDateValue` are passed as separate arguments.
     * This is useful if the overlap test is performed as part of the validation for properties `fromDateProperty` or `toDateProperty`.
     *
     * @param entity
     *           An entity that is tested for overlapping.
     * @param co
     *           An entity companion used for executing a matching query.
     * @param fromDateProperty
     *           A property name representing a `from` date.
     * @param toDateProperty
     *           A property name representing a `to` date.
     * @param fromDateValue
     *           A value serving as the `from` date.
     * @param toDateValue
     *            A value serving as the `to` date.
     * @param matchProperties
     *           Optional properties that can be used to fine-tune entity matching when identifying those that should be considered for overlapping.
     *           For example, property `person` can be specified when searching for overlaps of entities `Timesheet`
     *           to consider only the timesheets for the same person as in `entity` being tested.
     * @return `true` if there is at least one overlapped entity.
     */
    public static <T extends AbstractEntity<?>> boolean overlaps(
            final T entity,
            final IEntityReader<T> co,
            final String fromDateProperty,
            final String toDateProperty,
            final Date fromDateValue,
            final Date toDateValue,
            final String... matchProperties)
    {
        return co.exists(composeOverlappingCheckQueryModel(entity, fromDateProperty, toDateProperty, fromDateValue, toDateValue, matchProperties));
    }

    /**
     * Returns the first overlapping entity if any, `null` value otherwise.
     *
     * @param entity
     *           An entity that is being validated for overlapping.
     * @param fetchModel
     *           An optional fetch model (i.e., can be `null`) used to initialise the overlapping entity, if any.
     * @param co
     *           An entity companion used for executing a matching query.
     * @param fromDateProperty
     *           A property name representing a `from` date.
     * @param toDateProperty
     *           A property name representing a `to` date.
     * @param matchProperties
     *           Optional properties that can be used to fine-tune entity matching when identifying those that should be considered for overlapping.
     *           For example, property `person` can be specified when searching for overlaps of entities `Timesheet`
     *           to consider only the timesheets for the same person as in `entity` being tested.
     * @return  either an overlapping entity or `null`.
     */
    public static <T extends AbstractEntity<?>> T findFirstOverlapping(
            final T entity,
            final fetch<T> fetchModel,
            final IEntityReader<T> co,
            final String fromDateProperty,
            final String toDateProperty,
            final String... matchProperties)
    {
        final var query = composeOverlappingCheckQueryModel(entity, fromDateProperty, toDateProperty, matchProperties);
        final var orderBy = orderBy().prop(fromDateProperty).asc().model();

        final var partialQem = from(query).with(orderBy);
        final var qem = fetchModel != null ? partialQem.with(fetchModel).model() : partialQem.model();
        final var result = co.getFirstEntities(qem, 1);
        return result.stream().findFirst().orElse(null);
    }

    /**
     * Returns the first overlapping entity if any, `null` otherwise.
     * Default entity query fetch model is used for initialising the overlapped entity.
     */
    public static <T extends AbstractEntity<?>> T findFirstOverlapping(
            final T entity,
            final IEntityReader<T> co,
            final String fromDateProperty,
            final String toDateProperty,
            final String... matchProperties)
    {
        return findFirstOverlapping(entity, null, co, fromDateProperty, toDateProperty, matchProperties);
    }

    /// Finds active deactivatable dependencies for `entity`, including indirect ones by virtual of being a union member.
    ///
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
            for (final var keyMember : keyMembers) {
                if (entityType.isAssignableFrom(keyMember.getType())
                   || (isUnionEntityType(keyMember.getType()) && isUnionMember((Class<? extends AbstractUnionEntity>) keyMember.getType(), entityType)))
                {
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

                final IEntityReader co = coFinder.find(dependentType, false);
                result.addAll(co.getAllEntities(from(query).with(co.getFetchProvider().fetchModel()).model()));
            }
        }
        return result;
    }

    /**
     * A helper method to produce a query model for overlapping validation.
     * The `from` and `to` dates are obtained from the `entity` instance as values of properties `fromDateProperty` and `toDateProperty` respectively.
     */
    private static <T extends AbstractEntity<?>> EntityResultQueryModel<T> composeOverlappingCheckQueryModel(
            final T entity,
            final String fromDateProperty,
            final String toDateProperty,
            final String... matchProperties)
    {
        // check preconditions
        final Date fromDateValue = entity.get(fromDateProperty);
        if (fromDateValue == null) {
            throw new IllegalArgumentException("Property “%s” should have a value.".formatted(fromDateProperty));
        }

        return composeOverlappingCheckQueryModel(entity, fromDateProperty, toDateProperty, fromDateValue, entity.get(toDateProperty), matchProperties);
    }


    /**
     * A helper method to produce a query model for overlapping validation.
     * The `from` and `to` dates are passed in as separate arguments.
     */
    private static <T extends AbstractEntity<?>> EntityResultQueryModel<T> composeOverlappingCheckQueryModel(
            final T entity,
            final String fromDateProperty,
            final String toDateProperty,
            final Date fromDateValue,
            final Date toDateValue,
            final String... matchProperties)
    {
        // check preconditions
        if (fromDateValue == null) {
            throw new IllegalArgumentException("fromDateValue should have a value.");
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
                throw new IllegalArgumentException("Entity “%s” should have a value for matching property “%s”.".formatted(entity, matchProperty));
            }
            cc = cc.and().prop(matchProperty).eq().val(value);
        }

        // Add overlapping test conditions.
        // Invariants: fromDateProperty always has a value.
        // There can be several cases:
        // 1. toDateProperty has a value (closed period).
        // 2. toDateProperty has no value (open period).

        condition_1:
        // Condition for the end of the period for potentially overlapped existing entities.
        // If `to` date property is annotated with annotation `DateOnly` then it should be greater than or equal to `fromDateValue`.
        if (isDateOnly(entity.getType(), toDateProperty)) {
            cc = cc.and()
                   .begin()
                      .prop(toDateProperty).isNull() // The end of a potentially overlapped entity is OPEN and thus is after the `fromDateProperty` value of the entity under test.
                      .or()
                      .dateOf().prop(toDateProperty).ge().dateOf().val(fromDateValue) // The end of a potentially overlapped entity is AFTER the `fromDateValue`.
                   .end();
        }
        // If to date property is not annotated with DateOnly annotation then it should be greater than fromDateValue
        else {
            cc = cc.and()
                   .begin()
                      .prop(toDateProperty).isNull() // The end of a potentially overlapped entity is OPEN and thus is after the `fromDateProperty` value of the entity under test.
                      .or()
                      .prop(toDateProperty).gt().val(fromDateValue) // The end of a potentially overlapped entity is AFTER the `fromDateValue`.
                   .end();
        }

        condition_2:
        // Condition for the beginning of the period for potentially overlapped existing entities.
        // An open-ended period does not require any condition, because any `toDateProperty` of the potentially overlapped entity would be BEFORE such an end.
        // Thus, if the `condition_1` holds, then there is an overlap and there is no reason to add any conditions.
        // If `condition_1` does not hold, then there is no overlap and there is no reason to add any conditions also.
        // A closed-ended period does require an additional condition to ensure the beginning of the potentially overlapped entity if BEFORE that end value of the entity under test.
        if (toDateValue != null) {
            // If `from` date property is annotated with annotation `DateOnly` then it should be less than or equal to `toDateValue`.
            if (isDateOnly(entity.getType(), fromDateProperty)) {
                cc = cc.and()
                       .begin()
                          .dateOf().prop(fromDateProperty).le().dateOf().val(toDateValue) // The beginning of the potentially overlapped entity is BEFORE the `toDateValue`.
                       .end();
            }
            // If `from` date property is not annotated with annotation `DateOnly` then it should be less than `toDateValue`.
            else {
                cc = cc.and()
                       .begin()
                          .prop(fromDateProperty).lt().val(toDateValue) // The beginning of the potentially overlapped entity is BEFORE the `toDateValue`.
                       .end();
            }
        }

        // Make a model with the result ordered by `fromDateProperty`,
        // which is only required if at some stage it would be used for selecting overlapped entities.
        return cc.model();
    }

}
