package ua.com.fielden.platform.swing.menu;

import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultSingleSelectionModel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SingleSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.menu.api.IItemSelector;
import ua.com.fielden.platform.swing.menu.filter.IFilter;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;
import ua.com.fielden.platform.swing.view.BasePanel;

import com.jidesoft.swing.JideTabbedPane;

/**
 * This is a tree menu component, which utilises tabbed pane for placement of views associated with menu items. There are two modes:
 * <ul>
 * <li>Each item view is placed on an individual tab starting with tab 0.
 * <li>Each item view is placed on an individual tab starting with tab 1, whereas tab 0 is reserved and can be used for other purposes.
 * </ul>
 * 
 * @author TG Team
 * 
 */
public class TreeMenuWithTabs<V extends BasePanel> extends TreeMenu<V> {
    private static final long serialVersionUID = 1L;

    /**
     * Tabbed pane containing menu views.
     */
    private final JideTabbedPane tabPane;

    /**
     * If true then the first tab (index 0) is reserved for some other purpose than location of a menu item view.
     */
    private final boolean firstTabClosable;

    private final JPanel defaultInfoPanel;

    // private final JPopupMenu popupMenu;

    private final ActionWithPostAction activateMenuItemAction;
    private final Action closeTabAction;

    private final String DEFAULT_INFO = "<html>"
            + "<h2>Information</h2>"
            + "This is a general information, which is displayed in cases where individual menu items do not provide their own information panels, providing user with a quick familiarisation with an application user interface."
            + "<br/><br/>"//
            + "Simple selection of a menu item does not open its content (the view), but instead displays its information panel. "
            + "Some menu items may not be provided with an information panel. " + "In this case a general window information panel like this one is displayed." + "<br/><br/>"
            + "Double clicking a menu item or selecting it and pressing the enter key loads item's content (also known as item's view) on a separate tab. "
            + "This way navigation between menu items can be done using tabs or the tree menu."
            + "Please note that opening a menu item automatically bring the input focus to its view."
            + "Once a menu is open selecting it in the tree automatically activates a corresponding tab. Otherwise, its information panel is displayed." + "<br/><br/>"
            + "<h3>Hot Keys</h3>"//
            + "The following hot keys are supported:"//
            + "<ul>"//
            + "  <li>CTRL+1 -- focuses the tree menu."//
            + "  <li>CTRL+2 -- focuses the tree filter."//
            + "  <li>CTRL+3 -- focuses the tabbed pane."//
            + "  <li>CRTL+I -- activates info tab."//
            + "  <li>CRTL+W -- closes the current tab."//
            + "  <li>CTRL+PAGE_DOWN -- moves to the next tab (circular action)."//
            + "  <li>CTRL+PAGE_UP -- moves to the previous tab (circular action)."//
            + "  <li>ENTER -- when applied to a closed menu item loads a corresponding view into a separate tab, or simply focuses a corresponding view in an open tab."//
            + "</ul>"//
            + "</html>";

    /**
     * A convenient constructor, which results in creation of a tree menu with default info panel.
     * 
     * @param menuItem
     * @param menuFilter
     * @param blockingPane
     */
    public TreeMenuWithTabs(final TreeMenuItem<V> menuItem, final IFilter menuFilter, final BlockingIndefiniteProgressPane blockingPane) {
        this(menuItem, menuFilter, null, blockingPane);
    }

    /**
     * Principle constructor.
     * 
     * @param menuItem
     *            -- a vector of tree menu items.
     * @param menuFilter
     *            -- filter used for user driven filtering of menu items
     * @param defaultInforPanel
     *            -- an info panel used in cases where individual menu items were not provided with their own info panels.
     * @param blockingPane
     *            -- the pane used for blocking UI upon activation of menu items
     */
    public TreeMenuWithTabs(final TreeMenuItem<V> menuItem, final IFilter menuFilter, final JPanel defaultInforPanel, final BlockingIndefiniteProgressPane blockingPane) {
        this(menuItem, menuFilter, defaultInforPanel, createViewHolderPanel(), blockingPane);

    }

