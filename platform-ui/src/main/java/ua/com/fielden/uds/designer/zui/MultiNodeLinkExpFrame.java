package ua.com.fielden.uds.designer.zui;

import java.util.ArrayList;
import java.util.Arrays;

import ua.com.fielden.uds.designer.zui.component.link.LeftCurlyLink;
import ua.com.fielden.uds.designer.zui.component.link.LineLink;
import ua.com.fielden.uds.designer.zui.component.link.LinkArrowLocation;
import ua.com.fielden.uds.designer.zui.event.LinkEventHandler;
import ua.com.fielden.uds.designer.zui.interfaces.ILink;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PDragEventHandler;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.PFrame;

/**
 * This a frame used for designing a rule.
 * 
 * @author 01es
 * 
 */
public class MultiNodeLinkExpFrame extends PFrame {

    private static final long serialVersionUID = 6293369164646614319L;

    public void initialize() {
	super.initialize();

	getCanvas().removeInputEventListener(getCanvas().getPanEventHandler());
	getCanvas().getLayer().setPickable(false);
	getCanvas().getLayer().setChildrenPickable(true);

	PLayer nodeLayer = getCanvas().getLayer();
	PLayer linkLayer = new PLayer();
	getCanvas().getCamera().addLayer(0, linkLayer);

	PDragEventHandler handler = new LinkEventHandler();
	nodeLayer.addInputEventListener(handler);

	// add two nodes linked with curly link
	PPath startNode = PPath.createEllipse(220, 200, 20, 20);
	PPath midNode = PPath.createEllipse(50, 150, 20, 20);
	PPath endNode = PPath.createEllipse(250, 300, 20, 20);
	nodeLayer.addChild(startNode);
	nodeLayer.addChild(midNode);
	nodeLayer.addChild(endNode);

	ILink firstLink = new LineLink(startNode, midNode, LinkArrowLocation.AT_BOTH_NODES);
	ILink secondLink = new LeftCurlyLink(midNode, endNode, LinkArrowLocation.AT_BOTH_NODES);

	startNode.addAttribute("links", new ArrayList<ILink>(Arrays.asList(new ILink[] { firstLink })));
	midNode.addAttribute("links", new ArrayList<ILink>(Arrays.asList(new ILink[] { firstLink, secondLink })));
	endNode.addAttribute("links", new ArrayList<ILink>(Arrays.asList(new ILink[] { secondLink })));

	linkLayer.addChild((PNode) firstLink);
	linkLayer.addChild((PNode) secondLink);

    }

    public static void main(String[] args) {
	PFrame frame = new MultiNodeLinkExpFrame();
	frame.setTitle("Demo");
	frame.setSize(350, 575);
	frame.setVisible(true);
    }
}
