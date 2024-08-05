package ua.com.fielden.benchmark;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import jakarta.inject.Singleton;
import org.joda.time.DateTime;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.utils.IUniversalConstants;

/**
 * A convenient implementation of the {@link IUniversalConstants} contract to provide flexible notion of the <code>now</code> for unit tests.
 *
 * @author TG Team
 *
 */
@Singleton
class UniversalConstantsForBenchmarking implements IUniversalConstants {

    private final String appName;
    private final String smtpServer;
    private final String fromEmailAddress;
    private final DatesForBenchmarking dates;

    @Inject
    public UniversalConstantsForBenchmarking(
            final @Named("app.name") String appName,
            final @Named("email.smtp") String smtpServer,
            final @Named("email.fromAddress") String fromEmailAddress,
            final IDates dates) {
        this.appName = appName;
        this.smtpServer = smtpServer;
        this.fromEmailAddress = fromEmailAddress;
        this.dates = (DatesForBenchmarking) dates;
    }


    @Override
    public DateTime now() {
        return getNow();
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
    public UniversalConstantsForBenchmarking setTimeSupplier(final Supplier<DateTime> timeSupplier) {
        dates.setTimeSupplier(timeSupplier);
        return this;
    }
    
    /**
     * A more explicit API for removing a time supplier if it was provided.
     * 
     * @return
     */
    public UniversalConstantsForBenchmarking removeTimeSupplier() {
        dates.removeTimeSupplier();
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
