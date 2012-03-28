package ua.com.fielden.uds.designer.zui.interfaces;

import java.awt.Paint;
import java.awt.Stroke;

import ua.com.fielden.uds.designer.zui.component.generic.AbstractNode.MutablePoint2D;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;

public interface IBasicNode {
    Paint getBackgroundColor();

    boolean canBeDetached();

    void highlight(Stroke stroke);

    void dehighlight();

    void highlight(PNode node, Paint paint);

    IBasicNode getDeepParent(MutablePoint2D offset);

    void onMouseEntered(PInputEvent event);

    void onMouseExited(PInputEvent event);

    void onStartDrag(PInputEvent event);

    void onEndDrag(PInputEvent event);

    void onDragging(PInputEvent event);

    boolean showToolTip();
}
