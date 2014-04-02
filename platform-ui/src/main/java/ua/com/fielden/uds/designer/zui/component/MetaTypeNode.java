package ua.com.fielden.uds.designer.zui.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.uds.designer.MetaType;
import ua.com.fielden.uds.designer.zui.component.fact.FactStubNode;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.component.generic.Button;
import ua.com.fielden.uds.designer.zui.component.generic.ExpanderButton;
import ua.com.fielden.uds.designer.zui.component.generic.ExpanderNode;
import ua.com.fielden.uds.designer.zui.component.generic.GenericContainerNode;
import ua.com.fielden.uds.designer.zui.component.generic.SerializableGradientPaint;
import ua.com.fielden.uds.designer.zui.component.generic.TitleBar;
import ua.com.fielden.uds.designer.zui.interfaces.ICollapsable;
import ua.com.fielden.uds.designer.zui.interfaces.IOnClickEventListener;
import ua.com.fielden.uds.designer.zui.util.GlobalObjects;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * This is a visual representation of an instance of the MetaClass class. It utilises Piccolo PNode/PPath composition for visual representation of MetaData.
 * 
 * @author 01es
 * 
 */
public class MetaTypeNode extends GenericContainerNode implements ICollapsable {
    private static final long serialVersionUID = -2594571171151000467L;

    private MetaType metaType;
    private Set<MetaTypeNode> propNodes = new HashSet<MetaTypeNode>();
    private ExpanderNode expanderNode;
    private PText nameNode;
    private TitleBar titleBar;

    private boolean compatible = true;

    boolean leftTop = true;
    boolean leftBottom = true;
    boolean rightTop = false;
    boolean rightBottom = true;

    public MetaTypeNode(MetaType metaType) {
        super(new GeneralPath(GeneralPath.WIND_EVEN_ODD));
        setCurvaturePrc(5);
        setRounding(new BorderRounding(leftTop, leftBottom, rightTop, rightBottom));
        setBackgroundColor(Color.white);

        setMetaType(metaType);
        if (metaType.getProperties().size() > 0) {
            setBackgroundColor(new Color((float) (218. / 255.), (float) (218. / 255.), (float) (218. / 255.), 0.8f));
        }

        if (metaType.isHighLevel()) {
            composeHighLevelNode();
            setBackgroundColor(new Color((float) (134. / 255.), (float) (156. / 255.), (float) (245. / 255.), 0.5f));
        } else {
            composeNode();
        }

        deepRebuildBorder();
    }

    private void deepRebuildBorder() {
        for (MetaTypeNode propNode : propNodes) {
            propNode.deepRebuildBorder();
        }
    }

    private void composeNode() {
        setPedding(new AbstractNode.Pedding(20, 5, 30, 5));
        // add class name
        nameNode = new PText(metaType.toString());
        getLayoutIgnorantNodes().add(nameNode);
        nameNode.setOffset(5, 3);
        nameNode.setPickable(false);
        addChild(nameNode);
        // add properties
        for (MetaType property : metaType.getProperties()) {
            MetaTypeNode propNode = new MetaTypeNode(property);
            propNode.reshape(false);
            propNodes.add(propNode);
        }
        // perform some additional initialisation, add expander node
        if (metaType.getProperties().size() > 0) {
            Font newFont = nameNode.getFont().deriveFont(Font.BOLD);
            nameNode.setFont(newFont);

            expanderNode = new ExpanderNode(this, true);
            expanderNode.up();
            addChild(expanderNode);
            getLayoutIgnorantNodes().add(expanderNode);
        }
        setMinConstraint(new PDimension(getBounds().width, nameNode.getHeight()));
        reshape(false);

        positionExpanderNode();

        // initially the node is collapsed
        finaliseNodeComposition();
    }

    /**
     * 
     */
    private void finaliseNodeComposition() {
        if (expanderNode != null) {
            expanderNode.down();
        }
        setCompatible(false);
    }

    private void composeHighLevelNode() {
        setPedding(new AbstractNode.Pedding(25, 5, 5, 5));
        // add title bar
        titleBar = composeTitle();
        getLayoutIgnorantNodes().add(titleBar);
        addChild(titleBar);
        // add properties
        for (MetaType property : metaType.getProperties()) {
            MetaTypeNode propNode = new MetaTypeNode(property);
            propNode.reshape(false);
            propNodes.add(propNode);
        }
        expanderNode.up();
        setMinConstraint(new PDimension(getBounds().width, titleBar.getHeight()));
        reshape(false);
        // fill all nodes into the width
        setMinWidth();
        finaliseNodeComposition();
    }

