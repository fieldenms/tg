package ua.com.fielden.platform.actionpanelmodel;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JToolBar;

import ua.com.fielden.platform.swing.actions.ActionChanger;
import ua.com.fielden.platform.swing.components.ActionChangeButton;

/**
 * Builder class for composing tool bar from a list of {@link IActionItem} instances.
 * 
 * @author TG Team
 * 
 */
public class ActionPanelBuilder {

    private final List<IActionItem> itemList;

    /**
     * creates new instance of ActionPanelBuilder with empty list of {@link IActionItem} items
     */
    public ActionPanelBuilder() {
        itemList = new ArrayList<IActionItem>();
    }

    /**
     * builds the tool bar from the list of {@link IActionItem} items
     * 
     * @return the builded tool bar
     */
    public JToolBar buildActionPanel() {
        final JToolBar toolBar = new JToolBar();
        for (final IActionItem actionItem : itemList) {
            actionItem.build(toolBar);
        }
        return toolBar;
    }

    /**
     * Adds new sub tool bar to the list of {@link IActionItem}. The specified sub tool bar must be null
     * 
     * @param toolBar
     *            - specified sub tool bar to be added.
     * @return
     */
    public ActionPanelBuilder addSubToolBar(final JToolBar toolBar) {
        if (toolBar != null) {
            itemList.add(new ToolBarItem(toolBar));
        }
        return this;
    }

    /**
     * Adds new {@link ComponentItem} to the item list of tool bar.
     * 
     * @param component
     * @return
     */
    public ActionPanelBuilder addComponent(final JComponent component) {
        itemList.add(new ComponentItem(component));
        return this;
    }

    /**
     * adds new {@link DefaultButtonItem} instance to the list {@link IActionItem}
     * 
     * @param action
     *            - specified action, is needed for creating button for tool bar
     * @return instance of {@link ActionPanelBuilder}
     */
    public ActionPanelBuilder addButton(final Action action) {
        itemList.add(new DefaultButtonItem(action));
        return this;
    }

    /**
     * adds new {@link ChangeActionButtonItem} instance to the list of {@link IActionItem}
     * 
     * @param actionList
     *            - the list of action for the {@link ActionChangeButton}
     * @return instance of {@link ActionPanelBuilder}
     */
    public ActionPanelBuilder addActionChangeButton(final List<? extends ActionChanger<?>> actionList) {
        itemList.add(new ChangeActionButtonItem(actionList));
        return this;
    }

    /**
     * adds the {@link SeparatorItem} instance, that represents the tool bar separator, to the list list of {@link IActionItem}
     * 
     * @param size
     *            - the size of the separator
     * @return instance of {@link ActionChangeButton}
     */
    public ActionPanelBuilder addSeparator(final Dimension size) {
        itemList.add(new SeparatorItem(size));
        return this;
    }

    /**
     * Adds all {@link IActionItem}s from the specified {@link ActionPanelBuilder} to this one.
     * 
     * @param panelBuilder
     * @return
     */
    public ActionPanelBuilder addActionItems(final ActionPanelBuilder panelBuilder) {
        if (panelBuilder != null) {
            itemList.addAll(panelBuilder.getItemList());
        }
        return this;
    }

    /**
     * see {{@link #addSeparator(Dimension)} for more information
     * 
     * @return
     */
    public ActionPanelBuilder addSeparator() {
        itemList.add(new SeparatorItem());
        return this;
    }

    /**
     * Returns true if this tool bar builder is empty or not
     * 
     * @return
     */
    public boolean isEmpty() {
        return itemList.isEmpty();
    }

    /**
     * returns the list of {@link IActionItem}
     * 
     * @return
     */
    private List<IActionItem> getItemList() {
        return Collections.unmodifiableList(itemList);
    }

}
