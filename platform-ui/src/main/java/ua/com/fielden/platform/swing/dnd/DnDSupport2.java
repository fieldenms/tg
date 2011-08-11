package ua.com.fielden.platform.swing.dnd;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

/**
 * Class, enabling drag-n-drop support for {@link JComponent}s
 * 
 * @author Yura
 */
public class DnDSupport2 {
    /**
     * Hide the default constructor -- this a pure static class.
     */
    private DnDSupport2() {
    }

    private static final Map<JComponent, MouseListener> INSTALLED_ADAPTERS = new HashMap<JComponent, MouseListener>();

    public static final DataFlavor TG_DATA_FLAVOR;

    static {
	// initialising DataFlavor that is supported
	try {
	    TG_DATA_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + TransferredObject.class.getName());
	} catch (final ClassNotFoundException e) {
	    throw new IllegalStateException("Class " + TransferredObject.class.getName() + " is not found, though it is declared as inner class of " + DnDSupport2.class.getName());
	}
    }

    /**
     * Installs drag-n-drop support for specified component. If only one kind of support is needed (i.e. only drag-from or only drag-to) one of the parameters (i.e.
     * <code>dragToSupport</code> or <code>dragFromSupport</code> respectively) could be null. <br>
     * Invocation of this method associates each of the supports with specified component in following way : <br>
     * - when user press the mouse button on specified component and starts to drag, subsequent calls to {@link DragFromSupport#getObject4DragAt(Point, JComponent)} occur with
     * following parameters : {@link Point} - point at which user pressed mouse button over the specified component, {@link JComponent} - component over which is mouse cursor now
     * (this parameter is null only one time just after the start of drag). Implementation of this method should return either null(indicating that it is not possible to drag from
     * that point, or to drop to that component) or some object(indicating object that's being dragged). <br>
     * - when user finally drops object to some component {@link DragFromSupport#dragNDropDone(Object, JComponent, int)} method is invoked with parameters : {@link Object} -
     * dragged object and {@link JComponent} - destination component of drag. Note : this method is invoked only when : dropped object is not null and destination component is not
     * the same as source one (thus drag from component to itself is disabled) <br>
     * <br>
     * - when user drags some object over specified component, immediately after call to {@link DragFromSupport#getObject4DragAt(Point, JComponent)} on source component, call to
     * {@link DragToSupport#canDropTo(Point, Object, JComponent)} occurs on destination component with following parameters : {@link Point} - point on the component over which is
     * currently mouse, {@link Object} - object being dragged, {@link JComponent} - source component of the drag. If this method returns false, then dropping of object is not
     * allowed. <br>
     * - when call to {@link DragToSupport#canDropTo(Point, Object, JComponent)} returned true and user released mouse button over specified component, call to
     * {@link DragToSupport#dropTo(Point, Object, JComponent)} method occurs with same parameters as in {@link DragToSupport#canDropTo(Point, Object, JComponent)} method. If during
     * invocation of this method occurred that it is not possible to drop object then you should return false and indicate unsuccessful object dropping.
     * 
     * <br>
     * <br>
     * Generally the lifecycle of drag is like that : while dragging over components calls to {@link DragFromSupport#getObject4DragAt(Point, JComponent)} on drag source component
     * and {@link DragToSupport#canDropTo(Point, Object, JComponent)} on target drop component occur. Note : while drag source and drag target components are the same drag is
     * disabled. <br>
     * - after dropping to target component(if of course it is allowed) at first {@link DragToSupport#dropTo(Point, Object, JComponent)} is invoked on drag target component and
     * then {@link DragFromSupport#dragNDropDone(Object, JComponent, int)} - on drag source component(if object was successfully dropped). <br>
     * <br>
     * Note : this method also adds {@link MouseAdapter} to specified component, that initiates drag action after mouse was pressed. <br>
     * Note: if you'd like to add your own specific {@link MouseAdapter} then dragStartRecognizer must be null<br>
     * Note : left mouse button press initiates drag behavior. To change this, call
     * {@link #installDnDSupport(JComponent, ua.com.fielden.platform.dnd.DnDSupport2.DragStartRecognizer, ua.com.fielden.platform.dnd.DnDSupport2.DragFromSupport, ua.com.fielden.platform.dnd.DnDSupport2.DragToSupport)}
     * method with custom {@link DragStartRecognizer}.
     * 
     * @param component
     * @param dragFromSupport
     * @param dragToSupport
     */
    public static void installDnDSupport(final JComponent component, final DragFromSupport dragFromSupport, final DragToSupport dragToSupport) {
	installDnDSupport(component, new DragStartRecognizer() {
	    @Override
	    public boolean isDragStartEvent(final MouseEvent e) {
		return e.getButton() == MouseEvent.BUTTON1;
	    }
	}, dragFromSupport, dragToSupport);
    }

    public static void installDnDSupport(final JComponent component, final DragFromSupport dragFromSupport, final DragToSupport dragToSupport, final boolean canImportToItself) {
	installDnDSupport(component, new DragStartRecognizer() {
	    @Override
	    public boolean isDragStartEvent(final MouseEvent e) {
		return e.getButton() == MouseEvent.BUTTON1;
	    }
	}, dragFromSupport, dragToSupport, canImportToItself);
    }

    public static void installDnDSupport(final JComponent component, final DragStartRecognizer dragStartRecognizer, final DragFromSupport dragFromSupport, final DragToSupport dragToSupport) {
	installAdapterIfNeeded(component, dragStartRecognizer);
	final DnDTransferHandler transferHandler = new DnDTransferHandler(component, dragFromSupport, dragToSupport, false);
	component.setTransferHandler(transferHandler);
    }

    /**
     * Does the same as {@link #installDnDSupport(JComponent, ua.com.fielden.platform.dnd.DnDSupport2.DragFromSupport, ua.com.fielden.platform.dnd.DnDSupport2.DragToSupport)} but
     * drag begins when {@link DragStartRecognizer#isDragStartEvent(MouseEvent)} return true on mouse event.
     * 
     * @see #installDnDSupport(JComponent, ua.com.fielden.platform.dnd.DnDSupport2.DragFromSupport, ua.com.fielden.platform.dnd.DnDSupport2.DragToSupport)
     * 
     * @param component
     * @param dragStartRecognizer
     * @param dragFromSupport
     * @param dragToSupport
     * @param canImportToItself
     *            - indicates whether importing from component to itself is allowed (this may be used to drag components inside sole {@link JPanel}).
     */
    public static void installDnDSupport(final JComponent component, final DragStartRecognizer dragStartRecognizer, final DragFromSupport dragFromSupport, final DragToSupport dragToSupport, final boolean canImportToItself) {
	installAdapterIfNeeded(component, dragStartRecognizer);
	final DnDTransferHandler transferHandler = new DnDTransferHandler(component, dragFromSupport, dragToSupport, canImportToItself);
	component.setTransferHandler(transferHandler);
    }

    /**
     * Tries to disable or enable default drag support on the component using Reflection API. <br>
     * <br>
     * Default drag support can be spotted by presence of public method setDragEnabled(boolean). If component doesn't support drag everything is ok, we don't do anything. But if it
     * does we try to call setDragEnabled(false) on it using Reflection API.
     * 
     * @param component
     */
    private static boolean setDragEnableForComponent(final JComponent component, final boolean dragEnable) {
	try {
	    // trying to find public method setDragEnabled(boolean)
	    final Method method = component.getClass().getMethod("setDragEnabled", boolean.class);
	    // if found (i.e. no NoSuchMethodException were thrown), then it means that this component supports drag and this means we should disable it
	    method.invoke(component, dragEnable);
	    return true;
	} catch (final Exception e) {
	    // this component doesn't have method setDragEnabled(boolean), so it doesn't support drag, so we shouldn't even disable it
	}
	return false;
    }

    /**
     * Removes drag-n-drop support provided by this class, if it was installed and returns the result of such removal.
     * 
     * @param component
     */
    public static boolean deinstallDnDSupport(final JComponent component) {
	if (component.getTransferHandler() instanceof DnDTransferHandler) {
	    // removing mouse listener enabling drag-from functionality if it was added to the component
	    if (INSTALLED_ADAPTERS.containsKey(component)) {
		component.removeMouseListener(INSTALLED_ADAPTERS.get(component));
		INSTALLED_ADAPTERS.remove(component);
	    }
	    component.setTransferHandler(null);
	    setDragEnableForComponent(component, false);
	    return true;
	} else {
	    return false;
	}
    }

    private static boolean installAdapterIfNeeded(final JComponent component, final DragStartRecognizer dragStartRecognizer) {
	if ((!setDragEnableForComponent(component, true)) && (dragStartRecognizer != null)) {
	    if (!INSTALLED_ADAPTERS.containsKey(component)) {
		final MouseAdapter adapter = new MouseAdapter() {

		    @Override
		    public void mousePressed(final MouseEvent e) {
			if (component.getTransferHandler() != null && dragStartRecognizer.isDragStartEvent(e)) {
			    component.getTransferHandler().exportAsDrag(component, e, TransferHandler.COPY);
			}
		    }
		};
		component.addMouseListener(adapter);
		INSTALLED_ADAPTERS.put(component, adapter);
		return true;
	    } else {
		return false;
	    }
	}
	return true;
    }

    /**
     * Interface providing custom drag-from support.
     * 
     * @see DnDSupport2#installDnDSupport(JComponent, ua.com.fielden.platform.dnd.DnDSupport2.DragFromSupport, ua.com.fielden.platform.dnd.DnDSupport2.DragToSupport)
     * 
     * @author Yura
     */
    public static interface DragFromSupport {
	Object getObject4DragAt(Point point);

	void dragNDropDone(Object object, JComponent dropTo, int action);
    }

    /**
     * Interface providing custom drag-to support.
     * 
     * @see DnDSupport2#installDnDSupport(JComponent, ua.com.fielden.platform.dnd.DnDSupport2.DragFromSupport, ua.com.fielden.platform.dnd.DnDSupport2.DragToSupport)
     * 
     * @author Yura
     */
    public static interface DragToSupport {
	boolean canDropTo(Point point, Object what, JComponent draggedFrom);

	boolean dropTo(Point point, Object what, JComponent draggedFrom);
    }

    /**
     * Interface for defining, what kind of mouse event should initiate drag. Implementors of this interface may return true, for instance if left mouse button was pressed along
     * with ctrl key etc.
     * 
     * @author Yura
     */
    public static interface DragStartRecognizer {
	boolean isDragStartEvent(MouseEvent e);
    }

    /**
     * Class that wraps standard {@link TransferHandler} functionality and redirects all calls to {@link DragFromSupport} and {@link DragToSupport} interfaces installed on
     * particular component.
     * 
     * @author Yura
     */
    private static class DnDTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;

	/*
	 * ---------- Immutable properties of this class ----------
	 */

	private final JComponent component;

	private final DragFromSupport dragFromSupport;

	private final DragToSupport dragToSupport;

	private final boolean canImportToItself;

	/*
	 * ---------- Mutable properties of this class ----------
	 */

	private MouseEvent lastMousePressedEvent = null;

	public DnDTransferHandler(final JComponent component, final DragFromSupport dragFromSupport, final DragToSupport dragToSupport, final boolean canImportToItself) {
	    this.component = checkValue(component, "component");
	    this.dragFromSupport = dragFromSupport;
	    this.dragToSupport = dragToSupport;
	    this.canImportToItself = canImportToItself;
	}

	/*
	 * --------------- Methods enabling drag-to support -----------------
	 */

	@Override
	public boolean canImport(final TransferSupport support) {
	    // checking common support properties
	    if (!isOkay4Import(support)) {
		return false;
	    }
	    // getting object being transferred and checking its properties
	    try {
		final TransferredObject transferredObject = (TransferredObject) support.getTransferable().getTransferData(TG_DATA_FLAVOR);
		if (transferredObject.getDraggedFrom() == null) {
		    // IllegalStateException could be thrown here because instance of TransferredObject could be created only by this DnDTransferHandler class or its inner
		    // classes, but transfer handler of component that sent this TransferredObject is not of that class
		    return false;
		}

		// telling drag-from support what was the last component on which canImport() was invoked, i.e. last component which drag-from object could be dropped to
		transferredObject.setDraggedTo(component);

		if (!canImportToItself && component.equals(transferredObject.getDraggedFrom())) {
		    // cannot import to itself(i.e. from JLabel to itself)
		    return false;
		}
		if (transferredObject.getObject() == null) {
		    // no reason to import null value; null value could be returned from DragFromSupport.getObject4DragAt(Point point, ...) method invocation, indicating that
		    // it is not possible to obtain some object from the specified location of component and thus drag-from is not possible.
		    return false;
		}

		// looks like everything is validated so asking dragFromSupport if everything is ok and object can be dropped
		return dragToSupport.canDropTo(support.getDropLocation().getDropPoint(), transferredObject.getObject(), transferredObject.getDraggedFrom());
	    } catch (final UnsupportedFlavorException e) {
		throw new IllegalStateException("flavor is not supported though support.isDataFlavorSupported() returns true");
	    } catch (final IOException e) {
		e.printStackTrace();
		return false;
	    }
	}

	@Override
	public boolean importData(final TransferSupport support) {
	    super.importData(support);
	    if (dragToSupport == null) {
		return false;
	    } else {
		// according to JavaDocs of TransferHandler class, canImport(...) method was called before this one and returned true,
		// so everything is validated and we can drop object down
		try {
		    final TransferredObject transferredObject = (TransferredObject) support.getTransferable().getTransferData(TG_DATA_FLAVOR);
		    return dragToSupport.dropTo(support.getDropLocation().getDropPoint(), transferredObject.getObject(), transferredObject.getDraggedFrom());
		} catch (final UnsupportedFlavorException e) {
		    throw new IllegalStateException("flavor is not supported though canImport(...) returns true");
		} catch (final IOException e) {
		    e.printStackTrace();
		    return false;
		}
	    }
	}

	/*
	 * --------------- Methods enabling drag-from support -----------------
	 */

	/**
	 * Because this method is invoked subsequently during dragging it is not the best way to return always new instance of {@link Transferable}, which leads to unnecessary
	 * memory consumption. So this should be fixed in future (for instance, new {@link Transferable} instance should be created if drag target component changed or drag was
	 * interrupted and new drag took place).
	 */
	@Override
	protected Transferable createTransferable(final JComponent c) {
	    if (dragFromSupport == null) {
		return null;
	    } else {
		if (!component.equals(c)) {
		    throw new IllegalStateException("createTransferable(JComponent) method was invoked for component, not supported by this transfer handler");
		}
		return new Transferable() {
		    private TransferredObject transferredObject;

		    @Override
		    public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (!isDataFlavorSupported(flavor)) {
			    throw new UnsupportedFlavorException(flavor);
			}
			if (transferredObject == null) {
			    final Object object4Transfer = dragFromSupport.getObject4DragAt(lastMousePressedEvent.getPoint());
			    transferredObject = new TransferredObject(object4Transfer, component);
			}
			return transferredObject;
		    }

		    @Override
		    public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] { TG_DATA_FLAVOR };
		    }

		    @Override
		    public boolean isDataFlavorSupported(final DataFlavor flavor) {
			return TG_DATA_FLAVOR.equals(flavor);
		    }
		};
	    }
	}

	@Override
	protected void exportDone(final JComponent source, final Transferable data, final int action) {
	    super.exportDone(source, data, action);
	    if (dragFromSupport == null) {
		return;
	    } else {
		try {
		    final TransferredObject transferredObject = (TransferredObject) data.getTransferData(TG_DATA_FLAVOR);

		    if (transferredObject.getDraggedTo() == null) {
			// only one case when lastComponent4Import can be null - when drag has just started
			// but exportDone is called when it is finished so user probably just clicked on the component without actual drag
			return;
		    }

		    if (!canImportToItself && component.equals(transferredObject.getDraggedTo())) {
			// no reason to do anything because object had been dragged from the same component, which it was dropped to
			return;
		    }

		    if (transferredObject.getObject() == null) {
			// null value indicates that not object could be dragged from and thus dropped to
			return;
		    }

		    // everything is validated so we can drop it
		    dragFromSupport.dragNDropDone(transferredObject.getObject(), transferredObject.getDraggedTo(), action);
		} catch (final UnsupportedFlavorException e) {
		    throw new IllegalStateException("this transfer handler flavor is somewhy not supported");
		} catch (final IOException e) {
		    e.printStackTrace();
		}
	    }

	}

	/*
	 * --------------- Other methods -----------------
	 */

	@Override
	public int getSourceActions(final JComponent c) {
	    return MOVE | COPY;
	}

	@Override
	public void exportAsDrag(final JComponent comp, final InputEvent e, final int action) {
	    // saving mouse position which is the place where drag started
	    if (e instanceof MouseEvent) {
		this.lastMousePressedEvent = (MouseEvent) e;
	    } else {
		this.lastMousePressedEvent = null;
	    }
	    super.exportAsDrag(comp, e, action);

	}

	/**
	 * Checks whether it is drop operation(not clipboard one), whether support component and component to which this import is sent are the same and whether data flavor of
	 * incoming import is supported.
	 * 
	 * @param support
	 * @return
	 */
	private boolean isOkay4Import(final TransferSupport support) {
	    if (!support.isDrop()) {
		// cannot import from clipboard paste - only drop action
		return false;
	    }
	    if (!support.getComponent().equals(component)) {
		// cannot import from other component than registered one
		return false;
	    }
	    if (!support.isDataFlavorSupported(TG_DATA_FLAVOR)) {
		// cannot import other data than registered one
		return false;
	    }
	    return true;
	}

    }

    /**
     * Helper class that incapsulates dragged object and drag source component. Actually instances of this class(not just {@link Object}) is dragged between components.
     * 
     * @author Yura
     */
    public static class TransferredObject {

	private final Object object;

	private final JComponent draggedFrom;

	private JComponent draggedTo = null;

	public TransferredObject(final Object object, final JComponent draggedFrom) {
	    // object could be null
	    this.object = object;
	    // object always should be dragged from some component
	    this.draggedFrom = checkValue(draggedFrom, "draggedFrom");
	}

	public Object getObject() {
	    return object;
	}

	public JComponent getDraggedFrom() {
	    return draggedFrom;
	}

	public JComponent getDraggedTo() {
	    return draggedTo;
	}

	public void setDraggedTo(final JComponent draggedTo) {
	    this.draggedTo = draggedTo;
	}

    }

    /**
     * Throws {@link IllegalArgumentException} if value is null.
     * 
     * @param <E>
     * @param value
     * @param valueName
     *            - name of value being passed (used in thrown exception's message)
     * @return value was being passed if it is not null
     */
    private static <E> E checkValue(final E value, final String valueName) {
	if (value == null) {
	    throw new IllegalArgumentException(valueName + " is null");
	}
	return value;
    }

}
