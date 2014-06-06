package ua.com.fielden.uds.designer.zui.component.fact.fieldcondition;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ua.com.fielden.uds.designer.zui.component.expression.OperatorType;
import ua.com.fielden.uds.designer.zui.component.fact.BoundNameNode;
import ua.com.fielden.uds.designer.zui.component.fact.FactNode;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.interfaces.IUpdater;
import ua.com.fielden.uds.designer.zui.util.GlobalObjects;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * This is the class, which represents FieldCondition for display (e.g. for embedding into other nodes like FactNode).
 * 
 * @author 01es
 * 
 */
public class DisplayMode extends AbstractNode {
    private static final long serialVersionUID = -7850796761752610044L;

    private transient FieldCondition fieldCondition;
    private transient PInputEventListener clickEventHandler;

    private transient List<PNode> orderedNodes = new ArrayList<PNode>();
    private BoundNameNode boundName;

    // TODO this is field is just a temporary measure to support transition between display/design modes before implementing
    // a proper expression visualisation from its string representation
    private transient ExpressionNode expressionNode = new ExpressionNode();

    public DisplayMode(FieldCondition conditionNode) {
        super(new RoundRectangle2D.Double(0., 0., 10., 10., 1, 6));

        boundName = createBoundNameNode(conditionNode);

        if (boundName != null) {
            addChild(boundName);
            boundName.getValue().registerUpdater(new Updater(this));
        }
        PText text = new PText(conditionNode.toStringWithoutBinding());
        text.setPickable(false);
        addChild(text);
        orderedNodes.add(text);

        setFieldCondition(conditionNode);

        clickEventHandler = new Event();

        enable();

        setBackgroundColor(Color.white);
        setRounding(new BorderRounding(true, true, true, true));
        reshape(false);
    }

    private class Event extends PBasicInputEventHandler {
        public Event() {
            getEventFilter().setAndMask(InputEvent.BUTTON1_MASK);
        }

        public void mouseClicked(PInputEvent event) {
            FactNode factNode = DisplayMode.this.fieldCondition.getParent();
            factNode.design(getFieldCondition());
            disable();
            System.out.println("DisplayMode clicked");
        }
    };

    public static BoundNameNode createBoundNameNode(FieldCondition conditionNode) {
        if (conditionNode.getFieldBinding() != null && !"".equals(conditionNode.getFieldBinding())) {
            return new BoundNameNode(conditionNode.getFieldBinding(), OperatorType.ARITHMETIC);
        }

        return null;
    }

    /**
     * It is necessary to override this method in order to keep boundName node updated with actually attached clone of the original node.
     */
    public void addChild(PNode node) {
        super.addChild(node);
        if (node instanceof BoundNameNode) {
            orderedNodes.remove(boundName);
            boundName = (BoundNameNode) node;
            orderedNodes.add(0, boundName);
        }
    }

    public void disable() {
        removeInputEventListener(clickEventHandler);
    }

    public void layoutComponents() {
        Comparator<PNode> comparator = new Comparator<PNode>() {
            public int compare(PNode node1, PNode node2) {
                Integer index1 = orderedNodes.indexOf(node1);
                Integer index2 = orderedNodes.indexOf(node2);
                return index1.compareTo(index2);
            }
        };

        hlrLayoutComponents(0, comparator);
    }

    public void enable() {
        addInputEventListener(clickEventHandler);
    }

    private FieldCondition getFieldCondition() {
        return fieldCondition;
    }

    private void setFieldCondition(FieldCondition conditionNode) {
        this.fieldCondition = conditionNode;
    }

    public void highlight(Stroke stroke) {
        Cursor normalCursor = new Cursor(Cursor.HAND_CURSOR);
        GlobalObjects.frame.setCursor(normalCursor);
    }

    public void dehighlight() {
        Cursor normalCursor = new Cursor(Cursor.DEFAULT_CURSOR);
        GlobalObjects.frame.setCursor(normalCursor);
    }

    public BoundNameNode getBoundName() {
        return boundName;
    }

    private static class Updater implements IUpdater<String>, Serializable {
        private static final long serialVersionUID = 4751693210475629677L;

        private DisplayMode mode;

        public Updater(DisplayMode mode) {
            this.mode = mode;
        }

        public void update(String newValue) {
            if (!newValue.equals(mode.getFieldCondition().getFieldBinding())) {
                mode.getFieldCondition().setFieldBinding(newValue);
            }
            if (mode.getParent() != null) {
                ((FactNode) mode.getParent()).justifyConditionNodes();
            }
        }
    }

    public ExpressionNode getExpressionNode() {
        return expressionNode;
    }

    public void setExpressionNode(ExpressionNode expressionNode) {
        this.expressionNode = expressionNode;
    }
}