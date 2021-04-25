package ua.com.fielden.platform.sample.domain.definers;

import java.util.Date;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractAfterChangeEventHandler;
import ua.com.fielden.platform.sample.domain.TgEntityWithTimeZoneDates;

/**
 * Definer 'datePropUtc' date property.
 *
 * @author TG Team
 * 
 */
public class UtcDatesToLocalDefiner extends AbstractAfterChangeEventHandler<Date> {
    
    @Override
    public void handle(final MetaProperty<Date> property, final Date utcDate) {
        final TgEntityWithTimeZoneDates wa = (TgEntityWithTimeZoneDates) property.getEntity();
        // We should only manipulate values of properties if entity is not initialising.
        // In case of the initialising a definer on corresponding local date property will populate this UTC property and should not re-populate its's local date brother.
        if (!wa.isInitialising()) {
            final String propName = property.getName();
            final String nonUtcPropName = propName.substring(0, propName.length() - 3);
            wa.set(nonUtcPropName, utcDate);
        }
    }
    
}