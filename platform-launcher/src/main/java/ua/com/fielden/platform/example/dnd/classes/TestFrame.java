package ua.com.fielden.platform.example.dnd.classes;

import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.events.MultipleDragEventHandler;
import ua.com.fielden.platform.events.MultipleSelectionHandler;
import ua.com.fielden.platform.events.MultipleDragEventHandler.ForcedDehighlighter;
import ua.com.fielden.platform.pmodels.SelectionHolder;
import ua.com.fielden.uds.designer.zui.event.WheelRatoteZoomEventHandler;
import ua.com.fielden.uds.designer.zui.interfaces.IBasicNode;
import ua.com.fielden.uds.designer.zui.util.PSwingFrame;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.util.PPaintContext;

public abstract class TestFrame extends PSwingFrame {

    private JPanel birdsEyeView;

    private BirdsEyeView birdView;

    @Override
    public void initialize() {
        super.initialize();
        // rendering settings

        birdsEyeView = new JPanel(new MigLayout("fill,insets 1"));
        birdView = new BirdsEyeView();
        birdView.connect(getCanvas().getCamera(), new PLayer[] { getCanvas().getLayer() });
        birdView.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        birdsEyeView.add(birdView, "grow");
        birdsEyeView.setLocation(0, 0);
        birdsEyeView.setSize(new Dimension(150, 150));
        getCanvas().add(birdsEyeView);
        birdsEyeView.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        getCanvas().setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        getCanvas().setAnimatingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        getCanvas().setInteractingRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
        getCanvas().removeInputEventListener(getCanvas().getZoomEventHandler());

        getCanvas().addInputEventListener(new WheelRatoteZoomEventHandler(0.2, 1.5, getCanvas().getCamera()));
        getCanvas().setPanEventHandler(null);

        getCanvas().getLayer().setPickable(false);
        getCanvas().getLayer().setChildrenPickable(true);

        final ForcedDehighlighter focusDehighlighter = new ForcedDehighlighter() {

            @Override
            public boolean shouldDehighlight(final IBasicNode node) {

                return true;
            }

        };
        final MultipleSelectionHandler handler = new MultipleSelectionHandler(getCanvas().getLayer(), getCanvas().getLayer());
        getCanvas().addInputEventListener(handler);
        final MultipleDragEventHandler mdeHandler = new MultipleDragEventHandler(getCanvas(), focusDehighlighter, new SelectionHolder(handler));
        mdeHandler.setPostDragAction(new Runnable() {

            @Override
            public void run() {
                birdView.updateFromViewed();
            }

        });
        getCanvas().addInputEventListener(mdeHandler);
        initWidgets(getCanvas().getLayer());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                birdView.updateFromViewed();
            }
        });

    }

    /**
     * Implements this method to add widgets to nodeLayer.
     * 
     * @param nodeLayer
     */
    public abstract void initWidgets(final PLayer nodeLayer);

    private JPopupMenu createPopupMenu() {
        final JPopupMenu menu = new JPopupMenu();
        final JMenuItem item1 = new JMenuItem("test1");
        final JMenuItem item2 = new JMenuItem("test2");
        menu.add(item1);
        menu.add(item2);
        return menu;
    }

}
