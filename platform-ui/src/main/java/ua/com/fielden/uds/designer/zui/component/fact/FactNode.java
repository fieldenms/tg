package ua.com.fielden.uds.designer.zui.component.fact;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.list.SetUniqueList;

import ua.com.fielden.uds.designer.zui.component.AnimationDelegate;
import ua.com.fielden.uds.designer.zui.component.MetaTypeNode;
import ua.com.fielden.uds.designer.zui.component.fact.fieldcondition.DesignMode;
import ua.com.fielden.uds.designer.zui.component.fact.fieldcondition.DisplayMode;
import ua.com.fielden.uds.designer.zui.component.fact.fieldcondition.FieldCondition;
import ua.com.fielden.uds.designer.zui.component.fact.fieldcondition.FieldCondition.Mode;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.component.generic.Button;
import ua.com.fielden.uds.designer.zui.component.generic.GenericContainerNode;
import ua.com.fielden.uds.designer.zui.component.generic.ReshapeActivity;
import ua.com.fielden.uds.designer.zui.component.generic.SerializableGradientPaint;
import ua.com.fielden.uds.designer.zui.component.generic.TitleBar;
import ua.com.fielden.uds.designer.zui.interfaces.IContainer;
import ua.com.fielden.uds.designer.zui.interfaces.IDraggable;
import ua.com.fielden.uds.designer.zui.interfaces.IOnClickEventListener;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * A rule consists of facts. A fact consists of field conditions. This class is a visual representation of a fact, which provides an interaction point between users and field
 * conditions.
 * 
 * @author 01es
 * 
 */
public class FactNode extends GenericContainerNode {
    private static final long serialVersionUID = -2670080782982575663L;

    private transient String factBinding;
    private transient String factName;
    private transient String boundName;

    private transient PLayer nodeLayer;
    /**
     * Conditions may contain only unique condition nodes.
     */
    private transient List<FieldCondition> conditions;

    private transient TitleBar titleBar;
    /**
     * A solo purposes of this list and the following comparator is to maintain an original order of condition nodes.
     */
    private transient List<DisplayMode> conditionNodes = new ArrayList<DisplayMode>();
    private transient Comparator<PNode> comparator = new Comparator<PNode>() {
        public int compare(PNode node1, PNode node2) {
            Integer index1 = conditionNodes.indexOf(node1);
            Integer index2 = conditionNodes.indexOf(node2);
            return index1.compareTo(index2);
        }
    };

    public FactNode(String boundName, String className, List<FieldCondition> conditions, PLayer nodeLayer) {
        super(new Rectangle2D.Double(0., 0., 10., 10.), false);
        setBackgroundColor(new Color(179, 213, 98));
        this.nodeLayer = nodeLayer;
        setFactName(className); // there shouldn't really be any cases where factName != className
        if (conditions != null) {
            setConditions(conditions);
        } else {
            setConditions(new ArrayList<FieldCondition>());
        }

        this.boundName = boundName;
        composeNode();
    }

    private TitleBar composeTitle() {
        // register a resizing animation activity so that title gets resized appropriately
        setReshapeActivity(new ReshapeActivity(300) {
            protected void onActivityFinished() {
                titleBar.adjustTitle();
                handleBorder();
            }
        });
        // compose title bar
        Button btnMoveToCentre = new Button(" centre ");
        btnMoveToCentre.reshape(false);
        btnMoveToCentre.addOnClickEventListener(new IOnClickEventListener() {
            private static final long serialVersionUID = 965408671353660692L;

            public void click(PInputEvent event) {
                Button button = (Button) event.getPickedNode();
                Rectangle2D point = button.getParentBar().getParent().getGlobalBounds();
                PActivity activity = event.getTopCamera().animateViewToCenterBounds(point, false, 100);
                // delegate is required to properly handle a situation where a mouse has been released outside of a button -- affects colouring
                activity.setDelegate(new AnimationDelegate(event) {
                    public void activityFinished(PActivity activity) {
                        ((Button) getEvent().getPickedNode()).getDefaultEventHandler().mouseReleased(getEvent());
                    }
                });
            }
        });

        Button btnHide = new Button(" hide ", btnMoveToCentre);
        btnHide.reshape(false);
        btnHide.addOnClickEventListener(new IOnClickEventListener() {
            private static final long serialVersionUID = 965408671353660692L;

            public void click(PInputEvent event) {
                FactNode.this.removeFromParent();
            }
        });

        TitleBar title = new TitleBar("Fact \"" + boundName + "\" for " + getFactName(), Arrays.asList(new Button[] { btnMoveToCentre, btnHide }));
        /*
         * Paint paint = new SerializableGradientPaint((float)title.getX(), (float)title.getY(), new Color(1, 21, 118), (float)(title.getX() ),
         * (float)(title.getY() + title.getHeight()), new Color(107, 133, 254));
         */
        Paint paint = new SerializableGradientPaint((float) title.getX(), (float) title.getY(), new Color(63, 81, 21), (float) (title.getX()), (float) (title.getY() + title.getHeight()), new Color(179, 213, 98));

        title.setPaint(paint);
        title.setPickable(false);
        title.setOffset(0, 0);
        setMinConstraint(new PDimension(title.getWidth(), title.getHeight()));
        return title;
    }

