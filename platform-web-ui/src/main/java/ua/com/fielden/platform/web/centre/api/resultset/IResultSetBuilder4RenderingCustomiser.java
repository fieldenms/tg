package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * This contract serves for specifying custom rendering logic.
 * Custom rendering is optional.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder4RenderingCustomiser<T extends AbstractEntity<?>> {


    void setRenderingCustomiser(final IRenderingCustomiser<T, ?> customiser);
}
