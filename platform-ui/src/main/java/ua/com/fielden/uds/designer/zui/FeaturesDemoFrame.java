package ua.com.fielden.uds.designer.zui;

import java.awt.Color;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ua.com.fielden.uds.designer.MetaType;
import ua.com.fielden.uds.designer.zui.component.MetaTypeNode;
import ua.com.fielden.uds.designer.zui.component.fact.ConstraintType;
import ua.com.fielden.uds.designer.zui.component.fact.FactNode;
import ua.com.fielden.uds.designer.zui.component.fact.Operation;
import ua.com.fielden.uds.designer.zui.component.fact.fieldcondition.FieldCondition;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.component.generic.ExpanderButton;
import ua.com.fielden.uds.designer.zui.component.generic.ExpanderNode;
import ua.com.fielden.uds.designer.zui.component.generic.GenericContainerLayer;
import ua.com.fielden.uds.designer.zui.component.generic.GenericContainerNode;
import ua.com.fielden.uds.designer.zui.component.generic.GenericSlotContainerNode;
import ua.com.fielden.uds.designer.zui.component.generic.PlaceHolderNode;
import ua.com.fielden.uds.designer.zui.component.generic.TextNode;
import ua.com.fielden.uds.designer.zui.component.generic.filter.WordDocumentFilter;
import ua.com.fielden.uds.designer.zui.component.generic.value.StringValue;
import ua.com.fielden.uds.designer.zui.component.link.CurlyLink;
import ua.com.fielden.uds.designer.zui.component.link.LeftCurlyLink;
import ua.com.fielden.uds.designer.zui.component.link.LineLink;
import ua.com.fielden.uds.designer.zui.component.link.LinkArrowLocation;
import ua.com.fielden.uds.designer.zui.component.link.RightCurlyLink;
import ua.com.fielden.uds.designer.zui.event.DragEventHandler;
import ua.com.fielden.uds.designer.zui.event.WheelRatoteZoomEventHandler;
import ua.com.fielden.uds.designer.zui.interfaces.ILink;
import ua.com.fielden.uds.designer.zui.interfaces.IUpdater;
import ua.com.fielden.uds.designer.zui.interfaces.IValue;
import ua.com.fielden.uds.designer.zui.util.GlobalObjects;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PDragEventHandler;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.PFrame;

/**
 * This a frame used for designing a rule.
 * 
 * @author 01es
 * 
 */
public class FeaturesDemoFrame extends PFrame {

    private static final long serialVersionUID = 6293369164646614319L;

    @Override
    public void initialize() {
        super.initialize();
        // rendering settings
        getCanvas().setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        getCanvas().setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        getCanvas().setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);

        getCanvas().removeInputEventListener(getCanvas().getZoomEventHandler());
        getCanvas().addInputEventListener(new WheelRatoteZoomEventHandler(0.2, 2.0));

        getCanvas().getLayer().setPickable(false);
        getCanvas().getLayer().setChildrenPickable(true);

        final PLayer nodeLayer = getCanvas().getLayer();
        final PLayer linkLayer = nodeLayer;
        // PLayer linkLayer = new PLayer();
        // getCanvas().getCamera().addLayer(0, linkLayer);
        // need to initialize global objects, which can (and are) be referenced from different part of an application
        GlobalObjects.canvas = getCanvas();
        GlobalObjects.nodeLayer = linkLayer;
        GlobalObjects.linkLayer = linkLayer;
        GlobalObjects.frame = this;

        final PDragEventHandler handler = new DragEventHandler(nodeLayer);
        nodeLayer.addInputEventListener(handler);

        final GenericContainerNode containerOne = new GenericContainerNode(new PDimension(50, 50));
        containerOne.setBackgroundColor(new Color(213, 30, 62));
        containerOne.translate(50, 200);
        nodeLayer.addChild(containerOne);

        final GenericContainerNode containerTwo = new GenericContainerNode(new PDimension(50, 50));
        containerTwo.setBackgroundColor(new Color(33, 134, 209));
        containerTwo.translate(200, 200);
        nodeLayer.addChild(containerTwo);

        final GenericContainerNode containerThree = new GenericContainerNode(new PDimension(50, 50));
        containerThree.setBackgroundColor(new Color(33, 104, 109));
        containerThree.translate(350, 200);
        nodeLayer.addChild(containerThree);

        ILink link = new CurlyLink(containerOne, containerTwo, LinkArrowLocation.AT_BOTH_NODES);
        link = new RightCurlyLink(containerOne, containerTwo, LinkArrowLocation.AT_BOTH_NODES);
        link = new LineLink(containerOne, containerTwo, LinkArrowLocation.AT_BOTH_NODES);
        link = new LeftCurlyLink(containerOne, containerTwo, LinkArrowLocation.AT_BOTH_NODES);
        linkLayer.addChild((PNode) link);

        containerOne.addLink(link);
        containerTwo.addLink(link);

        link = new RightCurlyLink(containerTwo, containerThree, LinkArrowLocation.AT_BOTH_NODES);
        // link = new LineLink(containerTwo, containerThree, LinkArrowLocation.AT_BOTH_NODES);
        linkLayer.addChild((PNode) link);
        containerTwo.addLink(link);
        containerThree.addLink(link);

        final GenericContainerNode containerFour = new GenericContainerNode(new PDimension(50, 50));
        containerFour.setBackgroundColor(new Color(103, 140, 109));
        nodeLayer.addChild(containerFour);

