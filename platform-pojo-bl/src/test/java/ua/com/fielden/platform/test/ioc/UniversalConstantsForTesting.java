package ua.com.fielden.platform.test.ioc;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import jakarta.inject.Singleton;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;

/// A convenient implementation of the [IUniversalConstants] contract to provide flexible notion of the `now` for unit tests.
///
@Singleton
public class UniversalConstantsForTesting implements IUniversalConstants {

    private final String appName;
    private final String smtpServer;
    private final String fromEmailAddress;
    private final DatesForTesting dates;

    @Inject
    public UniversalConstantsForTesting(
            final @Named("app.name") String appName,
            final @Named("email.smtp") String smtpServer,
            final @Named("email.fromAddress") String fromEmailAddress,
            final IDates dates) {
        this.appName = appName;
        this.smtpServer = smtpServer;
        this.fromEmailAddress = fromEmailAddress;
        this.dates = (DatesForTesting) dates;
    }


    @Override
    public DateTime now() {
        return dates.now();
    }

    @Override
    public DateTime serverNow() {
        return dates.serverNow();
    }

    @Override
    public Locale locale() {
        return Locale.getDefault();
    }

    public DateTime getNow() {
        return dates.getNow();
    }

    public void setNow(final DateTime now) {
        dates.setNow(now);
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
    public UniversalConstantsForTesting setTimeSupplier(final Supplier<DateTime> timeSupplier) {
        dates.setTimeSupplier(timeSupplier);
        return this;
    }

    /// A more explicit API for removing a time supplier if it was provided.
    ///
    public UniversalConstantsForTesting removeTimeSupplier() {
        dates.removeTimeSupplier();
        return this;
    }

    /// Models a web request from `requestTimeZone`, as [DatesForTesting#setRequestTimeZone(DateTimeZone)].
    ///
    public UniversalConstantsForTesting setRequestTimeZone(final DateTimeZone requestTimeZone) {
        dates.setRequestTimeZone(requestTimeZone);
        return this;
    }

    /// Removes a modelled request time zone, as [DatesForTesting#removeRequestTimeZone()].
    ///
    public UniversalConstantsForTesting removeRequestTimeZone() {
        dates.removeRequestTimeZone();
        return this;
    }

    @Override
    public String appName() {
        return appName;
    }

    @Override
    public String smtpServer() {
        return smtpServer;
    }

    @Override
    public String fromEmailAddress() {
        return fromEmailAddress;
    }

    public Supplier<DateTime> mkMillisTicker(final int stepInMillis) {
        final AtomicReference<DateTime> atomicNow = new AtomicReference<>(getNow());
        return () -> atomicNow.updateAndGet(time -> time.plusMillis(stepInMillis));
    }
}
