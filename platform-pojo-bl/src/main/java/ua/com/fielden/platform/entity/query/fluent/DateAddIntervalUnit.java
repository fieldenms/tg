package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateAddIntervalFunctionTo;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateAddIntervalUnit;

abstract class DateAddIntervalUnit<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IDateAddIntervalUnit<T, ET> {

    protected DateAddIntervalUnit(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForDateAddIntervalUnit(final Tokens tokens);

	@Override
	public IDateAddIntervalFunctionTo<T, ET> seconds() {
		return createDateAddIntervalFunctionTo(getTokens().secondsInterval());
	}
	
	@Override
	public IDateAddIntervalFunctionTo<T, ET> minutes() {
		return createDateAddIntervalFunctionTo(getTokens().minutesInterval());
	}

	@Override
	public IDateAddIntervalFunctionTo<T, ET> hours() {
		return createDateAddIntervalFunctionTo(getTokens().hoursInterval());
	}

	@Override
	public IDateAddIntervalFunctionTo<T, ET> days() {
		return createDateAddIntervalFunctionTo(getTokens().daysInterval());
	}

	@Override
	public IDateAddIntervalFunctionTo<T, ET> months() {
		return createDateAddIntervalFunctionTo(getTokens().monthsInterval());
	}

	@Override
	public IDateAddIntervalFunctionTo<T, ET> years() {
		return createDateAddIntervalFunctionTo(getTokens().yearsInterval());
	}

	private IDateAddIntervalFunctionTo<T, ET> createDateAddIntervalFunctionTo(final Tokens tokens) {
		return new DateAddIntervalFunctionTo<T, ET>(tokens) {

			@Override
			protected T nextForDateAddIntervalFunctionTo(final Tokens tokens) {
				return DateAddIntervalUnit.this.nextForDateAddIntervalUnit(tokens);
			}

		};
	}
}