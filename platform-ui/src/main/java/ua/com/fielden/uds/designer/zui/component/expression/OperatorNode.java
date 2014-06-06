package ua.com.fielden.uds.designer.zui.component.expression;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.component.generic.DefaultStroke;
import ua.com.fielden.uds.designer.zui.component.generic.GenericContainerNode;
import ua.com.fielden.uds.designer.zui.component.generic.RingMenu;
import ua.com.fielden.uds.designer.zui.component.generic.RingMenuInvoker;
import ua.com.fielden.uds.designer.zui.component.generic.RingMenuItem;
import ua.com.fielden.uds.designer.zui.interfaces.IOnClickEventListener;
import ua.com.fielden.uds.designer.zui.interfaces.IValue;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * This class is a visual representation of an operator. Currently only multi-argument operators are supported.
 * 
 * @author 01es
 * 
 */
public class OperatorNode extends GenericContainerNode implements Cloneable {
    private static final long serialVersionUID = 7444871761718412290L;

    private IOperator operator;
    /**
     * This list contains nodes representing operator expression and operands. It's main purpose is to display nodes in specific order.
     */
    private List<PNode> orderedNodes = new ArrayList<PNode>();

    private PPath leftParenthesesNode;
    private PPath rightParenthesesNode;
    private Stroke originalStroke = new DefaultStroke(1);

    private PPath menuPlace;
    private RingMenu menu;

    private boolean canBeDetached = true; // not currently used. but can be useful

    private boolean showMenu = true;

    public OperatorNode(IOperator operator) {
        setStroke(null);
        setOperator(operator);
        composeNode(this);
    }

    private boolean enabled = true;

    static void composeNode(OperatorNode operatorNode) {
        operatorNode.setBackgroundColor(Color.white);
        operatorNode.removeAllChildren();
        operatorNode.orderedNodes.clear();
        operatorNode.setPedding(new AbstractNode.Pedding(4, 4, 4, 7));

        List<IOperand<IValue>> operands = operatorNode.getOperator().getOperands();
        for (int index = 0; index < operands.size() - 1; index++) {
            IOperand<IValue> operand = operands.get(index);
            OperandNode node = new OperandNode(operand);
            operatorNode.orderedNodes.add(node);
            PText expression = new PText(operatorNode.getOperator().getExpression());
            expression.setPickable(false);
            operatorNode.orderedNodes.add(expression);
        }
        IOperand<IValue> operand = operands.get(operands.size() - 1);
        OperandNode node = new OperandNode(operand);
        operatorNode.orderedNodes.add(node);

        for (PNode nextNode : operatorNode.orderedNodes) {
            operatorNode.addChild(nextNode);
        }
        operatorNode.drawParenthesis(operatorNode.getBounds());

        addMenu(operatorNode);
    }

    protected static void addMenu(OperatorNode operatorNode) {
        operatorNode.menu = new RingMenu(new String[] { "Add operand", "Remove operand", "Clone", "Delete operand's content" }, new Color[] { new Color(208, 32, 13),
                new Color(47, 132, 53), new Color(255, 128, 0), new Color(132, 36, 65) }, // new Color(46, 91, 124)
        new IOnClickEventListener[] { new RemoveOperator(operatorNode), new AddOperand(operatorNode), new CloneExpression(operatorNode), new DeleteOperand(operatorNode) }, 12);
    }

    /**
     * This is an implementation of the add operator action invoked by an event handler of one of the ring menu items.
     * 
     * @author 01es
     * 
     */
    private static class AddOperand implements IOnClickEventListener {
        private static final long serialVersionUID = 3454340694150637154L;
        private OperatorNode opNode;

        public AddOperand(OperatorNode opNode) {
            this.opNode = opNode;
        }

        @SuppressWarnings("unchecked")
        public void click(PInputEvent event) {
            // create new empty operand
            IOperand<IValue> newOperand = new HybridOperand(opNode.getOperator().getDefaultValue().toString());
            opNode.getOperator().append(newOperand);
            // recompose this operator node
            composeNode(opNode);
        }
    }

    /**
     * This is an implementation of the add operator action invoked by an event handler of one of the ring menu items.
     * 
     * @author 01es
     * 
     */
    private static class DeleteOperand implements IOnClickEventListener {
        private static final long serialVersionUID = 4821292578118655208L;
        private OperatorNode opNode;

        public DeleteOperand(OperatorNode opNode) {
            this.opNode = opNode;
        }

        public void click(PInputEvent event) {
            // remove the last operand
            opNode.getOperator().removeLast();
            // recompose this operator node
            composeNode(opNode);
        }
    }

    /**
     * This is an implementation of the delete content action invoked by an event handler of one of the ring menu items.
     * 
     * @author 01es
     */
    private static class RemoveOperator implements IOnClickEventListener {
        private static final long serialVersionUID = -4311922010730916618L;
        private OperatorNode opNode;

