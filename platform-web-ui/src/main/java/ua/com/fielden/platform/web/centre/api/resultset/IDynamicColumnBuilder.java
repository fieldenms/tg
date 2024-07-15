package ua.com.fielden.platform.web.centre.api.resultset;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.centre.api.IDynamicColumnConfig;

/**
 * A contract to define configuration of dynamic columns to EGI.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IDynamicColumnBuilder<T extends AbstractEntity<?>> {

    /**
     * Optionally returns a configuration of dynamic columns.
     *
     * @param context
     * @return
     */
    Optional<IDynamicColumnConfig> getColumnsConfig(final Optional<CentreContext<T, ?>> context);

}
