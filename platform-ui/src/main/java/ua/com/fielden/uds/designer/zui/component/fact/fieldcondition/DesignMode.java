package ua.com.fielden.uds.designer.zui.component.fact.fieldcondition;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import ua.com.fielden.uds.designer.zui.component.fact.ConstraintType;
import ua.com.fielden.uds.designer.zui.component.fact.FactNode;
import ua.com.fielden.uds.designer.zui.component.fact.Operation;
import ua.com.fielden.uds.designer.zui.component.fact.OperationNode;
import ua.com.fielden.uds.designer.zui.component.fact.OperationPlaceHolderNode;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.component.generic.Button;
import ua.com.fielden.uds.designer.zui.component.generic.GenericContainerNode;
import ua.com.fielden.uds.designer.zui.component.generic.ReshapeActivity;
import ua.com.fielden.uds.designer.zui.component.generic.SerializableGradientPaint;
import ua.com.fielden.uds.designer.zui.component.generic.TextNode;
import ua.com.fielden.uds.designer.zui.component.generic.TitleBar;
import ua.com.fielden.uds.designer.zui.component.generic.filter.WordDocumentFilter;
import ua.com.fielden.uds.designer.zui.component.link.CurlyLink;
import ua.com.fielden.uds.designer.zui.component.link.LineLink;
import ua.com.fielden.uds.designer.zui.component.link.LinkArrowLocation;
import ua.com.fielden.uds.designer.zui.component.link.RightCurlyLink;
import ua.com.fielden.uds.designer.zui.interfaces.ILink;
import ua.com.fielden.uds.designer.zui.interfaces.IOnClickEventListener;
import ua.com.fielden.uds.designer.zui.util.GlobalObjects;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * This is the class, which represents FieldCondition for design, e.i. modification. This mode implies bidirectional FieldCondition->DesignNode->FieldCondition conversion.
 * 
 * @author 01es
 * 
 */
public class DesignMode extends GenericContainerNode {
    private static final long serialVersionUID = -7850796761752610044L;

    private transient FieldCondition fieldCondition;

    private TextNode fieldBindingNode;
    private TextNode colonNode;
    private TextNode fieldNameNode;
    private OperationPlaceHolderNode operationNode;

    private ExpressionNode expressionNode;

    private ILink fieldBindingNode_colonNode;
    private ILink colonNode_fieldNameNode;
    private ILink fieldNameNode_operationNode;
    private ILink operationNode_literalConstraintNode;

    private transient TitleBar titleBar;

    public DesignMode(FieldCondition conditionNode, ExpressionNode expression) {
	super(new Rectangle2D.Double(0., 0., 10., 10.), false);
	setBackgroundColor(new Color(255, 255, 185));
	setFieldCondition(conditionNode);

	// TODO temporary measure or is it?
	setExpressionNode(expression);

	composeNode();
    }

    private TitleBar composeTitle() {
	// register a resizing animation activity so that title gets resized appropriately
	setReshapeActivity(new ReshapeActivity(300) {
	    protected void onActivityFinished() {
		handleParent(DesignMode.this);
		titleBar.adjustTitle();
		handleBorder();
	    }
	});
	// apply button switches FieldCondition to display mode and applies changes
	Button btnApply = new Button(" apply ");
	btnApply.addOnClickEventListener(new IOnClickEventListener() {
	    private static final long serialVersionUID = -259565889433027351L;

	    public void click(PInputEvent event) {
		System.out.println("apply");
		// updateCondition();
		FactNode factNode = fieldCondition.getParent();
		factNode.applyDesign(DesignMode.this);
	    }
	});
	btnApply.reshape(false);
	// cancel button switches FieldCondition to display mode and ignores changes
	Button btnCancel = new Button(" cancel ");
	btnCancel.addOnClickEventListener(new IOnClickEventListener() {
	    private static final long serialVersionUID = -259565889433027351L;

	    public void click(PInputEvent event) {
		System.out.println("cancel");
		FactNode factNode = fieldCondition.getParent();
		factNode.cancelDesign(DesignMode.this);
	    }
	});
	btnCancel.reshape(false);
	TitleBar title = new TitleBar("Design Mode for field " + getFieldCondition().getFieldName(), Arrays.asList(new Button[] { btnApply, btnCancel }));
	Paint paint = new SerializableGradientPaint((float) title.getX(), (float) title.getY(), new Color(63, 81, 21), (float) (title.getX()), (float) (title.getY() + title.getHeight()), new Color(179, 213, 98));
	title.setPaint(paint);
	title.setPickable(false);
	title.setOffset(0, 0);
	setMinConstraint(new PDimension(title.getWidth(), title.getHeight()));
	return title;
    }

