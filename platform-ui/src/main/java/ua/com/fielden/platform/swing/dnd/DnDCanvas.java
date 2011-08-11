package ua.com.fielden.platform.swing.dnd;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.Timer;

import ua.com.fielden.platform.events.MultipleDragEventHandler;
import ua.com.fielden.platform.events.MultipleDragEventHandler.ForcedDehighlighter;
import ua.com.fielden.platform.events.MultipleSelectionHandler;
import ua.com.fielden.platform.pmodels.SelectionHolder;
import ua.com.fielden.platform.swing.dnd.DnDSupport2.TransferredObject;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolox.pswing.PSwingCanvas;

/**
 * {@inheritDoc}+ support Drag-and-Drop
 * 
 * @author oleh
 * 
 */
public class DnDCanvas extends PSwingCanvas {

    private static final long serialVersionUID = -8573453090071313878L;

    // indicates whether the drag is enabled or not
    private boolean dragEnabled;

    // indicates whether the drag processing started or not
    private boolean isDnD;

    // needed for drag-and-drop support
    private DragSource source;

    // Piccolo event handlers
    private MultipleSelectionHandler selectionHandler;
    private ExtendedMultipleDrag dragEventHandler;

    // timer that will indicate whether the user wants to scroll canvas while dragging or not
    private Timer timer;
    private int maxDist;
    private boolean autoscrollEnable;
    private Point2D lastPosition;
    private boolean isMoving;

    /**
     * creates new DnDCanvas instance and adds selection event listener
     */
    public DnDCanvas(final ForcedDehighlighter forcedDehighlighter) {
	super();

	// initiating the dragSource
	source = DragSource.getDefaultDragSource();
	source.createDefaultDragGestureRecognizer(this, getAccepetableDropAction(), new DragSourceHandler());

	// initiating auto scroll and drag-and-drop supporters
	setDragEnabled(false);
	setAutoscrollEnable(false);
	isDnD = false;
	isMoving = false;

	// adding selection and drag event handlers to the canvas
	setPanEventHandler(null);
	setZoomEventHandler(null);
	setSelectionHandler(createSelectionHandler(getLayer(), getLayer()));
	addInputEventListener(getSelectionHandler());
	setDragEventHandler(new ExtendedMultipleDrag(this, forcedDehighlighter, new SelectionHolder(getSelectionHandler())));
	addInputEventListener(getDragEventHandler());

	// setting the drop target for this component
	setDropTarget(new DropTarget(this, new DropTargetHandler()));

	// initializing timer and minimal distance for where the scrolling process starts
	final Toolkit toolkit = Toolkit.getDefaultToolkit();
	Integer property = (Integer) toolkit.getDesktopProperty("DnD.Autoscroll.interval");
	timer = new Timer(property == null ? 100 : property.intValue(), new TimerHandler());
	property = (Integer) toolkit.getDesktopProperty("DnD.Autoscroll.initialDelay");
	timer.setInitialDelay(property == null ? 100 : property.intValue());
	setMaxDist(20);
    }

