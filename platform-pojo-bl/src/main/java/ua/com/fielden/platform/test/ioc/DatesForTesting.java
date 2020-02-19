package ua.com.fielden.platform.test.ioc;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.joda.time.DateTime;

import ua.com.fielden.platform.utils.IDates;

public class DatesForTesting implements IDates {
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
    public DatesForTesting setTimeSupplier(final Supplier<DateTime> timeSupplier) {
        this.timeSupplier = timeSupplier;
        return this;
    }
    
    /**
     * A more explicit API for removing a time supplier if it was provided.
     * 
     * @return
     */
    public DatesForTesting removeTimeSupplier() {
        setTimeSupplier(null);
        return this;
    }
    
}