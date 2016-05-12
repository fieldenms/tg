package ua.com.fielden.platform.entity;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;

public class ExportActionOptionValidator implements IBeforeChangeEventHandler<Boolean> {

    @Override
    public Result handle(final MetaProperty<Boolean> property, final Boolean newValue, final Boolean oldValue, final Set<Annotation> mutatorAnnotations) {
        if (!newValue) {
            final Set<String> props = new HashSet<String>(Arrays.asList("all", "pageRange", "selected"));
            final EntityExportAction entity = (EntityExportAction) property.getEntity();
            props.remove(property.getName());
            for (final String prop : props) {
                if ((Boolean)entity.get(prop)) {
                    return Result.successful(property);
                }
            }
            return Result.failure("At least one option should be selected!");
        }
        return Result.successful(property);
    }

}
