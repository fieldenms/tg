package ua.com.fielden.platform.swing.dnd;

import static java.util.Arrays.asList;
import static ua.com.fielden.platform.swing.dnd.DnDSupport2.installDnDSupport;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.dnd.DnDSupport2.DragFromSupport;
import ua.com.fielden.platform.swing.dnd.DnDSupport2.DragToSupport;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

/**
 * Panel that enables dragging of components on it. When adding components to this panel, one should specify absolute layout constraints of components, because after drag-n-drop
 * they are all removed and added in arbitrary order.
 *
 * @author TG Team
 */
public class DndPanel extends JPanel {

    private static final long serialVersionUID = -868297718929609244L;

    private final Map<JComponent, String> layoutConstraints = new HashMap<JComponent, String>();

    private final Action changeLayoutAction;

    private final Action backToNormalAction;

    public DndPanel(final MigLayout migLayout) {
	super(migLayout);

	installDnDSupport(this, createDragFromSupport(), createDragToSupport(), true);
	changeLayoutAction = createChangeLayoutAction();
	backToNormalAction = createBackToNormalAction();
    }

    private Action createBackToNormalAction() {
	final Action action = new AbstractAction("Back to normal") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		switchToNormalMode();
		getChangeLayoutAction().setEnabled(true);
		setEnabled(false);
	    }
	};
	action.setEnabled(false);
	return action;
    }

    private Action createChangeLayoutAction() {
	final Action action = new AbstractAction("Change layout") {
	    private static final long serialVersionUID = 1L;

	    @Override
	    public void actionPerformed(final ActionEvent e) {
		switchToLayoutEditingMode();
		getBackToNormalAction().setEnabled(true);
		setEnabled(false);
	    }
	};
	action.setEnabled(true);
	return action;
    }

    public Action getChangeLayoutAction() {
	return changeLayoutAction;
    }

    public Action getBackToNormalAction() {
	return backToNormalAction;
    }

    private DragToSupport createDragToSupport() {
	return new DragToSupport() {

	    private ComponentCopy dropToComponent;

	    @Override
	    public boolean canDropTo(final Point point, final Object what, final JComponent draggedFrom) {
		// removing highlighting from previously hovered component
		if (dropToComponent != null) {
		    dropToComponent.setDrawBorder(false);
		}

		final Component dropToComponentComp = getComponentAt(point);

		// we can only drag ComponentCopy instances
		if (dropToComponentComp instanceof ComponentCopy) {
		    dropToComponent = (ComponentCopy) dropToComponentComp;
		    // highlighting hovered component
		    dropToComponent.setDrawBorder(true);
		    // no need to drop component to itself
		    return what != dropToComponent;
		} else {
		    return false;
		}
	    }

	    @Override
	    public boolean dropTo(final Point point, final Object what, final JComponent draggedFrom) {
		// can only drop ComponentCopy instances
		if (what != null && what instanceof ComponentCopy) {
		    final Component whereToDrop = getComponentAt(point);

		    // can only drop component to another instance of ComponentCopy
		    if (what != whereToDrop && whereToDrop instanceof ComponentCopy) {
			// if dropping, removing highlighting from hovered component
			dropToComponent.setDrawBorder(false);
			// swapping component positions
			swapCopies((ComponentCopy) whereToDrop, (ComponentCopy) what);
			return true;
		    } else {
			return false;
		    }
		} else {
		    return false;
		}
	    }
	};
    }

    private DragFromSupport createDragFromSupport() {
	return new DragFromSupport() {
	    @Override
	    public void dragNDropDone(final Object object, final JComponent dropTo, final int action) {
	    }

	    @Override
	    public Object getObject4DragAt(final Point point) {
		final Component comp = getComponentAt(point);
		// can only drag instances of ComponentCopy
		return comp instanceof ComponentCopy ? comp : null;
	    }
	};
    }

    /**
     * Switches panel to layout editing mode with enabled component dragging but disabled other component functionality (only concerns draggable components).
     */
    public void switchToLayoutEditingMode() {
	// creating copy of components array because we are going to add/remove components
	for (final Component comp : asList(getComponents())) {
	    final Object compConstraints = layoutConstraints.get(comp);
	    if (compConstraints != null) {
		// means this component was added as draggable
		// removing this component, but leaving its constraints
		super.remove(comp);
		// creating visual copy
		final ComponentCopy copy = new ComponentCopy((JComponent) comp);
		// adding it on the same place
		add(copy, compConstraints);
	    }
	}

	revalidate();
    }

    /**
     * Switches panel to normal mode with disabled component dragging.
     */
    public void switchToNormalMode() {
	// creating copy of components array because we are going to add/remove components
	for (final Component comp : asList(getComponents())) {
	    if (comp instanceof ComponentCopy) {
		final ComponentCopy copy = (ComponentCopy) comp;
		// removing copy
		super.remove(copy);
		// adding original on its place
		add(copy.getOriginal(), layoutConstraints.get(copy.getOriginal()));
	    }
	}

	revalidate();
    }

    /**
     * Adds component to this panel which, after switching to layout-editing mode, will be draggable.
     */
    public void addDraggable(final JComponent comp, final String constraints) {
	add(comp, constraints);

	layoutConstraints.put(comp, constraints);
    }

    /**
     * Removes component from this panel and removes corresponding entry in {@link #layoutConstraints} mapping.
     */
    @Override
    public void remove(final Component comp) {
	super.remove(comp);

	layoutConstraints.remove(comp);
    }

    /**
     * Removes all components from this panel and clears {@link #layoutConstraints}.
     */
    @Override
    public void removeAll() {
	super.removeAll();

	layoutConstraints.clear();
    }

    /**
     * Swaps positions of the specified components on this panel
     */
    private void swapCopies(final ComponentCopy comp1, final ComponentCopy comp2) {
	// removing components from panel, but leaving their constraints in layoutConstraints mapping
	super.remove(comp1);
	super.remove(comp2);

	// storing constraints of comp1 and comp2
	final String comp1Constraints = layoutConstraints.get(comp1.getOriginal());
	final String comp2Constraints = layoutConstraints.get(comp2.getOriginal());
	// adding comp1 again but with constraints of comp2
	add(comp1, comp2Constraints);
	layoutConstraints.put(comp1.getOriginal(), comp2Constraints);
	// adding comp2 with previous constrains of comp1
	add(comp2, comp1Constraints);
	layoutConstraints.put(comp2.getOriginal(), comp1Constraints);

	// revalidating so that changes take place
	revalidate();
    }

    /**
     * Class for creating visual component copies without any functionality.
     *
     * @author TG Team
     *
     */
    public static class ComponentCopy extends JComponent {

	private static final Color HIGHLIGHT_COLOUR = new Color(115,164,209); // nimbusFocus #73a4d1 (115,164,209); nimbusBase	#33628c (51,98,140)

	private static final long serialVersionUID = 4303146470257585749L;

	private final JComponent original;

	private boolean drawBorder = false;

	/**
	 * Creates visual copy of the specified component.<br>
	 * <br>
	 * Note: disables double-buffering for the specified component and all of its sub-components in order to let Swing correctly paint offscreen components, containing
	 * children. One may observe rendering artifacts like thick black border around component copy. In this case, consider adding original component to {@link JPanel} and then
	 * copying {@link JPanel} with this sole component.
	 *
	 * @param original
	 */
	public ComponentCopy(final JComponent original) {
	    this.original = original;
	    disableDoubleBuffering(original);

	    if (getOriginal() instanceof JLabel) {
		setMinimumSize(getOriginal().getSize());
	    }
	}

	/**
	 * Recursively disables double-buffering for the specified component and all of its sub-components.
	 *
	 * @param component
	 */
	private void disableDoubleBuffering(final JComponent component) {
	    component.setDoubleBuffered(false);
	    for (final Component comp : component.getComponents()) {
		if (comp instanceof JComponent) {
		    disableDoubleBuffering((JComponent) comp);
		}
	    }
	}

	@Override
	protected void paintComponent(final Graphics g) {
	    super.paintComponent(g);
	    final Graphics2D g2 = (Graphics2D) g;

	    if (!getSize().equals(original.getSize())) {
		original.setSize(getSize());
	    }
	    // because original component may not have parent, it won't be rendered due to Swing performance issues
	    // thus we should call addNotify() which should enable correct rendering even of parentless component
	    original.addNotify();
	    // during testing it was discovered that we should not call validate this component to render it correctly
	    // however if some rendering problems occurs, consider un-commenting following line
	    //original.validate();
	    original.paintAll(g2);

	    if (drawBorder) {
		final Color prevColor = g2.getColor();

		g2.setColor(HIGHLIGHT_COLOUR);
		g2.setStroke(new BasicStroke(3));
		g2.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

		g2.setColor(prevColor);
	    }
	}

	protected JComponent getOriginal() {
	    return original;
	}

	public void setDrawBorder(final boolean drawBorder) {
	    this.drawBorder = drawBorder;
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		@Override
		public void run() {
		    repaint();
		}
	    });
	}
    }

}
