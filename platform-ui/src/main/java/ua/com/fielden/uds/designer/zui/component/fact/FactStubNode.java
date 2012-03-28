package ua.com.fielden.uds.designer.zui.component.fact;

import java.awt.Color;
import java.awt.Paint;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;
import java.io.Serializable;

import ua.com.fielden.uds.designer.MetaType;
import ua.com.fielden.uds.designer.zui.component.RuleNode;
import ua.com.fielden.uds.designer.zui.component.expression.OperatorType;
import ua.com.fielden.uds.designer.zui.interfaces.IUpdater;
import ua.com.fielden.uds.designer.zui.util.GlobalObjects;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * This node is used for a simplified representation of a fact, and is used within RuleNode.
 * 
 * @author 01es
 * 
 */
public class FactStubNode extends BoundNameNode {
    private static final long serialVersionUID = 6729074033823284628L;

    private static final String defaultName = "fact";
    private static Integer no = 0;
    private FactNode factNode;
    private MetaType clazz;

    public FactStubNode(String factName, MetaType clazz) {
	super(checkName(factName), OperatorType.BOOLEAN);
	this.clazz = clazz;
	factNode = new FactNode(getValue().getValue(), clazz.getName(), null, GlobalObjects.nodeLayer);
	addInputEventListener(new Event(this));

	getValue().registerUpdater(new TitleUpdater(this));
    }

    private static String checkName(String factName) {
	if ("".equals(factName) || null == factName) {
	    return defaultName + (no++);
	}

	return factName;
    }

    protected void decorate() {
	Paint paint = new Color(103, 121, 61);
	setBackgroundColor(paint);
	setStrokePaint(paint);
	setPedding(new Pedding(2, 2, 2, 2));
	setCurvaturePrc(140);
	setRounding(new BorderRounding(true, true, true, true));
    }

    public Object clone() {
	FactStubNode clone = new FactStubNode(getValue().getValue(), clazz); // ???
	clone.setOffset(this.getOffset());
	clone.setValue(this.getValue());
	clone.getValue().registerUpdater(new TitleUpdater(clone));
	clone.getValue().registerUpdater(new Updater(clone));
	clone.factNode = factNode;
	return clone;
    }

    private static class Event extends PBasicInputEventHandler {
	private FactStubNode node;

	public Event(FactStubNode node) {
	    this.node = node;
	    getEventFilter().setAndMask(InputEvent.BUTTON1_MASK);
	}

	public void mouseClicked(PInputEvent event) {
	    // move factNode to centre of the viewable area
	    Point2D point = GlobalObjects.nodeLayer.getCamera(0).getBounds().getCenter2D();

	    if (node.factNode.getOffset().getX() == 0 && node.factNode.getOffset().getY() == 0) { // direct comparison od double with 0 in this case is safe
		node.factNode.setOffset(new Point2D.Double(point.getX() - node.factNode.getWidth() / 2, point.getY() - node.factNode.getHeight() / 2));
	    }

	    GlobalObjects.nodeLayer.addChild(node.factNode);
	}
    };

    protected static class TitleUpdater implements IUpdater<String>, Serializable {
	private static final long serialVersionUID = 4751693210475629677L;

	private FactStubNode node;

	public TitleUpdater(FactStubNode node) {
	    this.node = node;
	}

	public void update(String newValue) {
	    if (node.factNode != null) {
		node.factNode.updateTitle(newValue);
	    }
	    if (node.getParent() instanceof RuleNode) {
		((RuleNode) node.getParent()).justifyConditionNodes();
	    }
	}
    }

    private boolean canBeDetached = true;

    public boolean canBeDetached() {
	return canBeDetached;
    }

    public void setCanBeDetached(boolean flag) {
	this.canBeDetached = flag;
    }

    public boolean canDrag() {
	return true;
    }

    private boolean removeAfterDrop = false;

    public boolean getRemoveAfterDrop() {
	return removeAfterDrop;
    }

    public void setRemoveAfterDrop(boolean flag) {
	removeAfterDrop = flag;
    }

    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof FactStubNode)) {
	    return false;
	}

	if (obj == this) {
	    return true;
	}
	FactStubNode cmpTo = (FactStubNode) obj;
	return getValue().getValue().equals(cmpTo.getValue().getValue()); // TODO add clazz comparison
    }
}
