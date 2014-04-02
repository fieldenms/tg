package ua.com.fielden.uds.designer.zui.component.expression;

import java.awt.Color;
import java.awt.Dimension;

import ua.com.fielden.uds.designer.zui.component.generic.GenericContainerNode;
import edu.umd.cs.piccolo.PNode;

/**
 * This node contains all available mathematical operators like plus, minus etc., which can be used for building an expression. All operators available currently are instances of
 * OperatorNode.
 * 
 * @author 01es
 * 
 */
public class OperatorPanelNode extends GenericContainerNode {
    private static final long serialVersionUID = -5874803529731692022L;

    private transient static final OperatorNode[] operators = new OperatorNode[] { new OperatorNode(new Or()), new OperatorNode(new And()), new OperatorNode(new Plus()),
            new OperatorNode(new Minus()), new OperatorNode(new Division()), new OperatorNode(new Multiplication()), new OperatorNode(new Pow()), new AbsOperatorNode(),
            new ParenthesisOperatorNode(), new OperatorNode(new Greater()), new OperatorNode(new GreaterEq()), new OperatorNode(new Smaller()), new OperatorNode(new SmallerEq()),
            new OperatorNode(new Equals()), new OperatorNode(new NotEquals()) };

    private transient static final Dimension cellDim = new Dimension(75, 36);

    public OperatorPanelNode() {
        setBackgroundColor(new Color(210, 210, 210));
        // add operators to the panel
        for (final OperatorNode opNode : operators) {
            opNode.disableMenu(); // to prevent ring menu popping up while on the panel
            opNode.disableChildrenMenu();
            addChild(opNode);
        }
        setRounding(new BorderRounding(true, true, true, true));
        reshape(false);
    }

    public boolean isCompatible(final PNode node) {
        // basically this allows attachment of clones upon D'n'D operations and reattachment of previously detached operation nodes.
        if (!(node instanceof OperatorNode)) {
            return false;
        }
        for (int index = 0; index < operators.length; index++) {
            if (operators[index] == null || ((OperatorNode) node).equalsByContent(operators[index])) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is implemented in order to support correct D'n'D activity where an original operator node is removed and its clone is associated with the panel. It is vital that
     * the clone is placed into the operators array and hopefully into the same place as the original operator node.
     */
    public void doAfterAttach(final PNode node) {
        for (int index = 0; index < operators.length; index++) {
            if (operators[index] == null) { // attaching a clone
                operators[index] = (OperatorNode) node;
                operators[index].setRemoveAfterDrop(false);
                operators[index].disableMenu();
                operators[index].disableChildrenMenu();
                break;
            }

            if (((OperatorNode) node).equalsByContent(operators[index])) { // reattaching an operation
                node.removeFromParent(); // simply ignore everything that comes in
                break;
            }
        }
    }

    /**
     * This method is implemented so that the operators array is properly updated upon detachment of an operator node by setting a null value instead of the removed node. The null
     * value is used for association of the cloned operator node with the operators array.
     */
    public void doAfterDetach(final PNode node) {
        for (int index = 0; index < operators.length; index++) {
            if (operators[index] == node) {
                operators[index].enableMenu(); // enable ring menu once not on the panel
                operators[index].enableChildrenMenu();
                operators[index] = null;
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

        final int numberOfColumns = 3;
        final int gap = 10;

        // add children
        int index = 0;
        while (index < operators.length) {
            for (int col = 0; col < numberOfColumns && index < operators.length; col++, index++) {
                final OperatorNode each = operators[index];
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
