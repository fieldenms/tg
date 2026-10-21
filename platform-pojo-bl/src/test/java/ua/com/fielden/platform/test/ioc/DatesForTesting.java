package ua.com.fielden.platform.test.ioc;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import jakarta.inject.Singleton;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import ua.com.fielden.platform.utils.DefaultDates;
import ua.com.fielden.platform.utils.IDates;

import static org.joda.time.DateTimeZone.getDefault;

/// A convenient implementation of the [IDates] contract for the unit testing purposes.
///
/// The main reason to have a separate implementation specifically for testing, is to support modelling time and flexible ways of controlling it.
///
@Singleton
public class DatesForTesting implements IDates {
    private DateTime now;
    private Supplier<DateTime> timeSupplier;
    private DateTimeZone requestTimeZone;

    /// Returns the modelled time.
    ///
    /// If a request time zone is modelled (see [#setRequestTimeZone(DateTimeZone)]), the modelled time is returned as [DefaultDates#now()] would return it
    /// during a request from that time zone with `independent.time.zone` enabled: the wall-clock time in the request time zone, relabelled as the server's time zone.
    ///
    @Override
    public DateTime now() {
        final DateTime now = getNow();
        return requestTimeZone == null ? now : now.withZone(requestTimeZone).withZoneRetainFields(getDefault());
    }

    /// Returns the modelled time, regardless of a modelled request time zone.
    ///
    @Override
    public DateTime serverNow() {
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

    /// A convenient way to specify the model time as a function.
    /// If time supplier is not specified, value `now` is used.
    /// A `null` value removes a time supplier, the same as [#removeTimeSupplier()].
    /// This provides a way to alternate between constant and changing model time.
    ///
    /// Possible implementation could use [AtomicReference] to emulate a ticking time like this.
    ///
    /// ```java
    /// final AtomicReference<DateTime> atomicNow = new AtomicReference<>(constants.getNow());
    /// constants.setTimeSupplier(() -> atomicNow.updateAndGet(time -> time.plusMillis(10)));
    /// ```
    ///
    public DatesForTesting setTimeSupplier(final Supplier<DateTime> timeSupplier) {
        this.timeSupplier = timeSupplier;
        return this;
    }

    /// A more explicit API for removing a time supplier if it was provided.
    ///
    public DatesForTesting removeTimeSupplier() {
        this.timeSupplier = null;
        return this;
    }

    /// Models a web request from `requestTimeZone` with `independent.time.zone` enabled, which affects [#now()] and [#requestTimeZone()].
    ///
    /// This makes it possible to test logic that must use [#serverNow()] during a web request.
    /// The modelled request time zone is shared by all threads, and remains in effect until [#removeRequestTimeZone()] is invoked.
    ///
    public DatesForTesting setRequestTimeZone(final DateTimeZone requestTimeZone) {
        Objects.requireNonNull(requestTimeZone, "requestTimeZone cannot be null.");
        this.requestTimeZone = requestTimeZone;
        return this;
    }

    /// Removes a modelled request time zone, if it was provided.
    /// After that, [#now()] and [#serverNow()] return the same value.
    ///
    public DatesForTesting removeRequestTimeZone() {
        this.requestTimeZone = null;
        return this;
    }

    @Override
    public Optional<DateTimeZone> requestTimeZone() {
        return Optional.ofNullable(requestTimeZone);
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
