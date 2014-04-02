package ua.com.fielden.uds.designer.zui.component.fact;

import java.awt.Color;

import ua.com.fielden.uds.designer.zui.component.generic.TextNode;
import ua.com.fielden.uds.designer.zui.interfaces.IDraggable;
import ua.com.fielden.uds.designer.zui.util.GlobalObjects;

/**
 * This is a visual representation of Operation enumeration values.
 * 
 * @author 01es
 * 
 */
public class OperationNode extends TextNode implements IDraggable {
    private static final long serialVersionUID = 7695745089476020411L;

    private Operation operation;

    public OperationNode(Operation operation) {
        super(GlobalObjects.canvas, operation.toString(), null);
        setBackgroundColor(Color.white);
        setOperation(operation);
        setEditable(false);
        setCurvaturePrc(20);
        setRounding(new BorderRounding(true, true, true, true));
        setPedding(new Pedding(10, 10, 10, 10));
    }

    public Operation getOperation() {
        return operation;
    }

    private void setOperation(Operation operation) {
        this.operation = operation;
    }

    public boolean canBeDetached() {
        return true;
    }

    public boolean canDrag() {
        return true;
    }

    private boolean removeAfterDrop = false;

    public boolean getRemoveAfterDrop() {
        return removeAfterDrop;
    }

    public void setRemoveAfterDrop(boolean flag) {
        removeAfterDrop = flag;
    }

    public String toString() {
        return getOperation().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OperationNode)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        return operation.equals(((OperationNode) obj).getOperation());
    }

    public Object clone() {
        setDummyCloning(true);
        return super.clone();
    }

}
