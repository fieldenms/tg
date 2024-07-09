package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateAddIntervalFunctionTo;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateAddIntervalUnit;

abstract class DateAddIntervalUnit<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IDateAddIntervalUnit<T, ET> {

	protected DateAddIntervalUnit(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T nextForDateAddIntervalUnit(final EqlSentenceBuilder builder);

	@Override
	public IDateAddIntervalFunctionTo<T, ET> seconds() {
		return createDateAddIntervalFunctionTo(builder.secondsInterval());
	}

	@Override
	public IDateAddIntervalFunctionTo<T, ET> minutes() {
		return createDateAddIntervalFunctionTo(builder.minutesInterval());
	}

	@Override
	public IDateAddIntervalFunctionTo<T, ET> hours() {
		return createDateAddIntervalFunctionTo(builder.hoursInterval());
	}

	@Override
	public IDateAddIntervalFunctionTo<T, ET> days() {
		return createDateAddIntervalFunctionTo(builder.daysInterval());
	}

	@Override
	public IDateAddIntervalFunctionTo<T, ET> months() {
		return createDateAddIntervalFunctionTo(builder.monthsInterval());
	}

	@Override
	public IDateAddIntervalFunctionTo<T, ET> years() {
		return createDateAddIntervalFunctionTo(builder.yearsInterval());
	}

	private IDateAddIntervalFunctionTo<T, ET> createDateAddIntervalFunctionTo(final EqlSentenceBuilder builder) {
		return new DateAddIntervalFunctionTo<T, ET>(builder) {

			@Override
			protected T nextForDateAddIntervalFunctionTo(final EqlSentenceBuilder builder) {
				return DateAddIntervalUnit.this.nextForDateAddIntervalUnit(builder);
			}

		};
	}

}
