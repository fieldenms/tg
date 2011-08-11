package ua.com.fielden.platform.swing.pagination;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * Provides a glue code in a form of actions for an instance of {@link IPageProvider} and {@link PropertyTableModel}. Basically, what it does is creates actions corresponding to
 * first, previous, next and last that can be associated with buttons or some other navigation UI elements.
 *
 * @author 01es
 *
 */
public class Paginator<T extends AbstractEntity> {

    /**
     * A contract for anything that would like to receive a feedback when paginator changes the current page.
     *
     * @author 01es
     *
     */
    public static interface IPageChangeFeedback {
	void feedback(IPage<?> page);

	void enableFeedback(boolean enable);
    }

    /**
     * A contract for anything that would like to enable or disable actions related to the controls of paginator.
     *
     * @author oleh
     *
     */
    public static interface IEnableAction {

	/**
	 * Enables or disables actions that is related to the pagination controls
	 *
	 * @param enable
	 *            - indicates whether the action must be enabled or disabled
	 */
	void enableAction(boolean enable);
    }

    /**
     * Provides facility that allows to define specific data representation.
     *
     * @author oleh
     *
     */
    public static interface IPageController {

	/**
	 * Loads specified page of data in to data set or model to represent it on it's own way (grid or graphics e.t.c.).
	 *
	 * @param page
	 */
	void loadPage(IPage<?> page);
    }

    private final BlockingLayerCommand<List<T>> prev, next, first, last;

    private final IPageController pageController;

    private IPage<T> currentPage;

    private final IPageChangeFeedback feedback;
    private final IEnableAction enableAction;

    public Paginator(final IPageController pageController, final BlockingIndefiniteProgressLayer blockingLayer) {
	this(pageController, null, blockingLayer, null);
    }

    public Paginator(final IPageController pageController, final BlockingIndefiniteProgressLayer blockingLayer, final IEnableAction enableAction) {
	this(pageController, null, blockingLayer, enableAction);
    }

    public Paginator(final IPageController pageController, final IPageChangeFeedback feedback, final BlockingIndefiniteProgressLayer blockingLayer, final IEnableAction enableAction) {
	this.enableAction = enableAction;
	this.feedback = feedback;
	this.pageController = pageController;
	first = createActionFirst(blockingLayer);
	prev = createActionPrev(blockingLayer);
	next = createActionNext(blockingLayer);
	last = createActionLast(blockingLayer);
    }

    /** Simple closure to reduce code complexity. */
    private interface IGetter <M> { M get(); }
    /** Creates action based on behaviour passed in parameters.*/
    private BlockingLayerCommand<List<T>> createAction(final BlockingIndefiniteProgressLayer blockingLayer, final IGetter<IPage<T>> pageIterator, final IGetter<String> pageMessage, final String iconPath, final String desc, final int mnemonicKey) {
	final BlockingLayerCommand<List<T>> action = new BlockingLayerCommand<List<T>>("", blockingLayer) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		blockingLayer.enableIncrementalLocking();
		setMessage(pageMessage.get());
		disableActions();
		if (enableAction != null) {
		    enableAction.enableAction(false);
		}
		return super.preAction();
	    }

	    @Override
	    protected List<T> action(final ActionEvent e) throws Exception {
		setCurrentPage(pageIterator.get());
		final List<T> data = getCurrentPage().data();
		return data;
	    }

	    @Override
	    protected void postAction(final List<T> data) {
		super.postAction(data);
		enableActions();
		if (enableAction != null) {
		    enableAction.enableAction(true);
		}
	    }

	    /**
	     * After default exception handling executed, post-actions should be performed to enable all necessary buttons, unlock layer etc.
	     */
	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
	        super.handlePreAndPostActionException(ex);

