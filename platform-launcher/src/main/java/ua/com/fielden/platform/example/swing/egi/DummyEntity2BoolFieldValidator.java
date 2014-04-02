/**
 *
 */
package ua.com.fielden.platform.example.swing.egi;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

/**
 * Validator for {@link DummyEntity2#isBoolField()} property.
 * 
 * @author Yura
 */
public class DummyEntity2BoolFieldValidator implements IBeforeChangeEventHandler<Boolean> {

    @Override
    public Result handle(final MetaProperty property, final Boolean newValue, final Boolean oldValue, final Set<Annotation> mutatorAnnotations) {
        final Object entity = property.getEntity();
        if (newValue) {
            return Result.successful(entity);
        } else {
            return new Result(entity, new IllegalArgumentException("boolField should be true"));
        }
    }

}
