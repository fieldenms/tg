package ua.com.fielden.uds.designer.zui.component.fact;

import ua.com.fielden.uds.designer.zui.component.generic.PlaceHolderNode;
import edu.umd.cs.piccolo.PNode;

public class OperationPlaceHolderNode extends PlaceHolderNode {
    private static final long serialVersionUID = 4469729685542238865L;

    public OperationPlaceHolderNode(String placeHoldingText) {
	super(placeHoldingText);
    }

    public boolean isCompatible(PNode node) {
	if (!super.isCompatible(node)) { // check the parent implementation of isCompatible
	    return false;
	}
	// perform PlaceHolder specific validations
	if (!(node instanceof OperationNode)) { // can accept only descendants of AbstractNode
	    return false;
	}

	return true;
    }

    public OperationNode getAttachedNode() {
	return (OperationNode) super.getAttachedNode();
    }

}
