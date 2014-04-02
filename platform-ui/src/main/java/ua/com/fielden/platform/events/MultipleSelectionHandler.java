package ua.com.fielden.platform.events;

import java.awt.event.InputEvent;
import java.util.List;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolox.event.PSelectionEventHandler;

/**
 * MultipleSelectionHandler extends PSelectionEventHandler class and is responsible for selecting nodes dragging for that selection handler was disabled
 * 
 * @author oleh
 * 
 */
public class MultipleSelectionHandler extends PSelectionEventHandler {

    /**
     * create new instance of the Multiple selection Handler for the given marquee parent and selectable parent
     * 
     * @param marqueeParent
     *            - specified instance of PNode that is where the marquee will be placed during marquee selection
     * @param selectableParent
     *            - specified PNode instance which children might be selected
     */
    public MultipleSelectionHandler(final PNode marqueeParent, final PNode selectableParent) {
        super(marqueeParent, selectableParent);
        setEventFilter(new PInputEventFilter(InputEvent.BUTTON1_MASK));
    }

    /**
     * creates new instance of the Multiple selection handler for the given marquee parent and selectable parents
     * 
     * @param marqueeParent
     *            - specified instance of PNode that is where the marquee will be placed during marquee selection
     * @param selectableParents
     *            - specified list of PNode elements which children might be selected
     */
    public MultipleSelectionHandler(final PNode marqueeParent, final List<PNode> selectableParents) {
        super(marqueeParent, selectableParents);
    }

    /**
     * decorates the PNode instance if it's selected
     */
    @Override
    public void decorateSelectedNode(final PNode node) {
        if (node instanceof IDecorable) {
            ((IDecorable) node).Decorate();
        }
    }

    /**
     * return true if the option key that is responsible for selecting the group of nodes was pressed during the selection process
     */
    @Override
    public boolean isOptionSelection(final PInputEvent pie) {
        return pie.isControlDown();
    }

    /**
     * changes the decoration of the PNode instance if was unselected
     */
    @Override
    public void undecorateSelectedNode(final PNode node) {
        if (node instanceof IDecorable) {
            ((IDecorable) node).Undecorate();
        }
    }

    /**
     * stub it's disables drag process
     */
    @Override
    protected void dragStandardSelection(final PInputEvent e) {

    }

    public boolean isSelecting(final PInputEvent pie) {
        return isMarqueeSelection(pie);
    }

}