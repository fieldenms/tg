package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * This contract serves for specifying logic to assign values to custom properties.
 * The custom properties' assignment logic is optional.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IResultSetBuilder3CustomPropAssignment<T extends AbstractEntity<?>> extends IResultSetBuilder4RenderingCustomiser<T> {

    IResultSetBuilder4RenderingCustomiser<T> setCustomPropsValueAssignmentHandler(final Class<? extends ICustomPropsAssignmentHandler<T>> handler);
}