        final GenericContainerNode containerFive = new GenericContainerNode(new PDimension(50, 50));
        containerFive.setBackgroundColor(new Color(193, 140, 129));
        containerFive.translate(150, 0);
        nodeLayer.addChild(containerFive);

        final GenericContainerNode containerSix = new GenericContainerNode(new PDimension(50, 50));
        containerSix.setBackgroundColor(new Color(193, 140, 129));
        containerSix.translate(250, 0);
        nodeLayer.addChild(containerSix);

        // field condition
        final List<FieldCondition> conditions = new ArrayList<FieldCondition>();
        final FieldCondition fieldCondition = new FieldCondition(null);
        fieldCondition.setFieldBinding("ID");
        fieldCondition.setFieldName("id");
        fieldCondition.setOperation(Operation.LESS);
        fieldCondition.setConstraint(ConstraintType.LITERAL);
        fieldCondition.setConstraintExpression("100");
        conditions.add(fieldCondition);
        final FactNode factNode = new FactNode("dummy", "WorkOrder", conditions, nodeLayer);
        factNode.translate(550, 300);
        nodeLayer.addChild(factNode);

        // TextNode example
        final TextNode textNode = new TextNode(getCanvas(), new WordDocumentFilter(), 12);
        textNode.translate(50, 350);
        textNode.setEditable(true);
        final IValue<StringValue> value = new IValue<StringValue>() {
            private StringValue value1 = new StringValue("IValueisbound");

            public StringValue getValue() {
                return value1;
            }

            public void setValue(final StringValue value) {
                value1 = value;
            }

            @Override
            public Object clone() {
                try {
                    return super.clone();
                } catch (final CloneNotSupportedException e) {
                    throw new RuntimeException(e);
                }
            }

            public void registerUpdater(final IUpdater<StringValue> updater) {
            }

            public void removeUpdater(final IUpdater<StringValue> updater) {
            }

            public String getDefaultValue() {
                return null;
            }

            public boolean isEmptyPermitted() {
                return false;
            }

            public void setDefaultValue(final String defaultValue) {
            }

            public void setEmptyPermitted(final boolean emptyPermitted) {
            }
        };
        textNode.bind(value);
        nodeLayer.addChild(textNode);

        final TextNode readonlyTextNode = new TextNode(getCanvas(), null, 12);
        readonlyTextNode.translate(400, 25);
        readonlyTextNode.setEditable(false);
        readonlyTextNode.setText(":");
        nodeLayer.addChild(readonlyTextNode);

        link = new LineLink(textNode, readonlyTextNode, LinkArrowLocation.AT_BOTH_NODES);
        linkLayer.addChild((PNode) link);
        textNode.addLink(link);
        readonlyTextNode.addLink(link);

        containerSix.attach(null, textNode, false);

        final PlaceHolderNode placeHolderNode = new PlaceHolderNode("place holder");
        placeHolderNode.translate(200, 350);
        nodeLayer.addChild(placeHolderNode);

        // generic spot container
        final GenericSlotContainerNode spotContainer = new GenericSlotContainerNode(4, new RoundRectangle2D.Double(0., 0., 10., 10., 2, 2));
        spotContainer.setBounds(0, 0, 250, 70);
        spotContainer.setPedding(new AbstractNode.Pedding(25, 25, 40, 40));
        spotContainer.reshape(false);
        spotContainer.translate(150, 50);
        nodeLayer.addChild(spotContainer);

        final GenericContainerLayer containeLayer = new GenericContainerLayer();
        containeLayer.setBackgroundColor(Color.blue);
        containeLayer.setBounds(100, 400, 200, 200);
        nodeLayer.addChild(containeLayer);

        // demostrating expander button behaviour
        ExpanderNode expanderNode = new ExpanderNode(null, true); // first create ExpanderNode
        ExpanderButton expanderButton = new ExpanderButton(expanderNode); // then create ExpanderButton and associate an instance of ExpanderNode with it
        expanderButton.translate(50, 330);
        nodeLayer.addChild(expanderButton);

        expanderNode = new ExpanderNode(null, true); // first create ExpanderNode
        expanderButton = new ExpanderButton(expanderNode, expanderButton); // then create ExpanderButton and associate an instance of ExpanderNode with it
        expanderButton.translate(50, 360);
        nodeLayer.addChild(expanderButton);

        // this is an example of how high level classes can be used as part of of other high level classes in a role of their properties
        final Set<MetaType> datePeriodProps = new HashSet<MetaType>();
        datePeriodProps.add(new MetaType("Date", "fromDate"));
        datePeriodProps.add(new MetaType("Date", "toDate"));
        final MetaType highLevelMetaTypeDatePeriod = new MetaType("DatePeriod", datePeriodProps);

        final Set<MetaType> typeProps = new HashSet<MetaType>();
        typeProps.add(new MetaType("String", "id"));
        typeProps.add(new MetaType(highLevelMetaTypeDatePeriod, "earlyPeriod"));
        typeProps.add(new MetaType("DatePeriod", "actualPeriod"));
        final MetaType highLevelMetaType = new MetaType("WorkOrder", typeProps);

        final MetaTypeNode metaTypeNode = new MetaTypeNode(highLevelMetaType);
        metaTypeNode.translate(70, 300);
        nodeLayer.addChild(metaTypeNode);

    }

    public static void main(final String[] args) {
        final PFrame frame = new FeaturesDemoFrame();
        frame.setTitle("UDS Designer: Components");
        frame.setSize(790, 545);
        frame.setVisible(true);
    }
}
