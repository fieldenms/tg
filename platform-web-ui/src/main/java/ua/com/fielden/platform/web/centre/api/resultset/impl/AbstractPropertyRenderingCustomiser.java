package ua.com.fielden.platform.web.centre.api.resultset.impl;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * The
 *
 * @author TG Team
 *
 * @param <T>
 */
public abstract class AbstractPropertyRenderingCustomiser<T extends AbstractEntity<?>> {

    private final T entity;

    public AbstractPropertyRenderingCustomiser(final T entity) {
        this.entity = entity;
    }

    public abstract Map<String, String> color(List<String> property);
}
