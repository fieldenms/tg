package ua.com.fielden.platform.swing.review.wizard.tree.editor;

import java.util.EventListener;

/**
 * {@link EventListener} that listens the start/finish edit events.
 * 
 * @author TG Team
 *
 */
public interface IPropertyEditListener extends EventListener {

    /**
     * Listens the start edit event.
     */
    void startEdit();

    /**
     * Listens the finish edit event.
     */
    void finishEdit();
}
