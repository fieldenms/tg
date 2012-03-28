package ua.com.fielden.uds.designer.zui;

import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.uds.designer.MetaType;
import ua.com.fielden.uds.designer.zui.component.MetaTypeNode;
import ua.com.fielden.uds.designer.zui.component.RuleNode;
import ua.com.fielden.uds.designer.zui.component.expression.OperatorPanelNode;
import ua.com.fielden.uds.designer.zui.component.fact.OperationPanelNode;
import ua.com.fielden.uds.designer.zui.event.DragEventHandler;
import ua.com.fielden.uds.designer.zui.util.GlobalObjects;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.event.PDragEventHandler;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.PFrame;

/**
 * This a frame used for designing a rule.
 * 
 * @author 01es
 * 
 */
public class DesignerFrame extends PFrame {

    private static final long serialVersionUID = 6293369164646614319L;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize() {
	super.initialize();
	// rendering settings
	getCanvas().setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
	getCanvas().setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
	getCanvas().setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);

	getCanvas().getLayer().setPickable(false);
	getCanvas().getLayer().setChildrenPickable(true);

	final PLayer nodeLayer = getCanvas().getLayer();
	final PLayer linkLayer = nodeLayer;
	// need to initialize global objects, which can (and are) be referenced from different part of an application
	GlobalObjects.canvas = getCanvas();
	GlobalObjects.nodeLayer = linkLayer;
	GlobalObjects.linkLayer = linkLayer;
	GlobalObjects.frame = this;

	final PDragEventHandler handler = new DragEventHandler(nodeLayer);
	nodeLayer.addInputEventListener(handler);

	// /////////////////////////////////// MetaType information ///////////////////////////////////////////////////////////
	// this is an example of how high level classes can be used as part of other high level classes as their properties //
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ////////////////// WorkOrder /////////////////////////
	Set<MetaType> datePeriodProps = new HashSet<MetaType>();
	datePeriodProps.add(new MetaType("Date", "fromDate"));
	datePeriodProps.add(new MetaType("Date", "toDate"));
	final MetaType highLevelMetaTypeEarlyDatePeriod = new MetaType("DatePeriod", datePeriodProps);
	datePeriodProps = new HashSet<MetaType>();
	datePeriodProps.add(new MetaType("Date", "fromDate"));
	datePeriodProps.add(new MetaType("Date", "toDate"));
	final MetaType highLevelMetaTypeActualDatePeriod = new MetaType("DatePeriod", datePeriodProps);

	Set<MetaType> costProps = new HashSet<MetaType>();
	costProps.add(new MetaType("Currency", "manCost"));
	costProps.add(new MetaType("Currency", "taskCost"));
	costProps.add(new MetaType("Currency", "dcCost"));
	final MetaType highLevelMetaTypeActualCost = new MetaType("Cost", costProps);

	costProps = new HashSet<MetaType>();
	costProps.add(new MetaType("Currency", "manCost"));
	costProps.add(new MetaType("Currency", "taskCost"));
	final MetaType manCost = new MetaType("Currency", "dcCost");
	costProps.add(manCost);
	final MetaType highLevelMetaTypeEstimatedCost = new MetaType("Cost", costProps);

	Set<MetaType> typeProps = new HashSet<MetaType>();
	typeProps.add(new MetaType("String", "woNo"));
	typeProps.add(new MetaType(highLevelMetaTypeActualCost, "actualCost"));
	typeProps.add(new MetaType(highLevelMetaTypeEstimatedCost, "estimatedCost"));
	typeProps.add(new MetaType(highLevelMetaTypeEarlyDatePeriod, "earlyPeriod"));
	typeProps.add(new MetaType(highLevelMetaTypeActualDatePeriod, "actualPeriod"));
	typeProps.add(new MetaType("String", "status"));
	typeProps.add(new MetaType("Set<DcBillMat>", "dcBillMats"));

	final MetaType workOrderMetaType = new MetaType("WorkOrder", typeProps);
	// //// End of WorkOrder metat type composition //////////
	final MetaTypeNode metaTypeNode = new MetaTypeNode(workOrderMetaType); // add a visual representation
	metaTypeNode.translate(20, 100); // 20, 20
	nodeLayer.addChild(metaTypeNode);

	// /////////////////////// DC Purcahse Order ///////////////////////////
	final Set<MetaType> supplierProps = new HashSet<MetaType>();
	supplierProps.add(new MetaType("String", "id"));
	supplierProps.add(new MetaType("String", "desc"));
	final MetaType supplierMetaType = new MetaType("Person", supplierProps);

	final Set<MetaType> originatorProps = new HashSet<MetaType>();
	originatorProps.add(new MetaType("String", "id"));
	originatorProps.add(new MetaType("String", "desc"));
	final MetaType originatorMetaType = new MetaType("Person", originatorProps);

	final Set<MetaType> authoriserProps = new HashSet<MetaType>();
	authoriserProps.add(new MetaType("String", "id"));
	authoriserProps.add(new MetaType("String", "desc"));
	final MetaType authoriserMetaType = new MetaType("Person", authoriserProps);

	final Set<MetaType> buyerProps = new HashSet<MetaType>();
	buyerProps.add(new MetaType("String", "id"));
	buyerProps.add(new MetaType("String", "desc"));
	final MetaType buyerMetaType = new MetaType("Person", buyerProps);

