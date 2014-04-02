package ua.com.fielden.uds.designer.zui.component.fact;

import java.awt.Color;
import java.awt.Paint;
import java.awt.event.InputEvent;
import java.io.Serializable;

import ua.com.fielden.uds.designer.zui.component.expression.OperatorType;
import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode;
import ua.com.fielden.uds.designer.zui.component.generic.TextNode;
import ua.com.fielden.uds.designer.zui.component.generic.filter.WordDocumentFilter;
import ua.com.fielden.uds.designer.zui.component.generic.value.SingularityStringValue;
import ua.com.fielden.uds.designer.zui.component.generic.value.StringValue;
import ua.com.fielden.uds.designer.zui.interfaces.IDraggable;
import ua.com.fielden.uds.designer.zui.interfaces.IOnClickEventListener;
import ua.com.fielden.uds.designer.zui.interfaces.IUpdater;
import ua.com.fielden.uds.designer.zui.interfaces.IValue;
import ua.com.fielden.uds.designer.zui.util.GlobalObjects;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * This is a node, which represents a bound to a fact or fact's condition name. Its main purpose is to be used for composition of boolean or arithmetical expressions.
 * 
 * @author 01es
 * 
 */
public class BoundNameNode extends AbstractNode implements IDraggable, Cloneable, IOnClickEventListener {
    private static final long serialVersionUID = 546674497844408784L;

    private TextNode boundNameNode;
    private transient SingularityStringValue value;

    private transient OperatorType operatorType = OperatorType.ANY;

    public BoundNameNode(String boundName, OperatorType operatorType) {
        decorate();
        this.operatorType = operatorType;

        createBoundNameNode(boundName);
        setValue(new SingularityStringValue());
        value.registerUpdater(new Updater(this));
        value.setValue(boundName);

        reshape(false);

        addInputEventListener(new Event(this));
    }

    protected void decorate() {
        Paint paint = new Color(175, 175, 16); // (new Color(47, 132, 53)).brighter();
        setBackgroundColor(paint);
        setStrokePaint(paint);
        setPedding(new Pedding(2, 2, 2, 2));
        setCurvaturePrc(40);
        setRounding(new BorderRounding(true, true, false, false));
    }

    private void createBoundNameNode(String boundName) {
        boundNameNode = new TextNode(GlobalObjects.canvas, boundName, new WordDocumentFilter());
        boundNameNode.setOnClickHook(this);
        boundNameNode.setPickable(false);
        boundNameNode.setFontSize(13);
        boundNameNode.setTextPaint(Color.white);
        boundNameNode.setStrokePaint(new Color(1, 1, 1, 0));
        boundNameNode.setBackgroundColor(new Color(1, 1, 1, 0));
        boundNameNode.getEditor().setBackground((Color) getBackgroundColor());
        boundNameNode.setPedding(new Pedding(3, 3, 3, 3));
        boundNameNode.reshape(false);
        addChild(boundNameNode);
    }

    private static class Event extends PBasicInputEventHandler {
        private BoundNameNode node;

        public Event(BoundNameNode node) {
            this.node = node;
            getEventFilter().setAndMask(InputEvent.BUTTON3_MASK);
        }

        public void mouseClicked(PInputEvent event) {
            node.boundNameNode.setTextPaint(Color.black);
            node.boundNameNode.initiateEditing(event);
        }
    };

    protected static class Updater implements IUpdater<String>, Serializable {
        private static final long serialVersionUID = 4751693210475629677L;

        private BoundNameNode node;

        public Updater(BoundNameNode node) {
            this.node = node;
        }

        public void update(String newValue) {
            node.boundNameNode.setText(newValue);
            node.boundNameNode.reshape(false);
            node.reshape(false);
            if (node.getParent() instanceof AbstractNode) {
                ((AbstractNode) node.getParent()).reshape(false);
            }
        }
    }

    public SingularityStringValue getValue() {
        return value;
    }

    private class DummyValue implements IValue<StringValue>, Serializable {
        private static final long serialVersionUID = -6486719761594914920L;
        private StringValue value1 = new StringValue("IValueisbound");

        public StringValue getValue() {
            return value1;
        }

        public void setValue(StringValue value) {
            this.value1 = value;
        }

        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        public void registerUpdater(IUpdater<StringValue> updater) {
        }

        public void removeUpdater(IUpdater<StringValue> updater) {
        }

        public String getDefaultValue() {
            return null;
        }

        public boolean isEmptyPermitted() {
            return false;
        }

        public void setDefaultValue(String defaultValue) {
        }

        public void setEmptyPermitted(boolean emptyPermitted) {
        }
    };

    protected void setValue(SingularityStringValue value) {
        this.value = value;
        DummyValue dummyValue = new DummyValue();
        dummyValue.setValue(value);
        boundNameNode.bind(dummyValue);
    }

    public boolean canBeDetached() {
        return true;
    }

    public boolean canDrag() {
        return true;
    }

    public boolean getRemoveAfterDrop() {
        return false;
    }

    public void setRemoveAfterDrop(boolean flag) {
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BoundNameNode)) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        BoundNameNode cmpTo = (BoundNameNode) obj;

        return getValue().equals(cmpTo.getValue());
    }

    public Object clone() {
        BoundNameNode clone = new BoundNameNode(boundNameNode.getText(), type());
        clone.setOffset(this.getOffset());
        clone.setValue(this.getValue());
        clone.getValue().registerUpdater(new Updater(clone));
        return clone;
    }

    public OperatorType type() {
        return operatorType;
    }

    public void click(PInputEvent event) {
        boundNameNode.setPickable(false);
        boundNameNode.setTextPaint(Color.white);
    }
}