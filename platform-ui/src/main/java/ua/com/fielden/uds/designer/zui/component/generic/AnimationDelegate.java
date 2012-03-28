package ua.com.fielden.uds.designer.zui.component.generic;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.util.PDimension;

public abstract class AnimationDelegate implements PActivity.PActivityDelegate {
    private PNode cloneNode;
    private PNode dragNode;
    private PDimension delta;

    public AnimationDelegate(PNode cloneNode, PNode draggedNode) {
	setCloneNode(cloneNode);
	setDragNode(draggedNode);
    }

    public AnimationDelegate(PNode cloneNode, PNode draggedNode, PDimension delta) {
	setCloneNode(cloneNode);
	setDragNode(draggedNode);
	setDelta(delta);
    }

    public PNode getCloneNode() {
	return cloneNode;
    }

    private void setCloneNode(PNode cloneNode) {
	this.cloneNode = cloneNode;
    }

    public PNode getDragNode() {
	return dragNode;
    }

    private void setDragNode(PNode draggedNode) {
	this.dragNode = draggedNode;
    }

    public void activityStarted(PActivity activity) {
	// this is just a stub
    }

    public void activityStepped(PActivity activity) {
	// this is just a stub
    }

    public void activityFinished(PActivity activity) {
	// this is just a stub
    }

    public PDimension getDelta() {
	return delta;
    }

    protected void setDelta(PDimension delta) {
	this.delta = delta;
    }

}
