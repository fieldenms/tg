package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunction;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffIntervalFunction;

abstract class DateDiffIntervalFunction<T, ET extends AbstractEntity<?>> extends AbstractQueryLink
		implements IDateDiffIntervalFunction<T, ET> {

	abstract T getParent();

	private DateDiffFunction<T, ET> createDateDiffFunction() {
		return new DateDiffFunction<T, ET>() {

			@Override
			T getParent() {
				return DateDiffIntervalFunction.this.getParent();
			}
			
		};
	}

	@Override
	public IDateDiffFunction<T, ET> seconds() {
		return copy(createDateDiffFunction(), getTokens().secondsInterval());
	}

	@Override
	public IDateDiffFunction<T, ET> minutes() {
		return copy(createDateDiffFunction(), getTokens().minutesInterval());
	}

	@Override
	public IDateDiffFunction<T, ET> hours() {
		return copy(createDateDiffFunction(), getTokens().hoursInterval());
	}

	@Override
	public IDateDiffFunction<T, ET> days() {
		return copy(createDateDiffFunction(), getTokens().daysInterval());
	}

	@Override
	public IDateDiffFunction<T, ET> months() {
		return copy(createDateDiffFunction(), getTokens().monthsInterval());
	}

	@Override
	public IDateDiffFunction<T, ET> years() {
		return copy(createDateDiffFunction(), getTokens().yearsInterval());
	}
}