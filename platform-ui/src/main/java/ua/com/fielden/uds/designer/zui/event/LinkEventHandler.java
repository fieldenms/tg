package ua.com.fielden.uds.designer.zui.event;

import java.awt.Color;
import java.util.List;

import ua.com.fielden.uds.designer.zui.interfaces.ILink;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PDragEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * This class implements PDragEventHandler to demonstrate sequential multi-link behaviour, where modifications to links joined to a node are performed sequentially.
 * 
 * @author 01es
 * 
 */
public class LinkEventHandler extends PDragEventHandler {
    public LinkEventHandler() {
        getEventFilter().setMarksAcceptedEventsAsHandled(true);
    }

    protected void startDrag(PInputEvent event) {
        super.startDrag(event);
    }

    @SuppressWarnings("unchecked")
    protected void drag(PInputEvent event) {
        super.drag(event);
        PNode node = event.getPickedNode();
        if (node.getAttribute("links") != null) {
            List<ILink> links = (List<ILink>) node.getAttribute("links");
            for (ILink link : links) {
                link.reset();
            }
        }
    }

    protected void endDrag(PInputEvent event) {
        super.endDrag(event);
    }

    public void mouseEntered(PInputEvent e) {
        if (e.getButton() == 0) {
            e.getPickedNode().setPaint(Color.red);
        }
    }

    public void mouseExited(PInputEvent e) {
        if (e.getButton() == 0) {
            e.getPickedNode().setPaint(Color.white);
        }
    }

}
