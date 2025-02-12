package ua.com.fielden.platform.entity.validation;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.types.RichTextSanitiser;

import java.lang.annotation.Annotation;
import java.util.Set;

public final class RichTextValidator extends AbstractBeforeChangeEventHandler<RichText> {

    @Override
    public Result handle(
            final MetaProperty<RichText> property,
            final RichText newValue,
            final Set<Annotation> mutatorAnnotations)
    {
        return RichTextSanitiser.sanitiseHtml(newValue);
    }

}
