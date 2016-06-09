package ua.com.fielden.platform.test.ioc;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;

import ua.com.fielden.platform.utils.IUniversalConstants;

import com.google.common.base.Ticker;
import com.google.inject.Inject;

/**
 * This is a session cache ticker for testing purposes, which is required to keep the model time in sync with the cache time used for eviction.
 *
 * @author TG Team
 *
 */
public class TickerForSessionCache extends Ticker {

    private DateTime startDateTime;
    private DateTime currDateTime;
    private final Optional<IUniversalConstants> constants;

    /**
     * This constructor binds the start time to the universal now at the construction time,
     * and binds the current time to the universal now every time ticker's {@link #read()} method is called.
     *
     * @param constants
     */
    @Inject
    public TickerForSessionCache(final IUniversalConstants constants) {
        setStartTime(constants.now());
        this.constants = Optional.of(constants);
    }

    /**
     * Should be used to construct a ticker that needs to be based on a specific time value.
     *
     * @param now
     */
    public TickerForSessionCache(final DateTime now) {
        setStartTime(now);
        this.constants = Optional.empty();
    }

    @Override
    public long read() {
        return getStartTimeNano() + getTimePassedNano();
    }

    public long getStartTimeNano() {
        return TimeUnit.NANOSECONDS.convert(startDateTime.getMillis(), TimeUnit.MILLISECONDS);
    }

    public void setStartTime(final DateTime time) {
        this.startDateTime = time;
    }

    public long getTimePassedNano() {
        if (constants.isPresent()) {
            currDateTime = constants.get().now();
        }
        final long diffMillis = currDateTime.getMillis() - startDateTime.getMillis();
        return TimeUnit.NANOSECONDS.convert(diffMillis, TimeUnit.MILLISECONDS);
    }

    public void setCurrTime(final DateTime time) {
        this.currDateTime = time;
    }
}
