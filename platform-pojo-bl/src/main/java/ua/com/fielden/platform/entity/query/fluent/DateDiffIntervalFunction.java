package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunction;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffIntervalFunction;

abstract class DateDiffIntervalFunction<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IDateDiffIntervalFunction<T, ET> {

    protected DateDiffIntervalFunction(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForDateDiffIntervalFunction(final Tokens tokens);

	@Override
	public IDateDiffFunction<T, ET> seconds() {
		return createDateDiffFunction(getTokens().secondsInterval());
	}

	@Override
	public IDateDiffFunction<T, ET> minutes() {
		return createDateDiffFunction(getTokens().minutesInterval());
	}

	@Override
	public IDateDiffFunction<T, ET> hours() {
		return createDateDiffFunction(getTokens().hoursInterval());
	}

	@Override
	public IDateDiffFunction<T, ET> days() {
		return createDateDiffFunction(getTokens().daysInterval());
	}

	@Override
	public IDateDiffFunction<T, ET> months() {
		return createDateDiffFunction(getTokens().monthsInterval());
	}

	@Override
	public IDateDiffFunction<T, ET> years() {
		return createDateDiffFunction(getTokens().yearsInterval());
	}
	
	private DateDiffFunction<T, ET> createDateDiffFunction(final Tokens tokens) {
		return new DateDiffFunction<T, ET>(tokens) {

			@Override
			protected T nextForDateDiffFunction(final Tokens tokens) {
				return DateDiffIntervalFunction.this.nextForDateDiffIntervalFunction(tokens);
			}

		};
	}	
}