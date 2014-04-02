package ua.com.fielden.uds.designer.zui.component.expression;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import javax.swing.text.DocumentFilter;

import ua.com.fielden.uds.designer.zui.component.fact.BoundNameNode;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.component.generic.GenericContainerNode;
import ua.com.fielden.uds.designer.zui.component.generic.RingMenu;
import ua.com.fielden.uds.designer.zui.component.generic.RingMenuInvoker;
import ua.com.fielden.uds.designer.zui.component.generic.TextNode;
import ua.com.fielden.uds.designer.zui.component.generic.filter.MixedDocumentFilter;
import ua.com.fielden.uds.designer.zui.component.generic.filter.WordDocumentFilter;
import ua.com.fielden.uds.designer.zui.interfaces.IOnClickEventListener;
import ua.com.fielden.uds.designer.zui.interfaces.IUpdater;
import ua.com.fielden.uds.designer.zui.interfaces.IValue;
import ua.com.fielden.uds.designer.zui.util.GlobalObjects;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;

/**
 * This class is a visual representation of an operand. Currently only HybridOperand is supported, which may have value of type String or IOperator.
 * 
 * @author 01es
 * 
 */
public class OperandNode extends GenericContainerNode {
    private static final long serialVersionUID = -6154261817509099628L;
    // this constant is introduced purely for convenient menu item referencing where necessary.
    public static final String MENU_ITEM_DELETE_CONTENT = "Delete operand's content";

    private IOperand<IValue> operand;

    /**
     * textNode needs to be bound to a corresponding operand so that when its value changes the operand values also changes.
     */
    private TextNode textNode;

    /**
     * operatorNode needs to be bound to a corresponding operand so that when its value changes the operand values also changes.
     */
    private OperatorNode operatorNode;

    /**
     * boundNameNode is a reference to a property bound name node; it is defined (i.e. != null) is an instance of PropertyBoundNameNode has been dropped onto this operand.
     */
    private BoundNameNode boundNameNode;

    private PPath menuPlace;

    private RingMenu menu;

    public OperandNode(IOperand<IValue> operand) {
        setOperand(operand);
        setDrag(false);
        setStroke(null);
        composeNode();
    }

    private boolean enabled = true;

    public void disableMenu() {
        enabled = false;
        menuPlace.setPickable(false);
    }

    public void enableMenu() {
        enabled = true;
        menuPlace.setPickable(true);
    }

    public void disableChildrenMenu() {
        disableMenu();
        if (operatorNode != null) {
            operatorNode.disableChildrenMenu();
        }
    }

    public void enableChildrenMenu() {
        enableMenu();
        if (operatorNode != null) {
            operatorNode.enableChildrenMenu();
        }
    }

    @SuppressWarnings("unchecked")
    private void composeNode() {
        removeTextNode();
        removeOperatorNode();
        removeBoundNameNode();

        setPedding(new AbstractNode.Pedding(2, 2, 2, 5)); // 5 here is in
        // order to provide
        // some additional
        // space for
        // menuPlace
        setBackgroundColor(Color.white);
        if (getOperand().getValue() instanceof IValue) {
            String value = ((IValue) getOperand().getValue()).getValue().toString();
            DocumentFilter filter = getDocumentFilter(value);

            ((IValue) getOperand().getValue()).setEmptyPermitted(false);
            ((IValue) getOperand().getValue()).setDefaultValue(getOperand().getOperator().getDefaultValue().toString());
            addTextNode(value, filter);
        } else if (getOperand().getValue() instanceof IOperator) {
            addOperatorNode();
        }

        reshape(false);

        addMenu(this);
    }

    private DocumentFilter getDocumentFilter(String value) {
        return new MixedDocumentFilter(); // looks like MixedDocumentFilter is the most comprehancive and should satisfy most cases
        /*
         * AbstractDocumentFilter filter = new WordDocumentFilter(); if (!filter.allowInput(value)) { filter = new MixedDocumentFilter(); } return filter;
         */
    }

    /**
     * This is a delegate, which deletes operand's content.
     * 
     * @author 01es
     */
    private static class DeleteContent implements IOnClickEventListener {
        private static final long serialVersionUID = -6663309057889815725L;

