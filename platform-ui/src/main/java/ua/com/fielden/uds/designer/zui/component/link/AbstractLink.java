package ua.com.fielden.uds.designer.zui.component.link;

import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.interfaces.ILink;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;

public abstract class AbstractLink extends PPath implements ILink, Serializable {
    private boolean globalBounds = true;

    protected void remove(final PNode node) {
	if (getChildrenReference().contains(node)) {
	    removeChild(node);
	}
    }

    @SuppressWarnings("unchecked")
    public void reset(final PNode activeNode, final PDimension delta, final Set<PNode> processedNodes) {
	reset();
	processedNodes.add(activeNode);

	if (activeNode.getAttribute("links") != null) {
	    final List<ILink> links = (List<ILink>) activeNode.getAttribute("links");

	    final Rectangle2D bounds = activeNode.getFullBounds(); // getBounds(activeNode); //

	    final double rightSide = bounds.getX() + bounds.getWidth();
	    final double bottomSide = bounds.getY() + bounds.getHeight();

	    for (final ILink link : links) {
		PNode nodeToProcess = null;
		if (!processedNodes.contains(link.getStartNode())) {
		    nodeToProcess = link.getStartNode();
		} else if (!processedNodes.contains(link.getEndNode())) {
		    nodeToProcess = link.getEndNode();
		}

		if (nodeToProcess != null) {
		    final Rectangle2D currBounds = nodeToProcess.getFullBounds();

		    final double currRightSide = currBounds.getX() + currBounds.getWidth();
		    final double currBottomSide = currBounds.getY() + currBounds.getHeight();

		    boolean flag = false;
		    if (currRightSide > rightSide) {
			final AbstractNode deepParent = ((AbstractNode) nodeToProcess).getDeepParent(null);
			if (!processedNodes.contains(deepParent) && deepParent.getLinks().size() > 0) {
			    deepParent.translate(delta.width, 0);
			    processedNodes.add(deepParent);
			}
			flag = true;
		    }
		    if (currBottomSide > bottomSide) {
			final AbstractNode deepParent = ((AbstractNode) nodeToProcess).getDeepParent(null);
			if (!processedNodes.contains(deepParent) && deepParent.getLinks().size() > 0) {
			    deepParent.translate(0, delta.height);
			    processedNodes.add(deepParent);
			}
			flag = true;
		    }

		    if (flag) {
			link.reset(nodeToProcess, delta, processedNodes);
		    } else {
			link.reset();
		    }
		}
	    } // for
	} // if
    } // method

    /**
     * This method highlights
     */
    public abstract void hightlight(Stroke stoke);

    public abstract void dehightlight();

    public PBounds getBounds(final PNode node) {
	if (isGlobalBounds()) {
	    return node.getGlobalFullBounds();
	} else {
	    return node.getFullBounds();
	}
    }

    public boolean isGlobalBounds() {
	return globalBounds;
    }

    public void setGlobalBounds(final boolean blobalCounds) {
	this.globalBounds = blobalCounds;
    }
}