    // creates multiple selection handler for the DnDCanvas instance
    private MultipleSelectionHandler createSelectionHandler(final PNode marqueParent, final PNode selectableParent) {
	return new MultipleSelectionHandler(marqueParent, selectableParent) {

	    @Override
	    protected void startDrag(final PInputEvent e) {
		super.startDrag(e);
		lastPosition = e.getCanvasPosition();
	    }

	    @Override
	    protected void drag(final PInputEvent e) {
		super.drag(e);
		lastPosition = e.getCanvasPosition();
		if (autoscrollEnable) {
		    updateAutoscroll(e.getCanvasPosition());
		}

	    }

	    @Override
	    protected void startStandardOptionSelection(final PInputEvent pie) {
		if (dragEnabled) {
		    if (!isDnDStarted(pie.getSourceSwingEvent())) {
			super.startStandardOptionSelection(pie);
		    }
		} else {
		    super.startStandardOptionSelection(pie);
		}
	    }

	    @Override
	    protected void endDrag(final PInputEvent e) {
		lastPosition = null;
		if (timer.isRunning()) {
		    timer.stop();
		}
		if (dragEnabled) {
		    if (isDnD) {
			isDnD = false;
			super.endDrag(e);
			return;
		    }
		    PNode pressNode = e.getInputManager().getMouseOver().getPickedNode();
		    if (pressNode instanceof PCamera) {
			pressNode = null;
		    }
		    if ((pressNode != null) && (isOptionSelection(e) && isDnDStarted(e.getSourceSwingEvent())) && (!isMarqueeSelection(e))) {
			if (isSelectable(pressNode)) {
			    if (isSelected(pressNode)) {
				unselect(pressNode);
			    } else {
				select(pressNode);
			    }
			}
		    }

		}
		super.endDrag(e);
	    }

	    @Override
	    protected void dragActivityStep(final PInputEvent event) {
		super.dragActivityStep(event);
		if (isMarqueeSelection(event)) {
		    updateMarquee(event);

		    if (!isOptionSelection(event)) {
			computeMarqueeSelection(event);
		    } else {
			computeOptionMarqueeSelection(event);
		    }
		}
	    }

	};
    }

    /**
     * enables or disables drag - and -drop support for this canvas
     * 
     * @param dragEnabled
     *            - the value that indicates whether the drag - and-drop is supported or not
     */
    public void setDragEnabled(final boolean dragEnabled) {
	this.dragEnabled = dragEnabled;
    }

    /**
     * enables or disables autoscroll support during drag process
     * 
     * @param autoscrollEnable
     *            - the value that indicates whether the autoscroll support must be enabled or disabled
     */
    public void setAutoscrollEnable(final boolean autoscrollEnable) {
	this.autoscrollEnable = autoscrollEnable;
    }

    /**
     * 
     * @param point
     *            - the point for which transferrable object must be returned
     * @return the object that must be transferred
     */
    protected Object getTransferObject(final Point2D point) {
	return getSelectionHandler().getSelection();
    }

    /**
     * set the MultipleSelectionHandler instance for this canvas
     * 
     * @param selectionHandler
     *            - specified MultipleSelectionHandler that must be added to this canvas as the InputEventHandler
     */
    private void setSelectionHandler(final MultipleSelectionHandler selectionHandler) {
	this.selectionHandler = selectionHandler;
    }

    /**
     * @return MultipleSelectionHandler instance associated with this Canvas
     */
    public MultipleSelectionHandler getSelectionHandler() {
	return selectionHandler;
    }

    // checks if the the transferred data can be dropped in to the canvas
    private boolean isDragOk(final DropTargetDragEvent e) {

	if (!e.isDataFlavorSupported(DnDSupport2.TG_DATA_FLAVOR)) {
	    return false;
	}

	// the actions specified when the source
	// created the DragGestureRecognizer
	final int sa = e.getSourceActions();

	// we're saying that these actions are necessary
	if ((sa & getAccepetableDropAction()) == 0) {
	    return false;
	}
	try {
	    final TransferredObject transferredObject = (TransferredObject) e.getTransferable().getTransferData(DnDSupport2.TG_DATA_FLAVOR);
	    if (transferredObject != null) {
		if (isOkToDrop(transferredObject.getObject(), getLayerPoint(e.getLocation()))) {
		    return true;
		} else {
		    return false;
		}
	    } else {
		return false;
	    }
	} catch (final UnsupportedFlavorException e1) {
	    e1.printStackTrace();
	} catch (final IOException e1) {
	    e1.printStackTrace();
	}
	return false;

    }

    /**
     * transforms specified point into the Piccolo's coordinate system
     * 
     * @param point
     *            - specified point that must be transformed
     * @return the transformed point
     */
    private Point2D getLayerPoint(final Point2D point) {
	Point2D layerPoint = getLayer().globalToLocal(new Point2D.Double(point.getX(), point.getY()));
	layerPoint = getCamera().localToView(layerPoint);
	return layerPoint;
    }

