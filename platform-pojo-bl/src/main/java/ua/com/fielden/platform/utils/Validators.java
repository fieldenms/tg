package ua.com.fielden.platform.utils;

import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;



public final class Validators {
    private Validators() {
    }

    /**
     * Identifies whether the provided entity overlaps with any of the existing entities.
     *
     * @param entity
     *            -- entity which should be tested for overlapping.
     * @param controller
     *            -- entity controller used for executing the query.
     * @param fromDateProperty
     *            -- name of the entity property serving as the from date.
     * @param toDateProperty
     *            -- name of the entity property serving as the to date.
     * @param matchProperties
     *            -- names of properties used for matching entity by, for example person in case of Timesheet entity.
     *            Could be empty, which would mean that overlapping happens against all existing entities of this type.
     * @return <i>true</i> if there is at least one overlapped entity.
     */
    public static <T extends AbstractEntity<?>> boolean overlaps(//
	    final T entity, //
	    final IEntityDao<T> controller, //
	    final String fromDateProperty, //
	    final String toDateProperty, //
	    final String... matchProperties) {
	final EntityResultQueryModel<T> model = composeOverlappingCheckQueryModel(entity, fromDateProperty, toDateProperty, matchProperties);
	return controller.count(model) > 0;
    }

    /**
     * Returns the first overlapping entity if any, <code>null</code> value otherwise.
     *
     * @param entity
     * @param fetchModel
     *            -- the fetch model is used to initialise the overlapped entity
     * @param controller
     * @param fromDateProperty
     * @param toDateProperty
     * @param matchProperties
     * @return
     */
    public static <T extends AbstractEntity<?>> T findFirstOverlapping(//
	    final T entity, //
	    final fetch<T> fetchModel,//
	    final IEntityDao<T> controller, //
	    final String fromDateProperty, //
	    final String toDateProperty, //
	    final String... matchProperties) {
	final EntityResultQueryModel<T> model = composeOverlappingCheckQueryModel(entity, fromDateProperty, toDateProperty, matchProperties);
	final OrderingModel orderBy = orderBy().prop(fromDateProperty).asc().model();

	final fetch<T> runFetch = fetchModel != null ? fetchModel : fetch((Class<T>) entity.getType());
	final List<T> result = controller.firstPage(from(model).with(runFetch).with(orderBy).model(), 1).data();
	return result.size() > 0 ? result.get(0) : null;
    }

    /**
     * Returns the first overlapping entity if any, <code>null</code> value otherwise. Default entity query fetch model is used for initialising the overlapped entity.
     *
     * @param entity
     * @param controller
     * @param fromDateProperty
     * @param toDateProperty
     * @param matchProperties
     * @return
     */
    public static <T extends AbstractEntity<?>> T findFirstOverlapping(//
	    final T entity, //
	    final IEntityDao<T> controller, //
	    final String fromDateProperty, //
	    final String toDateProperty, //
	    final String... matchProperties) {
	return findFirstOverlapping(entity, null, controller, fromDateProperty, toDateProperty, matchProperties);
    }

    /**
     * A helper method, which produces query model for overlapping validation.
     *
     * @param entity
     * @param fromDateProperty
     * @param toDateProperty
     * @param matchProperties
     * @return
     */
    private static <T extends AbstractEntity<?>> EntityResultQueryModel<T> composeOverlappingCheckQueryModel(final T entity, final String fromDateProperty, final String toDateProperty, final String... matchProperties) {
	// check preconditions
	if (entity.get(fromDateProperty) == null) {
	    throw new IllegalArgumentException("Property \"" + fromDateProperty + "\" should have a value.");
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
	condition_1: cc = cc.and().//
		begin().//
		prop(toDateProperty).isNull()./* the end of the potentially overlapped entity is OPEN and thus is after the fromDateProperty value of the entity under test */
		or().//
		prop(toDateProperty).gt().val(entity.get(fromDateProperty))./* the end of the potentially overlapped entity is AFTER the fromDateProperty value of the entity under test */
		end();//.

	// Condition for the beginning of the period for potentially overlapped existing entities
	// Open ended period does not require any condition, because any toDateProperty of the potentially overlapped entity would be BEFORE such an end.
	// Thus, if the condition_1 holds then there is an overlap and there is no reason to add any conditions
	// If condition_1 does not hold then there is no overlap and there is no reason to add any conditions too.
	// Closed ended period does require an additional condition to ensure the beginning of the potentially overlapped entity if BEFORE that end value of the entity under test
	condition_2: if (entity.get(toDateProperty) != null) {
	    cc = cc.and().//
		    begin().//
		    prop(fromDateProperty).lt().val(entity.get(toDateProperty))./* the beginning of the potentially overlapped entity is BEFORE the toDateProperty value of the entity under test */
		    end();//.
	}

	// make a model with result ordered by fromDateProperty, which is only required if at some stage it would be used for selecting overlapped entities.
	return cc.model();
    }
}
