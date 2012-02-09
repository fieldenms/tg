package ua.com.fielden.platform.swing.components.smart.development;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;

/**
 * The JIDE implementation of the hovering effect is provided by class Sticky. However, IMO its implementation is defected because it is based on item selection, which naturally
 * upsets selection performed by user. A much better approach was suggested by Christian Kaufhold where a custom cell renderer provides the desired background colour
 * (http://groups.google.com/group/comp.lang.java.gui/browse_frm/thread/1f9886beebe795b).
 * 
 * <code>Hover</code> is basically his code. Some improvements might be required.
 * 
 * In addition to mouse, it also supports keyboard navigation.
 * 
 * @author 01es
 * 
 */
public class Hover {
    private static class Listener extends MouseInputAdapter implements ListDataListener, PropertyChangeListener, ComponentListener, HierarchyListener, HierarchyBoundsListener, ListSelectionListener, Runnable {
	private JList list;
	private int hoverIndex;
	private boolean enabled, running;
	private Point lastLocation;

	private int caretIndex;
	private int selectionSize = -1;

	public Listener(final JList list) {
	    this.list = list;
	    hoverIndex = -1;
	}

	public void setEnabled(final boolean value) {
	    if (enabled == value) {
		return;
	    }
	    enabled = value;

	    if (enabled) {
		list.addMouseListener(this);
		list.addMouseMotionListener(this);
		list.addPropertyChangeListener(this);
		list.getModel().addListDataListener(this);

		list.addComponentListener(this);
		list.addHierarchyListener(this);
		list.addHierarchyBoundsListener(this);

		list.putClientProperty(HOVER, this);

		list.addListSelectionListener(this);
	    } else {
		repaint(hoverIndex());

		list.removeMouseListener(this);
		list.removeMouseMotionListener(this);
		list.removePropertyChangeListener(this);
		list.getModel().removeListDataListener(this);

		list.removeHierarchyBoundsListener(this);
		list.removeHierarchyListener(this);
		list.removeComponentListener(this);

		list.putClientProperty(HOVER, null);

		list.addListSelectionListener(this);
	    }
	}

	public int hoverIndex() {
	    return hoverIndex;
	}

	/**
	 * Keeps in synch both hoverIndex and caretIndex except the case where hoverIndex becomes -1. This is needed to correctly reflect a current list item when navigating with a
	 * keyboard.
	 * 
	 * @param value
	 */
	private void setHoverIndex(final int value) {
	    hoverIndex = value;
	    if (value > -1) {
		caretIndex = value;
	    }
	}

	private void repaint(final int index) {
	    if (index != -1 && list.getCellBounds(index, index) != null) {
		list.repaint(list.getCellBounds(index, index));
	    }
	}

	private Point toScreen(Point p) {
	    p = new Point(p);
	    SwingUtilities.convertPointToScreen(p, list);
	    return p;
	}

	private Point toList(final Point p) {
	    if (!list.isShowing()) {
		return null;
	    }

	    final Point s = list.getLocationOnScreen();
	    s.x = p.x - s.x;
	    s.y = p.y - s.y;
	    return s;
	}

	// p is in screen coordinate system (or zero, denoting not known or outside)
	private void updateHover(final Point p) {
	    final int h = hoverIndex();

	    final Point q = p == null ? null : toList(p);

	    final int newHoverIndex = p == null ? -1 : list.locationToIndex(q);

	    if (h != newHoverIndex) {
		repaint(h);
		repaint(newHoverIndex);
		setHoverIndex(newHoverIndex);
	    }

	    lastLocation = p;
	}

	/**
	 * This is similar to method updateHover(final Point p), but implemented specifically to support keyboard driven navigation.
	 * 
	 * @param firstIndex
	 * @param lastIndex
	 * @param numberOfSelectedItems
	 */
	private void updateHover(final int firstIndex, final int lastIndex, final int numberOfSelectedItems) {
	    if (selectionSize != numberOfSelectedItems) { // if there was change in selection then there is nothing to do
		selectionSize = numberOfSelectedItems;
		return;
	    }
	    final int h = caretIndex;
	    final int newCaretIndex = lastIndex > h ? lastIndex : lastIndex - 1;
	    if (h != newCaretIndex) {
		repaint(h);
		repaint(newCaretIndex);
		setHoverIndex(newCaretIndex);
	    }
	}

