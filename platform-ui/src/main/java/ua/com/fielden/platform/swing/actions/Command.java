package ua.com.fielden.platform.swing.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingWorker;

import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.swing.utils.SwingWorkerEx;

/**
 * Provides {@link Action} implementation, which executed the action in a {@link SwingWorker}.
 * <p>
 * Has three methods:
 * <ul>
 * <li>{@link #preAction()} -- default implementation simply sets enabled to false; should be overridden if custom behaviour to be provided; executes on EDT.</li>
 * <li>{@link #action(ActionEvent)} -- an abstract method, which should be implemented by every descendant to provide custom behaviour; is executed on the background thread.</li>
 * <li>{@link #postAction(T)} -- default implementation simply sets enabled to true; should be overridden if custom behaviour to be provided; executes on EDT.</li>
 * </ul>
 *
 * @author 01es
 *
 * @param <T>
 *            -- type of the result returned by method {@link #action(ActionEvent)}
 */
public abstract class Command<T> extends AbstractAction {
    private static final long serialVersionUID = 1L;

    public Command(final String name) {
	super(name);
    }

    /**
     * Executes on EDT. If this method returns false, then {@link #action(ActionEvent)} will not be executed as well as {@link #postAction(Object)}. If this method returns true
     * (and it does in this class), then everything will happen as in {@link Command} JavaDocs
     */
    protected boolean preAction() {
	setEnabled(false);
	return true;
    }

    /**
     * Executes in {@link SwingWorker}
     */
    protected abstract T action(final ActionEvent e) throws Exception;

    /**
     * Executes on EDT.
     */
    protected void postAction(final T value) {
	setEnabled(true);
    }

    @Override
    public final void actionPerformed(final ActionEvent e) {
	if (preActionWrapper()) {
	    final SwingWorkerEx<T, Void> sw = new SwingWorkerEx<T, Void>() {

		/**
		 * A state indicating whether an exception when performing a background action occurred.
		 */
		private boolean exceptionInAction = false;

		@Override
		protected T backgroundAction() throws Exception {
		    exceptionInAction = false;
		    try {
			return action(e);
		    } catch (final Throwable e) {
			exceptionInAction = true;
			handleException(e);
			return null;
		    }
		}

		@Override
		protected void done() {
		    try {
			if (!exceptionInAction) {
			    postAction(get());
			}
		    } catch (final Throwable e) {
			handlePreAndPostActionException(e);
		    }
		}

		@Override
		protected void handleException(final Throwable ex) {
		    SwingUtilitiesEx.invokeLater(new Runnable() {
			@Override
			public void run() {
			    handlePreAndPostActionException(ex);
			}
		    });
		}
	    };
	    // bug : more than one !SwingWorker thread could not be executed simultaneously.
	    // More information : http://bugs.sun.com/view_bug.do?bug_id=6880336.
	    // Additional information : http://forums.sun.com/thread.jspa?threadID=5424356&start=0&tstart=0.

	    // To avoid this bug :
	    // 1. Use JDK 6 update version >= 21 (or <= 17).
	    // 2. Execute SwingWorker instances like : "java.util.concurrent.Executors.newCachedThreadPool().execute(sw);" instead of "sw.execute();"

	    sw.execute();
//	    java.util.concurrent.Executors.newCachedThreadPool().execute(sw);
	}
    }

    /**
     * A convenient method that wraps preAction() execution in try/catch block for error handling.
     *
     * @return
     */
    protected boolean preActionWrapper() {
	try {
	    final boolean success = preAction();
	    if (!success){
		setEnabled(true);
	    }
	    return success;
	} catch (final Throwable e) {
	    handlePreAndPostActionException(e);
	    return false;
	}
    }

    /**
     * Implements a default exception handling mechanism for methods preAction and postAction. This method would be used only if an actual exception bubbles up (i.e. not handled
     * within pre- and postAction implementations as provided by Command descendants).
     *
     * Enabling of this command is sutured inside default implementation of this method to make this behaviour customizable for descendants.
     *
     * @param ex
     */
    protected void handlePreAndPostActionException(final Throwable ex) {
	new DialogWithDetails(null, "Exception in action", ex).setVisible(true);
	setEnabled(true);
    }
}
