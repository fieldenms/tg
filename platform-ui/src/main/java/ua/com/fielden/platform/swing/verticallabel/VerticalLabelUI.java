package ua.com.fielden.platform.swing.verticallabel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicLabelUI;

/**
 * A UI delegate for JLabel that rotates the label 90º
 * 
 * @author oleh
 */
public class VerticalLabelUI extends BasicLabelUI {

    // set the rotating direction
    private boolean clockwise = false;
    // see comment in BasicLabelUI
    Rectangle verticalViewR = new Rectangle();
    Rectangle verticalIconR = new Rectangle();
    Rectangle verticalTextR = new Rectangle();
    protected static VerticalLabelUI verticalLabelUI = new VerticalLabelUI();
    private final static VerticalLabelUI SAFE_VERTICAL_LABEL_UI = new VerticalLabelUI();

    /**
     * Constructs a <code>VerticalLabelUI</code> with the default counterclockwise rotation
     */
    public VerticalLabelUI() {
        this(false);
    }

    /**
     * Constructs a <code>VerticalLabelUI</code> with the desired rotation.
     * <P>
     * 
     * @param clockwise
     *            true to rotate clockwise, false for counterclockwise
     */
    public VerticalLabelUI(final boolean clockwise) {
        this.clockwise = clockwise;
    }

    /**
     * @see ComponentUI#createUI(javax.swing.JComponent)
     */
    public static ComponentUI createUI(final JComponent c) {
        if (System.getSecurityManager() != null) {
            return SAFE_VERTICAL_LABEL_UI;
        } else {
            return verticalLabelUI;
        }
    }

    /**
     * Overridden to always return -1, since a vertical label does not have a meaningful baseline.
     * 
     * @see ComponentUI#getBaseline(JComponent, int, int)
     */
    @Override
    public int getBaseline(final JComponent c, final int width, final int height) {
        super.getBaseline(c, width, height);
        return -1;
    }

    /**
     * Overridden to always return Component.BaselineResizeBehavior.OTHER, since a vertical label does not have a meaningful baseline
     * 
     * @see ComponentUI#getBaselineResizeBehavior(javax.swing.JComponent)
     */
    @Override
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(final JComponent c) {
        super.getBaselineResizeBehavior(c);
        return Component.BaselineResizeBehavior.OTHER;
    }

    /**
     * Transposes the view rectangles as appropriate for a vertical view before invoking the super method and copies them after they have been altered by
     * {@link SwingUtilities#layoutCompoundLabel(FontMetrics, String, Icon, int, int, int, int, Rectangle, Rectangle, Rectangle, int)}
     */
    @Override
    protected String layoutCL(final JLabel label, final FontMetrics fontMetrics, String text, final Icon icon, Rectangle viewR, Rectangle iconR, Rectangle textR) {

        viewR.setBounds(viewR.x, viewR.y + 5, viewR.width, viewR.height + 10);
        verticalViewR = transposeRectangle(viewR, verticalViewR);
        verticalIconR = transposeRectangle(iconR, verticalIconR);
        verticalTextR = transposeRectangle(textR, verticalTextR);

        text = super.layoutCL(label, fontMetrics, text, icon, verticalViewR, verticalIconR, verticalTextR);

        viewR = copyRectangle(verticalViewR, viewR);
        iconR = copyRectangle(verticalIconR, iconR);
        textR = copyRectangle(verticalTextR, textR);
        return text;
    }

    /**
     * Transforms the Graphics for vertical rendering and invokes the super method.
     */
    @Override
    public void paint(final Graphics g, final JComponent c) {
        final Graphics2D g2 = (Graphics2D) g.create();
        if (clockwise) {
            g2.rotate(Math.PI / 2, c.getSize().width / 2, c.getSize().width / 2);
        } else {
            g2.rotate(-Math.PI / 2, c.getSize().height / 2, c.getSize().height / 2);
        }

        super.paint(g2, c);
    }

    /**
     * Returns a Dimension appropriate for vertical rendering
     * 
     * @see ComponentUI#getPreferredSize(javax.swing.JComponent)
     */
    @Override
    public Dimension getPreferredSize(final JComponent c) {
        return transposeDimension(super.getPreferredSize(c));
    }

    /**
     * Returns a Dimension appropriate for vertical rendering
     * 
     * @see ComponentUI#getMaximumSize(javax.swing.JComponent)
     */
    @Override
    public Dimension getMaximumSize(final JComponent c) {
        return transposeDimension(super.getMaximumSize(c));
    }

    /**
     * Returns a Dimension appropriate for vertical rendering
     * 
     * @see ComponentUI#getMinimumSize(javax.swing.JComponent)
     */
    @Override
    public Dimension getMinimumSize(final JComponent c) {
        return transposeDimension(super.getMinimumSize(c));
    }

    private Dimension transposeDimension(final Dimension from) {
        return new Dimension(from.height, from.width + 10);
    }

    private Rectangle transposeRectangle(final Rectangle from, Rectangle to) {
        if (to == null) {
            to = new Rectangle();
        }
        to.x = from.y;
        to.y = from.x;
        to.width = from.height;
        to.height = from.width;
        return to;
    }

    private Rectangle copyRectangle(final Rectangle from, Rectangle to) {
        if (to == null) {
            to = new Rectangle();
        }
        to.x = from.x;
        to.y = from.y;
        to.width = from.width;
        to.height = from.height;
        return to;
    }

}
