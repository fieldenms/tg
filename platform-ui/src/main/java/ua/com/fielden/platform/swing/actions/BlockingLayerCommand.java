package ua.com.fielden.platform.swing.actions;

import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.components.blocking.IBlockingLayerProvider;
import ua.com.fielden.platform.swing.dialogs.DialogWithDetails;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;

/**
 * This class behaves much the same as {@link BlockingCommand} class with exception that it blocks the provided {@link BlockingIndefiniteProgressLayer} instance.
 *
 * @author TG Team
 *
 * @param <T>
 *            - result type
 */
public abstract class BlockingLayerCommand<T> extends Command<T> {
    private static final long serialVersionUID = 1L;

    private final IBlockingLayerProvider provider;

    public BlockingLayerCommand(final String name, final IBlockingLayerProvider provider) {
	super(name);
	this.provider = provider;
    }

    public BlockingLayerCommand(final String name, final BlockingIndefiniteProgressLayer blockingLayer) {
	this(name, new IBlockingLayerProvider() {
	    @Override
	    public BlockingIndefiniteProgressLayer getBlockingLayer() {
		return blockingLayer;
	    }
	});

    }

    /**
     * Enhanced the notion of enable/disable by taking into account blocking layer locking.
     *
     * @param enable
     *            -- if <code>true</code> then action is enabled and blocking layer unlocked; otherwise -- action is disabled and blocking layer locked.
     */
    @Override
    public void setEnabled(final boolean enable) {
	setEnabled(enable, !enable);
    }

    /**
     * Method {@link #setEnabled(boolean)} handles both action enabling/disabling and blocking layer locking/unlocking where action disabling happens together with locking, and
     * enabling with unlocking.
     * <p>
     * However, this usage pattern is not always applicable. For instance, page navigation -- when the last page is active then the go to last page action should be disabled, at
     * the same time no locking should be happening.
     * <p>
     * In order to facilitate asymmetric enabling/locking this method has been proved, which accepts two parameters.
     *
     * @param enable
     *            -- controls action enabling/disabling.
     * @param locked
     *            -- controls blocking layer locking/unlocking.
     */
    public void setEnabled(final boolean enable, final boolean locked) {
	setEnabled(enable, locked, false);
    }

    /**
     * Base method for enabling/locking.
     * <p> Locking could be performed in case when "blocking layer" could be retrieved from provider.
     * <p> Parameters <code>locked</code> and <code>forced</code> control locking.
     * Locking/unlocking will be performed only when "blocking layer" is not in "incremental locking" mode or when <code>forced == true</code>.
     *
     * @param enable -- controls action enabling/disabling.
     * @param locked -- controls blocking layer locking/unlocking.
     * @param force -- indicates whether locking should be forced (even in case of "incremental locking").
     */
    protected void setEnabled(final boolean enable, final boolean locked, final boolean force) {
	super.setEnabled(enable);
	if (provider.getBlockingLayer() != null && (force || !provider.getBlockingLayer().isIncrementalLocking() )) {
	    provider.getBlockingLayer().setLocked(locked);
	}
    }

    /**
     * Overridden in order to force locking even when "incremental locking" mode is turned on for blocking layer.
     *
     */
    @Override
    protected boolean preAction() {
	setEnabled(false, true, true);
	return true;
    }

    /**
     * Overridden in order to force unlocking even when "incremental locking" mode is turned on for blocking layer.
     */
    @Override
    protected void postAction(final T value) {
	setEnabled(true, false, true);
    }

    /**
     * Overridden in order to force unlocking even when "incremental locking" mode is turned on for blocking layer in case of unsuccessful preAction() execution.
     *
     * @return
     */
    @Override
    protected boolean preActionWrapper() {
	try {
	    final boolean success = preAction();
	    if (!success){
		setEnabled(true, false, true);
	    }
	    return success;
	} catch (final Throwable e) {
	    handlePreAndPostActionException(e);
	    return false;
	}
    }


    /**
     * Updates blocking layer with provided message.
     *
     * @param value
     */
    public void setMessage(final String value) {
	if (provider.getBlockingLayer() != null) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		@Override
		public void run() {
		    provider.getBlockingLayer().setText(value);
		}
	    });
	}
    }

    public IBlockingLayerProvider getProvider() {
        return provider;
    }

    @Override
    protected void handlePreAndPostActionException(final Throwable ex) {
	super.handlePreAndPostActionException(ex);
	provider.getBlockingLayer().unlock();
    }
}
