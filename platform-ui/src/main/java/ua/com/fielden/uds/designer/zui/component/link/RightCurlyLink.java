package ua.com.fielden.uds.designer.zui.component.link;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.util.Arrays;

import ua.com.fielden.uds.designer.zui.component.generic.DefaultStroke;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;

public class RightCurlyLink extends AbstractLink {
    private static final long serialVersionUID = -1725863881823482655L;

    private static double arcDepth = 20.;
    private transient PNode startNode;
    private transient PNode endNode;

    private transient Arc outgoingArc;
    private transient Arc incomingArc;
    private transient PPath line;
    private transient PPath arrowAtStartNode;
    private transient PPath arrowAtEndNode;
    private transient LinkArrowLocation linkArrowLocation;

    public RightCurlyLink(final PNode startNode, final PNode endNode) {
	super();
	setStroke(new DefaultStroke(1));
	compose(startNode, endNode);
    }

    public RightCurlyLink(final PNode startNode, final PNode endNode, final LinkArrowLocation linkArrowLocation) {
	super();
	setStroke(new DefaultStroke(1));
	this.linkArrowLocation = linkArrowLocation;
	compose(startNode, endNode);
    }

    @Override
    public void reset() {
	super.reset();
	remove(line);
	if (outgoingArc != null) {
	    remove(outgoingArc.arc);
	}
	if (incomingArc != null) {
	    remove(incomingArc.arc);
	}
	remove(arrowAtStartNode);
	remove(arrowAtEndNode);
	compose(getStartNode(), getEndNode());
    }

    private void compose(final PNode startNode, final PNode endNode) {
	if (startNode == null || endNode == null) {
	    return;
	}

	final PBounds startNodeBound = getBounds(startNode);
	final PBounds endNodeBound = getBounds(endNode);
	// get centres of nodes
	final Point2D.Double startNodeBoundCenre = (Point2D.Double) startNodeBound.getCenter2D();
	final Point2D.Double endNodeBoundCentre = (Point2D.Double) endNodeBound.getCenter2D();
	// define joints for the link to be plugged into
	Point2D.Double startNodeJoint = new Point2D.Double(startNodeBoundCenre.getX() + startNodeBound.getWidth() / 2., startNodeBoundCenre.getY());
	final Point2D.Double endNodeJoint = new Point2D.Double(endNodeBoundCentre.getX() - endNodeBound.getWidth() / 2., endNodeBoundCentre.getY());
	// check where startNodeJoint should actually be...
	if (startNodeJoint.getX() > endNodeJoint.getX()) {
	    startNodeJoint = new Point2D.Double(startNodeBoundCenre.getX() - startNodeBound.getWidth() / 2., startNodeBoundCenre.getY());
	}
	// calculate middle point between the nodes using their current location and bounds
	final Point2D.Double middle = calcMiddle(startNodeJoint, endNodeJoint);

	// ////////////////////////////////////////// calculate the outgoing arc ///////////////////////////////////////////
	outgoingArc = buildOutgoingArc(startNodeJoint, endNodeJoint, middle);
	// ////////////////////////////////////////// calculate the incoming arc ///////////////////////////////////////////
	incomingArc = buildIncomingArc(startNodeJoint, endNodeJoint, middle);
	// ///////////////////////////////////////// build a line between the arcs ///////////////////////////////////////
	line = new PPath();
	line.moveTo((float) startNodeJoint.getX(), (float) startNodeJoint.getY());
	line.lineTo((float) outgoingArc.points.get(0).getX(), (float) outgoingArc.points.get(0).getY());

	drawArrow(startNodeJoint, endNodeJoint);

	// add everything to the LinkNode
	addChild(outgoingArc.arc);
	addChild(incomingArc.arc);
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

    private Point2D.Double calcMiddle(final Point2D startNodeJoint, final Point2D endNodeJoint) {
	double right = 0;
	double left = 0;
	if (startNodeJoint.getX() < endNodeJoint.getX()) {
	    right = endNodeJoint.getX();
	    left = startNodeJoint.getX();
	} else {
	    right = startNodeJoint.getX();
	    left = endNodeJoint.getX();
	}

	final double midX = left + Math.abs(right - left) / 2.; // half of the distance between the closet vertical sides of the nodes' bounds

	double bottom = 0;
	double top = 0;
	if (startNodeJoint.getY() < endNodeJoint.getY()) {
	    bottom = endNodeJoint.getY();
	    top = startNodeJoint.getY();
	} else {
	    bottom = startNodeJoint.getY();
	    top = endNodeJoint.getY();
	}

	final double midY = bottom - Math.abs(bottom - top) / 2.; // half of the distance between the closet horizontal sides of the nodes' bounds

	final Point2D.Double middle = new Point2D.Double(midX, midY); // the middle point
	return middle;
    }

    private Arc buildOutgoingArc(final Point2D.Double startNodeJoint, final Point2D.Double endNodeJoint, final Point2D.Double middle) {
	double startArcX = endNodeJoint.getX() - 2 * arcDepth;
	double delta = arcDepth;
	if (startArcX < startNodeJoint.getX()) {
	    startArcX = startNodeJoint.getX();
	    delta = (endNodeJoint.getX() - arcDepth) - startNodeJoint.getX();
	    if (Math.abs(delta) > arcDepth) {
		delta = -arcDepth;
		startArcX = endNodeJoint.getX();
	    }
	}

	final Point2D.Double startArc = new Point2D.Double(startArcX, startNodeJoint.getY());

	final double endArcX = endNodeJoint.getX() - arcDepth;
	final double endArcY = middle.getY();
	final Point2D.Double endArc = new Point2D.Double(endArcX, endArcY);
	// TODO at the moment endArcMiddle1 == endArcMiddle2
	final Point2D.Double endArcMiddle1 = new Point2D.Double(startArc.getX() + delta, startArc.getY()); // first approximation point starting from startArc
	final Point2D.Double endArcMiddle2 = new Point2D.Double(startArc.getX() + delta, startArc.getY()); // second approximation point starting from startArc
	// build an outgoing arc using just calculated points
	final Arc arc = new Arc(Arrays.asList(new Point2D.Double[] { startArc, endArcMiddle1, endArcMiddle2, endArc }));
	return arc;
    }

    private Arc buildIncomingArc(final Point2D.Double startNodeJoint, final Point2D.Double endNodeJoint, final Point2D.Double middle) {
	final Point2D.Double startArc = new Point2D.Double(endNodeJoint.getX() - arcDepth, middle.getY());

	final Point2D.Double endArc = endNodeJoint;
	// TODO at the moment endArcMiddle1 == endArcMiddle2
	final Point2D.Double endArcMiddle1 = new Point2D.Double(endArc.getX() - arcDepth, endArc.getY());
	final Point2D.Double endArcMiddle2 = new Point2D.Double(endArc.getX() - arcDepth, endArc.getY());
	// build an incoming arc using just calculated points
	final Arc arc = new Arc(Arrays.asList(new Point2D.Double[] { startArc, endArcMiddle1, endArcMiddle2, endArc }));
	return arc;
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
	if (outgoingArc != null) {
	    outgoingArc.arc.setStroke(stroke);
	}
	if (incomingArc != null) {
	    incomingArc.arc.setStroke(stroke);
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
