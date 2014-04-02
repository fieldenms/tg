package ua.com.fielden.platform.swing.components;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import net.miginfocom.swing.MigLayout;

/**
 * Tooltip component that allows arbitrary Swing components to be used within tooltips. To use this class, provide the constructor with both the source component (the component to
 * provide the tooltip for) and the tooltip component (a JComponent to use as the displayed tooltip). This class can be used to provide a custom tooltip for a prefuse
 * {@link prefuse.Display} instance, by registering it with the {@link prefuse.Display#setCustomToolTip(JToolTip)} method.
 * 
 * <p>
 * In general, <code>JCustomTooltip</code> can be used with any Swing widget. This is done by overriding JComponent's <code>createToolTip</code> method such that it returns the
 * custom tooltip instance.
 * </p>
 * 
 * <p>
 * Before using this class, you might first check if you can achieve your desired custom tooltip by using HTML formatting. As with JLabel instances, the standard Swing tooltip
 * mechanism includes support for HTML tooltip text, allowing multi-line tooltips using coloring and various fonts to be created. See See <a
 * href="http://examples.oreilly.com/jswing2/code/ch04/HtmlLabel.java"> this example</a> for an instance of using HTML formatting in a JLabel. The same HTML string could be used as
 * the input to JComponent's <code>setToolTipText</code> method.
 * </p>
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public abstract class CustomTooltip extends JToolTip {

    private boolean m_persist = false;
    private Listener m_lstnr = null;

    /**
     * Create a new JCustomTooltip
     * 
     * @param src
     *            the component for which this is a tooltip
     * @param content
     *            the component to use as the tooltip content
     */
    public CustomTooltip(final JComponent src, final JComponent content) {
        this(src, content, false);
    }

    /**
     * Create a new JCustomTooltip
     * 
     * @param src
     *            the component for which this is a tooltip
     * @param content
     *            the component to use as the tooltip content
     * @param inter
     *            indicates if the tooltip should be interactive
     */
    public CustomTooltip(final JComponent src, final JComponent content, final boolean inter) {

        System.err.println("This = " + this);
        this.removeAll();

        //, ::200
        this.setLayout(new MigLayout("debug, fill, insets 4", "[grow, fill, ::200]", "[grow]")); // new SpringLayout()); // new BoxLayout(this, BoxLayout.X_AXIS)); // BorderLayout
        //this.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.gray), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        // this.setComponent(src);
        this.setComponent(src);
        //this.add(content, "grow");
        setContent(content);

        //	final int maximumWidth = 200;
        //        this.setMaximumSize(new Dimension(maximumWidth, 2000));

        setPersistent(inter);
    }

    /**
     * Indicates if the tooltip will stay persistent on the screen to support interaction within the tooltip component.
     * 
     * @return true if persistent, false otherwise.
     */
    public boolean isPersistent() {
        return m_persist;
    }

    public abstract void updateTipText(final String tipText);

    @Override
    public void setTipText(final String tipText) {
        super.setTipText(tipText);

        updateTipText(tipText);
    }

    /**
     * Sets if the tooltip will stay persistent on the screen to support interaction within the tooltip component.
     * 
     * @param inter
     *            true for persistence, false otherwise.
     */
    public void setPersistent(final boolean inter) {
        if (inter == m_persist)
            return;

        if (inter) {
            m_lstnr = new Listener();
            this.addAncestorListener(m_lstnr);
        } else {
            this.removeAncestorListener(m_lstnr);
            m_lstnr = null;
        }
        m_persist = inter;
    }

    /**
     * Set the content component of the tooltip
     * 
     * @param content
     *            the tooltip content
     */
    public void setContent(final JComponent content) {
        this.removeAll();
        this.add(content);
    }

    /**
     * @see java.awt.Component#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        if (getComponentCount() > 0) {
            final Dimension d = getComponent(0).getPreferredSize();
            final Insets ins = getInsets();
            return new Dimension(d.width + ins.left + ins.right, d.height + ins.top + ins.bottom);
        } else {
            return super.getPreferredSize();
        }
    }

    /**
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    public void paintComponent(final Graphics g) {
        if (getComponentCount() > 0) {
            // paint background
            g.setColor(getBackground());
            g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            g.setColor(getComponent(0).getBackground());
            g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
        }
    }

    /**
     * Listener class that registers the tooltip component and performs persistence management.
     */
    private class Listener extends MouseAdapter implements AncestorListener {
        private Point point = new Point();
        private boolean showing = false;
        private Popup popup;

        public void ancestorAdded(final AncestorEvent event) {
            if (showing) {
                return;
            }

            final Window ttip = SwingUtilities.getWindowAncestor(getParent());
            if (ttip == null || !ttip.isVisible()) {
                return;
            }
            //ttip.addMouseListener(this);
            ttip.getLocation(point);
            ttip.setVisible(false); // TODO ??????????????????????????????????????????????????
            getParent().remove(CustomTooltip.this);

            final JComponent c = getComponent();
            c.setToolTipText(null);
            c.removeMouseMotionListener(ToolTipManager.sharedInstance());

            popup = PopupFactory.getSharedInstance().getPopup(c, CustomTooltip.this, point.x, point.y);
            final Window w = SwingUtilities.getWindowAncestor(CustomTooltip.this);
            w.addMouseListener(this);
            w.setFocusableWindowState(true);
            popup.show();

            showing = true;
        }

        @Override
        public void mouseEntered(final MouseEvent e) {
            //            final Window ttip = SwingUtilities.getWindowAncestor(getParent());
            //            ttip.removeMouseListener(this);
            //            if ( ttip == null || !ttip.isVisible() ) {
            //                return;
            //            }
            //            ttip.getLocation(point);
            //            ttip.hide();
            //            getParent().remove(JCustomTooltip.this);
            //
            //            final JComponent c = getComponent();
            //            c.setToolTipText(null);
            //            c.removeMouseMotionListener(ToolTipManager.sharedInstance());
            //
            //            popup = PopupFactory.getSharedInstance().getPopup(
            //                    c, JCustomTooltip.this, point.x, point.y);
            //            final Window w = SwingUtilities.getWindowAncestor(JCustomTooltip.this);
            //            w.addMouseListener(this);
            //            w.setFocusableWindowState(true);
            //            popup.show();
            //
            //            showing = true;
        }

        @Override
        public void mouseExited(final MouseEvent e) {
            if (!showing)
                return;
            final int x = e.getX(), y = e.getY();
            final Component c = (Component) e.getSource();
            if (x < 0 || y < 0 || x > c.getWidth() || y > c.getHeight()) {
                final Window w = SwingUtilities.getWindowAncestor(CustomTooltip.this);
                w.removeMouseListener(this);
                w.setFocusableWindowState(false);
                popup.hide();
                popup = null;
                getComponent().setToolTipText("?");
                showing = false;
            }
        }

        public void ancestorMoved(final AncestorEvent event) {
        }

        public void ancestorRemoved(final AncestorEvent event) {
        }
    }

} // end of class JCustomTooltip