	typeProps = new HashSet<MetaType>();
	typeProps.add(new MetaType("String", "poNo"));
	typeProps.add(new MetaType(supplierMetaType, "supplier"));
	typeProps.add(new MetaType(originatorMetaType, "originator"));
	typeProps.add(new MetaType(authoriserMetaType, "authoriser"));
	typeProps.add(new MetaType(buyerMetaType, "buyer"));
	typeProps.add(new MetaType("String", "status"));
	typeProps.add(new MetaType("Boolean", "isAuthorised"));
	typeProps.add(new MetaType("Date", "authDate"));
	typeProps.add(new MetaType("Date", "requiredDate"));
	typeProps.add(new MetaType("Set<DcItem>", "items"));
	final MetaType purchaseOrderMetaType = new MetaType("DcPurchaseOrder", typeProps);

	final MetaTypeNode poMetaTypeNode = new MetaTypeNode(purchaseOrderMetaType); // add a visual representation
	poMetaTypeNode.translate(20, 20); // 320, 20
	nodeLayer.addChild(poMetaTypeNode);
	// /////////////////////////////////////////////////////////////////////////
	typeProps = new HashSet<MetaType>();
	typeProps.add(new MetaType("String", "itemNo"));
	typeProps.add(new MetaType("Date", "promisedDate"));
	typeProps.add(new MetaType("String", "status"));
	typeProps.add(new MetaType("Integer", "qtyOrd"));
	typeProps.add(new MetaType("Integer", "qtyRcvd"));
	typeProps.add(new MetaType("Currency", "unitPrice"));
	typeProps.add(new MetaType(purchaseOrderMetaType.clone(), "po"));
	typeProps.add(new MetaType(workOrderMetaType.clone(), "wo"));
	final MetaType dcItemMetaType = new MetaType("DcPoItem", typeProps);

	final MetaTypeNode dcItemMetaTypeNode = new MetaTypeNode(dcItemMetaType); // add a visual representation
	dcItemMetaTypeNode.translate(20, 60); // 670, 20
	nodeLayer.addChild(dcItemMetaTypeNode);
	// /////////////////////////////////////////////////////////////////////////
	// field condition
	/*
	 * List<FieldCondition> conditions = new ArrayList<FieldCondition>();
	 * FieldCondition fieldCondition = new FieldCondition(null);
	 * fieldCondition.setFieldBinding("est_cost"); StringBuffer path = new
	 * StringBuffer(); manCost.getTopParentType(path);
	 * fieldCondition.setFieldName(path.toString());
	 * fieldCondition.setOperation(Operation.LESS);
	 * fieldCondition.setConstraint(ConstraintType.LITERAL);
	 * fieldCondition.setConstraintExpression("100");
	 * conditions.add(fieldCondition);
	 * 
	 * FactNode factNode = new FactNode("dummy", "WorkOrder", conditions,
	 * nodeLayer); factNode.translate(280, 220);
	 * nodeLayer.addChild(factNode);
	 */

	// expression builder
	/*
	 * IOperator plusOperator = new Plus(); IOperand operand =
	 * plusOperator.getOperands().get(0); operand.setValue("100"); operand =
	 * plusOperator.getOperands().get(1); operand.setValue("20");
	 * 
	 * System.out.println(plusOperator.getRepresentation());
	 * 
	 * plusOperator.append(new HybridOperand("12"));
	 * 
	 * System.out.println(plusOperator.getRepresentation());
	 * 
	 * IOperator minusOperator = new Minus(); operand =
	 * minusOperator.getOperands().get(0); operand.setValue("100"); operand
	 * = minusOperator.getOperands().get(1); operand.setValue("20");
	 * 
	 * plusOperator.append(new HybridOperand(minusOperator));
	 * 
	 * System.out.println(plusOperator.getRepresentation());
	 * 
	 * OperatorNode operatorNode = new OperatorNode(plusOperator);
	 * operatorNode.translate(350, 1); nodeLayer.addChild(operatorNode);
	 */

	final OperatorPanelNode opContainer = new OperatorPanelNode();
	opContainer.translate(535, 5);
	nodeLayer.addChild(opContainer);

	final OperationPanelNode operContainer = new OperationPanelNode();
	operContainer.translate(810, 5);
	nodeLayer.addChild(operContainer);

	final RuleNode woCostRule = new RuleNode("Work Order Cost Monitoring");
	woCostRule.translate(20, 250);
	nodeLayer.addChild(woCostRule);

	final RuleNode overdueDcItemRule = new RuleNode("Overdue DC Item Monitoring");
	overdueDcItemRule.translate(20, 350);
	nodeLayer.addChild(overdueDcItemRule);

	final RuleNode woInactivityRule = new RuleNode("Inactive Work Order Monitoring");
	woInactivityRule.translate(20, 450);
	nodeLayer.addChild(woInactivityRule);

	/*
	 * GenericContainerNode containerSix = new GenericContainerNode(new
	 * PDimension(50, 50)); containerSix.setBackgroundColor(new Color(193,
	 * 140, 129)); containerSix.translate(350, 0);
	 * nodeLayer.addChild(containerSix);
	 */
    }

    public static void main(final String[] args) {
	final PFrame frame = new DesignerFrame();
	frame.setTitle("UDS Designer: Components");
	frame.setSize(1000, 600);
	frame.setVisible(true);
    }
}
