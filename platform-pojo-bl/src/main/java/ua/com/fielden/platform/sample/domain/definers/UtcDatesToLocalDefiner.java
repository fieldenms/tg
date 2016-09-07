package ua.com.fielden.platform.sample.domain.definers;

import java.util.Date;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractAfterChangeEventHandler;
import ua.com.fielden.platform.sample.domain.TgEntityWithTimeZoneDates;

/**
 * Common definer for the following UTC dates - datePropUtc.
 *
 * @author JK / TG Team
 *
 */
public class UtcDatesToLocalDefiner extends AbstractAfterChangeEventHandler<Date> {

    @Override
    public void handle(final MetaProperty<Date> property, final Date utcDate) {
        final TgEntityWithTimeZoneDates wa = (TgEntityWithTimeZoneDates) property.getEntity();
        final String propName = property.getName();
        final String nonUtcPropName = propName.substring(0, propName.length() - 3);
        wa.set(nonUtcPropName, utcDate);
        wa.getProperty(nonUtcPropName).resetState();
    }
}