    public void updateTitle(String title) {
        boundName = title;
        titleBar.updateTitle("Fact \"" + boundName + "\" for " + getFactName());
    }

    private void composeNode() {
        titleBar = composeTitle();
        getLayoutIgnorantNodes().add(titleBar);
        addChild(titleBar);

        // add nodes representing filed constraints
        for (FieldCondition condition : getConditions()) {
            PNode node = condition.visualise(FieldCondition.Mode.DISPLAY);
            conditionNodes.add((DisplayMode) node);
            addChild(node);
        }
        setPedding(new AbstractNode.Pedding(25, 7, 7, 7));

        reshape(false); // reshape to fit all just added nodes
        titleBar.adjustTitle();
    }

    protected void layoutComponents() {
        vtbLayoutComponents(0, 0, comparator);
        if (titleBar != null) {
            titleBar.layoutComponents();
        }
    }

    /**
     * This method is invoked by DisplayMode of FieldCondition in order to visualise a design mode for user to perform changes.
     * 
     * @param condition
     */
    public void design(FieldCondition condition) {
        assert getConditions().contains(condition) : "an unknown instance of FieldCondition";

        AbstractNode desingNode = condition.visualise(FieldCondition.Mode.DESIGN);
        // move designNode to centre of the viewable area
        Point2D point = nodeLayer.getCamera(0).getBounds().getCenter2D();
        desingNode.setOffset(new Point2D.Double(point.getX() - desingNode.getWidth() / 2, point.getY() - desingNode.getHeight() / 2));
        nodeLayer.addChild(desingNode);
    }

    /**
     * This method is invoked by DesignMode of FieldCondition in order to refresh condition's graphical representation.
     * 
     * @param designMode
     */
    public void applyDesign(DesignMode designMode) {
        FieldCondition condition = designMode.getFieldCondition();
        assert getConditions().contains(condition) : "an unknown instance of FieldCondition";
        // /////////////// update FieldCondition instance using values from DesignMode ////////////////////////////////
        getConditions().remove(condition); // once updated condition becomes different to the one associated with this fact node, therefore it should be
        // removed before its modification.

        condition.setFieldBinding(designMode.getFieldBindingNode().getText());
        if (designMode.getOperationNode().getAttachedNode() != null) {
            condition.setOperation(designMode.getOperationNode().getAttachedNode().getOperation());
            condition.setConstraintExpression(designMode.getExpressionNode().getOperator().getRepresentation());
        } else {
            condition.setOperation(null);
        }
        getConditions().add(condition); // once updated, condition should be re-associated with this fact node

        designMode.removeFromParent();
        DisplayMode currDisplayNode = condition.getDisplayModeNode();
        currDisplayNode.removeFromParent();
        int index = conditionNodes.indexOf(currDisplayNode); // index is necessary for keeping the original order of condition nodes

        BoundNameNode boundName = currDisplayNode.getBoundName();
        AbstractNode displayNode = condition.visualise(FieldCondition.Mode.DISPLAY);
        displayNode.setOffset(currDisplayNode.getOffset());

        // TODO this is a temporary measure
        ((DisplayMode) displayNode).setExpressionNode(designMode.getExpressionNode());

        // need to ensure that the bound name is taken from the previous instance of the display node
        if (boundName != null) {
            DisplayMode newDisplayNode = (DisplayMode) displayNode;
            BoundNameNode newBoundName = newDisplayNode.getBoundName();
            if (newBoundName != null) {
                newBoundName.removeFromParent();
                boundName.getValue().setValue(newBoundName.getValue().getValue());
                newDisplayNode.addChild(boundName);
            }
            newDisplayNode.reshape(false);
        }
        conditionNodes.add(index, (DisplayMode) displayNode); // insert a new condition node at index
        conditionNodes.remove(currDisplayNode); // remove the old node
        justifyConditionNodes();
        addChild(displayNode);
        reshape(true);
    }

