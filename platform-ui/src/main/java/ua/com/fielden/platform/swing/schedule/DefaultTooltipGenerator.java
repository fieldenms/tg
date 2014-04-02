package ua.com.fielden.platform.swing.schedule;

import org.joda.time.Period;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.lifecycle.ValuedInterval;

public class DefaultTooltipGenerator<T extends AbstractEntity<?>> implements ITooltipGenerator<T> {

    @Override
    public String getTooltip(final T entity, final ScheduleSeries<T> series) {
        return entity.getKey()
                + ValuedInterval.periodLengthStr(new Period(series.getScheduleEntity().getFrom(entity).getTime(), series.getScheduleEntity().getTo(entity).getTime()), true);
    }

}
