package ua.com.fielden.platform.sample.domain.validators;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.TgAuthor;

public class TgPublishedYearly_AuthorValidator implements IBeforeChangeEventHandler<TgAuthor> {

    @Override
    public Result handle(final MetaProperty property, final TgAuthor newValue, final TgAuthor oldValue, final Set<Annotation> mutatorAnnotations) {
        System.out.println("              IT WORKS!!!");
        return Result.successful(newValue);
    }
}