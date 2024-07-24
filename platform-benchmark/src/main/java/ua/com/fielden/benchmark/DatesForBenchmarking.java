package ua.com.fielden.benchmark;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import ua.com.fielden.platform.utils.IDates;

/**
 * A convenient implementation of the {@link IDates} contract for benchmarking purposes.
 *
 * @author TG Team
 * 
 */
public class DatesForBenchmarking implements IDates {
    private DateTime now;
    private Supplier<DateTime> timeSupplier;

    @Override
    public DateTime now() {
        return getNow();
    }

    public DateTime getNow() {
        if (timeSupplier != null) {
            return timeSupplier.get();
        }
        if (now != null) {
            return now;
        }
        return new DateTime();
    }

    public void setNow(final DateTime now) {
        this.timeSupplier = null;
        this.now = now;
    }

    /**
     * A convenient way to specify the model time as a function.
     * If time supplier is not specified, value <code>now</code> is used.
     * This provides a way to alternate between constant and changing model time.
     * <p>
     * Possible implementation could use {@link AtomicReference} to emulate a ticking time like this.
     * <pre>
         final AtomicReference<DateTime> atomicNow = new AtomicReference<>(constants.getNow());
         constants.setTimeSupplier(() -> atomicNow.updateAndGet(time -> time.plusMillis(10)));
     * </pre>
     * @param timeSupplier
     */
    public DatesForBenchmarking setTimeSupplier(final Supplier<DateTime> timeSupplier) {
        this.timeSupplier = timeSupplier;
        return this;
    }

    /**
     * A more explicit API for removing a time supplier if it was provided.
     *
     * @return
     */
    public DatesForBenchmarking removeTimeSupplier() {
        setTimeSupplier(null);
        return this;
    }

    @Override
    public Optional<DateTimeZone> requestTimeZone() {
        return Optional.empty();
    }

    @Override
    public int finYearStartDay() {
        return 1;
    }

    @Override
    public int finYearStartMonth() {
        return 7;
    }

    @Override
    public int startOfWeek() {
        return 1;
    }

}
