/**
 *
 */
package ua.com.fielden.platform.entity.validation.test_entities;

import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

/**
 * Test validator for {@link SubClass2#bigDecimalProp}.
 *
 * @author Yura
 */
public class BigDecimalPropValidator implements IBeforeChangeEventHandler<BigDecimal> {

    @Override
    public Result handle(final MetaProperty<BigDecimal> property, final BigDecimal newValue, final Set<Annotation> mutatorAnnotations) {
        return Result.successful(property.getEntity());
    }

}
