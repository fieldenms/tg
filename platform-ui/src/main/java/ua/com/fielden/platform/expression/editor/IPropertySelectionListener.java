package ua.com.fielden.platform.expression.editor;

import java.util.EventListener;

/**
 * An {@link EventListener} that listens property selection events.
 * 
 * @author TG Team
 * 
 */
public interface IPropertySelectionListener extends EventListener {

    /**
     * Would be invoked if property selection event was triggered.
     * 
     * @param property
     * @param isSelected
     */
    void propertyStateChanged(final String property, final boolean isSelected);
}
