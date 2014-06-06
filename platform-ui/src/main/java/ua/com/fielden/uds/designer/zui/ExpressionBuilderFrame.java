package ua.com.fielden.uds.designer.zui;

import ua.com.fielden.uds.designer.zui.component.expression.OperatorPanelNode;
import ua.com.fielden.uds.designer.zui.event.DragEventHandler;
import ua.com.fielden.uds.designer.zui.util.GlobalObjects;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.event.PDragEventHandler;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.PFrame;

/**
 * This a frame used for designing a rule.
 * 
 * @author 01es
 * 
 */
public class ExpressionBuilderFrame extends PFrame {

    private static final long serialVersionUID = 6293369164646614319L;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize() {
        super.initialize();
        // rendering settings
        getCanvas().setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        getCanvas().setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        getCanvas().setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);

        getCanvas().getLayer().setPickable(false);
        getCanvas().getLayer().setChildrenPickable(true);

        final PLayer nodeLayer = getCanvas().getLayer();
        final PLayer linkLayer = nodeLayer;
        // need to initialize global objects, which can (and are) be referenced from different part of an application
        GlobalObjects.canvas = getCanvas();
        GlobalObjects.nodeLayer = linkLayer;
        GlobalObjects.linkLayer = linkLayer;
        GlobalObjects.frame = this;

        final PDragEventHandler handler = new DragEventHandler(nodeLayer);
        nodeLayer.addInputEventListener(handler);

        // expression builder
        final OperatorPanelNode opContainer = new OperatorPanelNode();
        opContainer.translate(5, 5);
        nodeLayer.addChild(opContainer);
    }

    public static void main(final String[] args) {
        final PFrame frame = new ExpressionBuilderFrame();
        frame.setTitle("UDS Designer: Components");
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
}
