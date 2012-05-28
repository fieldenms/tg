package ua.com.fielden.platform.entity.meta.entities;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

public class BceNotPermittedValue implements IBeforeChangeEventHandler<String> {
    private String notPermittedValue = "failure";

    @Override
    public Result handle(final MetaProperty property, final String newValue, final String oldValue, final Set<Annotation> mutatorAnnotations) {
	if (notPermittedValue.equalsIgnoreCase(newValue)) {
	    return new Result(newValue, new IllegalArgumentException("The value is not permitted"));
	}
	return Result.successful(newValue);
    }

}
