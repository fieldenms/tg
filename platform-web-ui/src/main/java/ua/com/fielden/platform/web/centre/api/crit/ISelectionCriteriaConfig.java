package ua.com.fielden.platform.web.centre.api.crit;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.IResultSetConfig;

/**
 *
 * Provides a convenient abstraction for specifying selection criteria for an entity centre.
 * <p>
 * It extends {@link IResultSetConfig} to cater for skipping addition of selection criteria and continuing directly with a set up of the result set.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISelectionCriteriaConfig<T extends AbstractEntity<?>> extends IResultSetConfig<T> {

}