    public boolean isCompatible(PNode node) {
	return false;
    }

    public void highlight(Stroke stroke) {
    }

    public void dehighlight() {
    }

    @Override
    protected Rectangle2D calcBounds() {
	titleBar.removeFromParent();
	Rectangle2D childrenBounds = getUnionOfChildrenBounds(null);
	double proposedWidth = childrenBounds.getWidth() + getPedding().getRight() + getPedding().getLeft();
	double proposedHeight = childrenBounds.getHeight() + titleBar.getHeight() + getPedding().getBottom();
	Rectangle2D newBounds = new Rectangle2D.Double(getBounds().getX(), getBounds().getY(), (getMinConstraint() != null && getMinConstraint().width > proposedWidth ? getMinConstraint().width
		: proposedWidth), (getMinConstraint() != null && getMinConstraint().height > proposedHeight ? getMinConstraint().height : proposedHeight));
	addChild(titleBar);
	titleBar.adjustTitle();
	return newBounds;
    }

    /**
     * This method is overridden because DesignNode follows a different layout principles than GenericContainer.
     */
    protected void layoutComponents() {
	if (titleBar != null) {
	    titleBar.layoutComponents();
	}

	setPedding(new AbstractNode.Pedding(10, 10, 10, 10));
	double xOffset = getPedding().getLeft();
	double yOffset = getPedding().getTop() + titleBar.getTitle().getHeight();

	clear();

	double minDist = 25;
	double maxHeight = 0;
	// ================== adding nodes one by one =======================//
	// first row of components
	fieldBindingNode.setOffset(xOffset, yOffset - fieldBindingNode.getY()); // adding fieldBindingNode
	fieldBindingNode.reshape(false);
	maxHeight = maxHeight < fieldBindingNode.getHeight() ? fieldBindingNode.getHeight() : maxHeight;
	xOffset += fieldBindingNode.getFullBoundsReference().getWidth() + minDist; // moving to the right
	colonNode.setOffset(xOffset, yOffset - fieldBindingNode.getY()); // adding colonNode
	colonNode.reshape(false);
	maxHeight = maxHeight < colonNode.getHeight() ? colonNode.getHeight() : maxHeight;
	xOffset += colonNode.getFullBoundsReference().getWidth() + minDist; // moving to the right
	fieldNameNode.setOffset(xOffset, yOffset - fieldBindingNode.getY()); // adding fieldNameNode
	maxHeight = maxHeight < fieldNameNode.getHeight() ? fieldNameNode.getHeight() : maxHeight;
	fieldNameNode.reshape(false);
	// next row of components
	xOffset = getPedding().getLeft() + minDist; // reset xOffset
	yOffset = getPedding().getTop() + maxHeight + 20 + minDist; // adjust yOffset

	operationNode.setOffset(xOffset, yOffset - operationNode.getY()); // adding operationNode
	operationNode.reshape(false);
	xOffset += operationNode.getFullBoundsReference().getWidth() + minDist; // moving to the right
	expressionNode.setOffset(xOffset + 20, yOffset - expressionNode.getY() - 1); // adding literalConstraintNode
	expressionNode.reshape(false);
	yOffset += expressionNode.getHeight() + minDist; // move down
	xOffset = getPedding().getLeft() + minDist; // reset xOffset

	/*
	 * predicateNode.setOffset(xOffset, yOffset - predicateNode.getY()); // adding predicateNode xOffset +=
	 * predicateNode.getFullBoundsReference().getWidth() + minDist; // moving to the right predicateNode.reshape(false);
	 * predicateConditionNode.setOffset(xOffset, yOffset - predicateConditionNode.getY()); // adding predicateConditionNode
	 * predicateConditionNode.reshape(false);
	 */
	// add all nodes
	addChild(fieldBindingNode);
	addChild(colonNode);
	addChild(fieldNameNode);
	addChild(operationNode);
	addChild(expressionNode);
	// add all links
	addChild((PNode) fieldBindingNode_colonNode);
	addChild((PNode) colonNode_fieldNameNode);
	addChild((PNode) fieldNameNode_operationNode);
	addChild((PNode) operationNode_literalConstraintNode);
    }

