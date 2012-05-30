package ua.com.fielden.platform.actionpanelmodel;

import java.awt.Component;

import javax.swing.JToolBar;

/**
 * Represents sub tool bar.
 * 
 * @author TG Team
 *
 */
public class ToolBarItem implements IActionItem {

    private final JToolBar toolBar;

    /**
     * Initiates this {@link ToolBarItem} with sub tool bar.
     * @param toolBar
     */
    public ToolBarItem(final JToolBar toolBar){
	this.toolBar = toolBar;
    }

    /**
     * Adds a sub tool bar to the specified one. If the sub tool bar or parent tool bar is null then nothing won't be created.
     * 
     * @param toolBar - parent tool bar to which the sub tool bar must be added. This parameter must not be null.
     */
    @Override
    public void build(final JToolBar toolBar) {
	if(this.toolBar != null && toolBar != null){
	    for(final Component component : this.toolBar.getComponents()){
		toolBar.add(component);
	    }
	}
    }

}
