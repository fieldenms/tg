package ua.com.fielden.platform.example.dnd.classes;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PDimension;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.nodes.P3DRect;

/**
 * The Birds Eye View Class specifically for camera. It is used for providing a high level view of a good workshop.
 * 
 * @author 01es Hodych
 */
public class BirdsEyeView extends PCanvas implements PropertyChangeListener {
    private static final long serialVersionUID = -792500448757056979L;

    /**
     * This is the node that shows the viewed area.
     */
    private PNode areaVisiblePNode;

    /**
     * This is the canvas that is being viewed
     */
    private PCamera camera;
    /**
     * The change listener to know when to update the birds eye view.
     */
    private PropertyChangeListener changeListener;

    private int layerCount;

    /**
     * Creates a new instance of a BirdsEyeView
     */
    public BirdsEyeView() {
        // create the PropertyChangeListener for listening to the viewed canvas
        changeListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                passiveUpdateFromViewed();
            }
        };
        // create the coverage node
        areaVisiblePNode = new P3DRect();
        areaVisiblePNode.setPaint(new Color(250, 214, 105));
        areaVisiblePNode.setTransparency(.5f);
        areaVisiblePNode.setBounds(0, 0, 100, 100);
        getCamera().addChild(areaVisiblePNode);
        // add the drag event handler
        getCamera().addInputEventListener(new PDragSequenceEventHandler() {
            protected void startDrag(PInputEvent e) {
                if (e.getPickedNode() == areaVisiblePNode) {
                    super.startDrag(e);
                    updateFromViewed();
                }
            }

            protected void drag(PInputEvent e) {
                PDimension dim = e.getDelta();
                camera.translateView(0 - dim.getWidth(), 0 - dim.getHeight());
            }
        });
        // remove Pan and Zoom
        removeInputEventListener(getPanEventHandler());
        removeInputEventListener(getZoomEventHandler());

        setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING); // LOW_QUALITY_RENDERING

    }

    public void connect(PCamera camera, PLayer[] viewedLayers) {
        this.camera = camera; // assigns camera to be viewed
        layerCount = 0;
        // assign changeListener event to the camera viewing a canvas to make sure the birds view is updated when something changes on the main canvas
        camera.addPropertyChangeListener(changeListener);

        for (layerCount = 0; layerCount < viewedLayers.length; ++layerCount) {
            getCamera().addLayer(layerCount, viewedLayers[layerCount]);
        }

    }

    /**
     * Add a layer to list of viewed layers
     */
    public void addLayer(PLayer new_layer) {
        getCamera().addLayer(new_layer);
        layerCount++;
    }

    /**
     * Remove the layer from the viewed layers
     */
    public void removeLayer(PLayer old_layer) {
        getCamera().removeLayer(old_layer);
        layerCount--;
    }

    /**
     * Stop the birds eye view from receiving events from the viewed canvas and remove all layers
     */
    public void disconnect() {
        camera.removePropertyChangeListener(changeListener);

        for (int i = 0; i < getCamera().getLayerCount(); ++i) {
            getCamera().removeLayer(i);
        }

    }

    /**
     * This method gets the state of the viewed canvas and updates the BirdsEyeViewer This can be called from outside code
     */
    public void updateFromViewed() {
        // keep the birds eye view centered
        getCamera().animateViewToCenterBounds(getCamera().getUnionOfLayerFullBounds(), true, 0);
        passiveUpdateFromViewed();
    }

    public void passiveUpdateFromViewed() {
        areaVisiblePNode.setBounds(getCamera().viewToLocal(camera.getViewBounds()));
    }

    /**
     * This method will get called when the viewed canvas changes
     */
    public void propertyChange(PropertyChangeEvent event) {
        updateFromViewed();
    }
}
