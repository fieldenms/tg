package ua.com.fielden.platform.swing.schedule;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Contract that allows to specify domain axis lable for entity.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ITooltipGenerator<T extends AbstractEntity<?>> {

    /**
     * Returns the tool tip for entity in series.
     *
     * @param entity
     * @param series
     * @return
     */
    String getTooltip(T entity, ScheduleSeries <T> series);
}
