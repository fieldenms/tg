package ua.com.fielden.platform.sample.domain.validators;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.types.Money;

import java.lang.annotation.Annotation;
import java.util.Set;

import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

public class TgFuelUsagePricePerLitreValidator implements IBeforeChangeEventHandler<Money> {

    public static final String ERR_CONTROLLING_PROPERTY = "Invalid due to the controlling property.";

    @Override
    public Result handle(final MetaProperty<Money> property, final Money newValue, final Set<Annotation> mutatorAnnotations) {
        final TgFuelUsage entity = property.getEntity();

        if (entity.getPricePerLitreValidation() != null && entity.getPricePerLitreValidation().startsWith("invalid")) {
            return failure(ERR_CONTROLLING_PROPERTY);
        }

        return successful();
    }

}
