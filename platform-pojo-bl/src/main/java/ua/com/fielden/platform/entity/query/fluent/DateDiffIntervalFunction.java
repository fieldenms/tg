package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunction;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffIntervalFunction;

public class DateDiffIntervalFunction<T, ET extends AbstractEntity<?>> extends AbstractQueryLink implements IDateDiffIntervalFunction<T, ET> {

    T parent;

    DateDiffIntervalFunction(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    public IDateDiffFunction<T, ET> days() {
	return new DateDiffFunction<T, ET>(getTokens().daysInterval(), parent);
    }

    @Override
    public IDateDiffFunction<T, ET> months() {
	return new DateDiffFunction<T, ET>(getTokens().monthsInterval(), parent);
    }

    @Override
    public IDateDiffFunction<T, ET> years() {
	return new DateDiffFunction<T, ET>(getTokens().yearsInterval(), parent);
    }
}