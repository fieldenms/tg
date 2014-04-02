package ua.com.fielden.platform.swing.view;

import java.awt.Component;
import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.components.NotificationLayer.MessageType;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.menu.MenuNotificationPanel;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.model.UModel;

/**
 * A guarded panel with notification area (see {@link MenuNotificationPanel}), which by default appears at the top.
 * <p>
 * Method {@link #layoutComponents()} should be overridden if an alternative layout is required.
 * <p>
 * This panel also provides blocking capabilities. Refer methods {@link #lock()}, {@link #unlock()} and {@link #setBlockingMessage(String)} for more details.
 * 
 * @author TG Team
 * 
 */
public abstract class BaseNotifPanel<MODEL extends UModel> extends BasePanelWithModel<MODEL> {
    private static final long serialVersionUID = -7534147287897859591L;

    private final MenuNotificationPanel notifPanel;

    private final JPanel holdingPanel = new JPanel(new MigLayout("fill, insets 0", "[fill, grow]", "[][c,grow,fill]"));
    private final BlockingIndefiniteProgressLayer blockingLayer = new BlockingIndefiniteProgressLayer(holdingPanel, "");

    /**
     * Principle constructor, which specifies the panel caption represented on the left side of the notification area, and a model.
     * 
     * @param caption
     * @param model
     */
    public BaseNotifPanel(final String caption, final MODEL model) {
        super(model);

        super.setLayout(new MigLayout("fill, insets 0", "[c,fill,grow]", "[c,grow,fill]"));
        super.add(blockingLayer);

        notifPanel = new MenuNotificationPanel(caption);
        layoutComponents();
    }

    /**
     * This constructor should be used when several views are mashed up and there should be only one notification panel (the provided one).
     * 
     * @param notifPanel
     * @param model
     */
    public BaseNotifPanel(final MenuNotificationPanel notifPanel, final MODEL model) {
        super(model);

        super.setLayout(new MigLayout("fill, insets 0", "[c,fill,grow]", "[c,grow,fill]"));
        super.add(blockingLayer);

        this.notifPanel = notifPanel;
        setLayout(new MigLayout("fill, insets 0", "[fill, grow]", "[c,grow,fill]"));
    }

    /** A convenient method to get the frame this view is associated with. */
    public BaseFrame getFrame() {
        return (BaseFrame) SwingUtilities.getWindowAncestor(this);
    }

    /**
     * Locks the panel. Must be invoked on EDT.
     */
    public void lock() {
        blockingLayer.setLocked(true);
    }

    /**
     * Unlocks the panel. Must be invoked on EDT.
     */
    public void unlock() {
        blockingLayer.setLocked(false);
    }

    /**
     * Sets message on the blocking layer, which is displayed while panel is locked.
     * 
     * @param msg
     */
    public void setBlockingMessage(final String msg) {
        blockingLayer.setText(msg);
    }

    /**
     * Adds notification panel and provides its default layout in respect to <code>this</code> container.
     */
    protected void layoutComponents() {
        setLayout(new MigLayout("fill, insets 0", "[fill, grow]", "[][c,grow,fill]"));
        add(getNotifPanel(), "wrap");
    }

    /**
     * Overridden to ensure that all layout is set on the holding panel.
     */
    @Override
    public void setLayout(final LayoutManager mgr) {
        if (holdingPanel != null) {
            holdingPanel.setLayout(mgr);
        } else {
            super.setLayout(mgr);
        }
    }

    /**
     * Overridden to ensure that components are added to the holding panel.
     */
    @Override
    public Component add(final Component comp) {
        return holdingPanel.add(comp);
    }

    /**
     * Overridden to ensure that components are added to the holding panel.
     */
    @Override
    public void add(final Component comp, final Object constraints) {
        holdingPanel.add(comp, constraints);
    }

    /**
     * Overridden to ensure that components are added to the holding panel.
     */
    @Override
    public void add(final Component comp, final Object constraints, final int index) {
        holdingPanel.add(comp, constraints, index);
    }

    /**
     * Overridden to ensure that components are added to the holding panel.
     */
    @Override
    public Component add(final Component comp, final int index) {
        return holdingPanel.add(comp, index);
    }

    /**
     * Overridden to ensure that components are added to the holding panel.
     */
    @Override
    public Component add(final String name, final Component comp) {
        return holdingPanel.add(name, comp);
    }

    /**
     * This method should be used to display notification on the associated notification panel.
     * 
     * @param message
     * @param messageType
     */
    @Override
    public void notify(final String message, final MessageType messageType) {
        notifPanel.setMessage(message, messageType);
    }

    public MenuNotificationPanel getNotifPanel() {
        return notifPanel;
    }

    @Override
    public String toString() {
        return getCaption();
    }

    public final String getCaption() {
        return notifPanel.getCaption();
    }

    public BlockingIndefiniteProgressLayer getBlockingLayer() {
        return blockingLayer;
    }

    @Override
    public boolean canLeave() {
        return getModel().canLeave();
    }

    @Override
    public ICloseGuard canClose() {
        return getModel().canClose();
    }

    @Override
    public String whyCannotClose() {
        return getModel().whyCannotClose();
    }

    protected JPanel getHoldingPanel() {
        return holdingPanel;
    }

}