        public RemoveOperator(OperatorNode opNode) {
            this.opNode = opNode;
        }

        public void click(PInputEvent event) {
            if (opNode.getOperator().isOperand()) {
                OperandNode containingOperand = ((OperandNode) opNode.getParent());
                RingMenuItem item = containingOperand.getRingMenu().getMenuItem(OperandNode.MENU_ITEM_DELETE_CONTENT);
                item.doClick(null);
            } else {
                for (PNode node : opNode.orderedNodes) {
                    if (node instanceof OperandNode) {
                        ((OperandNode) node).erase();
                    }
                }
                opNode.removeFromParent();
            }
        }
    }

    /**
     * Produces a clone of the expression, where expression is an operator at the high level (i.e. is not an operand).
     * 
     * @author 01es
     */
    private static class CloneExpression implements IOnClickEventListener {
        private static final long serialVersionUID = -7143896808628085967L;
        private OperatorNode opNode;

        public CloneExpression(OperatorNode opNode) {
            this.opNode = opNode;
        }

        public void click(PInputEvent event) {
            if (!opNode.getOperator().isOperand()) {
                OperatorNode clone = (OperatorNode) opNode.clone();
                clone.setOffset(opNode.getOffset().getX() + 10, opNode.getOffset().getY() + opNode.getHeight() + 10);
                opNode.getParent().addChild(clone);
            } else {
                AbstractNode parent = opNode.getDeepParent(null);

                OperatorNode clone = (OperatorNode) opNode.clone();
                clone.getOperator().setContainingOperand(null);
                clone.setOffset(parent.getOffset().getX() + 10, parent.getOffset().getY() + parent.getHeight() + 10);
                parent.getParent().addChild(clone);
            }
        }
    }

    protected void layoutComponents() {
        double xOffset = 0;
        // find a node with the largest height...
        double H = 0.0;
        for (PNode each : orderedNodes) {
            if (getLayoutIgnorantNodes().contains(each)) {
                continue;
            }
            if (each.getHeight() > H) {
                H = each.getHeight();
            }
        }

        for (PNode each : orderedNodes) {
            if (getLayoutIgnorantNodes().contains(each)) {
                continue;
            }
            double yOffset = (H - each.getHeight()) / 2.0;
            each.setOffset(xOffset - each.getX(), yOffset);
            xOffset += each.getFullBoundsReference().getWidth() + 3;
        }
    }

    public IOperator getOperator() {
        return operator;
    }

    private void setOperator(IOperator operator) {
        this.operator = operator;
    }

    public boolean canBeDetached() {
        return canBeDetached;
    }

    public void setCanBeDetached(boolean flag) {
        canBeDetached = flag;
    }

    /**
     * This method is overridden in order to ensure proper parenthesis placement.
     */
    public boolean setBounds(double x, double y, double width, double height) {
        boolean result = super.setBounds(x, y, width, height);

        if (getLayoutIgnorantNodes() != null && operator != null) {
            drawParenthesis(getBounds());
            handleMenu(getBounds());
        }
        return result;
    }

    private static final float curvature = 5;

    /**
     * Instantiates parenthesis. This method is also responsible for parenthesis relocation.
     * 
     * @param newBounds
     */
    protected void drawParenthesis(Rectangle2D newBounds) {
        if (leftParenthesesNode != null) {
            leftParenthesesNode.removeFromParent();
            getLayoutIgnorantNodes().remove(leftParenthesesNode);
        }
        if (rightParenthesesNode != null) {
            rightParenthesesNode.removeFromParent();
            getLayoutIgnorantNodes().remove(rightParenthesesNode);
        }

        newBounds = new Rectangle2D.Double(2, 2, newBounds.getWidth() - 5, newBounds.getHeight() - 2); // newBounds.getWidth() - (operator.isOperand() ? 2 : 5)

        GeneralPath leftParentheses = leftParenthesis(newBounds);
        GeneralPath rightParentheses = rightParenthesis(newBounds);

        leftParenthesesNode = new PPath(leftParentheses);
        leftParenthesesNode.setPickable(false);
        leftParenthesesNode.setStroke(originalStroke);

        getLayoutIgnorantNodes().add(leftParenthesesNode);
        addChild(leftParenthesesNode);

        rightParenthesesNode = new PPath(rightParentheses);
        rightParenthesesNode.setPickable(false);
        rightParenthesesNode.setStroke(originalStroke);

        getLayoutIgnorantNodes().add(rightParenthesesNode);
        addChild(rightParenthesesNode);
    }