    private void remove(PNode node) {
	if (node != null) {
	    node.removeFromParent();
	}
    }

    private void clear() {
	remove(fieldBindingNode);
	remove(colonNode);
	remove(fieldNameNode);
	remove(operationNode);
	remove(expressionNode);

	remove((PNode) fieldBindingNode_colonNode);
	remove((PNode) colonNode_fieldNameNode);
	remove((PNode) fieldNameNode_operationNode);
	remove((PNode) operationNode_literalConstraintNode);
    }

    private void composeNode() {
	assert GlobalObjects.isInitialised() : "GlobalObjects is not initialized properly.";

	titleBar = composeTitle();
	getLayoutIgnorantNodes().add(titleBar);
	addChild(titleBar);

	fieldBindingNode = composeFieldBinding(getFieldCondition().getFieldBinding());
	colonNode = composeColon(fieldBindingNode); // fieldBindingNode is passed for linking
	fieldNameNode = composeFieldName(getFieldCondition().getFieldName(), colonNode); // colonNode is passed for linking

	operationNode = composeOperation(getFieldCondition().getOperation(), fieldNameNode); // fieldNameNode is passed for linking
	expressionNode = composeConstraint(ConstraintType.LITERAL, getFieldCondition(), operationNode); // operationNode is passed for linking

	reshape(false);
	// adjust title bar in order for it to fit the while width
	titleBar.adjustTitle();
    }

    private ExpressionNode composeConstraint(ConstraintType constraintType, FieldCondition fieldCondition, AbstractNode operationNode) {
	ExpressionNode resultedNode = null;
	// constraintType == LITERAL
	if (expressionNode == null) {
	    expressionNode = new ExpressionNode();
	}
	// TODO need to provide some mechanism for building a visial representation of expression from its string representation
	/*
	 * if (fieldCondition.getConstraint() == constraintType) { expressionNode.setText(fieldCondition.getConstraintExpression() == null ||
	 * "".equals(fieldCondition.getConstraintExpression()) ? "<" + constraintType.toString() + ">" : fieldCondition.getConstraintExpression()); } else {
	 * expressionNode.setText("<" + constraintType.toString() + ">"); }
	 */
	resultedNode = expressionNode;

	assert resultedNode != null : "resultedNode must be properly instantiated";
	// once resultedNode has been instantiated it should be linked to operationNode
	ILink link = new RightCurlyLink(operationNode, resultedNode, LinkArrowLocation.AT_END_NODE);
	link.setGlobalBounds(false);
	// GlobalObjects.linkLayer.addChild((PNode) link);
	operationNode.addLink(link);
	resultedNode.addLink(link);

	operationNode_literalConstraintNode = link;

	resultedNode.setBackgroundColor(new Color(255, 255, 128));
	return resultedNode;
    }

    private OperationPlaceHolderNode composeOperation(Operation operation, TextNode fieldNameNode) {
	if (operationNode == null) {
	    operationNode = new OperationPlaceHolderNode("<operation>");
	    operationNode.setPedding(new Pedding(5, 5, 5, 5));
	    operationNode.setCurvaturePrc(20);
	    operationNode.setRounding(new BorderRounding(true, true, true, true));

	    if (operation != null) {
		operationNode.attach(null, new OperationNode(operation), false);
	    }
	    operationNode.setBackgroundColor(new Color(255, 255, 128));
	}
	operationNode.setDrag(false);
	fieldNameNode_operationNode = new CurlyLink(fieldNameNode, operationNode, LinkArrowLocation.AT_END_NODE);
	fieldNameNode_operationNode.setGlobalBounds(false);
	// GlobalObjects.linkLayer.addChild((PNode) link);
	fieldNameNode.addLink(fieldNameNode_operationNode);
	operationNode.addLink(fieldNameNode_operationNode);

	return operationNode;
    }

