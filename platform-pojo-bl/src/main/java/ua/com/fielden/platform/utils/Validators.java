package ua.com.fielden.platform.utils;

import static ua.com.fielden.platform.equery.equery.select;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IOthers.ICompoundCondition;
import ua.com.fielden.platform.equery.interfaces.IOthers.IWhere;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;

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
    public static <T extends AbstractEntity> boolean overlaps(//
	    final T entity, //
	    final IEntityDao<T> controller, //
	    final String fromDateProperty, //
	    final String toDateProperty, //
	    final String... matchProperties) {
	// check preconditions
	if (entity.get(fromDateProperty) == null) {
	    throw new IllegalArgumentException("Property \"" + fromDateProperty  + "\" should have a value.");
	}
	if (!entity.isValid().isSuccessful()) {
	    throw new IllegalArgumentException("Entity \"" + entity + "\" should be valid: " + entity.isValid().getMessage());
	}

	/////////////// start query composition ///////////////////
	// add matching conditions
	final IWhere where = select(entity.getType()).where();
	// if the entity being checked for overlapping has already been persisted it would most likely overlap itself
	// thus, need to exclude entity itself
	ICompoundCondition cc =  where.prop("id").ne().val(entity.getId()); // should be ignore if id is null
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
	condition_1:
	cc = cc.and().//
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
	condition_2:
	if (entity.get(toDateProperty) != null) {
	    cc = cc.and().//
	    begin().//
	    	prop(fromDateProperty).lt().val(entity.get(toDateProperty))./* the beginning of the potentially overlapped entity is BEFORE the toDateProperty value of the entity under test */
	    end();//.
	}

	// make a model with result ordered by fromDateProperty, which is only required if at some stage it would be used for selecting overlapped entities.
	final IQueryOrderedModel<T> model = cc.orderBy(fromDateProperty).model();
	return controller.count(model) > 0;
    }
}
