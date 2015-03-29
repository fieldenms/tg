package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * This is just a gluing contract to add fluency to the result set secondary action adding expressions.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAlsoSecondaryAction<T extends AbstractEntity<?>> extends IResultSetBuilder3CustomPropAssignment<T> {


    IResultSetBuilder2SecondaryAction<T> also();
}
