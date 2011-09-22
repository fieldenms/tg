/**
 *
 */
package ua.com.fielden.platform.entity.validation.test_entities;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

/**
 * Test validator for {@link AbstractBaseClass} intProp.
 * 
 * @author Yura
 */
public class IntPropValidator3 implements IBeforeChangeEventHandler {

    @Override
    public Result handle(final MetaProperty property, final Object newValue, final Object oldValue, final Set<Annotation> mutatorAnnotations) {
	return Result.successful(property.getEntity());
    }

}
