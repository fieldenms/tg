package ua.com.fielden.platform.swing.booking;

import org.joda.time.Period;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.lifecycle.ValuedInterval;

public class DefaultTooltipGenerator<T extends AbstractEntity<?>, ST extends AbstractEntity<?>> implements ITooltipGenerator<T, ST> {

    @Override
    public String getTooltip(final T entity, final ST subEntity, final BookingSeries<T, ST> series) {
        return entity.getKey()
                + ValuedInterval.periodLengthStr(new Period(series.getBookingEntity().getFrom(entity, subEntity).getTime(), series.getBookingEntity().getTo(entity, subEntity).getTime()), true);
    }

}