    /**
     * This method simply disregards changes to a field condition and hides DesignMode node.
     * 
     * @param desigMode
     */
    public void cancelDesign(DesignMode desigMode) {
        FieldCondition condition = desigMode.getFieldCondition(); // It is assumed that the passed FieldCondition reference is already updated with changes by
        // its DesignMode.
        assert getConditions().contains(condition) : "an unknown instance of FieldCondition";
        condition.getDisplayModeNode().enable(); // make is susceptible to an onClick event

        desigMode.removeFromParent();
    }

    public List<FieldCondition> getConditions() {
        return conditions;
    }

    @SuppressWarnings("unchecked")
    private void setConditions(List<FieldCondition> conditions) {
        for (FieldCondition condition : conditions) {
            condition.setParent(this);
        }
        this.conditions = SetUniqueList.decorate(conditions); // unfortunately SetUniqueList is not generic
    }

    public boolean addFieldCondition(FieldCondition node) {
        return getConditions().add(node);
    }

    public FieldCondition getFieldConditionNode(int index) {
        return getConditions().get(index);
    }

    public String getFactName() {
        return factName;
    }

    private void setFactName(String factName) {
        this.factName = factName;
    }

    public String getFactBinding() {
        return factBinding;
    }

    public void setFactBinding(String factBinding) {
        this.factBinding = factBinding;
    }

    /**
     * FactNode can accept only instances of MetaClassPropertyNode.
     */
    public boolean isCompatible(PNode node) {
        if (!(node instanceof MetaTypeNode)) {
            return false;
        }
        if (((MetaTypeNode) node).getMetaType().isHighLevel()) {
            return false;
        }
        // check that property belongs to the same class
        if (!getFactName().equals(((MetaTypeNode) node).getMetaType().getTopParentType(null).getName())) {
            return false;
        }
        // need to avoid duplicates
        MetaTypeNode pNode = (MetaTypeNode) node;
        for (FieldCondition condition : getConditions()) {
            StringBuffer path = new StringBuffer();
            pNode.getMetaType().getTopParentType(path);
            if (condition.getFieldName().equals(path.toString())) {
                return false;
            }
        }

        return true;
    }

    /**
     * This method is overridden in order to provide a custom behaviour.
     */
    @Override
    public void attach(PInputEvent event, PNode node, boolean animate) {
        if (isCompatible(node)) {
            // need to remove a node from its original parent
            if (node.getParent() instanceof IContainer) {
                ((IContainer) node.getParent()).detach(event, node, animate, false);
            } else {
                node.removeFromParent();
            }

            MetaTypeNode pNode = (MetaTypeNode) node;
            FieldCondition fieldCondition = new FieldCondition(this);
            StringBuffer path = new StringBuffer();
            pNode.getMetaType().getTopParentType(path);
            fieldCondition.setFieldName(path.toString());
            getConditions().add(fieldCondition);

            AbstractNode dNode = fieldCondition.visualise(Mode.DISPLAY);
            conditionNodes.add((DisplayMode) dNode);
            addChild(dNode);

            // ensure that all cpndition nodes are of the same width
            justifyConditionNodes();

            if (node instanceof IDraggable) {
                ((IDraggable) node).setRemoveAfterDrop(true);
            }
            // doAfterAttach(node);
            reshape(animate); // it is essential to perform reshape after doAfterAttach as it is possible that this method would introduce new visual
            // components
        }
    }

    public void justifyConditionNodes() {
        double width = 0;
        for (DisplayMode currNode : conditionNodes) {
            if (width < currNode.getWidth()) {
                width = currNode.getWidth();
            }
        }
        for (DisplayMode currNode : conditionNodes) {
            currNode.setMinConstraint(new PDimension(width, currNode.getHeight()));
            currNode.reshape(false);
        }
    }

    public String getBoundName() {
        return boundName;
    }

    public void setBoundName(String boundName) {
        this.boundName = boundName;
    }
}
