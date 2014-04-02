package ua.com.fielden.uds.designer.zui.component.fact;

import java.awt.Color;
import java.awt.Dimension;

import ua.com.fielden.uds.designer.zui.component.generic.GenericContainerNode;
import edu.umd.cs.piccolo.PNode;

/**
 * This is a panel, which is used for holding OperationNode instances.
 * 
 * @author 01es
 */
public class OperationPanelNode extends GenericContainerNode {
    private static final long serialVersionUID = -110732023574052397L;
    /**
     * This array contains all the operations displayed on a panel.
     */
    private transient static OperationNode[] operations = new OperationNode[] { new OperationNode(Operation.LESS), new OperationNode(Operation.GREATER),
            new OperationNode(Operation.EQUAL), new OperationNode(Operation.NOT_EQUAL), new OperationNode(Operation.LESS_EQUAL), new OperationNode(Operation.GREATER_EQUAL),
            new OperationNode(Operation.CONTAINS), new OperationNode(Operation.EXCLUDES), new OperationNode(Operation.MATCHES), new OperationNode(Operation.IN) };

    private transient static final Dimension cellDim = new Dimension((int) 70, (int) 34);

    public OperationPanelNode() {
        for (OperationNode operNode : operations) {
            addChild(operNode);
        }
        setBackgroundColor(new Color(210, 210, 210));
        setRounding(new BorderRounding(true, true, true, true));
        reshape(false);
    }

    public boolean isCompatible(PNode node) {
        // basically this allows attachment of clones upon D'n'D operations and reattachment of previously detached operation nodes.
        return (node instanceof OperationNode);
    }

    /**
     * This method is implemented in order to support correct D'n'D activity where an original operation node is removed and its clone is associated with the panel. It is vital
     * that the clone is placed into the operations array and hopefully into the same place as the original operation node.
     */
    public void doAfterAttach(PNode node) {
        System.out.println("doAfterAttach");
        for (int index = 0; index < operations.length; index++) {
            if (operations[index] == null) { // attaching a clone
                operations[index] = (OperationNode) node;
                operations[index].setRemoveAfterDrop(false);
                break;
            }

            if (operations[index] != node && operations[index].equals(node)) { // reattaching an operation
                operations[index].removeFromParent();
                operations[index] = (OperationNode) node;
                operations[index].setRemoveAfterDrop(false);
                break;
            }
        }
    }

    /**
     * This method is implemented so that the operations array is properly updated upon detachment of an operation node by setting a null value instead of the removed node. The
     * null value is used for association of the cloned operation node with the operations array.
     */
    public void doAfterDetach(PNode node) {
        for (int index = 0; index < operations.length; index++) {
            if (operations[index] == node) {
                operations[index] = null;
                break;
            }
        }
    }

    /**
     * This method implements a table like layout for children components.
     */
    protected void layoutComponents() {
        double xOffset = 0;
        double yOffset = 0;

        int numberOfColumns = 2;
        int gap = 10;

        // add children
        int index = 0;
        while (index < operations.length) {
            for (int col = 0; col < numberOfColumns && index < operations.length; col++, index++) {
                OperationNode each = operations[index];
                if (each != null) {
                    each.setOffset(xOffset + ((cellDim.width - each.getWidth()) / 2), yOffset + (cellDim.height - each.getHeight()) / 2);
                }
                xOffset += (cellDim.width + gap);
            }
            xOffset = 0;
            yOffset += cellDim.height + gap;
        }
    }
}
