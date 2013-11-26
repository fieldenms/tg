package ua.com.fielden.platform.swing.schedule;

import java.awt.Paint;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.Pair;

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

    /**
     * Returns the legend items for this series.
     *
     * @return
     */
    List<Pair<String, Paint>> getAvailableLegendItems();
}
