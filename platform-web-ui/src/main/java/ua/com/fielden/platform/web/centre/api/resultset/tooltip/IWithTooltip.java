package ua.com.fielden.platform.web.centre.api.resultset.tooltip;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.resultset.summary.IWithSummary;

/**
 * A contract for declaring tooltip binding for result-set property.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IWithTooltip<T extends AbstractEntity<?>> extends IWithSummary<T> {

    /**
     * Adds a property binding for tooltip text that should be displayed for the entity property in the specific column.
     *
     * @param propertyName
     *            - the property name that holds tooltip for specific column.
     * @return
     */
    IWithSummary<T> withTooltip(final String propertyName);
}