    protected GeneralPath rightParenthesis(Rectangle2D newBounds) {
        GeneralPath rightParentheses = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        rightParentheses.moveTo((float) (newBounds.getWidth() - curvature), (float) (newBounds.getHeight()));
        rightParentheses.curveTo((float) (newBounds.getWidth()), (float) (newBounds.getHeight()), (float) (newBounds.getWidth()), (float) (newBounds.getHeight()), (float) (newBounds.getWidth()), (float) (newBounds.getHeight())
                - curvature);
        rightParentheses.lineTo((float) (newBounds.getWidth()), curvature);
        // right top corner
        rightParentheses.curveTo((float) (newBounds.getWidth()), (float) newBounds.getY(), (float) (newBounds.getWidth()), (float) newBounds.getY(), (float) (newBounds.getWidth() - curvature), (float) newBounds.getY());
        return rightParentheses;
    }

    protected GeneralPath leftParenthesis(Rectangle2D newBounds) {
        GeneralPath leftParentheses = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        // left top corner
        leftParentheses.moveTo((float) newBounds.getX() + curvature, (float) newBounds.getY());
        leftParentheses.curveTo((float) newBounds.getX(), (float) newBounds.getY(), (float) newBounds.getX(), (float) newBounds.getY(), (float) newBounds.getX(), curvature);
        leftParentheses.lineTo((float) newBounds.getX(), (float) (newBounds.getHeight() - curvature));

        // left bottom corner
        leftParentheses.curveTo((float) newBounds.getX(), (float) (newBounds.getHeight()), (float) newBounds.getX(), (float) (newBounds.getHeight()), (float) newBounds.getX()
                + curvature, (float) (newBounds.getHeight()));
        return leftParentheses;
    }

    private void handleMenu(Rectangle2D bounds) {
        if (menuPlace != null) {
            menuPlace.removeFromParent();
            getLayoutIgnorantNodes().remove(menuPlace);
            enabled = menuPlace.getPickable();
        }

        if (!isShowMenu()) {
            return;
        }

        bounds = new Rectangle2D.Double(-0.5, 0.0, bounds.getWidth(), 0);
        GeneralPath triangle = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

        float side = 3;
        // left top corner
        triangle.moveTo((float) (bounds.getX() + bounds.getWidth() - side), (float) bounds.getY());
        triangle.lineTo((float) (bounds.getX() + bounds.getWidth()), (float) bounds.getY());
        triangle.lineTo((float) (bounds.getX() + bounds.getWidth()), (float) (bounds.getY() + side));
        triangle.closePath();

        menuPlace = new PPath(triangle);
        Color color = new Color(152, 177, 203);
        menuPlace.setStrokePaint(color);
        menuPlace.setPaint(color);
        menuPlace.setPickable(enabled);

        getLayoutIgnorantNodes().add(menuPlace);
        addChild(menuPlace);

        menuPlace.addInputEventListener(menuEventListener);
    }

    public RingMenu getRingMenu() {
        return menu;
    }

    private final PInputEventListener menuEventListener = new RingMenuInvoker(this);

    public void disableMenu() {
        enabled = false;
        if (menuPlace != null) {
            menuPlace.setPickable(false);
        }
    }

    public void enableMenu() {
        enabled = true;
        if (menuPlace != null) {
            menuPlace.setPickable(true);
        }
    }

    /**
     * Enables node's ring menu as well as its children's ring menu
     */
    public void disableChildrenMenu() {
        disableMenu();
        for (PNode node : orderedNodes) {
            if (node instanceof OperandNode) {
                ((OperandNode) node).disableChildrenMenu();
            }
        }
    }

    /**
     * Disables node's ring menu as well as its children's ring menu
     */
    public void enableChildrenMenu() {
        enableMenu();
        for (PNode node : orderedNodes) {
            if (node instanceof OperandNode) {
                ((OperandNode) node).enableChildrenMenu();
            }
        }
    }

    public void highlight(Stroke stroke) {
        leftParenthesesNode.setStroke(new DefaultStroke(2));
        rightParenthesesNode.setStroke(new DefaultStroke(2));
    }

    public void dehighlight() {
        leftParenthesesNode.setStroke(originalStroke);
        rightParenthesesNode.setStroke(originalStroke);
    }

    public boolean isCompatible(PNode node) {
        return false;
    }

    public boolean equalsByContent(OperatorNode cmpTo) {
        if (cmpTo == this) {
            return true;
        }

        return getOperator().equalsByContent(cmpTo.getOperator());
    }

    protected OperatorNode(OperatorNode operatorNode) {
        setShowMenu(operatorNode.isShowMenu());
        setOffset(operatorNode.getOffset());
        setStroke(operatorNode.getStroke());
        setOperator((IOperator) operatorNode.getOperator().clone());

        setBackgroundColor(operatorNode.getBackgroundColor());
        setPedding(operatorNode.getPedding());

        composeNode(this);

        addMenu(this);
    }

    /**
     * This method is overridden for proper cloning of operator nodes.
     */
    public Object clone() {
        return new OperatorNode(this);
    }

    protected boolean isShowMenu() {
        return showMenu;
    }

    protected void setShowMenu(boolean showMenu) {
        this.showMenu = showMenu;
    }

}