    private void setMinWidth() {
        double width = getWidth() - getPedding().getLeft() - getPedding().getRight();
        for (MetaTypeNode node : propNodes) {
            node.setWidth(width);
            node.setMinConstraint(new PDimension(width, node.getMinConstraint().getHeight()));
            node.setMinWidth();
            node.positionExpanderNode();
        }
    }

    protected void layoutComponents() {
        super.layoutComponents();
        if (titleBar != null) {
            titleBar.layoutComponents();
        }
    }

    private TitleBar composeTitle() {
        // compose title
        Button btnMoveToCentre = new Button(" centre ");
        btnMoveToCentre.reshape(false);
        btnMoveToCentre.addOnClickEventListener(new IOnClickEventListener() {
            private static final long serialVersionUID = -259565889433027351L;

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
        Button btnNewFact = new Button(" + fact ");
        btnNewFact.reshape(false);
        btnNewFact.addOnClickEventListener(new IOnClickEventListener() {
            private static final long serialVersionUID = -3734608168585035423L;

            public void click(PInputEvent event) {
                FactStubNode factStub = new FactStubNode("", MetaTypeNode.this.getMetaType());
                Point2D point = new Point2D.Double(MetaTypeNode.this.getOffset().getX(), MetaTypeNode.this.getOffset().getY() + MetaTypeNode.this.getBounds().getHeight() + 20);
                factStub.setOffset(point);
                GlobalObjects.nodeLayer.addChild(factStub);
            }
        });

        expanderNode = new ExpanderNode(this, true);
        expanderNode.up(); // this up call is necessary to figure out a correct optimal width for properties
        ExpanderButton expanderButton = new ExpanderButton(expanderNode, btnNewFact);

        TitleBar title = new TitleBar(metaType.getName(), Arrays.asList(new Button[] { expanderButton, btnMoveToCentre, btnNewFact }));
        Paint paint = new SerializableGradientPaint((float) title.getX(), (float) title.getY(), new Color(1, 21, 118), (float) (title.getX()), (float) (title.getY() + title.getHeight()), new Color(107, 133, 254));
        title.setPaint(paint);
        title.setPickable(false);
        title.setOffset(0, 0);
        setMinConstraint(new PDimension(title.getWidth(), title.getHeight()));

        return title;
    }

    private void positionExpanderNode() {
        if (expanderNode != null && nameNode != null) {
            expanderNode.setOffset(getBounds().getWidth() - expanderNode.getWidth() - 4, nameNode.getOffset().getY() + nameNode.getBounds().getCenterY() + expanderNode.getHeight()
                    / 2);
        }
    }

    public MetaType getMetaType() {
        return metaType;
    }

    private void setMetaType(MetaType metaType) {
        this.metaType = metaType;
    }

    public String toString() {
        return "Node for " + getMetaType();
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof MetaTypeNode))
            return false;

        MetaTypeNode cmpTo = (MetaTypeNode) obj;
        return getMetaType().equals(cmpTo.getMetaType());
    }

    public int hashCode() {
        return getMetaType().hashCode() * 37;
    }

    public boolean getRemoveAfterDrop() {
        return true;
    }

    public void setRemoveAfterDrop(boolean flag) {
    }

    public boolean canDrag() {
        return true;
    }

    public boolean canBeDetached() {
        return getMetaType().isHighLevel();
    }

    public void collapse() {
        for (MetaTypeNode propNode : propNodes) {
            // detach(null, propNode, false, false);
            propNode.removeFromParent();
        }
        reshape(false);
        positionExpanderNode();
    }

    public void expand() {
        for (MetaTypeNode propNode : propNodes) {
            // attach(null, propNode, false);
            propNode.reshape(false);
            addChild(propNode);
        }
        reshape(false);
        positionExpanderNode();
    }

    public boolean isCompatible(PNode node) {
        return compatible || (propNodes.contains(node) && !getChildrenReference().contains(node));
    }

    private void setCompatible(boolean flag) {
        compatible = flag;
    }

    /*
     * private MetaTypeNode clone;
     * 
     * public void doAfterAttach(PNode node) { clone = (MetaTypeNode) node; System.out.println("doAfterAttach: contains clone? " + propNodes.contains(clone));
     * compatible = false; propNodes.add(clone); clone = null; }
     * 
     * public void doAfterDetach(PNode node) { compatible = true; System.out.println("doAfterDetach: is clone set? " + (clone != null)); propNodes.remove(node); }
     */
}
