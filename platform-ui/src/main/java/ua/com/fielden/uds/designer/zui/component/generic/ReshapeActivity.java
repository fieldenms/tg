package ua.com.fielden.uds.designer.zui.component.generic;

import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * This is an abstract class, which is used by AbstractNode (one of the constructor's parameters) for the purpose of proper animation of the reshaping process.
 * 
 * @author 01es
 */
public abstract class ReshapeActivity extends PActivity {

    private AbstractNode node;
    private Rectangle2D newBounds;

    public ReshapeActivity(final long aDuration) {
        super(aDuration);
    }

    public ReshapeActivity(final long aDuration, final AbstractNode node, final Rectangle2D newBounds) {
        super(aDuration);
        this.node = node;
        this.newBounds = newBounds;
    }

    public void init(final AbstractNode node, final Rectangle2D newBounds) {
        this.node = node;
        this.newBounds = newBounds;
    }

    static int count = 0;

    protected void activityStep(final long elapsedTime) {
        super.activityStep(elapsedTime);
        process(elapsedTime);
    }

    private void process(final long elapsedTime) {
        final double elapsedTimePrc = (double) elapsedTime * 100 / getDuration();
        // expand bounds
        final Rectangle2D prevBounds = node.getBounds();
        final PDimension deltaBounds = new PDimension(newBounds.getWidth() - prevBounds.getWidth(), newBounds.getHeight() - prevBounds.getHeight());

        final double newWidth = prevBounds.getWidth() + deltaBounds.getWidth() * elapsedTimePrc / 100.;
        final double newHeight = prevBounds.getHeight() + deltaBounds.getHeight() * elapsedTimePrc / 100.;
        node.setBounds(newBounds.getX(), newBounds.getY(), newWidth, newHeight);
        // move linked nodes and handle parents
        final Set<PNode> processedNodes = new HashSet<PNode>();
        final PDimension delta = new PDimension(newWidth - prevBounds.getWidth(), newHeight - prevBounds.getHeight());
        node.resetAll(delta, processedNodes); // .getDeepParent(null)
        node.handleParent(node); // .getDeepParent(null)
        processedNodes.clear();
    }

    protected void activityFinished() {
        process(getDuration());
        onActivityFinished();
    }

    /**
     * Should be implemented if some post reshaping activity is required.
     * 
     */
    protected abstract void onActivityFinished();
}
