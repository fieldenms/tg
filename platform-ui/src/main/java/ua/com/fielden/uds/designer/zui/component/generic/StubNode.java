package ua.com.fielden.uds.designer.zui.component.generic;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;

/**
 * Represents an image of another PNode.
 * 
 * The original intended use of this class is for D'n'D operations, where the actual PNode is dragged and its image remains in the original position until the PNode is dropped.
 * 
 * @author 01es
 */
public class StubNode extends PImage {
    private static final long serialVersionUID = 1L;

    public StubNode(final PNode node) {
	super(node.toImage());
	setPickable(false);
	// full bounds are used for setting the offset because they are utilised by toImage() method
	setOffset(node.getFullBounds().getX(), node.getFullBounds().getY());
    }
}