        private OperandNode opNode;

        public DeleteContent(OperandNode opNode) {
            this.opNode = opNode;
        }

        public void click(PInputEvent event) {
            opNode.erase();
        }
    }

    /**
     * This is a delegate, which removes this operand.
     * 
     * @author 01es
     */
    private static class RemoveOperand implements IOnClickEventListener {
        private static final long serialVersionUID = 8969905129010857596L;

        private OperandNode opNode;

        public RemoveOperand(OperandNode opNode) {
            this.opNode = opNode;
        }

        @SuppressWarnings("unchecked")
        public void click(PInputEvent event) {
            opNode.erase();
            opNode.getOperand().getOperator().remove(opNode.getOperand());
            OperatorNode.composeNode((OperatorNode) opNode.getParent());
        }
    }

    /**
     * This is a delegate, which inserts a new operand at the right of its position.
     * 
     * @author 01es
     */
    private static class InsertRightOperand implements IOnClickEventListener {
        private static final long serialVersionUID = -5997134419614047784L;

        private OperandNode opNode;

        public InsertRightOperand(OperandNode opNode) {
            this.opNode = opNode;
        }

        @SuppressWarnings("unchecked")
        public void click(PInputEvent event) {
            int index = opNode.getOperand().getOperator().getOperands().indexOf(opNode.getOperand()) + 1;
            if (index >= opNode.getOperand().getOperator().getOperands().size()) {
                opNode.getOperand().getOperator().append(new HybridOperand(opNode.getOperand().getOperator().getDefaultValue().toString()));
            } else {
                opNode.getOperand().getOperator().insert(index, new HybridOperand(opNode.getOperand().getOperator().getDefaultValue().toString()));
            }
            OperatorNode.composeNode((OperatorNode) opNode.getParent());
        }
    }

    /**
     * This is a delegate, which inserts a new operand at the left of its position.
     * 
     * @author 01es
     */
    private static class InsertLeftOperand implements IOnClickEventListener {
        private static final long serialVersionUID = -5997134419614047784L;

        private OperandNode opNode;

        public InsertLeftOperand(OperandNode opNode) {
            this.opNode = opNode;
        }

        @SuppressWarnings("unchecked")
        public void click(PInputEvent event) {
            int index = opNode.getOperand().getOperator().getOperands().indexOf(opNode.getOperand());
            opNode.getOperand().getOperator().insert(index, new HybridOperand(opNode.getOperand().getOperator().getDefaultValue().toString()));
            OperatorNode.composeNode((OperatorNode) opNode.getParent());
        }
    }

    private void addOperatorNode() {
        operatorNode = new OperatorNode((IOperator) getOperand().getValue());
        addChild(operatorNode);
    }

    @SuppressWarnings("unchecked")
    private void addTextNode(String defaultValue, DocumentFilter filter) {
        textNode = new TextNode(GlobalObjects.canvas, getOperand().getValue().toString(), filter);
        // textNode.setPedding(new AbstractNode.Pedding(2, 2, 2, 2));
        textNode.setBackgroundColor(Color.white);
        textNode.setStroke(null);
        textNode.bind((IValue) getOperand());
        textNode.reshapeEditor();
        addChild(textNode);

        updater = new Updater(this);
        ((IValue) ((IValue) getOperand()).getValue()).registerUpdater(updater);
    }

    private void removeTextNode() {
        if (textNode != null) {
            textNode.removeFromParent();
            textNode = null;
        }
    }

    private void removeOperatorNode() {
        if (operatorNode != null) {
            operatorNode.removeFromParent();
            operatorNode.getOperator().setContainingOperand(null);
            operatorNode = null;
        }
    }

    private void removeBoundNameNode() {
        if (boundNameNode != null) {
            boundNameNode.removeFromParent();
            boundNameNode = null;
        }
    }

    private transient Updater updater;

    public IOperand getOperand() {
        return operand;
    }

    private void setOperand(IOperand<IValue> operand) {
        this.operand = operand;
    }

