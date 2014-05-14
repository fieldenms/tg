package ua.com.fielden.platform.swing.booking;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Contract that allows to specify domain axis lable for entity.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ITooltipGenerator<T extends AbstractEntity<?>, ST extends AbstractEntity<?>> {

    /**
     * Returns the tool tip for entity in series.
     *
     * @param entity
     * @param subEntity
     * @param series
     * @return
     */
    String getTooltip(T entity, ST subEntity, BookingSeries<T, ST> series);
}
