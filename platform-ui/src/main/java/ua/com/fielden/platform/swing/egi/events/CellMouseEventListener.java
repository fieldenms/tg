/**
 *
 */
package ua.com.fielden.platform.swing.egi.events;

import java.util.EventListener;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Event listener for {@link CellMouseEvent}s.
 * 
 * @author Yura
 */
@SuppressWarnings("unchecked")
public interface CellMouseEventListener<T extends AbstractEntity> extends EventListener {

    void cellClicked(CellMouseEvent<T> event);

    void cellPressed(CellMouseEvent<T> event);

    void cellReleased(CellMouseEvent<T> event);

}