    private TextNode composeFieldName(String fieldName, TextNode colonNode) {
	if (fieldNameNode == null) {
	    fieldNameNode = new TextNode(GlobalObjects.canvas, null);
	    fieldNameNode.setBackgroundColor(new Color(255, 255, 128));
	    fieldNameNode.setPedding(new Pedding(5, 5, 5, 5));
	    fieldNameNode.setCurvaturePrc(20);
	    fieldNameNode.setRounding(new BorderRounding(true, true, true, true));
	    fieldNameNode.setEditable(false);
	}
	fieldNameNode.setText(fieldName);
	// link nodes together
	colonNode_fieldNameNode = new LineLink(colonNode, fieldNameNode, LinkArrowLocation.AT_END_NODE);
	colonNode_fieldNameNode.setGlobalBounds(false);
	// GlobalObjects.linkLayer.addChild((PNode) link);
	colonNode.addLink(colonNode_fieldNameNode);
	fieldNameNode.addLink(colonNode_fieldNameNode);

	return fieldNameNode;
    }

    private TextNode composeColon(AbstractNode fieldBindingNode) {
	if (colonNode == null) {
	    colonNode = new TextNode(GlobalObjects.canvas, null);
	    colonNode.setBackgroundColor(new Color(255, 255, 128));
	    colonNode.setPedding(new Pedding(5, 5, 5, 5));
	    colonNode.setCurvaturePrc(20);
	    colonNode.setRounding(new BorderRounding(true, true, true, true));
	    colonNode.setEditable(false);
	}
	colonNode.setText(":");
	// link nodes together
	fieldBindingNode_colonNode = new LineLink(fieldBindingNode, colonNode, LinkArrowLocation.AT_END_NODE);
	fieldBindingNode_colonNode.setGlobalBounds(false);
	// GlobalObjects.linkLayer.addChild((PNode) link);
	fieldBindingNode.addLink(fieldBindingNode_colonNode);
	colonNode.addLink(fieldBindingNode_colonNode);

	return colonNode;
    }

    private TextNode composeFieldBinding(String fieldBinding) {
	if (fieldBindingNode == null) {
	    fieldBindingNode = new TextNode(GlobalObjects.canvas, new WordDocumentFilter());
	    fieldBindingNode.setPedding(new Pedding(5, 5, 5, 5));
	    fieldBindingNode.setCurvaturePrc(20);
	    fieldBindingNode.setRounding(new BorderRounding(true, true, true, true));
	    fieldBindingNode.setBackgroundColor(new Color(255, 255, 128));
	}
	if ("".equals(fieldBinding) || fieldBinding == null) {
	    fieldBindingNode.setText(getNextCondName());
	} else {
	    fieldBindingNode.setText(fieldBinding);
	}
	return fieldBindingNode;
    }

    private static Integer index = 0;

    private String getNextCondName() {
	return "condition" + (index++);
    }

    public FieldCondition getFieldCondition() {
	return fieldCondition;
    }

    private void setFieldCondition(FieldCondition conditionNode) {
	assert conditionNode != null : "Instance of FieldCondition is requried.";
	this.fieldCondition = conditionNode;
    }

    public TextNode getFieldBindingNode() {
	return fieldBindingNode;
    }

    public OperationPlaceHolderNode getOperationNode() {
	return operationNode;
    }

    public ExpressionNode getExpressionNode() {
	return expressionNode;
    }

    private void setExpressionNode(ExpressionNode node) {
	if (expressionNode != null) {
	    expressionNode.removeFromParent();
	}
	expressionNode = node;
	addChild(expressionNode);
    }
}