package ua.com.fielden.platform.sample.domain.validators;

import java.lang.annotation.Annotation;
import java.util.Date;
import java.util.Set;

import org.joda.time.DateTime;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

public class DateValidator implements IBeforeChangeEventHandler<Date> {

    @Inject
    public DateValidator() {
        super();
    }

    @Override
    public Result handle(final MetaProperty<Date> property, final Date newValue, final Date oldValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue != null && new DateTime(2003, 2, 1, 6, 19).equals(new DateTime(newValue))) {
            return Result.failure(newValue, "[1/2/3 6:19] is not acceptable.");
        }
        return Result.successful(newValue);
    }

}