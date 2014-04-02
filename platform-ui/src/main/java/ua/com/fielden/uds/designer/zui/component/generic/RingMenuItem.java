package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.Color;
import java.awt.Shape;
import java.awt.Stroke;

import ua.com.fielden.uds.designer.zui.interfaces.IOnClickEventListener;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PActivity.PActivityDelegate;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * This class represents an item for the ring menu.
 * 
 * @author 01es
 * 
 */
public class RingMenuItem extends PPath {
    private static final long serialVersionUID = 1919851180836459352L;
    // TODO need to provide support for a glyph
    private Color backgroundColor;
    private Color highlightColor;
    private IOnClickEventListener listener;

    public RingMenuItem(Shape shape, IOnClickEventListener listener) {
        super(shape);
        this.listener = listener;

        Stroke stroke = null; // new DefaultStroke(0.5f, DefaultStroke.CAP_BUTT, DefaultStroke.JOIN_ROUND);
        setStroke(stroke);

        setBackgroundColor(new Color(46, 91, 124));
        highlightColor = getBackgroundColor().brighter();
    }

    public RingMenuItem(Shape shape, Color color, IOnClickEventListener listener) {
        this(shape, listener);
        setBackgroundColor(color);
        highlightColor = getBackgroundColor().brighter();
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color color) {
        backgroundColor = color;
        setPaint(color);
        setStrokePaint(color);
        highlightColor = color.brighter();
    }

    public void doClick(PInputEvent event) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener is not specified");
        }

        listener.click(event);
        ((RingMenu) getParent()).hide();
    }

    public void highlight() {
        animateToColor(highlightColor, 200);
    }

    public void dehighlight() {
        animateToColor((Color) getBackgroundColor(), 200);
    }

    public void animateClick() {
        PActivity activity = animateToColor(Color.white, 200);
        activity.setDelegate(new PActivityDelegate() {
            Color paint;

            public void activityFinished(PActivity arg0) {
                // TODO Auto-generated method stub
                animateToColor(paint, 200);
            }

            public void activityStarted(PActivity arg0) {
                paint = (Color) getPaint();
            }

            public void activityStepped(PActivity arg0) {
            }
        });
    }
}
