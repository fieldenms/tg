package ua.com.fielden.uds.designer.zui.component;

import java.awt.Color;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import ua.com.fielden.uds.designer.zui.component.expression.OperatorNode;
import ua.com.fielden.uds.designer.zui.component.expression.OperatorType;
import ua.com.fielden.uds.designer.zui.component.fact.FactStubNode;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.component.generic.Button;
import ua.com.fielden.uds.designer.zui.component.generic.GenericContainerNode;
import ua.com.fielden.uds.designer.zui.component.generic.ReshapeActivity;
import ua.com.fielden.uds.designer.zui.component.generic.SerializableGradientPaint;
import ua.com.fielden.uds.designer.zui.component.generic.TitleBar;
import ua.com.fielden.uds.designer.zui.interfaces.IOnClickEventListener;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * This node is a visual representation of a rule, which may contain many fact nodes and one boolean expression.
 * 
 * @author 01es
 * 
 */
public class RuleNode extends GenericContainerNode {
    private static final long serialVersionUID = -5039370831376992771L;

    private transient String ruleName;

    private transient TitleBar titleBar;

    private transient OperatorNode rule;
    /**
     * A solo purposes of this list and the following comparator is to maintain an original order of fact nodes.
     */
    private transient List<FactStubNode> factStubNodes = new ArrayList<FactStubNode>();
    private transient Comparator<PNode> comparator = new Comparator<PNode>() {
        public int compare(PNode node1, PNode node2) {
            Integer index1 = factStubNodes.indexOf(node1);
            Integer index2 = factStubNodes.indexOf(node2);
            if (node1 instanceof FactStubNode && node2 instanceof FactStubNode) {
                System.out.println(index1);
                System.out.println(index2);
                System.out.println();
            }
            return index1.compareTo(index2);
        }
    };

    public RuleNode(String ruleName) {
        super(new Rectangle2D.Double(0., 0., 10., 10.), false);
        setBackgroundColor(new Color(1f, 1f, 1f, 0.5f));

        setRuleName(ruleName);
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
                RuleNode.this.removeFromParent();
            }
        });

        TitleBar title = new TitleBar("Rule \"" + ruleName + "\"", Arrays.asList(new Button[] { btnMoveToCentre, btnHide }));
        Paint paint = new SerializableGradientPaint((float) title.getX(), (float) title.getY(), new Color(63, 81, 21), (float) (title.getX()), (float) (title.getY() + title.getHeight()), new Color(179, 213, 98));
        title.setPaint(paint);
        title.setPickable(false);
        title.setOffset(0, 0);
        setMinConstraint(new PDimension(title.getWidth(), title.getHeight()));
        return title;
    }

    public void updateTitle(String title) {
        ruleName = title;
        titleBar.updateTitle("Rule \"" + ruleName + "\"");
    }

    private void composeNode() {
        titleBar = composeTitle();
        getLayoutIgnorantNodes().add(titleBar);
        addChild(titleBar);

        // add nodes representing fact stub nodes
        for (FactStubNode factStubNode : getFactStubNodes()) {
            addChild(factStubNode);
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

    public List<FactStubNode> getFactStubNodes() {
        return factStubNodes;
    }

    /**
     * RuleNode can accept only instances of FactStubNode or a logical expression.
     */
    public boolean isCompatible(PNode node) {
        if (!(node instanceof FactStubNode) && !(node instanceof OperatorNode)) {
            return false;
        }

        if (node instanceof OperatorNode) {
            if (OperatorType.BOOLEAN != ((OperatorNode) node).getOperator().type() && OperatorType.ANY != ((OperatorNode) node).getOperator().type()) {
                return false;
            }
        }

        return true;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    @Override
    public void doAfterAttach(PNode node) {
        if (node instanceof FactStubNode) {
            FactStubNode fNode = (FactStubNode) node;
            fNode.removeFromParent();
            for (int index = 0; index < getFactStubNodes().size(); index++) {
                if (getFactStubNodes().get(index) == null) {
                    getFactStubNodes().set(index, fNode);
                    addChild(fNode);
                    break;
                }
            }

            if (!getFactStubNodes().contains(fNode)) {
                getFactStubNodes().add(fNode);
                addChild(fNode);
            }
            fNode.setRemoveAfterDrop(false);
            fNode.setCanBeDetached(false);
        } else if (node instanceof OperatorNode) {
            if (rule != null) {
                rule.removeFromParent();
            }
            rule = (OperatorNode) node;
            reshape(false);
        }
        justifyConditionNodes();
    }

    public void doAfterDetach(PNode node) {
        if (node instanceof FactStubNode) {
            FactStubNode fNode = (FactStubNode) node;
            int index = getFactStubNodes().indexOf(fNode);
            if (index >= 0) {
                getFactStubNodes().set(index, null);
            }
        }
        justifyConditionNodes();
    }

    public void justifyConditionNodes() {
        double width = 0;
        for (FactStubNode currNode : getFactStubNodes()) {
            if (currNode != null && width < currNode.getWidth()) {
                width = currNode.getWidth();
            }
        }
        for (FactStubNode currNode : getFactStubNodes()) {
            if (currNode != null) {
                currNode.setMinConstraint(new PDimension(width, currNode.getHeight()));
                currNode.reshape(false);
            }
        }
    }

}
