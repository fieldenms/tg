package ua.com.fielden.platform.web.centre.api.resultset.impl;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.IRenderingCustomiser;

public class PropertyRenderingCustomiser<T extends AbstractEntity<?>, R extends AbstractPropertyRenderingCustomiser<T>> implements IRenderingCustomiser<T, R> {

    public Optional<R> getCustomRenderingFor(final T entity) {
        return Optional.empty();
    }
}
