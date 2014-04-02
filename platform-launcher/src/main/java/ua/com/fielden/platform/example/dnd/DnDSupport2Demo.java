package ua.com.fielden.platform.example.dnd;

import java.awt.Point;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.application.AbstractUiApplication;
import ua.com.fielden.platform.branding.SplashController;
import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.example.entities.RotableClass;
import ua.com.fielden.platform.example.entities.RotableStatus;
import ua.com.fielden.platform.example.entities.Wheelset;
import ua.com.fielden.platform.swing.dnd.DnDSupport2;
import ua.com.fielden.platform.swing.dnd.DnDSupport2.DragFromSupport;
import ua.com.fielden.platform.swing.dnd.DnDSupport2.DragToSupport;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolox.pswing.PSwingCanvas;

/**
 * Shows the usage of {@link DnDSupport2} class. Possibility to drag rotable between {@link JLabel} and {@link PSwingCanvas} is implemented.
 * 
 * @author Yura
 */
public class DnDSupport2Demo extends AbstractUiApplication {

    private final Rotable rotable = new Wheelset("7E1S044048", "70t Pack 740mmx58mm offset SG").setStatus(RotableStatus.R).setRotableClass(new RotableClass("7E1S", "desc"));

    private final String property = "associatedObject";

    @Override
    protected void exposeUi(final String[] args, final SplashController splashController) throws Exception {
        final JFrame mainFrame = new JFrame("Demo");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLayout(new MigLayout());

        mainFrame.add(createLabel(), "w 400:400:400, h 25:25:25, wrap");
        mainFrame.add(createCanvas(), "w 400:400:400, h 400:400:400");

        mainFrame.validate();
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private JLabel createLabel() {
        final JLabel label = new JLabel(rotable.getKey().toString());
        label.putClientProperty(property, rotable);
        try {
            DnDSupport2.installDnDSupport(label, new DragFromSupport() {

                @Override
                public void dragNDropDone(final Object object, final JComponent dropTo, final int action) {
                    // removing rotable from this label
                    label.putClientProperty(property, null);
                    label.setText("Empty slot");
                }

                @Override
                public Object getObject4DragAt(final Point point) {
                    // if this label contains rotable ...
                    if (label.getClientProperty(property) != null && label.getClientProperty(property) instanceof Rotable) {
                        // returning this rotable
                        return label.getClientProperty(property);
                    } else {
                        // it doesn't contain rotable - drag is not possible - returning null
                        return null;
                    }
                }
            }, new DragToSupport() {
                @Override
                public boolean canDropTo(final Point point, final Object what, final JComponent draggedFrom) {
                    // dropping to is allowed if there is no rotable in this label
                    return label.getClientProperty(property) == null;
                }

                @Override
                public boolean dropTo(final Point point, final Object what, final JComponent draggedFrom) {
                    // putting rotable into this label
                    label.putClientProperty(property, what);
                    label.setText(((Rotable) what).getKey().toString());
                    return true;
                }
            });
        } catch (final Exception e) {
            System.err.println(e);
        }
        return label;
    }

    private PSwingCanvas createCanvas() {
        final PSwingCanvas canvas = new PSwingCanvas();
        canvas.removeInputEventListener(canvas.getPanEventHandler());
        canvas.removeInputEventListener(canvas.getZoomEventHandler());
        try {
            // much the same as with JLabel
            DnDSupport2.installDnDSupport(canvas, new DragFromSupport() {

                @Override
                public void dragNDropDone(final Object object, final JComponent dropTo, final int action) {
                    if (canvas.getClientProperty(property) != null && canvas.getClientProperty(property) instanceof RotableNode) {
                        canvas.getLayer().removeChild((RotableNode) canvas.getClientProperty(property));
                        canvas.putClientProperty(property, null);
                    }
                }

                @Override
                public Object getObject4DragAt(final Point point) {
                    if (canvas.getClientProperty(property) != null && canvas.getClientProperty(property) instanceof RotableNode) {
                        final RotableNode rotableNode = (RotableNode) canvas.getClientProperty(property);
                        if (rotableNode.getGlobalBounds().contains(point)) {
                            return rotableNode.getRotable();
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }

            }, new DragToSupport() {

                @Override
                public boolean canDropTo(final Point point, final Object what, final JComponent draggedFrom) {
                    return canvas.getClientProperty(property) == null;
                }

                @Override
                public boolean dropTo(final Point point, final Object what, final JComponent draggedFrom) {
                    final RotableNode rotableNode = new RotableNode((Rotable) what);
                    rotableNode.setOffset(point);
                    canvas.putClientProperty(property, rotableNode);
                    canvas.getLayer().addChild(rotableNode);
                    return true;
                }

            });
        } catch (final Exception e) {
            System.err.println(e);
        }
        return canvas;
    }

    public static class RotableNode extends PText {

        private static final long serialVersionUID = -3172288467582780254L;
        private final Rotable rotable;

        public RotableNode(final Rotable rotable) {
            this.rotable = rotable;
            setText(rotable.getKey().toString());
        }

        public Rotable getRotable() {
            return rotable;
        }

    }

    public static void main(final String[] args) {
        new DnDSupport2Demo().launch(args);
    }

}
