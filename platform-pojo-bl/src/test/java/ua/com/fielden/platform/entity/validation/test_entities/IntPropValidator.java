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
public class IntPropValidator implements IBeforeChangeEventHandler<Integer> {

    @Override
    public Result handle(final MetaProperty<Integer> property, final Integer newValue, final Integer oldValue, final Set<Annotation> mutatorAnnotations) {
        return Result.successful(property.getEntity());
    }

}
