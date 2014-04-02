package ua.com.fielden.platform.swing.menu.filter.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JTextField;
import javax.swing.text.DefaultCaret;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;

import ua.com.fielden.platform.swing.utils.Utils2D;

/**
 * This is a UI for {@link FilterControl}, which paints caption and support "cancel filter" button support.
 * <p>
 * It features support for a progress indicator, which is currently commented out due to the fact that filtering is happening on EDT. In case it is moved off EDT, the progress
 * indicator should be reinstanted.
 * 
 * @author 01es
 * 
 */
class FilterUi extends AbstractLayerUI<JTextField> implements ActionListener {
    private final JTextField component;
    private final FilterControl layer;
    private final String caption;

    ///////// class members concerning progress painting ///////////////
    private static final int DEFAULT_NUMBER_OF_BARS = 12;
    private int numBars;
    private final double dScale = 1.2;
    private Area[] bars;
    private Rectangle barsBounds = null;
    private AffineTransform centerAndScaleTransform = null;
    //private final Timer timer = new Timer(1000 / 16, this); // this timer is used to draw infinite progress indicator
    private Color[] colours = null;
    private int colorOffset = 0;

    private Rectangle bounds; // bounds of the cancel filter button -- calculated dynamically

    private static final Image xIcon = Toolkit.getDefaultToolkit().getImage(Thread.currentThread().getContextClassLoader().getResource("images/x.png"));

    private Point mousePos;