	// updateLater is used to make sure that the cell bounds are already updated.
	// this may have problems if called initially not from the event-dispatch thread
	private void updateLater() {
	    if (!running) {
		running = true;
		EventQueue.invokeLater(this);
	    }
	}

	public void run() {
	    running = false;
	    updateHover(lastLocation);
	}

	public void propertyChange(final PropertyChangeEvent e) {
	    final String name = e.getPropertyName();

	    if (name.equals("model")) {
		((ListModel) e.getOldValue()).removeListDataListener(this);
		((ListModel) e.getNewValue()).addListDataListener(this);

		setHoverIndex(-1);
		updateLater();
	    } else if (name.equals("font") || name.equals("cellRenderer") || name.equals("fixedRowWidth") || name.equals("fixedRowHeight") || name.equals("prototypeCellValue")
		    || name.equals("layoutOrientation")) {
		list.repaint();
		updateLater();
	    }
	}

	public void hierarchyChanged(final HierarchyEvent e) {
	    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
		if (!list.isShowing()) {
		    lastLocation = null;
		    setHoverIndex(-1);
		}
		// else: nothing to be done as mouse location is not known (only 1.5)
	    }
	}

	/**
	 * Invoked when the current value or selection in JList is changed.
	 */
	public void valueChanged(final ListSelectionEvent listSelectionEvent) {
	    if (!listSelectionEvent.getValueIsAdjusting()) {
		updateHover(listSelectionEvent.getFirstIndex(), listSelectionEvent.getLastIndex(), ((JList) listSelectionEvent.getSource()).getSelectedValues().length);
	    }
	}

	public void componentShown(final ComponentEvent e) {
	}

	public void componentHidden(final ComponentEvent e) {
	    // handled by hierarchyChanged
	}

	public void componentMoved(final ComponentEvent e) {
	    updateHover(lastLocation);
	}

	public void ancestorMoved(final HierarchyEvent e) {
	    updateHover(lastLocation);
	}

	public void componentResized(final ComponentEvent e) {
	    updateLater();
	}

	public void ancestorResized(final HierarchyEvent e) {
	    updateLater();
	}

	public void mouseMoved(final MouseEvent e) {
	    updateHover(toScreen(e.getPoint()));
	}

	public void mouseEntered(final MouseEvent e) {
	    updateHover(toScreen(e.getPoint()));
	}

	public void mouseExited(final MouseEvent e) {
	    updateHover(null);
	}

	// These implementations are quite unspecific.

	public void intervalAdded(final ListDataEvent e) {
	    if (hoverIndex() >= e.getIndex0()) {
		setHoverIndex(-1);
	    }

	    updateLater();
	}

	public void intervalRemoved(final ListDataEvent e) {
	    if (hoverIndex() >= e.getIndex0()) {
		setHoverIndex(-1);
	    }

	    updateLater();
	}

	public void contentsChanged(final ListDataEvent e) {
	    updateLater();
	}
    }

    private static Object HOVER = "xxx.Hover";

    public static int index(final JList list) {
	final Listener h = (Listener) list.getClientProperty(HOVER);

	return h == null ? -1 : h.hoverIndex();
    }

    public static void install(final JList list) {
	if (list.getClientProperty(HOVER) == null) {
	    final Listener h = new Listener(list);
	    h.setEnabled(true);
	}
    }

    public static void uninstall(final JList list) {
	final Listener h = (Listener) list.getClientProperty(HOVER);
	if (list != null) {
	    h.setEnabled(false);
	}
    }

    public static void main(final String[] args) {
	final DefaultListModel data = new DefaultListModel();
	final Object[] o = new java.io.File(System.getProperty("user.home")).list();
	for (int i = 0; i < o.length; i++) {
	    data.addElement(o[i]);
	}

	final JList list = new JList(data);
	Hover.install(list);
	list.setCellRenderer(new DefaultListCellRenderer() {
	    private Color hoverBackground = new Color(150, 255, 200);

	    public Component getListCellRendererComponent(final JList list, final Object value, final int index, final boolean selected, final boolean focused) {
		super.getListCellRendererComponent(list, value, index, selected, focused);

		if (Hover.index(list) == index) {
		    setBackground(hoverBackground);
		}

		return this;
	    }
	});
	list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		final JFrame frame = new JFrame();
		frame.getContentPane().add(new JScrollPane(list));
		frame.pack();
		frame.setVisible(true);
	    }
	});
    }
}