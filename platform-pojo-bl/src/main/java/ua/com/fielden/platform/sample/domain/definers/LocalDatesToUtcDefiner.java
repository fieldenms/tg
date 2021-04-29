package ua.com.fielden.platform.sample.domain.definers;

import java.util.Date;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractAfterChangeEventHandler;
import ua.com.fielden.platform.sample.domain.TgEntityWithTimeZoneDates;

/**
 * Definer 'dateProp' date property.
 *
 * @author TG Team
 *
 */
public class LocalDatesToUtcDefiner extends AbstractAfterChangeEventHandler<Date> {
    
    @Override
    public void handle(final MetaProperty<Date> property, final Date localDate) {
        final TgEntityWithTimeZoneDates wa = (TgEntityWithTimeZoneDates) property.getEntity();
        // We should only manipulate values of properties if entity is not initialising.
        // In case of the initialising a definer on corresponding UTC date property will populate this property and should not re-populate its's UTC brother.
        if (!wa.isInitialising()) {
            final String propName = property.getName();
            final String utcPropName = propName.concat("Utc");
            wa.set(utcPropName, localDate);
        }
    }
    
}