    public FilterUi(final FilterControl layer, final String caption) {
        initProgress(dScale); // initialise progress indicator for Progress state
        layer.setUI(this);
        this.layer = layer;
        this.caption = caption;
        component = layer.getView();
        component.setCaret(new SmartCaret());
        // load image representing cancel filter button with media tracker to ensure it is fully loaded at the time of class instantiation
        final MediaTracker tracker = new MediaTracker(component);
        tracker.addImage(xIcon, 1);
        try {
            tracker.waitForAll();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintLayer(final Graphics2D g2, final JXLayer<JTextField> l) {
        // this paints layer as is
        super.paintLayer(g2, l);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // calculate cancel filter button bounds
        final Insets insets = component.getInsets();
        final Dimension dim = component.getSize();
        final int width = component.getSize().height - insets.bottom - insets.top;
        bounds = new Rectangle(dim.width - width - insets.right, insets.top, width, width);
        // clear the area designated to smart button
        final Rectangle oClip = bounds;
        g2.setColor(component.getBackground());
        g2.fillRect(oClip.x, oClip.y, oClip.width, oClip.height);

        // TODO if filtering will be moved to worker thread then will need to should progress
        /*
          centerAndScaleTransform = new AffineTransform();
          centerAndScaleTransform.translate(bounds.x + bounds.width / 2d, bounds.y + bounds.height / 2d);
          centerAndScaleTransform.scale(dScale, dScale);
          // calc new bars bounds
          if (barsBounds != null) {
              final Area oBounds = new Area(barsBounds);
              oBounds.transform(centerAndScaleTransform);
          }
          paintProgressTickers(g2);
        */

        // smart button custom painting, which depends on the current state
        if (!StringUtils.isEmpty(getComponent().getText())) {
            g2.drawImage(xIcon, bounds.x, bounds.y, bounds.width, bounds.height, null);
        } else if (!StringUtils.isEmpty(caption) && !getComponent().hasFocus()) {
            // should clear the bounds to pain nothing, and paint the label if it was provided
            // clear the area designated to smart button
            g2.setColor(component.getBackground());
            int w = component.getSize().width - (component.getInsets().left + component.getInsets().right);
            int h = component.getSize().height - (component.getInsets().top + component.getInsets().bottom);
            g2.fillRect(component.getInsets().left, component.getInsets().top, w, h);
            // pain the caption
            g2.setColor(new Color(0f, 0f, 0f, 0.6f));
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
            // define how many characters in the caption can be drawn
            final String textToDisplay = Utils2D.abbreviate(g2, caption, w);
            w = component.getSize().width;
            h = component.getSize().height;
            final double xPos = insets.left;
            final Rectangle2D textBounds = g2.getFontMetrics().getStringBounds(textToDisplay, g2);
            final double yPos = (h - textBounds.getHeight()) / 2. + g2.getFontMetrics().getAscent();
            g2.drawString(textToDisplay, (float) xPos, (float) yPos);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////// cancel filter button event handler ///////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void processMouseEvent(final MouseEvent event, final JXLayer<JTextField> layer) {
        if (event.getID() == MouseEvent.MOUSE_CLICKED) {
            if (bounds != null && bounds.contains(event.getPoint())) {
                this.layer.clear();
            }
        }
        super.processMouseEvent(event, layer);
    }

    /**
     * This class is responsible for correct positioning of the text component caret. When user click the clear filter button the caret should not move.
     * 
     * @author 01es
     * 
     */
    private class SmartCaret extends DefaultCaret {
        private static final long serialVersionUID = 1L;

        @Override
        protected void positionCaret(final MouseEvent event) {
            if (bounds != null && !bounds.contains(event.getPoint())) {
                super.positionCaret(event);
            }
        }

        @Override
        protected void moveCaret(final MouseEvent event) {
            if (bounds != null && !bounds.contains(event.getPoint())) {
                super.moveCaret(event);
            }
        }

        /**
         * Controls the shape of mouse cursor for the text component, and updates current mouse position.
         */
        @Override
        public void mouseMoved(final MouseEvent event) {
            super.mouseMoved(event);
            mousePos = event.getPoint(); // store current mouse position
            updateMouseCursor();
        }

        @Override
        public void mousePressed(final MouseEvent event) {
            if (bounds != null && !bounds.contains(event.getPoint())) {
                super.mousePressed(event);
            }
            super.mousePressed(event);
        }

        @Override
        public void mouseClicked(final MouseEvent event) {
            if (bounds != null && !bounds.contains(event.getPoint())) {
                super.mouseClicked(event);
            }
        }
    }

    /**
     * Test whether mouse is positioned over the area representing smart button.
     * 
     * @return
     */
    protected boolean isMouseOver() {
        return mousePos != null && bounds != null ? bounds.contains(mousePos) : false;
    }

    protected void updateMouseCursor() {
        if (isMouseOver() && !StringUtils.isEmpty(getComponent().getText())) {
            getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            getComponent().setCursor(Cursor.getDefaultCursor());
        }
    }

    ////////////////////////////////////////////////////////////
    ///////////////// PROGRESS PAINTING LOGIC //////////////////
    ////////////////////////////////////////////////////////////
    /**
     * Called to animate the rotation of the bar's colours
     */
    public void actionPerformed(final ActionEvent e) {
        // rotate colours
        if (colorOffset == (numBars - 1)) {
            colorOffset = 0;
        } else {
            colorOffset++;
        }
        // repaint
        setDirty(true);
    }

    /**
     * Builds the initial shape of the progress indicator.
     * 
     * @param scale
     */
    public void initProgress(final double scale) {
        this.numBars = DEFAULT_NUMBER_OF_BARS;
        colours = new Color[numBars * 2];
        // build bars
        bars = buildTicker(numBars);
        // calculate bars bounding rectangle
        barsBounds = new Rectangle();
        for (final Area bar : bars) {
            barsBounds = barsBounds.union(bar.getBounds());
        }
        // create colours
        for (int i = 0; i < bars.length; i++) {
            final int channel = 224 - 128 / (i + 1);
            colours[i] = new Color(channel, channel, channel);
            colours[numBars + i] = colours[i];
        }
    }

    /**
     * Paints progress indicator using passed Graphics instance.
     * 
     * @param g
     */
    protected void paintProgressTickers(final Graphics g) {
        // move to centre
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.transform(centerAndScaleTransform);
        // draw ticker
        for (int i = 0; i < bars.length; i++) {
            g2.setBackground(new Color(255, 255, 255, 255));
            g2.setColor(colours[i + colorOffset]);
            g2.fill(bars[i]);
        }
    }

    /**
     * Builds the circular shape and returns the result as an array of <code>Area</code>. Each <code>Area</code> is one of the bars composing the shape.
     */
    private Area[] buildTicker(final int i_iBarCount) {
        final Area[] ticker = new Area[i_iBarCount];
        final Point2D.Double center = new Point2D.Double(0, 0);
        final double fixedAngle = 2.0 * Math.PI / (i_iBarCount);

        for (double i = 0.0; i < i_iBarCount; i++) {
            final Area primitive = buildPrimitive();

            final AffineTransform toCenter = AffineTransform.getTranslateInstance(center.getX(), center.getY());
            final AffineTransform toBorder = AffineTransform.getTranslateInstance(2.0, -0.4);
            final AffineTransform toCircle = AffineTransform.getRotateInstance(-i * fixedAngle, center.getX(), center.getY());

            final AffineTransform toWheel = new AffineTransform();
            toWheel.concatenate(toCenter);
            toWheel.concatenate(toBorder);

            primitive.transform(toWheel);
            primitive.transform(toCircle);

            ticker[(int) i] = primitive;
        }

        return ticker;
    }

    /**
     * Builds a bar.
     */
    private Area buildPrimitive() {
        /* Rectangle2D.Double body = new Rectangle2D.Double(6, 0, 30, 12);
        Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 12, 12);
        Ellipse2D.Double tail = new Ellipse2D.Double(30, 0, 12, 12);
        */

        /* Rectangle2D.Double body = new Rectangle2D.Double(3, 0, 15, 6);
        Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 6, 6);
        Ellipse2D.Double tail = new Ellipse2D.Double(15, 0, 6, 6);
        */

        /* Rectangle2D.Double body = new Rectangle2D.Double(2, 0, 10, 4);
        Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 4, 4);
        Ellipse2D.Double tail = new Ellipse2D.Double(10, 0, 4, 4);
        */

        final Rectangle2D.Double body = new Rectangle2D.Double(0, 0, 6, 1);
        //Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 2, 2);
        //Ellipse2D.Double tail = new Ellipse2D.Double(5, 0, 2, 2);

        final Area tick = new Area(body);
        //tick.add(new Area(head));
        //tick.add(new Area(tail));
        return tick;
    }

    /////////////////////// END OF PROGRESS PAINTING LOGIC ////////////////////

    public JTextField getComponent() {
        return component;
    }

    protected Rectangle getBounds() {
        return bounds;
    }

    public void setEditable(final boolean flag) {
        getComponent().setEditable(flag);
    }
}
