package ua.com.fielden.platform.attachment.validators;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

public class LongitudeValidator implements IBeforeChangeEventHandler<BigDecimal> {

    public static final double LONGITUDE_MAX = 180;
    public static final double LONGITUDE_MIN = -180;

    public static final String ERR_LONGITUDE_RANGE = "Longitude must be in range [%s, %s].".formatted(LONGITUDE_MIN, LONGITUDE_MAX);

    @Override
    public Result handle(final MetaProperty<BigDecimal> property, final BigDecimal newLongitude, final Set<Annotation> mutatorAnnotations) {
        if (newLongitude != null) {
            // use double for efficient comparison, potential loss of BigDecimal precision has no effect
            final var longitudeD = newLongitude.doubleValue();
            if (longitudeD < LONGITUDE_MIN || longitudeD > LONGITUDE_MAX) {
                return failure(ERR_LONGITUDE_RANGE);
            }
        }

        return successful();
    }

}
