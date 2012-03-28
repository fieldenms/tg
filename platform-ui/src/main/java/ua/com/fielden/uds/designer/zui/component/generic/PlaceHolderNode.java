package ua.com.fielden.uds.designer.zui.component.generic;

import java.util.Iterator;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * This visual component is a highly specialised container, which may hold only one item. If an item is attached then it is displayed within a container. Otherwise container
 * displays a place holder -- usually a string value.
 * 
 * @author 01es
 * 
 */
public class PlaceHolderNode extends GenericContainerNode {
    private static final long serialVersionUID = 1387200992759368582L;

    private PNode placeHolder;
    private AbstractNode attachedNode;

    public PlaceHolderNode(String placeHoldingText) {
	super();
	setPlaceHolder(new PText(placeHoldingText));
	addChild(getPlaceHolder());
	getPlaceHolder().setPickable(false);
	reshape(false);
    }

    public boolean isCompatible(PNode node) {
	// check the parent implementation of isCompatible
	if (!super.isCompatible(node)) {
	    return false;
	}
	// perform PlaceHolder specific validations
	if (!(node instanceof AbstractNode)) { // can accept only descendants of AbstractNode
	    return false;
	}
	if (getAttachedNode() != null) { // check if there is already a node attached
	    return false; // basically an attached node needs to be detached first and only then a new one ca nbe attached
	}
	// if this point in execution flow is reached then a node can be attached
	return true;
    }

    /**
     * This method is overridden because a placeHolder should not participate in layouting, which would happen with default implementation.
     */
    protected void layoutComponents() {
	// layout a place holder
	PNode each = getPlaceHolder();
	double xOffset = 0;
	double yOffset = 0;

	each.setOffset(xOffset, yOffset);
	// return default values
	Iterator iter = getChildrenIterator();
	while (iter.hasNext()) {
	    each = (PNode) iter.next();
	    if (each == getPlaceHolder()) {
		continue;
	    }
	    each.setOffset(xOffset, yOffset - each.getY());
	    yOffset += each.getFullBoundsReference().getHeight() + 3;
	}
    }

    /**
     * This method is invoked after all "attachment" validation is successful
     */
    public void doAfterAttach(PNode node) {
	System.out.println("doAfterAttach");
	setAttachedNode((AbstractNode) node); // this typecasting is safe -- see isCompatible for validation
	// need to remove placeHoder from its parent (this), which is substituted with an attached node
	getPlaceHolder().removeFromParent();
    }

    public void doAfterDetach(PNode node) {
	System.out.println("doAfterDetach");
	setAttachedNode(null); // the node was detached, so assign null
	addChild(getPlaceHolder());
    }

    public AbstractNode getAttachedNode() {
	return attachedNode;
    }

    private void setAttachedNode(AbstractNode attachedNode) {
	this.attachedNode = attachedNode;
    }

    private PNode getPlaceHolder() {
	return placeHolder;
    }

    private void setPlaceHolder(PNode placeHoder) {
	this.placeHolder = placeHoder;
    }
}