		super.postAction(null);
		enableActions();
		if (enableAction != null) {
		    enableAction.enableAction(true);
		}
	    }

	};
	action.setEnabled(false, false);
	final Icon icon = ResourceLoader.getIcon(iconPath);
	action.putValue(Action.LARGE_ICON_KEY, icon);
	action.putValue(Action.SMALL_ICON, icon);
	action.putValue(Action.SHORT_DESCRIPTION, desc);
	action.putValue(Action.MNEMONIC_KEY, mnemonicKey);
	return action;
    }

    private BlockingLayerCommand<List<T>> createActionFirst(final BlockingIndefiniteProgressLayer blockingLayer) {
	return createAction(blockingLayer,
		new IGetter<IPage<T>>(){ public IPage<T> get() { return getCurrentPage().first(); } },
		new IGetter<String>(){ public String get() { return "Loading first page..."; } },
		"images/navigation/01-first.png", "Go to first page", KeyEvent.VK_HOME);
    }

    private BlockingLayerCommand<List<T>> createActionPrev(final BlockingIndefiniteProgressLayer blockingLayer) {
	return createAction(blockingLayer,
		new IGetter<IPage<T>>(){ public IPage<T> get() { return getCurrentPage().prev(); } },
		new IGetter<String>(){ public String get() { return "Loading previous " + getCurrentPage().capacity() + " records..."; } },
		"images/navigation/02-prev.png", "Go to previous page", KeyEvent.VK_PAGE_UP);
    }

    private BlockingLayerCommand<List<T>> createActionNext(final BlockingIndefiniteProgressLayer blockingLayer) {
	return createAction(blockingLayer,
		new IGetter<IPage<T>>(){ public IPage<T> get() { return getCurrentPage().next(); } },
		new IGetter<String>(){ public String get() { return "Loading next " + getCurrentPage().capacity() + " records..."; } },
		"images/navigation/03-next.png", "Go to next page", KeyEvent.VK_PAGE_DOWN);
    }

    private BlockingLayerCommand<List<T>> createActionLast(final BlockingIndefiniteProgressLayer blockingLayer) {
	return createAction(blockingLayer,
		new IGetter<IPage<T>>(){ public IPage<T> get() { return getCurrentPage().last(); } },
		new IGetter<String>(){ public String get() { return "Loading last page..."; } },
		"images/navigation/04-last.png", "Go to last page", KeyEvent.VK_END);
    }

    public Command<List<T>> getPrev() {
	return prev;
    }

    public Command<List<T>> getNext() {
	return next;
    }

    public IPage<T> getCurrentPage() {
	return currentPage;
    }

    /**
     * This methods reloads the a table model with the data from current page. Its solo purpose is to populate the table model upon setting up the current page for the first time.
     */
    private void load() {
	SwingUtilitiesEx.invokeLater(new Runnable() {
	    @Override
	    public void run() {
		getPageController().loadPage(getCurrentPage());
		enableActions();
	    }
	});
    }

    public void setCurrentPage(final IPage<T> currentPage) {
	this.currentPage = currentPage;
	load();
	feedback();
    }

    /**
     * Set the current page for this {@link Paginator} without loading it in to the {@link IPageController}.
     *
     * @param currentPage
     */
    public void synchronizeWithView(final IPage<T> currentPage) {
	this.currentPage = currentPage;
	enableActions();
	feedback();
    }

    /**
     * Enables navigation actions based on the current page.
     */
    public void enableActions() {
	enable(prev, getCurrentPage() != null && getCurrentPage().hasPrev(), false);
	enable(next, getCurrentPage() != null && getCurrentPage().hasNext(), false);
	enable(first, prev.isEnabled(), false);
	enable(last, next.isEnabled(), false);
	if (feedback != null) {
	    feedback.enableFeedback(true);
	}
    }

    /**
     * Disables all navigation actions, which is required while data loading is in progress.
     */
    public void disableActions() {
	enable(first, false, true);
	enable(prev, false, true);
	enable(next, false, true);
	enable(last, false, true);
	if (feedback != null) {
	    feedback.enableFeedback(false);
	}
    }

    private void enable(final BlockingLayerCommand<List<T>> action, final boolean enabled, final boolean locked){
	if (action != null){
	    action.setEnabled(enabled, locked);
	}
    }

    private IPageController getPageController() {
	return pageController;
    }

    public Command<List<T>> getFirst() {
	return first;
    }

    public Command<List<T>> getLast() {
	return last;
    }

    private void feedback() {
	if (feedback != null) {
	    SwingUtilitiesEx.invokeLater(new Runnable() {
		@Override
		public void run() {
		    feedback.feedback(getCurrentPage());
		}
	    });
	}
    }
}
