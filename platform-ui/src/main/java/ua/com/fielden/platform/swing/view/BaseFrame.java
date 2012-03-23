package ua.com.fielden.platform.swing.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.model.ICloseGuard;

/**
 * This is a general purpose JFrame, which supports proper bring to front behaviour and leverages its child components, which implement a {@link ICloseGuard} contract.
 * <p>
 * Also, it provides a way to specify {@link ICloseHook}, which gets invoked upon frame closure. This can be very useful to avoid dead references preventing collection of the
 * garbage.
 *
 * @author 01es
 *
 */
public class BaseFrame extends JFrame implements ICloseGuard {
    private static final long serialVersionUID = 1L;

    private final Map<Class<? extends AbstractEntity<?>>, IEntityMasterCache> cache;
    private boolean disposed = false;

    public BaseFrame() {
	this("No title", null, null, null);
    }

    public BaseFrame(final ICloseHook hook) {
	this("No title", null, hook, null);
    }

    public BaseFrame(final String title) {
	this(title, null, null, null);
    }

    public BaseFrame(final String title, final Map<Class<? extends AbstractEntity<?>>, IEntityMasterCache> cache) {
	this(title, null, null, cache);
    }

    public BaseFrame(final String title, final ICloseHook hook) {
	this(title, null, hook, null);
    }

    public BaseFrame(final String title, final LayoutManager layoutManager) {
	this(title, layoutManager, null, null);
    }

    public BaseFrame(final String title, final LayoutManager layoutManager, final ICloseHook hook, final Map<Class<? extends AbstractEntity<?>>, IEntityMasterCache> cache) {
	super(title);
	this.cache = cache;
	if (layoutManager != null) {
	    setLayout(layoutManager);
	}
	setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

	addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowDeactivated(final WindowEvent e) {
		super.windowDeactivated(e);
		//		e.getWindow().setAlwaysOnTop(false);
	    }

	    @Override
	    public void windowClosing(final WindowEvent e) {
		final ICloseGuard guard = checkCanClose(BaseFrame.this);
		if (guard == null) {
		    close();
		} else {
		    BaseFrame.this.notify(guard);
		}
	    }

	    @Override
	    public void windowClosed(final WindowEvent e) {
		if (hook != null) {
		    hook.closed(BaseFrame.this);
		}
	    }
	});
    }

    /**
     * Traverses the tree of components in search for the first {@link ICloseGuard} descendant, which cannot be closed.
     *
     * @param parent
     * @return
     */
    protected ICloseGuard checkCanClose(final Container parent) {
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
	// there could also be a cache to check...
	if (cache != null) {
	    for (final IEntityMasterCache ch : cache.values()) {
		for (final BaseFrame frame : ch.all()) {
		    final ICloseGuard guard = frame.checkCanClose(frame);
		    if (guard != null) {
			frame.setVisible(true);
			return guard;
		    }
		}
	    }
	}
	return null;
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

    @Override
    public void setVisible(final boolean visible) {
	// make sure that frame is marked as not disposed if it is asked to be visible
	if (visible) {
	    setDisposed(false);
	}
	// let's handle visibility...
	if (!visible || !isVisible()) { // have to check this condition simply because super.setVisible(true) invokes toFront is frame was already visible
	    super.setVisible(visible);
	}
	// ...and bring frame to the front.. in a strange and weird way
	if (visible) {
	    int state = super.getExtendedState();
	    state &= ~JFrame.ICONIFIED;
	    super.setExtendedState(state);
	    super.setAlwaysOnTop(true);
	    super.toFront();
	    super.requestFocus();
	    super.setAlwaysOnTop(false);
	}
    }

    @Override
    public void toFront() {
	super.setVisible(true);
	int state = super.getExtendedState();
	state &= ~JFrame.ICONIFIED;
	super.setExtendedState(state);
	super.setAlwaysOnTop(true);
	super.toFront();
	super.requestFocus();
	super.setAlwaysOnTop(false);
    }

    @Override
    public void setDefaultCloseOperation(final int operation) {
	if (operation != JFrame.DO_NOTHING_ON_CLOSE) {
	    throw new RuntimeException("Only DO_NOTHING_ON_CLOSE operation is permitted for guraded frames.");
	}
	super.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    }

    @Override
    public ICloseGuard canClose() {
	return checkCanClose(this);
    }

    @Override
    public String whyCannotClose() {
	final ICloseGuard guard = checkCanClose(this);
	if (guard != null) {
	    return guard.whyCannotClose();
	}
	return "";
    }

    @Override
    public void close() {
	tryClosing(this);
	dispose();
	setDisposed(true);
    }

    /**
     * Notifies user if frame could not be closed. This method can be overridden if an alternative notification mechnism is required.
     *
     * @param guard
     */
    protected void notify(final ICloseGuard guard) {
	JOptionPane.showMessageDialog(this, guard.whyCannotClose(), "Warning", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public boolean canLeave() {
	return canClose() == null;
    }

    public boolean isDisposed() {
	return disposed;
    }

    protected void setDisposed(final boolean disposed) {
	this.disposed = disposed;
    }

}
