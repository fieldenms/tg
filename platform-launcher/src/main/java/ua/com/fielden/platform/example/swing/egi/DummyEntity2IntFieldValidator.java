package ua.com.fielden.platform.example.swing.egi;

import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IValidator;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;

/**
 * Validator for {@link DummyEntity2#getIntField()} property.
 * @author yura
 *
 */
public class DummyEntity2IntFieldValidator implements IValidator {

    @Override
    public Result validate(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	final Integer newInt = (Integer) newValue;
	if(newInt == null || newInt > 16) {
	    return successful(property.getEntity());
	} else if(newInt >= 10) {
	    return new Warning("Value is less than 15.");
	} else {
	    return new Result(new Exception("Value is less than 10."));
	}
    }

}
