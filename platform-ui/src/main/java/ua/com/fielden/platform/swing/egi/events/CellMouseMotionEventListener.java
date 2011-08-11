/**
 *
 */
package ua.com.fielden.platform.swing.egi.events;

import java.util.EventListener;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Mouse motion event listener for {@link CellMouseEvent}.
 * 
 * @author Yura
 */
@SuppressWarnings("unchecked")
public interface CellMouseMotionEventListener<T extends AbstractEntity> extends EventListener {

    void mouseMoved(CellMouseEvent<T> event);

    void mouseDragged(CellMouseEvent<T> event);

}
