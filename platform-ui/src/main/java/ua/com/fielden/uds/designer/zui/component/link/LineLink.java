package ua.com.fielden.uds.designer.zui.component.link;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import ua.com.fielden.uds.designer.zui.component.generic.DefaultStroke;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * This link is depicted having two curls -- one at the starting node, second -- at the finishing (ending) node.
 * 
 * @author 01es
 * 
 */
public class LineLink extends AbstractLink {
    private static final long serialVersionUID = -1725863881823482655L;

    private PNode startNode;
    private PNode endNode;
    private transient PPath line;
    private transient PPath arrowAtStartNode;
    private transient PPath arrowAtEndNode;
    private transient LinkArrowLocation linkArrowLocation;

    public LineLink(final PNode startNode, final PNode endNode) {
        super();
        setStroke(new DefaultStroke(1));
        compose(startNode, endNode);
    }

    public LineLink(final PNode startNode, final PNode endNode, final LinkArrowLocation linkArrowLocation) {
        super();
        setStroke(new DefaultStroke(1));
        this.linkArrowLocation = linkArrowLocation;
        compose(startNode, endNode);
    }

    @Override
    public void reset() {
        super.reset();
        remove(line);
        remove(arrowAtStartNode);
        remove(arrowAtEndNode);
        compose(getStartNode(), getEndNode());
    }

    private void compose(final PNode startNode, final PNode endNode) {
        // TODO in some cases need to use getFullBounds(), but in some -- getGlobalFullBounds(). Need to autodetermine when what.
        // This pertains to all links.
        final PBounds startNodeBound = getBounds(startNode); // startNode.getGlobalFullBounds();
        final PBounds endNodeBound = getBounds(endNode); // endNode.getFullBounds();
        // get centres of nodes, which are joints for the link to be plugged into
        final Point2D.Double startNodeBoundCenre = (Point2D.Double) startNodeBound.getCenter2D();
        final Point2D.Double endNodeBoundCentre = (Point2D.Double) endNodeBound.getCenter2D();
        // define joints for the link to be plugged into
        // Point2D.Double startNodeJoint = new Point2D.Double(startNodeBoundCenre.getX(), startNodeBoundCenre.getY());
        // Point2D.Double endNodeJoint = new Point2D.Double(endNodeBoundCentre.getX(), endNodeBoundCentre.getY());
        final Point2D.Double startNodeJoint = new Point2D.Double(startNodeBoundCenre.getX() + startNodeBound.getWidth() / 2., startNodeBoundCenre.getY());
        final Point2D.Double endNodeJoint = new Point2D.Double(endNodeBoundCentre.getX() - endNodeBound.getWidth() / 2., endNodeBoundCentre.getY());

        // ///////////////////////////////////////// build a line between the nodes ///////////////////////////////////////
        line = new PPath();
        line.moveTo((float) startNodeJoint.getX(), (float) startNodeJoint.getY());
        line.lineTo((float) endNodeJoint.getX(), (float) endNodeJoint.getY());

        drawArrow(startNodeJoint, endNodeJoint);

        // add everything to the LinkNode
        addChild(line);
        if (arrowAtStartNode != null) {
            addChild(arrowAtStartNode);
        }
        if (arrowAtEndNode != null) {
            addChild(arrowAtEndNode);
        }

        setStartNode(startNode);
        setEndNode(endNode);
    }

    private void drawArrow(final Point2D.Double startNodeJoint, final Point2D.Double endNodeJoint) {
        /*
         * TODO Every class implementing ILink interface may have drawArrow method for drawing arrows (or something else) at the end of the link. It may look
         * like it is a duplication, however it is not. Currently the implementation is ineed the same, however it will be modified in due course to reflect
         * behaviour of each specific link.
         */
        if (linkArrowLocation == LinkArrowLocation.AT_START_NODE || linkArrowLocation == LinkArrowLocation.AT_BOTH_NODES) {
            arrowAtStartNode = PPath.createEllipse((float) startNodeJoint.getX() - 4, (float) startNodeJoint.getY() - 4, 8, 8);
            arrowAtStartNode.setPaint(Color.yellow);
        }
        if (linkArrowLocation == LinkArrowLocation.AT_END_NODE || linkArrowLocation == LinkArrowLocation.AT_BOTH_NODES) {
            arrowAtEndNode = PPath.createEllipse((float) endNodeJoint.getX() - 4, (float) endNodeJoint.getY() - 4, 8, 8);
            arrowAtEndNode.setPaint(Color.yellow);
        }
    }

    public PNode getEndNode() {
        return endNode;
    }

    private void setEndNode(final PNode endNode) {
        this.endNode = endNode;
    }

    public PNode getStartNode() {
        return startNode;
    }

    private void setStartNode(final PNode startNode) {
        this.startNode = startNode;
    }

    @Override
    public void hightlight(final Stroke stroke) {
        if (line != null) {
            line.setStroke(stroke);
        }
        if (arrowAtStartNode != null) {
            arrowAtStartNode.setStroke(stroke);
        }
        if (arrowAtEndNode != null) {
            arrowAtEndNode.setStroke(stroke);
        }
    }

    @Override
    public void dehightlight() {
        hightlight(getStroke());
    }
}
