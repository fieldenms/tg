package ua.com.fielden.platform.web.centre.api.resultset;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.layout.ICollapsedCardLayoutConfig;

/**
 * This is just a gluing contract to add fluency to the result set property adding expressions.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IAlsoProp<T extends AbstractEntity<?>> extends ICollapsedCardLayoutConfig<T> {
    IResultSetBuilder2Properties<T> also();
}