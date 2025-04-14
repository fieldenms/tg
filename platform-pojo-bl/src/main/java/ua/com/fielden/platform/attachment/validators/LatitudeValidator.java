package ua.com.fielden.platform.attachment.validators;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

public class LatitudeValidator implements IBeforeChangeEventHandler<BigDecimal> {

    public static final double LATITUDE_MAX = 90;
    public static final double LATITUDE_MIN = -90;

    public static final String ERR_LATITUDE_RANGE = "Latitude must be in range [%s, %s].".formatted(LATITUDE_MIN, LATITUDE_MAX);

    @Override
    public Result handle(final MetaProperty<BigDecimal> property, final BigDecimal newLatitude, final Set<Annotation> mutatorAnnotations) {
        if (newLatitude != null) {
            // use double for efficient comparison, potential loss of BigDecimal precision has no effect
            final var latitudeD = newLatitude.doubleValue();
            if (latitudeD < LATITUDE_MIN || latitudeD > LATITUDE_MAX) {
                return failure(ERR_LATITUDE_RANGE);
            }
        }

        return successful();
    }

}
