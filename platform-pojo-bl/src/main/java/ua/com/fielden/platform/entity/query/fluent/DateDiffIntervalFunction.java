package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunction;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffIntervalFunction;

abstract class DateDiffIntervalFunction<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IDateDiffIntervalFunction<T, ET> {

	protected abstract T nextForDateDiffIntervalFunction();

	private DateDiffFunction<T, ET> createDateDiffFunction() {
		return new DateDiffFunction<T, ET>() {

			@Override
			protected T nextForDateDiffFunction() {
				return DateDiffIntervalFunction.this.nextForDateDiffIntervalFunction();
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