    /**
     * Only nodes of types OperatorNode or PropertyBoundNameNode can be attached and only when no another instance of OperatorNode is already attached.
     */
    public boolean isCompatible(PNode node) {
        // if one of the nodes is attached then deny
        if (operatorNode != null) { // || boundNameNode != null
            return false;
        }
        // if one of the nodes is not of an appropriate type then deny
        if (!(node instanceof OperatorNode) && !(node instanceof BoundNameNode)) {
            return false;
        }
        // check type compatibility
        OperatorType thisType = ((OperatorNode) this.getParent()).getOperator().type();
        OperatorType type = null;
        if (node instanceof OperatorNode) {
            type = ((OperatorNode) node).getOperator().type();
        } else if (node instanceof BoundNameNode) {
            type = ((BoundNameNode) node).type();
        }

        return OperatorType.areCompatible(thisType, type);
    }

    /**
     * Handles attachment of operators and property bound names.
     */
    @SuppressWarnings("unchecked")
    public void doAfterAttach(PNode node) {
        erase();
        removeTextNode();
        removeOperatorNode();
        removeBoundNameNode();

        if (node instanceof OperatorNode) {
            operatorNode = (OperatorNode) node;
            getOperand().setValue(operatorNode.getOperator());
            operatorNode.getOperator().setContainingOperand(getOperand());
            // type cast operator: more restrictive type is used
            OperatorType thisType = ((OperatorNode) getParent()).getOperator().type();
            OperatorType type = (((OperatorNode) node).getOperator()).type();
            if (thisType == OperatorType.ANY) {
                ((OperatorNode) getParent()).getOperator().setType(type);
            } else {
                (((OperatorNode) node).getOperator()).setType(thisType);
            }
        } else if (node instanceof BoundNameNode) {
            boundNameNode = (BoundNameNode) node;
            boundNameNode.removeFromParent();
            OperatorType thisType = ((OperatorNode) getParent()).getOperator().type();
            OperatorType type = ((BoundNameNode) node).type();
            if (thisType == OperatorType.ANY) {
                ((OperatorNode) getParent()).getOperator().setType(type);
            }
            // boundNameNode is removed from the parent (operand in this case) and its textual representation is used instead
            getOperand().erase();
            getOperand().setValue(boundNameNode.getValue());
            ((IValue) getOperand().getValue()).setEmptyPermitted(false);
            ((IValue) getOperand().getValue()).setDefaultValue(boundNameNode.getValue().getValue());

            addTextNode(boundNameNode.getValue().getValue(), new WordDocumentFilter());

            System.out.println(getOperand().getOperator().getRepresentation());
        }
    }

    /**
     * In case where an operator node is removed it is necessary to reintroduce text node and assign an empty string to the operand property.
     */
    public void doAfterDetach(PNode node) {
        // handle situation where the removed node is ParenthesisOperatorNode
        if (node instanceof ParenthesisOperatorNode) {
            if (((ParenthesisOperatorNode) node).getOperator().getOperands().get(0).getValue() instanceof IValue) {
                ((ParenthesisOperatorNode) node).getOperator().setType(OperatorType.ANY);
            }
        }
        // handle situation where the node was removed from
        // ParenthesisOperatorNode
        if (this.getParent() instanceof ParenthesisOperatorNode) {
            ParenthesisOperatorNode parent = (ParenthesisOperatorNode) this.getParent();

            if (parent.getOperator().getOperands().get(0).getValue() instanceof IValue && parent.getOperator().getContainingOperand() == null) {
                parent.getOperator().setType(OperatorType.ANY);
            }
        }

        erase();
    }

    private static class Updater implements IUpdater<String>, Serializable {
        private static final long serialVersionUID = -157761029760590338L;

        private OperandNode node;

        public Updater(OperandNode node) {
            this.node = node;
        }

        public void update(String newValue) {
            if (!node.textNode.getText().equals(newValue)) {
                node.textNode.setText(newValue);
                node.reshape(false);
            }
        }
    }