    /**
     * DragSourceHandler class is implemented to hide the low level implementation of the DragSourceListener and DragGestuerListener interfaces
     * 
     * @author oleh
     * 
     */
    private class DragSourceHandler implements DragGestureListener, DragSourceListener {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dragGestureRecognized(final DragGestureEvent dge) {
	    // if drag is not enabled then there is nothing to do any more
	    if ((!dragEnabled)) {
		return;
	    }
	    // the additional test that determines whether the drag will be handled or not
	    if (!isDnDStarted(dge.getTriggerEvent())) {
		return;
	    }

	    // Figure out where the drag started
	    final MouseEvent inputEvent = (MouseEvent) dge.getTriggerEvent();
	    final int x = inputEvent.getX();
	    final int y = inputEvent.getY();
	    final Point2D localPoint = getLayerPoint(new Point(x, y));

	    // searching for the chosen nodes
	    final Rectangle2D selectableRect = new Rectangle2D.Double(localPoint.getX(), localPoint.getY(), 0.1, 0.1);
	    final ArrayList<PNode> intersectedNodes = new ArrayList<PNode>();
	    DnDCanvas.this.getLayer().findIntersectingNodes(selectableRect, intersectedNodes);

	    // if there is selected node then create transferable and initiate dragging
	    for (final PNode node : intersectedNodes) {
		if (selectionHandler.isSelected(node)) {
		    isDnD = true;
		    isMoving = false;
		    final Transferable transferable = new Transferable() {

			/**
			 * {@inheritDoc}
			 * 
			 * @param flavor
			 *            - preferred data type
			 * @return the object that must be transfered
			 * @throws UnsupportedFlavorException
			 *             if the data flavor of the preferred object to transfer isn't supported by this Transferable Object then the UnsupportedFlavorException will
			 *             be thrown
			 * @throws IOException
			 *             if the data isn't available any longer then the IOException will be thrown
			 */
			@Override
			public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			    if (!isDataFlavorSupported(flavor)) {
				throw new UnsupportedFlavorException(flavor);
			    }
			    final Object object4Transfer = getTransferObject(localPoint);
			    final TransferredObject transferObject = new TransferredObject(object4Transfer, DnDCanvas.this);
			    return transferObject;
			}

			/**
			 * {@inheritDoc}
			 * 
			 * @return the supported data flavor by this Transferable instance
			 */
			@Override
			public DataFlavor[] getTransferDataFlavors() {
			    return new DataFlavor[] { DnDSupport2.TG_DATA_FLAVOR };
			}

			/**
			 * {@inheritDoc}
			 * 
			 * indicates whether the given data flavor is supported by this Transferable instance or not
			 * 
			 * @param flavor
			 *            - given data flavor to test the compatibility
			 * @return - true if the given data flavor is supported else it returns false
			 */
			@Override
			public boolean isDataFlavorSupported(final DataFlavor flavor) {
			    if (flavor.equals(DnDSupport2.TG_DATA_FLAVOR)) {
				return true;
			    } else {
				return false;
			    }
			}

		    };
		    dge.startDrag(null, transferable, this);
		}

	    }

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dragDropEnd(final DragSourceDropEvent dsde) {
	    final DragSourceContext dsc = dsde.getDragSourceContext();
	    TransferredObject transferObject = null;
	    try {
		transferObject = (TransferredObject) dsc.getTransferable().getTransferData(DnDSupport2.TG_DATA_FLAVOR);
	    } catch (final UnsupportedFlavorException e) {
		e.printStackTrace();
	    } catch (final IOException e) {
		e.printStackTrace();
	    }
	    if ((dsde.getDropSuccess()) && (transferObject != null)) {
		dropDone(transferObject.getDraggedTo(), transferObject.getObject(), dsde.getDropAction());
	    } else {
		dropDone(transferObject.getDraggedTo(), transferObject.getObject(), DnDConstants.ACTION_NONE);
	    }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dragEnter(final DragSourceDragEvent dsde) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dragExit(final DragSourceEvent dse) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dragOver(final DragSourceDragEvent dsde) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dropActionChanged(final DragSourceDragEvent dsde) {

	}

    }

