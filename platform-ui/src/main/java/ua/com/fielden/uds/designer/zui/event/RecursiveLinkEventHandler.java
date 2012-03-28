package ua.com.fielden.uds.designer.zui.event;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.fielden.uds.designer.zui.interfaces.ILink;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PDragEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * This class implements PDragEventHandler to demonstrate recursive multi-link behaviour, where modifications to links joined to a node are performed recursively and managed
 * completely by the link itself. It demonstrates "hard" links' behaviour -- dragging
 * 
 * @author 01es
 * 
 */
public class RecursiveLinkEventHandler extends PDragEventHandler {
    public RecursiveLinkEventHandler() {
	getEventFilter().setMarksAcceptedEventsAsHandled(true);
    }

    private Set<PNode> processedNodes;

    protected void startDrag(PInputEvent event) {
	super.startDrag(event);
	processedNodes = new HashSet<PNode>();
    }

    @SuppressWarnings("unchecked")
    protected void drag(PInputEvent event) {
	super.drag(event);
	PNode node = event.getPickedNode();
	List<ILink> links = (List<ILink>) node.getAttribute("links");
	if (links != null && links.size() > 0) {
	    ILink link = links.get(0); // can pick up any link -- the rest should be triggered recursively
	    link.reset(node, event.getDelta(), processedNodes);
	}
	processedNodes.clear(); // need to clear a set of processed nodes so that the next drag could process all nodes again
    }

    protected void endDrag(PInputEvent event) {
	super.endDrag(event);
	processedNodes.clear();
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
