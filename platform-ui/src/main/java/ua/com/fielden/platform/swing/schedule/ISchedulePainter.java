package ua.com.fielden.platform.swing.schedule;

import java.awt.Paint;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * The contract for schedule painter.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface ISchedulePainter<T extends AbstractEntity<?>> {

    /**
     * Returns the {@link Paint} object for the specified entity.
     *
     * @param entity
     * @return
     */
    Paint getPainterFor(T entity);
}
