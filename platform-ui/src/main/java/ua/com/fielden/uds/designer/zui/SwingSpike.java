package ua.com.fielden.uds.designer.zui;

import java.awt.Color;

import javax.swing.JButton;

import ua.com.fielden.uds.designer.zui.component.generic.GenericContainerNode;
import ua.com.fielden.uds.designer.zui.event.DragEventHandler;
import ua.com.fielden.uds.designer.zui.event.WheelRatoteZoomEventHandler;
import ua.com.fielden.uds.designer.zui.util.GlobalObjects;
import ua.com.fielden.uds.designer.zui.util.PSwingFrame;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.event.PDragEventHandler;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.pswing.PSwing;

public class SwingSpike extends PSwingFrame {

    private static final long serialVersionUID = 6293369164646614319L;

    @Override
    public void initialize() {
	super.initialize();
	// rendering settings
	getCanvas().setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
	getCanvas().setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
	getCanvas().setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);

	getCanvas().removeInputEventListener(getCanvas().getZoomEventHandler());
	getCanvas().addInputEventListener(new WheelRatoteZoomEventHandler(0.2, 2.0));

	getCanvas().getLayer().setPickable(false);
	getCanvas().getLayer().setChildrenPickable(true);

	final PLayer nodeLayer = getCanvas().getLayer();
	final PLayer linkLayer = nodeLayer;

	GlobalObjects.canvas = getCanvas();
	GlobalObjects.nodeLayer = linkLayer;
	GlobalObjects.linkLayer = linkLayer;
	GlobalObjects.frame = this;

	final PDragEventHandler handler = new DragEventHandler(nodeLayer);
	nodeLayer.addInputEventListener(handler);

	final GenericContainerNode containerOne = new GenericContainerNode(new PDimension(50, 50));
	containerOne.setBackgroundColor(new Color(213, 30, 62));
	containerOne.translate(50, 200);
	nodeLayer.addChild(containerOne);

	final JButton button = new JButton("Button");
	final PSwing swing = new PSwing(getCanvas(), button);
	// nodeLayer.addChild(swing);
	containerOne.addChild(swing);
    }

    public static void main(final String[] args) {
	final PSwingFrame frame = new SwingSpike();
	frame.setTitle("Spike: Swing rendering using Piccolo");
	frame.setSize(790, 545);
	frame.setVisible(true);
    }

}