    protected TreeMenuWithTabs(final TreeMenuItem<V> menuItem, final IFilter menuFilter, final JPanel defaultInforPanel, final JPanel holderPanel, final BlockingIndefiniteProgressPane blockingPane) {
        super(menuItem, menuFilter, holderPanel, false, false, blockingPane);
        // this basically disables collapsing of tree branches on double click making it 512-click action...
        // hopefully no one will reproduce such an action...
        setToggleClickCount(512);
        // construct tabbed pane
        tabPane = new JideTabbedPane();
        tabPane.setModel(createTabbedPaneModel());
        tabPane.setHideOneTab(true); // no need to show tab if there is only one
        tabPane.setShowCloseButton(true);
        tabPane.setShowCloseButtonOnTab(true);
        tabPane.setShowCloseButtonOnSelectedTab(true);
        tabPane.setColorTheme(JideTabbedPane.COLOR_THEME_OFFICE2003);
        tabPane.setTabShape(JideTabbedPane.SHAPE_OFFICE2003);

        // adding mouse listener to the tab panel that selects tree menu item associated with selected view on double click mouse event
        tabPane.addMouseListener(new SynchronizeWithMenuHandler());

        // Create tab close action, which is then associated with tab's close button
        // and CTRL+W key stroke for tabs available any where in the window.

        closeTabAction = createCloseTabAction();
        tabPane.setCloseAction(closeTabAction);
        final KeyStroke closeTab = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_DOWN_MASK);
        final String CLOSE_TAB = "CLOSE_TAB";
        tabPane.getActionMap().put(CLOSE_TAB, closeTabAction);
        tabPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(closeTab, CLOSE_TAB);
        // Create CTRL+PAGE_DOWN key stroke and action to go to the next tab
        final KeyStroke nextTab = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_DOWN_MASK);
        final String NEXT_TAB = "NEXT_TAB";
        tabPane.getActionMap().put(NEXT_TAB, createNextTabAction());
        tabPane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(nextTab, NEXT_TAB);
        // Create CTRL+PAGE_UP key stroke and action to go to the previous tab
        final KeyStroke prevTab = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_DOWN_MASK);
        final String PREV_TAB = "PREV_TAB";
        tabPane.getActionMap().put(PREV_TAB, createPrevTabAction());
        tabPane.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(prevTab, PREV_TAB);
        // Create CTRL+I key stroke and action to go to the info tab
        final KeyStroke infoTab = KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK);
        final String INFO_TAB = "INFO_TAB";
        tabPane.getActionMap().put(INFO_TAB, createInfoTabAction());
        tabPane.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(infoTab, INFO_TAB);

        // Assign the default info panel or create one if none was provided
        defaultInfoPanel = defaultInforPanel != null ? defaultInforPanel : new SimpleInfoPanel(getDefaultInfo());
        firstTabClosable = false; // the use of this property is reserved for a case where no info panel is required
        tabPane.addTab("Info", defaultInfoPanel);
        tabPane.setTabClosableAt(0, false);

        // create ENTER key stroke for the tree menu and associated it with menu item activation action.
        final KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0); // no modifiers
        final String ACTIVATE_MENU_ITEM = "ACTIVATE_MENU_ITEM";
        activateMenuItemAction = createActivateItemAction();
        getActionMap().put(ACTIVATE_MENU_ITEM, activateMenuItemAction);
        getInputMap().put(enter, ACTIVATE_MENU_ITEM);

        // handle double clicking on a menu item, which should do the same as enter key stroke
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2) {
                    e.consume();
                    activateMenuItemAction.actionPerformed(null);
                }
            }
        });

        // add tabbed pane to the holder panel
        getHolder().add(tabPane);
        // add focus listener to the holder panel to ensure that focus is forwarded to a selected tab's component
        getHolder().addFocusListener(new FocusListener() {

            @Override
            public void focusGained(final FocusEvent e) {
                if (tabPane.getSelectedComponent() != null) {
                    tabPane.getSelectedComponent().requestFocusInWindow();
                }
            }

            @Override
            public void focusLost(final FocusEvent e) {
            }

        });

        // select the first menu item
        setSelectionRow(0);
    }

    /**
     * Should be overridden for the purpose of providing alternative default info.
     * 
     * @return
     */
    protected String getDefaultInfo() {
        return DEFAULT_INFO;
    }

    protected JideTabbedPane getTabPane() {
        return tabPane;
    }

    protected JPanel getDefaultInfoPanel() {
        return defaultInfoPanel;
    }

    /**
     * Creates an action for selecting info tab.
     * 
     * @return
     */
    private Action createInfoTabAction() {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                tabPane.setSelectedIndex(0);
            }
        };
    }

    /**
     * Construct a custom tabbed pane model in order to implement handling on tab changing (can not be covered by an event listener) logic in order to provide ICloseGuard
     * leveraging for menu item's view.
     * 
     * @return
     */
    private SingleSelectionModel createTabbedPaneModel() {
        return new DefaultSingleSelectionModel() {
            private static final long serialVersionUID = 1L;

            /**
             * Sets the specified tab index as selected only if the currently active menu item can be closed.
             */
            @Override
            public void setSelectedIndex(final int index) {
                // If some tab was already selected then it is associated with a menu item.
                // Thus, need to check if it can be left.
                // However, in case of firstTabClosable == false there is no need to check anything if the current tab index is 0 (i.e. first tab) as
                // it cannot be associated with any of the menu item
                if (shouldCheck(getSelectedIndex(), index)) {
                    // a different tab is being selected and thus need to check whether the current one can be closed/left
                    final BasePanel view = (BasePanel) tabPane.getComponentAt(getSelectedIndex());
                    if (view.canLeave()) {
                        super.setSelectedIndex(index);
                    } else {
                        view.notify(view.whyCannotClose(), MessageType.WARNING);
                    }
                } else { // nothing to check, so simply set the selected index
                    super.setSelectedIndex(index);
                }
            }

            /**
             * Identifies whether a new tab associated with some menu item is being selected.
             * 
             * @param fromIndex
             * @param toIndex
             * @return
             */
            private boolean shouldCheck(final int fromIndex, final int toIndex) {
                return fromIndex < tabPane.getTabCount() && fromIndex != toIndex && (fromIndex >= 0 && firstTabClosable || fromIndex > 0 && !firstTabClosable);
            }
        };
    }

    /**
     * Construct close action for tabs in order to provide ICloseGuard leveraging for menu item's view.
     * 
     * @return
     */
    private Action createCloseTabAction() {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                final int selectedTabIndex = tabPane.getSelectedIndex();
                if (selectedTabIndex > 0) {
                    final BasePanel view = (BasePanel) tabPane.getComponentAt(selectedTabIndex);
                    closeViewInTab(view);
                }
            }
        };
    }

    @Override
    protected boolean canChangePathSelection(final TreeMenuItem<?> item) {
        return item.getView().canLeave();
    }

    @Override
    protected boolean canCollapseItem(final TreeMenuItem<?> item) {
        return item.getView().canLeave();
    }

    @Override
    protected boolean canHideNode(final TreeMenuItem<?> item) {
        return item.getView().canLeave();
    }

    private boolean closeViewInTab(final BasePanel view) {
        final int tabIndex = viewItemTab(view);
        if (tabIndex >= 0) {
            final ICloseGuard unclosable = view.canClose();
            if (unclosable == null) {
                view.close();
                tabPane.removeTabAt(tabIndex);
                if (view.getAssociatedTreeMenuItem() != null) {
                    view.getAssociatedTreeMenuItem().setState(TreeMenuItemState.ALL);
                }
            } else {
                final String message = unclosable.whyCannotClose();
                view.notify(message, MessageType.WARNING);
            }
            if (tabPane.getTabCount() == 0) {
                tabPane.updateUI();
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Create an action for switching to the next tab on the right.
     * 
     * @return
     */
    private Action createNextTabAction() {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                final int selectedTabIndex = tabPane.getSelectedIndex();
                if (selectedTabIndex < tabPane.getTabCount() - 1) { // can still move to the right
                    tabPane.setSelectedIndex(selectedTabIndex + 1);
                } else {
                    tabPane.setSelectedIndex(0);
                }
            }
        };
    }

    /**
     * Create an action for switching to the previous tab on the left.
     * 
     * @return
     */
    private Action createPrevTabAction() {
        return new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(final ActionEvent e) {
                final int selectedTabIndex = tabPane.getSelectedIndex();
                if (selectedTabIndex > 0) { // can still move to the left
                    tabPane.setSelectedIndex(selectedTabIndex - 1);
                } else {
                    tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
                }
            }
        };
    }

    /**
     * Implements tab-driven menu item activation placing views of menu items on separate tabs. There is no need to leverage ICloseGuard in this methods, because it is invoked only
     * after the such check implemented in the super type.
     */
    @Override
    protected void activateItem(final TreeMenuItem<?> item) {
        // check if this menu item's view is already present amongst tabs
        final int itemIndex = menuItemTab(item);
        if (itemIndex >= 0) { // item's view is present -- activate it
            tabPane.setSelectedIndex(itemIndex);
            selectMenuItem(item);
            if (item.hasInfoPanel()) {
                tabPane.setComponentAt(0, item.getInfoPanel());
            }
        } else {

            // item's view is not yet present -- activate a corresponding info panel
            if (item.hasInfoPanel()) {
                tabPane.setComponentAt(0, item.getInfoPanel());
            } else {
                tabPane.setComponentAt(0, defaultInfoPanel);
            }
            tabPane.setSelectedIndex(0);
            tabPane.updateUI();
        }
    }

    /**
     * Selects specific view of the {@link TreeMenuItem}.
     * 
     * @param treeMenuItem
     */
    protected void selectMenuItem(final TreeMenuItem<?> treeMenuItem) {
        if (treeMenuItem instanceof MiDashboard) {
            final MiDashboard miDashboard = (MiDashboard) treeMenuItem;
            final Command<Void> command = new BlockingLayerCommand<Void>("Refresh all", miDashboard.getView().getBlockingLayer()) {

                private static final long serialVersionUID = -510484118651692975L;

                @Override
                protected boolean preAction() {
                    return super.preAction();
                }

                @Override
                protected Void action(final java.awt.event.ActionEvent e) throws Exception {
                    setMessage("Refresh sentinel list...");
                    miDashboard.getView().getDashboardPanel().refreshSentinelList();
                    setMessage("Run sentinels...");
                    miDashboard.getView().getDashboardPanel().runSentinels();
                    setMessage("Run sentinels...done");
                    Thread.sleep(500);
                    return null;
                }

                @Override
                protected void postAction(final Void value) {
                    super.postAction(value);
                    miDashboard.getView().getDashboardPanel().initSceneIfNotInitialised();
                }
            };
            command.actionPerformed(null);
        }
        if (treeMenuItem instanceof IItemSelector) {
            ((IItemSelector) treeMenuItem).selectTreeMenuItem();
        }
    }

    /**
     * Creates a menu item activation action, which reacts to enter key press on the node representing menu item and upon its double clicking.
     * 
     * @return
     */
    private ActionWithPostAction createActivateItemAction() {
        return new ActionWithPostAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void action(final ActionEvent e) {
                final TreeMenuItem<?> item = getSelectedItem();
                if (item != null && !item.isGroupItem()) {
                    final int tabIndex = menuItemTab(item);
                    if (tabIndex >= 0) { // item's view is present -- activate corresponding tab and request focus for the view
                        if (!item.getView().canOpen()) {
                            // TODO potentially need to notify user of the reason for view opening restriction
                        } else {
                            tabPane.setSelectedIndex(tabIndex);
                            selectMenuItem(item);
                            item.getView().requestFocus();
                        }
                    } else { // item is not present -- add a new tab with item's view
                        if (!item.getView().canOpen()) {
                            // TODO potentially need to notify user of the reason for view opening restriction
                        } else if (item.getState() == TreeMenuItemState.ALL) {
                            item.setState(TreeMenuItemState.DOCK);
                            tabPane.addTab(item.getTitle(), item.getView());
                            tabPane.setSelectedIndex(tabPane.getTabCount() - 1);
                            // initialisation must occur after a new tab is selected to ensure correct focus traversal
                            selectMenuItem(item);
                            item.getView().init(getBlockingPane(), item.getView());
                        }
                    }

                }
            }
        };
    }

    @Override
    public TreeMenuWithTabs<V> expandAll() {
        super.expandAll();
        return this;
    }

    /**
     * Check if the menu item's view is already present amongst tabs. If it is then the tab's index is returned. Otherwise, -1 is returned.
     * 
     * @param item
     * @return
     */
    protected int menuItemTab(final TreeMenuItem<?> item) {
        return viewItemTab(item.getView());
    }

    /**
     * Check if the view is already present amongst tabs. If it is then the tab's index is returned. Otherwise, -1 is returned.
     * 
     * @param item
     * @return
     */
    protected int viewItemTab(final BasePanel view) {
        for (int index = 0; index < tabPane.getTabCount(); index++) {
            if (tabPane.getComponentAt(index) == view) {
                return index;
            }
        }
        return -1;
    }

    private static JPanel createViewHolderPanel() {
        final JPanel holder = new BasePanel(new MigLayout("fill, insets 0", "[grow,fill]", "[grow,fill]")) {

            private static final long serialVersionUID = -1064369069412130067L;

            @Override
            public String getInfo() {
                return "Panel that holds all opened reports";
            }

            @Override
            public String whyCannotClose() {
                return "Please save or cancel all changes";
            }

            @Override
            public ICloseGuard canClose() {
                final JideTabbedPane tabPanel = getComponent(0) instanceof JideTabbedPane ? (JideTabbedPane) getComponent(0) : null;
                if (tabPanel == null) {
                    return null;
                }
                int shiftTabCount = 0;
                while (tabPanel.getTabCount() - shiftTabCount > 0) {
                    final Component componentAt = tabPanel.getComponentAt(shiftTabCount);
                    ICloseGuard result = null;
                    if (componentAt instanceof ICloseGuard) {
                        tabPanel.setSelectedComponent(componentAt);
                        result = ((ICloseGuard) componentAt).canClose();
                        if (result == null) {
                            if (componentAt instanceof BaseNotifPanel) {
                                ((BaseNotifPanel<?>) componentAt).getAssociatedTreeMenuItem().setState(TreeMenuItemState.ALL);
                                ((BaseNotifPanel<?>) componentAt).close();
                            }
                            tabPanel.removeTabAt(shiftTabCount);
                        } else {
                            return result;
                        }
                    } else {
                        shiftTabCount++;
                    }
                }
                return null;
            }

        };
        holder.setBorder(new LineBorder(new Color(140, 140, 140)));
        return holder;
    }

    /**
     * A convenient method for activating the first menu item programmatically without user action.
     */
    public void activateFirstItem() {
        setSelectionRow(0);
        activateMenuItemAction.setPostAction(null);
        activateMenuItemAction.actionPerformed(null);
    }

    public void activateFirstItem(final Action followedByAction) {
        setSelectionRow(0);
        activateMenuItemAction.setPostAction(followedByAction);
        activateMenuItemAction.actionPerformed(null);
    }

    public void selectFirstItem() {
        setSelectionRow(0);
    }

    /**
     * A convenient method for activating the specified item programmatically
     * 
     * @param item
     *            - the item that must be activated
     */
    public void activateOrOpenItem(final TreeMenuItem<?> item) {
        setSelectionPath(new TreePath(item.getPath()));
        activateMenuItemAction.actionPerformed(null);
    }

    /**
     * Closes selected tab sheet, and returns value that indicates whether panel was found among tab sheets or not.
     */
    public boolean closeView(final BasePanel panel) {
        return closeViewInTab(panel);
    }

    /**
     * Selects tab sheet that contains specified view.
     * 
     * @param view
     */
    public void selectItemWithView(final BasePanel view) {
        for (int index = 0; index < tabPane.getTabCount(); index++) {
            if (tabPane.getComponentAt(index) == view) {
                tabPane.setSelectedIndex(index);
            }
        }
    }

    private class SynchronizeWithMenuHandler extends MouseAdapter {

        @Override
        public void mouseClicked(final MouseEvent e) {
            final Rectangle rect = tabPane.getUI().getTabBounds(tabPane, tabPane.getSelectedIndex());
            if (rect != null && rect.contains(e.getPoint()) && e.getClickCount() == 2) {
                final BaseNotifPanel<?> selectedComponent = tabPane.getSelectedComponent() instanceof BaseNotifPanel ? (BaseNotifPanel<?>) tabPane.getSelectedComponent() : null;
                if (selectedComponent == null || selectedComponent.getAssociatedTreeMenuItem() == null) {
                    return;
                }
                final TreePath chosenTreePath = new TreePath(selectedComponent.getAssociatedTreeMenuItem().getPath());
                scrollPathToVisible(chosenTreePath);
                setSelectionPath(chosenTreePath);
            }

        }
    }

}
