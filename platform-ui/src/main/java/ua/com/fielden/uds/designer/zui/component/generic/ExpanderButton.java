package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.Color;

import ua.com.fielden.uds.designer.zui.interfaces.IOnClickEventListener;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * This is a button, which contains only an expander node and does not containt a title. Its onClick event is redirected to the provided expander node. The "pedding" concept
 * (pedding property of AbstractNode) plays a significant role for determining ExpanderButton size. The use of padding property ensure that an expander node is located at an
 * appropriate location. Refer also to available constructors.
 * 
 * @author 01es
 * 
 */
public class ExpanderButton extends Button {
    private static final long serialVersionUID = -7453712003664202329L;

    private ExpanderNode expander;

    /**
     * This constructor uses the default pedding value and place an expander node in the centre of the button.
     * 
     * @param expander
     */
    public ExpanderButton(ExpanderNode expander) {
        setPedding(new AbstractNode.Pedding(8, 8, 17, 15));
        init(expander);
    }

    /**
     * This constructor uses the provided pedding to locate an expander node. If the provided pedding is null it falls back to the default pedding value.
     * 
     * @param expander
     * @param pedding
     */
    public ExpanderButton(ExpanderNode expander, Pedding pedding) {
        if (pedding == null) {
            setPedding(new AbstractNode.Pedding(8, 8, 17, 15));
        } else {
            setPedding(pedding);
        }
        init(expander);
    }

    /**
     * This is a convenience constructor, which uses the button parameter to determine the size of the button to be constructed. In this case an expander node is placed at the
     * centre of the constructed button. This constructor if very useful where a set of buttons is created for the same tool bar in order to make them sized evenly.
     * 
     * @param expander
     * @param button
     *            -- button to be used as a model for the creation of an expander button
     */
    public ExpanderButton(ExpanderNode expander, Button button) {
        double horisontalPedding = button.getWidth() - expander.getWidth();
        double verticalPedding = button.getHeight() - expander.getHeight();
        // FIXME there is a rounding issue here, which needs to be fixed somehow...
        setPedding(new AbstractNode.Pedding((int) (verticalPedding / 2. + 1), (int) (verticalPedding / 2. + 0.6), (int) (horisontalPedding / 2. + 2), (int) (horisontalPedding / 2.)));
        setMinConstraint(new PDimension(button.getWidth() + 1, button.getHeight() + 1));
        init(expander);
    }

    private void init(ExpanderNode expander) {
        this.expander = expander;
        setCurvaturePrc(20);
        setUp(null);
        reshape(false);
    }

    protected void setUp(String title) {
        expander.setPickable(false); // the on click behaviour is redirected to the parent button
        addChild(expander);
        reshape(false);
        fillWithGradient(new Color(17, 64, 111), new Color(148, 193, 239), true); // default color filling

        setDefaultEventHandler(new ExpanderDefaultEventHandler(this));

        addOnClickEventListener(new IOnClickEventListener() {
            private static final long serialVersionUID = -7732080335350183919L;

            public void click(PInputEvent event) {
                expander.getDefaultEventHandler().mouseClicked(event);
            }
        });
    }

    public String toString() {
        return "";
    }

    /**
     * This class implements button's behaviour (e.g. highlighting, onClick event).
     * 
     * @author 01es
     * 
     */
    protected static class ExpanderDefaultEventHandler extends DefaultEventHandler {
        private static final long serialVersionUID = -4900315528622765955L;

        public ExpanderDefaultEventHandler(Button button) {
            super(button);
        }

        /**
         * Handles button highlighting.
         */
        public void mouseEntered(PInputEvent event) {
            if (event.isLeftMouseButton()) {
                return;
            }

            if (highPaint == null) {
                highPaint = new SerializableGradientPaint((float) button.getX(), (float) button.getY(), new Color(185, 223, 244), (float) (button.getX()), (float) (button.getY() + +button.getHeight()), new Color(61, 168, 226));
            }

            button.setPaint(highPaint);
        }
    }

}
