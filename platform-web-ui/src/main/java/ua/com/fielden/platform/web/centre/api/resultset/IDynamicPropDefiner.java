package ua.com.fielden.platform.web.centre.api.resultset;

import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.egi.PropertyColumn;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * A contract to define dynamic columns and data for this columns in EGI.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IDynamicPropDefiner<T extends AbstractEntity<?>> {

    /**
     * Returns the list of
     *
     * @param context
     * @param data
     * @return
     */
    List<PropertyColumn> getColumns(final Optional<CentreContext<T, ?>> context, List<T> data);

}
