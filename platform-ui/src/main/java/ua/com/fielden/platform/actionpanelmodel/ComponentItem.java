package ua.com.fielden.platform.actionpanelmodel;

import javax.swing.JComponent;
import javax.swing.JToolBar;

/**
 * Represents the the component item on the tool bar.
 * 
 * @author TG Team
 * 
 */
public class ComponentItem implements IActionItem {

    private final JComponent component;

    /**
     * Initiates this tool bar item with specified {@link JComponent} instance.
     * 
     * @param component
     */
    public ComponentItem(final JComponent component) {
        this.component = component;
    }

    @Override
    public void build(final JToolBar toolBar) {
        toolBar.add(component);
    }
}
