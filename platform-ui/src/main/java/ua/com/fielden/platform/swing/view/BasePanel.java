package ua.com.fielden.platform.swing.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressPane;
import ua.com.fielden.platform.swing.ei.editors.development.IPropertyEditor;
import ua.com.fielden.platform.swing.menu.TreeMenuItem;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.model.IOpenGuard;
import ua.com.fielden.platform.swing.utils.DummyBuilder;

/**
 * A base class for all guarded panels.
 * 
 * @author 01es
 * 
 */
public abstract class BasePanel extends JPanel implements ICloseGuard, IOpenGuard {
    private static final long serialVersionUID = 1L;

    private TreeMenuItem<? extends BasePanel> associatedTreeMenuItem = null;

    public BasePanel() {
    }

    public BasePanel(final LayoutManager layoutManager) {
        super(layoutManager);
    }

    @Override
    public ICloseGuard canClose() {
        return checkCanClose(this);
    }

    @Override
    public String whyCannotClose() {
        return "default implementation: should have been overridden";
    }

    @Override
    public void close() {
        tryClosing(this);
    }

    /**
     * Closes guarded items. Even if guarded container has guarded children only this guarded (top level) container is closed.
     * 
     * @param parent
     */
    private void tryClosing(final Container parent) {
        for (final Component component : parent.getComponents()) {
            if (component instanceof ICloseGuard) {
                final ICloseGuard guard = (ICloseGuard) component;
                guard.close();
            } else if (component instanceof Container) {
                tryClosing((Container) component);
            }
        }
    }

    /**
     * Traverses the tree of components in search for the first {@link ICloseGuard} descendant, which cannot be closed.
     * 
     * @param parent
     * @return
     */
    private ICloseGuard checkCanClose(final Container parent) {
        for (final Component component : parent.getComponents()) {
            if (component instanceof ICloseGuard) {
                final ICloseGuard guard = (ICloseGuard) component;
                final ICloseGuard result = guard.canClose();
                if (result != null) {
                    return result;
                }
            } else if (component instanceof Container) {
                final ICloseGuard guard = checkCanClose((Container) component);
                if (guard != null) {
                    return guard;
                }
            }
        }
        return null;
    }

    @Override
    public boolean canLeave() {
        return canClose() == null;
    }

    /**
     * Should be overwritten to provide at least some short information about panel's purpose.
     * 
     * @return
     */
    public abstract String getInfo();

    /**
     * Returns {@link TreeMenuItem} associated with this {@link BaseNotifPanel}.
     * 
     * @return
     */
    public TreeMenuItem<? extends BasePanel> getAssociatedTreeMenuItem() {
        return associatedTreeMenuItem;
    }

    /**
     * Set the associated {@link TreeMenuItem} to the specified one. Please notice that it is possible to set associated tree menu item just once.
     * 
     * @param associatedTreeMenuItem
     */
    public void setAssociatedTreeMenuItem(final TreeMenuItem<? extends BasePanel> associatedTreeMenuItem) {
        if (this.associatedTreeMenuItem == null) {
            this.associatedTreeMenuItem = associatedTreeMenuItem;
        }
    }

    /**
     * This method should be used to notify the panel of some message. Exactly how this modification is handled visually is controlled by subclasses of this class. By default a
     * message dialog is displayed.
     * 
     * @param message
     * @param messageType
     */
    public void notify(final String message, final MessageType messageType) {
        JOptionPane.showMessageDialog(this, message, "Warning", messageType.jopMessageType);
    };

    /**
     * Should be overridden by subtypes in order to implement custom initialisation logic for the view.
     * 
     * @param blockingPane
     *            -- this blocking pane is used to block the view during its initialisation.
     * @param toBeFocusedAfterInit
     *            -- this component
     */
    public void init(final BlockingIndefiniteProgressPane blockingPane, final JComponent toBeFocusedAfterInit) {
    }

    /**
     * Similar as above, but does not specify that component should be focused once initialisation is completed. This method is really just a convenience that simply invokes method
     * {@link #init(BlockingIndefiniteProgressPane, JComponent)} with <code>null</code> as the second argument.
     * 
     * @param blockingPane
     */
    public final void init(final BlockingIndefiniteProgressPane blockingPane) {
        init(blockingPane, null);
    }

    @Override
    public boolean canOpen() {
        return true;
    }

    @Override
    public String whyCannotOpen() {
        return "default implementation: should have been overridden";
    }

    // ///////////////////////////////////////////////////////////////////////////////////
    // ////////////////////// A set of UI layout helper methods //////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////
    /**
     * Determines an editor from a map of editors by property name and adds its label and the editor itself to the provide panel. The last parameter is responsible for layout
     * settings of the editor component.
     */
    protected void addWithParamsForEditor(final JPanel panel, final Map<String, IPropertyEditor> editors, final String propertyName, final String layoutParameter) {
        final IPropertyEditor editor = editors.get(propertyName);
        panel.add(editor.getLabel());
        panel.add(editor.getEditor(), layoutParameter);
    }

    protected void addWithParamsForEditorPlaceholder(final JPanel panel, final String layoutParameter) {
        panel.add(new JLabel("<html><b><font color=\"red\">Placeholder:</font></b></html>"));
        panel.add(new JTextField(" "), layoutParameter);
    }

    /**
     * Determines an editor from a map of editors by property name and adds its label and the editor itself to the provide panel. The last parameter is responsible for layout
     * settings of the label component.
     */
    protected void addWithParamsForLabel(final JPanel panel, final Map<String, IPropertyEditor> editors, final String propertyName, final String layoutParameter) {
        final IPropertyEditor editor = editors.get(propertyName);
        panel.add(editor.getLabel(), layoutParameter);
        panel.add(editor.getEditor());
    }

    /** This is a convenient layout helper method, which does not require any layout settings. */
    protected void add(final JPanel panel, final Map<String, IPropertyEditor> editors, final String propertyName) {
        addWithParamsForEditor(panel, editors, propertyName, "");
    }

    /** This is a convenient layout helper method, which does accepts any component and simply adds it to the panel with the specified parameters. */
    protected void addComponent(final JPanel panel, final JComponent component, final String layoutParameter) {
        panel.add(component, layoutParameter);
    }

    /** This is a convenient layout helper method for adding editor following the wrap setting. */
    protected void addAndWrap(final JPanel panel, final Map<String, IPropertyEditor> editors, final String propertyName) {
        addWithParamsForEditor(panel, editors, propertyName, "wrap");
    }

    /** Adds a named separator */
    protected void addSeparator(final JPanel panel, final String label) {
        panel.add(DummyBuilder.label(label, new Color(0x175c9a)), "gapbottom 5, gaptop 20px, span, split 2, aligny center");
        panel.add(new JSeparator(), "gapleft rel, gapbottom 5, gaptop 20px, growx");
    }

}
