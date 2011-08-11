package ua.com.fielden.platform.swing.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;

import javax.swing.JPanel;

import ua.com.fielden.platform.swing.model.ICloseGuard;

/**
 * A base class for all guarded panels.
 * 
 * @author 01es
 * 
 */
public abstract class BasePanel extends JPanel implements ICloseGuard {
    private static final long serialVersionUID = 1L;

    public BasePanel() {
    }

    public BasePanel(final LayoutManager layoutManager) {
	super(layoutManager);
    }

    @Override
    public ICloseGuard canClose() {
	return checkCanClose(this);
    }

    @Override
    public String whyCannotClose() {
	return "default implementation: should have been overridden";
    }

    @Override
    public void close() {
	tryClosing(this);
    }

    /**
     * Closes guarded items. Even if guarded container has guarded children only this guarded (top level) container is closed.
     * 
     * @param parent
     */
    private void tryClosing(final Container parent) {
	for (final Component component : parent.getComponents()) {
	    if (component instanceof ICloseGuard) {
		final ICloseGuard guard = (ICloseGuard) component;
		guard.close();
	    } else if (component instanceof Container) {
		tryClosing((Container) component);
	    }
	}
    }

    /**
     * Traverses the tree of components in search for the first {@link ICloseGuard} descendant, which cannot be closed.
     * 
     * @param parent
     * @return
     */
    private ICloseGuard checkCanClose(final Container parent) {
	for (final Component component : parent.getComponents()) {
	    if (component instanceof ICloseGuard) {
		final ICloseGuard guard = (ICloseGuard) component;
		final ICloseGuard result = guard.canClose();
		if (result != null) {
		    return result;
		}
	    } else if (component instanceof Container) {
		final ICloseGuard guard = checkCanClose((Container) component);
		if (guard != null) {
		    return guard;
		}
	    }
	}
	return null;
    }

    @Override
    public boolean canLeave() {
	return canClose() == null;
    }

    /**
     * Should be overwritten to provide at least some short information about panel's purpose.
     * 
     * @return
     */
    public abstract String getInfo();
}
