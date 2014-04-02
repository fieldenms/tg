package ua.com.fielden.uds.designer.zui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ua.com.fielden.uds.designer.zui.component.link.CurlyLink;
import ua.com.fielden.uds.designer.zui.component.link.FixedCurlyLink;
import ua.com.fielden.uds.designer.zui.component.link.LeftCurlyLink;
import ua.com.fielden.uds.designer.zui.component.link.LineLink;
import ua.com.fielden.uds.designer.zui.component.link.LinkArrowLocation;
import ua.com.fielden.uds.designer.zui.component.link.RightCurlyLink;
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
public class LinkExpFrame extends PFrame {

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
        PPath endNode = PPath.createEllipse(50, 150, 20, 20);
        nodeLayer.addChild(startNode);
        nodeLayer.addChild(endNode);

        ILink link = new CurlyLink(startNode, endNode, LinkArrowLocation.AT_BOTH_NODES);
        List<ILink> links = new ArrayList<ILink>(Arrays.asList(new ILink[] { link }));
        startNode.addAttribute("links", links);
        endNode.addAttribute("links", links);
        linkLayer.addChild((PNode) link);

        // add two nodes linked with a straight link (line)
        startNode = PPath.createEllipse(50, 50, 20, 20);
        endNode = PPath.createEllipse(250, 100, 20, 20);
        nodeLayer.addChild(startNode);
        nodeLayer.addChild(endNode);

        link = new LineLink(startNode, endNode, LinkArrowLocation.AT_BOTH_NODES);
        links = new ArrayList<ILink>(Arrays.asList(new ILink[] { link }));
        startNode.addAttribute("links", links);
        endNode.addAttribute("links", links);
        linkLayer.addChild((PNode) link);

        // add two nodes linked with a right curly link
        startNode = PPath.createEllipse(50, 250, 20, 20);
        endNode = PPath.createEllipse(250, 300, 20, 20);
        nodeLayer.addChild(startNode);
        nodeLayer.addChild(endNode);

        link = new RightCurlyLink(startNode, endNode, LinkArrowLocation.AT_BOTH_NODES);
        links = new ArrayList<ILink>(Arrays.asList(new ILink[] { link }));
        startNode.addAttribute("links", links);
        endNode.addAttribute("links", links);
        linkLayer.addChild((PNode) link);

        // add two nodes linked with a left curly link
        startNode = PPath.createEllipse(250, 350, 20, 20);
        endNode = PPath.createEllipse(50, 400, 20, 20);
        nodeLayer.addChild(startNode);
        nodeLayer.addChild(endNode);

        link = new LeftCurlyLink(startNode, endNode, LinkArrowLocation.AT_BOTH_NODES);
        links = new ArrayList<ILink>(Arrays.asList(new ILink[] { link }));
        startNode.addAttribute("links", links);
        endNode.addAttribute("links", links);
        linkLayer.addChild((PNode) link);

        // add two nodes linked with a fixed curly link
        startNode = PPath.createEllipse(250, 420, 20, 20);
        endNode = PPath.createEllipse(50, 490, 20, 20);
        nodeLayer.addChild(startNode);
        nodeLayer.addChild(endNode);

        link = new FixedCurlyLink(startNode, endNode, LinkArrowLocation.AT_BOTH_NODES);
        links = new ArrayList<ILink>(Arrays.asList(new ILink[] { link }));
        startNode.addAttribute("links", links);
        endNode.addAttribute("links", links);
        linkLayer.addChild((PNode) link);

    }

    public static void main(String[] args) {
        PFrame frame = new LinkExpFrame();
        frame.setTitle("Demo");
        frame.setSize(350, 575);
        frame.setVisible(true);
    }
}