    /**
     * handles the drop target events and hides the implementation of the DropTargetListener
     * 
     * @author oleh
     * 
     */
    private class DropTargetHandler implements DropTargetListener {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dragEnter(final DropTargetDragEvent dtde) {
	    try {
		final TransferredObject transferObject = (TransferredObject) dtde.getTransferable().getTransferData(DnDSupport2.TG_DATA_FLAVOR);
		transferObject.setDraggedTo(DnDCanvas.this);
		initDrag(getLayerPoint(dtde.getLocation()), transferObject.getObject());
	    } catch (final UnsupportedFlavorException e) {
		e.printStackTrace();
		dtde.rejectDrag();
		return;
	    } catch (final IOException e) {
		e.printStackTrace();
		dtde.rejectDrag();
		return;
	    }
	    if (isDragOk(dtde)) {
		dtde.acceptDrag(dtde.getDropAction());
	    } else {
		dtde.rejectDrag();
	    }
	    lastPosition = dtde.getLocation();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dragExit(final DropTargetEvent dte) {
	    if (timer.isRunning()) {
		timer.stop();
	    }
	    lastPosition = null;
	    cleanUpDrag(dte, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dragOver(final DropTargetDragEvent dtde) {
	    if (isDragOk(dtde)) {
		dtde.acceptDrag(dtde.getDropAction());
	    } else {
		dtde.rejectDrag();
	    }
	    updateDrag(getLayerPoint(dtde.getLocation()));
	    if (autoscrollEnable) {
		updateAutoscroll(dtde.getLocation());
	    }
	    lastPosition = dtde.getLocation();

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void drop(final DropTargetDropEvent dtde) {
	    if (timer.isRunning()) {
		timer.stop();
	    }
	    lastPosition = null;
	    if (!dtde.isDataFlavorSupported(DnDSupport2.TG_DATA_FLAVOR)) {
		dtde.rejectDrop();
		cleanUpDrag(dtde, false);
		return;
	    }

	    // the actions specified when the source
	    // created the DragGestureRecognizer
	    final int sa = dtde.getSourceActions();

	    // we're saying that these actions are necessary
	    if ((sa & getAccepetableDropAction()) == 0) {
		dtde.rejectDrop();
		cleanUpDrag(dtde, false);
		return;
	    }
	    TransferredObject transferredObject = null;
	    boolean success;
	    try {

		transferredObject = (TransferredObject) dtde.getTransferable().getTransferData(DnDSupport2.TG_DATA_FLAVOR);
		if (transferredObject != null) {
		    if (isOkToDrop(transferredObject.getObject(), getLayerPoint(dtde.getLocation()))) {
			success = importTranserred(transferredObject.getObject(), getLayerPoint(dtde.getLocation()));
			dtde.acceptDrop(dtde.getDropAction());
			cleanUpDrag(dtde, true);
		    } else {
			dtde.rejectDrop();
			cleanUpDrag(dtde, false);
			return;
		    }
		} else {
		    dtde.rejectDrop();
		    cleanUpDrag(dtde, false);
		    return;
		}
	    } catch (final UnsupportedFlavorException e1) {
		e1.printStackTrace();
		cleanUpDrag(dtde, false);
		success = false;

	    } catch (final IOException e1) {
		e1.printStackTrace();
		cleanUpDrag(dtde, false);
		success = false;
	    }
	    dtde.dropComplete(success);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dropActionChanged(final DropTargetDragEvent dtde) {
	    if (isDragOk(dtde)) {
		dtde.acceptDrag(dtde.getDropAction());
	    } else {
		dtde.rejectDrag();
	    }
	}
    }

    private class TimerHandler implements ActionListener {

	@Override
	public void actionPerformed(final ActionEvent e) {
	    if (autoscrollEnable) {
		scroll();
	    }
	}

	private void scroll() {
	    final Point2D pos = new Point2D.Double(lastPosition.getX(), lastPosition.getY());
	    final Dimension canvasBounds = getSize();
	    final Point2D delta = new Point2D.Double();
	    double incrementX = 0, incrementY = 0;
	    if (pos.getX() < getMaxDist()) {
		incrementX = -1 * getScrollincrementX(getMaxDist() - pos.getX());
	    } else if (canvasBounds.getWidth() - pos.getX() < getMaxDist()) {
		incrementX = getScrollincrementX(getMaxDist() - (canvasBounds.getWidth() - pos.getX()));
	    }
	    if (pos.getY() < getMaxDist()) {
		incrementY = -1 * getScrollincrementY(getMaxDist() - pos.getY());
	    } else if (canvasBounds.getHeight() - pos.getY() < getMaxDist()) {
		incrementY = getScrollincrementY(getMaxDist() - (canvasBounds.getHeight() - pos.getY()));
	    }
	    delta.setLocation(incrementX, incrementY);
	    getCamera().translateView(-1 * incrementX, -1 * incrementY);
	    if (isMoving) {
		dragEventHandler.updateDragPosition(delta);
	    } else {
		updateCanvas(delta);
	    }
	}
    }

    /**
     * extends MultipleDragEventHandler and needed only for internal use in the DnDCanvas
     * 
     * @author oleh
     * 
     */
    private class ExtendedMultipleDrag extends MultipleDragEventHandler {

	public ExtendedMultipleDrag(final PCanvas canvas, final ForcedDehighlighter forcedDehighlighter, final SelectionHolder selectionHolder) {
	    super(canvas, forcedDehighlighter, selectionHolder);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param pie
	 *            - generated inputEvent
	 */
	@Override
	protected void startDrag(final PInputEvent pie) {
	    if (isDnDStarted(pie.getSourceSwingEvent())) {
		return;
	    }
	    super.startDrag(pie);
	    isMoving = true;
	    lastPosition = pie.getCanvasPosition();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param e
	 *            -generated inputEvent
	 */
	@Override
	protected void drag(final PInputEvent e) {
	    if (isDnD) {
		return;
	    }
	    super.drag(e);
	    lastPosition = e.getCanvasPosition();
	    if (autoscrollEnable) {
		updateAutoscroll(e.getCanvasPosition());
	    }
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param e
	 *            - generated inputEvent
	 */
	@Override
	protected void endDrag(final PInputEvent e) {
	    super.endDrag(e);
	    if (timer.isRunning()) {
		timer.stop();
	    }
	    lastPosition = null;
	    isMoving = false;
	}

	/**
	 * used only to update position of the dragged nodes when scroll was performed for the canvas
	 * 
	 * @param offset
	 *            - the offset of the view transform
	 */
	public void updateDragPosition(final Point2D offset) {
	    for (final PNode draggedNode : draggedNodes) {
		draggedNode.offset(offset.getX(), offset.getY());
	    }
	}
    }

    private void updateAutoscroll(final Point2D cursorPosition) {
	final Dimension canvasBounds = getSize();
	if ((cursorPosition.getX() < getMaxDist()) || (cursorPosition.getY() < getMaxDist()) || (canvasBounds.getWidth() - cursorPosition.getX() < getMaxDist())
		|| (canvasBounds.getHeight() - cursorPosition.getY() < getMaxDist())) {
	    if (!timer.isRunning()) {
		timer.start();
	    }
	} else {
	    if (timer.isRunning()) {
		timer.stop();
	    }
	}
    }

    /**
     * checks if the transferable data can be dropped or not
     * 
     * @param object
     *            - transferable data
     * @param location
     *            - location where the transferable data can be dropped
     * @return return the value that indicates whether the data can be dropped or not
     */
    protected boolean isOkToDrop(final Object object, final Point2D location) {
	return true;
    }

    /**
     * imports the transferred data
     * 
     * @param object
     *            - transferred data that must be imported
     * @param location
     *            - point where that data must be located after import
     * @return the value that indicates whether the data import was successful or not
     */
    protected boolean importTranserred(final Object object, final Point2D location) {
	return false;
    }

    /**
     * handles dragDropEnd event of the SourceDragListener.
     * 
     * @param c
     *            the component where the transferObject was dropped
     * @param transferObject
     *            the dropped object
     * @param dropAction
     *            the action that indicates what action must be performed for the transferObject
     */
    protected void dropDone(final JComponent c, final Object transferObject, final int dropAction) {

    }

    /**
     * user must override that method to be able to add custom logic to the dragGusterRecognized method
     * 
     * @param dge
     *            the generated event by the drag recognizer
     * @return the value that indicates whether the drag event is recognized or not
     */
    protected boolean isDnDStarted(final InputEvent dge) {
	return false;
    }

    /**
     * override that method to initiate custom dragging
     * 
     * @param location
     *            - location of the mouse cursor when the drag entered event was generated
     * @param object
     *            - object that is transferring from the other component to the current
     */
    protected void initDrag(final Point2D location, final Object object) {

    }

    /**
     * override that method to perform custom clean up methods
     * 
     * @param dte
     *            - specified DropTargetEvnet instance that hold the information about the actions drop target context e.t.c
     * @param wasDropped
     *            - the value that specified whether the transfered data was dropped successfully or not
     */
    protected void cleanUpDrag(final DropTargetEvent dte, final boolean wasDropped) {

    }

    /**
     * updates custom drag properties if needed, user must override that method to be able to add some custom renderer while dragging the data on the canvas
     * 
     * @param location
     *            - the current location of the mouse cursor in the global coordinate system
     */
    protected void updateDrag(final Point2D location) {

    }

    /**
     * 
     * @return the acceptable drop action for this canvas
     */
    protected int getAccepetableDropAction() {
	return DnDConstants.ACTION_COPY_OR_MOVE;
    }

    /**
     * user must override that method to be able to update the position of the dragged node that represents the transferred data
     * 
     * @param offset
     */
    protected void updateDragPos(final Point2D offset) {

    }

    /**
     * user can override that method in order to control the autoscroll speed
     * 
     * @param diff
     *            - the difference between the max distance property and the X coordinate of the mouse cursor position
     * @return the increment value that is used while scrolling the camera view
     */
    protected double getScrollincrementX(final double diff) {
	return diff + 5;
    }

    /**
     * user can override that method in order to control the autoscroll speed
     * 
     * @param diff
     *            - the difference between the max distance property and the Y coordinate of the mouse cursor position
     * @return the increment value that is used while scrolling the camera view
     */
    protected double getScrollincrementY(final double diff) {
	return diff + 5;
    }

    /**
     * updates the canvas after the scroll was performed
     * 
     * @param delta
     *            - the offset of the view transform
     */
    protected void updateCanvas(final Point2D delta) {

    }

    /**
     * set the drag event handler associated with this canvas instance
     * 
     * @param dragEventHandler
     *            - specified MultipleDragEventHandler instance
     */
    private void setDragEventHandler(final ExtendedMultipleDrag dragEventHandler) {
	this.dragEventHandler = dragEventHandler;
    }

    /**
     * @return the MultipleDragEventHandler instance associated with this canvas
     */
    public MultipleDragEventHandler getDragEventHandler() {
	return dragEventHandler;
    }

    /**
     * @param maxDist
     *            the distance of the mouse cursor to the border of the component where the autoscroll process activates
     */
    public void setMaxDist(final int maxDist) {
	this.maxDist = maxDist;
    }

    /**
     * @return the maxDist - the distance of the mouse cursor to the border of the component where the autoscroll process activates
     */
    public int getMaxDist() {
	return maxDist;
    }

}
