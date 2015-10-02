package ua.com.fielden.platform.web.view.master.api;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.entity.AbstractEntity;

public interface IMasterWithEntityMatchers<T extends AbstractEntity<?>> extends IMaster<T> {
    Class<? extends IValueMatcherWithContext<T, ?>> matcherTypeFor(final String propName);
}
