package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.uds.designer.zui.interfaces.IOnClickEventListener;
import ua.com.fielden.uds.designer.zui.util.GlobalObjects;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PActivity.PActivityDelegate;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.nodes.PComposite;

/**
 * This widget represents a form of a pop up menu, which takes a shape of a ring. The menu items are represented as ring sectors.
 * 
 * @author 01es
 * 
 */
public class RingMenu extends PComposite {
    private static final long serialVersionUID = 5279584426877295338L;
    private Map<String, RingMenuItem> menuItems = new HashMap<String, RingMenuItem>();

    public RingMenu(final String[] names, final Color[] colors, final IOnClickEventListener[] listeners, final int radius) { // , Point2D position
        assert colors != null : "Colours are not specified.";
        assert listeners != null : "Listeners are not specified.";
        assert names != null : "Menu item names are not specified.";
        assert colors.length == listeners.length : "The number of colours and listeners should match";
        assert names.length == colors.length : "The number of names and colors should match";

        setTransparency(0.f);
        buildRingMenu(names, colors, listeners, radius);
    }

    private void buildRingMenu(final String[] names, final Color[] colors, final IOnClickEventListener[] listeners, final int radius) {
        final int numOfEntries = colors.length;
        int angle = 360 / numOfEntries;
        final int gap = angle * 20 / 100;
        angle -= gap;
        // build the ring
        final int centreX = 0;
        final int centreY = 0;
        final int diameter = radius * 2;
        final Shape innerCircle = new Ellipse2D.Float(centreX - 4, centreY - 4, 8, 8);
        final Shape outerCircle = new Ellipse2D.Float(centreX - radius, centreY - radius, diameter, diameter);

        final Area innerArea = new Area(innerCircle);
        final Area ring = new Area(outerCircle);
        ring.subtract(innerArea); // removing the inner circle from the outer circle ot form a ring shape

        // calculate the ring segment shape, all ring segment nodes are base on this shape
        final double tg = Math.tan(Math.toRadians(angle / 2.));
        final int h = (radius);
        final int side = (int) (h * tg);

        final int x1 = centreX;
        final int y1 = centreY;
        final int xm = x1;
        final int ym = centreY - (h);
        final int x2 = xm + side;
        final int y2 = ym;
        final int x3 = xm - side;
        final int y3 = ym;

        final Polygon tri = new Polygon(new int[] { x1, x2, x3 }, new int[] { y1, y2, y3 }, 3); // the segment shape as a triangle ABC, where A is located at the
        // circle centre, BC is horisontal
        final Area triArea = new Area(tri);
        triArea.subtract(ring); // this removes the desired segment shape from a triangle
        final Area sector = new Area(tri);
        sector.exclusiveOr(triArea); // this bring the desired segment shape and removes all the rest from a triangle.

        // build a transparent background, which is slightly wider than the base circle used for sectors
        final int widerBy = 1;
        final Shape baseCircle = new Ellipse2D.Float(centreX - (radius + widerBy), centreY - (radius + widerBy), diameter + 2 * widerBy, diameter + 2 * widerBy);
        final PPath background = new PPath(baseCircle);
        background.setPaint(new Color(1.f, 1.f, 1.f, 0.7f));
        background.setStroke(null);
        addChild(background);

        angle += gap;

        if (numOfEntries == 1) {
            menuItems.put(names[0], produceMenuItem(ring, 0, colors[0], listeners[0], x1, y1));
            addChild(menuItems.get(names[0]));
        } else {
            for (int index = 0; index < numOfEntries; index++) {
                menuItems.put(names[index], produceMenuItem(sector, angle * index, colors[index], listeners[index], x1, y1));
                addChild(menuItems.get(names[index]));
            }
        }
        setMenuEventHandler(new MenuEvent());
        addInputEventListener(menuEventHandler);
    }

    private MenuEvent menuEventHandler;

    protected RingMenuItem produceMenuItem(final Shape sector, final int angle, final Color color, final IOnClickEventListener listener, final int x, final int y) {
        Shape rotatedSector = sector;
        if (angle > 0) { // no point performing rotation if angle is 0
            final AffineTransform at = AffineTransform.getRotateInstance(Math.toRadians(angle), x, y);
            rotatedSector = at.createTransformedShape(sector);
        }

        final RingMenuItem menuItem = new RingMenuItem(rotatedSector, color, listener);
        return menuItem;
    }

    public void show(final Point2D position) {
        show(position.getX(), position.getY());
    }

    public void show(final double x, final double y) {
        if (getParent() == null) {
            setOffset(x - getWidth() / 2., y + getHeight() / 2.);
            GlobalObjects.nodeLayer.addChild(this);
            this.moveToFront();
            animateToTransparency(1.f, 200);
        }
    }

    public void hide() {
        final PActivity activity = animateToTransparency(0.f, 200);
        activity.setDelegate(new PActivityDelegate() {
            public void activityFinished(final PActivity arg0) {
                removeFromParent();
            }

            public void activityStarted(final PActivity arg0) {
            }

            public void activityStepped(final PActivity arg0) {
            }
        });
    }

    /**
     * This method is implemented in order to provide event listener re-association upon menu instance recovery from serialization
     */
    private void readObject(final ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();

        removeInputEventListener(menuEventHandler);
        addInputEventListener(menuEventHandler);
    }

    /**
     * This a menu event listener implementation.
     * 
     * @author 01es
     */
    private class MenuEvent extends PBasicInputEventHandler implements Serializable {
        private static final long serialVersionUID = -4203715529707842477L;

        public MenuEvent() {
        }

        /**
         * Due to the fact that menu is of type PComposite there is no possiblity to assign events to its composites (i.e. menu items). Therefore it is necessary to determine,
         * which menu item is affected by checking mouse position, which is implemented by this method.
         * 
         * @param mousePosition
         * @return
         */
        private RingMenuItem determineAffectedMenuItem(final Point2D mousePosition) {
            for (final RingMenuItem menuItem : getMenuItems()) {
                if (menuItem.getPathReference().contains(mousePosition)) {
                    return menuItem;
                }
            }
            return null;
        }

        public void mouseExited(final PInputEvent event) {
            hide();
        }

        /**
         * This event handler implements invocation of menu item onClick action.
         */
        public void mouseClicked(final PInputEvent event) {
            if (event.getButton() == 1) {
                final RingMenuItem item = determineAffectedMenuItem(event.getPositionRelativeTo(RingMenu.this));
                if (item != null) {
                    item.doClick(event);
                }
            } else {
                mouseExited(event);
            }
        }

        RingMenuItem prevAffectedMenuItem;

        /**
         * This event handler implements menu items highlighting.
         */
        public void mouseMoved(final PInputEvent event) {
            final RingMenuItem menuItem = determineAffectedMenuItem(event.getPositionRelativeTo(RingMenu.this));

            if (prevAffectedMenuItem != menuItem) {
                if (prevAffectedMenuItem != null) {
                    prevAffectedMenuItem.dehighlight();
                }
                if (menuItem != null) {
                    menuItem.highlight();
                }
            }

            prevAffectedMenuItem = menuItem;
        }
    }

    private Collection<RingMenuItem> getMenuItems() {
        return menuItems.values();
    }

    public RingMenuItem getMenuItem(final String name) {
        return menuItems.get(name);
    }

    public MenuEvent getMenuEventHandler() {
        return menuEventHandler;
    }

    private void setMenuEventHandler(final MenuEvent menuEventHandler) {
        this.menuEventHandler = menuEventHandler;
    }
}
