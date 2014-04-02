/**
 *
 */
package ua.com.fielden.platform.swing.egi.events;

import java.awt.event.MouseEvent;
import java.util.EventObject;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Represents EGI cell mouse event, which corresponds to mouse event on some property(column) of entity(row).
 * 
 * @author Yura
 */
@SuppressWarnings("unchecked")
public class CellMouseEvent<T extends AbstractEntity> extends EventObject {

    private static final long serialVersionUID = -3615895958303806006L;

    private final T entity;

    private final String propertyName;

    private final MouseEvent initiatorEvent;

    public CellMouseEvent(final MouseEvent event, final T entity, final String propertyName) {
        super(event.getSource());
        this.entity = entity;
        this.propertyName = propertyName;
        this.initiatorEvent = event;
    }

    public T getEntity() {
        return entity;
    }

    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Returns {@link MouseEvent} that initiated this one.
     * 
     * @return
     */
    public MouseEvent getInitiatorEvent() {
        return initiatorEvent;
    }

}
