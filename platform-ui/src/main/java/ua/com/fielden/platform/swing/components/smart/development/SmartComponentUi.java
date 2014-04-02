package ua.com.fielden.platform.swing.components.smart.development;

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
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.DefaultCaret;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.AbstractLayerUI;

import ua.com.fielden.platform.swing.utils.Utils2D;

/**
 * Implement custom painting using LayerUI to display smart button: progress indicator, search, accept and none icons. It should also implement mouse event handler to process
 * onClick events in the area of the smart button. </p> This Ui class could be used for text layers wrapped around text field with "smart" button. (e.g. autocompleters, date
 * pickers etc.)
 * 
 * @author 01es
 * 
 */
public abstract class SmartComponentUi<N extends JTextField, T extends JXLayer<N>> extends AbstractLayerUI<N> implements ActionListener {
    private final JTextField component;
    private final T layer;
    private String caption;

    ///////// class members concerning progress painting ///////////////
    private static final int DEFAULT_NUMBER_OF_BARS = 12;
    private int numBars;
    private final double dScale = 1.2;
    private Area[] bars;
    private Rectangle barsBounds = null;
    private AffineTransform centerAndScaleTransform = null;
    private final Timer timer = new Timer(1000 / 16, this);
    private Color[] colours = null;
    private int colorOffset = 0;

    private Rectangle bounds; // bounds of the smart button -- calculated dynamically

    private static final Image searchIcon = Toolkit.getDefaultToolkit().getImage(Thread.currentThread().getContextClassLoader().getResource("images/search.png"));
    private static final Image acceptIcon = Toolkit.getDefaultToolkit().getImage(Thread.currentThread().getContextClassLoader().getResource("images/checkmark.png"));
    private static final Image calendarIcon = Toolkit.getDefaultToolkit().getImage(Thread.currentThread().getContextClassLoader().getResource("images/calendar.png"));

    private final Image defaultIcon;

    private State state; // indicates the current state of the smart button
    private Point mousePos;

    public SmartComponentUi(final T layer, final String caption, final boolean useCalendarIcon) {
        initProgress(dScale); // initialise progress indicator for Progress state
        layer.setUI(this);
        this.layer = layer;
        this.caption = caption;
        component = layer.getView();
        component.setCaret(new SmartCaret());
        // load image representing Search and Accept states with media tracker to ensure they are fully loaded at the time of class instantiation
        defaultIcon = useCalendarIcon ? calendarIcon : searchIcon;
        final MediaTracker tracker = new MediaTracker(component);
        tracker.addImage(getDefaultIcon(), 1);
        tracker.addImage(acceptIcon, 2);
        try {
            tracker.waitForAll();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        setState(State.NONE); // set initial state
    }

    @Override
    protected void paintLayer(final Graphics2D g2, final JXLayer<N> l) {
        // this paints layer as is
        super.paintLayer(g2, l);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // calculate smart button bounds
        final Insets insets = component.getInsets();
        final Dimension dim = component.getSize();
        final int width = component.getSize().height - insets.bottom - insets.top;
        bounds = new Rectangle(dim.width - width - insets.right, insets.top, width, width);
        // clear the area designated to smart button
        final Rectangle oClip = bounds;
        g2.setColor(component.getBackground());
        g2.fillRect(oClip.x, oClip.y, oClip.width, oClip.height);
        // smart button custom painting, which depends on the current state
        switch (getState()) {
        case PROGRESS:
            centerAndScaleTransform = new AffineTransform();
            centerAndScaleTransform.translate(bounds.x + bounds.width / 2d, bounds.y + bounds.height / 2d);
            centerAndScaleTransform.scale(dScale, dScale);
            // calc new bars bounds
            if (barsBounds != null) {
                final Area oBounds = new Area(barsBounds);
                oBounds.transform(centerAndScaleTransform);
            }
            paintProgressTickers(g2);
            break;
        case SEARCH:
            g2.drawImage(getDefaultIcon(), bounds.x, bounds.y, bounds.width, bounds.height, null);
            break;
        case ACCEPT:
            g2.drawImage(acceptIcon, bounds.x, bounds.y, bounds.width, bounds.height, null);
            break;
        case NONE:
            // should clear the bounds to pain nothing, and paint the label if it was provided
            if (StringUtils.isEmpty(component.getText()) && !StringUtils.isEmpty(caption)) {
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
    }

    ////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////// smart button event handler //////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void processMouseEvent(final MouseEvent event, final JXLayer<N> layer) {
        if (event.getID() == MouseEvent.MOUSE_CLICKED) {
            if (bounds != null && bounds.contains(event.getPoint())) {
                getState().action(this);
            }
        }
        super.processMouseEvent(event, layer);
    }

    /**
     * This class is responsible for correct positioning of the text component caret. When user click the smart button the caret should not move.
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
    public boolean isMouseOver() {
        return mousePos != null && bounds != null ? bounds.contains(mousePos) : false;
    }

    protected void updateMouseCursor() {
        if (isMouseOver() && getState() != State.PROGRESS && getState() != State.NONE) {
            getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            getComponent().setCursor(Cursor.getDefaultCursor());
        }
    }

    ////////////////////////////////////////////////////////////
    ///////////////// progress painting logic //////////////////
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
        // move to center
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
        /*
         * Rectangle2D.Double body = new Rectangle2D.Double(6, 0, 30, 12);
         * Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 12, 12);
         * Ellipse2D.Double tail = new Ellipse2D.Double(30, 0, 12, 12);
         */

        /*
         * Rectangle2D.Double body = new Rectangle2D.Double(3, 0, 15, 6);
         * Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 6, 6);
         * Ellipse2D.Double tail = new Ellipse2D.Double(15, 0, 6, 6);
         */

        /*
         * Rectangle2D.Double body = new Rectangle2D.Double(2, 0, 10, 4);
         * Ellipse2D.Double head = new Ellipse2D.Double(0, 0, 4, 4);
         * Ellipse2D.Double tail = new Ellipse2D.Double(10, 0, 4, 4);
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

    /**
     * Paints smart button depending in the current state.
     * 
     */
    public void paintState() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // first of all stop progress indicator
                if (getState() == State.PROGRESS) {
                    timer.start(); // start anim
                } else {
                    timer.stop(); // stop anim
                }
                // the rest of the painting logic is implemented in method paintLayer,
                // which is invoked by triggering layer repainting
                setDirty(true);
            }
        });
    }

    public State getState() {
        return state;
    }

    public void setState(final State state) {
        this.state = state;
        updateMouseCursor();
    }

    public JTextField getComponent() {
        return component;
    }

    protected Rectangle getBounds() {
        return bounds;
    }

    public void setEditable(final boolean flag) {
        getComponent().setEditable(flag);
    }

    public T getLayer() {
        return layer;
    }

    /**
     * Determines the state of popup component (visible or not).
     * 
     * @return
     */
    public abstract boolean isHintsPopupVisible();

    /**
     * Shows popup component.
     */
    public abstract void showHintsPopup();

    /**
     * Performs actual popup value/s accepting.
     */
    public abstract void performAcceptAction();

    public Image getDefaultIcon() {
        return defaultIcon;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(final String caption) {
        this.caption = caption;
    }
}