    @SuppressWarnings("unchecked")
    void erase() {
        // TODO this area requires improvement. Specifically, removal of
        // updaters does not seem to be happening correctly.
        if (getOperand().getValue() instanceof IValue) {
            ((IValue) ((IValue) getOperand()).getValue()).removeUpdater(updater); // need to unregister updater if
            // it existed
        } else if (getOperand().getValue() instanceof IOperator) {
            for (IOperand operand : ((IOperator) (getOperand().getValue())).getOperands()) {
                operand.removeUpdater(updater);
            }
        }
        removeOperatorNode();
        removeTextNode();
        removeBoundNameNode();

        getOperand().erase();
        getOperand().setValue(getOperand().getOperator().getDefaultValue());
        String value = getOperand().getOperator().getDefaultValue().toString();
        addTextNode(value, getDocumentFilter(value));
        ((IValue) getOperand().getValue()).setEmptyPermitted(false);
        ((IValue) getOperand().getValue()).setDefaultValue(getOperand().getOperator().getDefaultValue().toString());
    }

    public boolean canBeDetached() {
        return false;
    }

    public void highlight(Stroke stroke) {
    }

    /**
     * This method is overridden in order to ensure proper menu placement.
     */
    public boolean setBounds(double x, double y, double width, double height) {
        boolean result = super.setBounds(x, y, width, height);

        if (operand != null) {
            handleMenu(getBounds());
        }

        return result;
    }

    private void handleMenu(Rectangle2D bounds) {
        if (menuPlace != null) {
            menuPlace.removeFromParent();
            getLayoutIgnorantNodes().remove(menuPlace);
            enabled = menuPlace.getPickable();
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
        Color color = new Color(210, 210, 210);
        menuPlace.setStrokePaint(color);
        menuPlace.setPaint(color);
        menuPlace.setPickable(enabled);

        getLayoutIgnorantNodes().add(menuPlace);
        addChild(menuPlace);

        menuPlace.addInputEventListener(menuEventListener);
    }

    private final PInputEventListener menuEventListener = new RingMenuInvoker(this);

    public RingMenu getRingMenu() {
        return menu;
    }

    /**
     * This private constructor is used for cloning.
     * 
     * @param operandNode
     */
    @SuppressWarnings("unchecked")
    private OperandNode(OperandNode operandNode) {
        setOperand((IOperand) operandNode.getOperand().clone());
        setDrag(operandNode.canDrag());
        setStroke(operandNode.getStroke());
        setOffset(operandNode.getOffset());

        setPedding(operandNode.getPedding());
        setBackgroundColor(operandNode.getBackgroundColor());
        if (getOperand().getValue() instanceof IValue) {
            textNode = operandNode.cloneTextNode();
            addChild(textNode);
        } else if (getOperand().getValue() instanceof IOperator) {
            operatorNode = (OperatorNode) operandNode.operatorNode.clone();
            addChild(operatorNode);
        }
    }

    private void addMenu(OperandNode node) {
        menu = new RingMenu(new String[] { "Remove operand", "Insert from right", MENU_ITEM_DELETE_CONTENT, "Insert from left" }, new Color[] { new Color(208, 32, 13),
                new Color(47, 132, 53), new Color(132, 36, 65), new Color(47, 132, 53) }, new IOnClickEventListener[] { new RemoveOperand(node), new InsertRightOperand(node),
                new DeleteContent(node), new InsertLeftOperand(node) }, 12);
    }

    private TextNode cloneTextNode() {
        textNode.setDummyCloning(false);
        TextNode clone = (TextNode) textNode.clone();
        clone.setPedding(new AbstractNode.Pedding(2, 2, 2, 2));
        clone.setBackgroundColor(Color.white);
        clone.setStroke(null);
        return clone;
    }

    @SuppressWarnings("unchecked")
    public Object clone() {
        OperandNode clone = new OperandNode(this);
        clone.setOffset(getOffset());
        if (clone.getOperand().getValue() instanceof IValue) {
            ((IValue) (clone.getOperand()).getValue()).removeUpdater(clone.updater);
            clone.updater = new Updater(clone);
            ((IValue) (clone.getOperand()).getValue()).registerUpdater(clone.updater);
        }
        clone.addMenu(clone);
        return clone;
    }
}
