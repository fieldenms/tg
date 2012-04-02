package ua.com.fielden.platform.swing.pagination.development;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.pagination.IPage2;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.egi.models.PropertyTableModel;
import ua.com.fielden.platform.swing.pagination.model.development.IPageChangedListener;
import ua.com.fielden.platform.swing.pagination.model.development.IPageHolderChangedListener;
import ua.com.fielden.platform.swing.pagination.model.development.IPaginatorModel;
import ua.com.fielden.platform.swing.pagination.model.development.IPaginatorModel.PageNavigationPhases;
import ua.com.fielden.platform.swing.pagination.model.development.PageChangedEvent;
import ua.com.fielden.platform.swing.pagination.model.development.PageHolderChangedEvent;
import ua.com.fielden.platform.swing.utils.SwingUtilitiesEx;
import ua.com.fielden.platform.utils.ResourceLoader;

/**
 * Provides a glue code in a form of actions for an instance of {@link IPageProvider} and {@link PropertyTableModel}. Basically, what it does is creates actions corresponding to
 * first, previous, next and last that can be associated with buttons or some other navigation UI elements.
 *
 * @author TGTeam
 *
 */
public class Paginator {

    /**
     * A contract for anything that would like to receive a feedback when paginator changes the current page.
     *
     * @author TGTeam
     *
     */
    public static interface IPageChangeFeedback {
	void feedback(IPage2<?> page);

	void enableFeedback(boolean enable);
    }

    private final BlockingLayerCommand<List<? extends AbstractEntity>> prev, next, first, last;

    private final IPaginatorModel paginatorModel;

    private final IPageChangeFeedback feedback;

    public Paginator(final IPaginatorModel paginatorModel, final IPageChangeFeedback feedback, final BlockingIndefiniteProgressLayer blockingLayer) {
	this.paginatorModel = paginatorModel;
	this.feedback = feedback;
	first = createActionFirst(blockingLayer);
	prev = createActionPrev(blockingLayer);
	next = createActionNext(blockingLayer);
	last = createActionLast(blockingLayer);

	this.paginatorModel.addPageChangedListener(new IPageChangedListener() {

	    @Override
	    public void pageChanged(final PageChangedEvent e) {
		stateChanged();
	    }
	});
	this.paginatorModel.addPageHolderChangedListener(new IPageHolderChangedListener() {

	    @Override
	    public void pageHolderChanged(final PageHolderChangedEvent e) {
		stateChanged();
	    }
	});
    }

    public BlockingLayerCommand<List<? extends AbstractEntity>> getFirst() {
	return first;
    }

    public BlockingLayerCommand<List<? extends AbstractEntity>> getPrev() {
	return prev;
    }

    public BlockingLayerCommand<List<? extends AbstractEntity>> getNext() {
	return next;
    }

    public BlockingLayerCommand<List<? extends AbstractEntity>> getLast() {
	return last;
    }

    public IPageChangeFeedback getFeedback() {
	return feedback;
    }

    /** Simple closure to reduce code complexity. */
    private interface IGetter <M> { M get(); }
    /** Provides simple API to navigate page. */
    private interface IPageNavigator { void navigatePage(); }
    /** Creates action based on behaviour passed in parameters.*/
    private BlockingLayerCommand<List<? extends AbstractEntity>> createAction(final BlockingIndefiniteProgressLayer blockingLayer, final IPageNavigator pageNavigator, final IGetter<String> pageMessage, final String iconPath, final String desc, final int mnemonicKey) {
	final BlockingLayerCommand<List<? extends AbstractEntity>> action = new BlockingLayerCommand<List<? extends AbstractEntity>>("", blockingLayer) {
	    private static final long serialVersionUID = 1L;

	    @Override
	    protected boolean preAction() {
		blockingLayer.enableIncrementalLocking();
		setMessage(pageMessage.get());
		setEnableActions(false, true);
		paginatorModel.pageNavigationPhases(PageNavigationPhases.PRE_NAVIGATE);
		return super.preAction();
	    }

	    @Override
	    protected List<? extends AbstractEntity> action(final ActionEvent e) throws Exception {
		pageNavigator.navigatePage();
		final List<? extends AbstractEntity> data = paginatorModel.getCurrentPage().data();
		paginatorModel.pageNavigationPhases(PageNavigationPhases.NAVIGATE);
		return data;
	    }

	    @Override
	    protected void postAction(final List<? extends AbstractEntity> data) {
		super.postAction(data);
		//enableActions();//TODO consider whether enabling actions is needed?
		paginatorModel.pageNavigationPhases(PageNavigationPhases.POST_NAVIGATE);
	    }

	    /**
	     * After default exception handling executed, post-actions should be performed to enable all necessary buttons, unlock layer etc.
	     */
	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);

		super.postAction(null);
		setEnableActions(true, false);
		paginatorModel.pageNavigationPhases(PageNavigationPhases.PAGE_NAVIGATION_EXCEPTION);
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

    private BlockingLayerCommand<List<? extends AbstractEntity>> createActionFirst(final BlockingIndefiniteProgressLayer blockingLayer) {
	return createAction(blockingLayer,
		new IPageNavigator() { public void navigatePage() { paginatorModel.firstPage(); } },
		new IGetter<String>(){ public String get() { return "Loading first page..."; } },
		"images/navigation/01-first.png", "Go to first page", KeyEvent.VK_HOME);
    }

    private BlockingLayerCommand<List<? extends AbstractEntity>> createActionPrev(final BlockingIndefiniteProgressLayer blockingLayer) {
	return createAction(blockingLayer,
		new IPageNavigator() { public void navigatePage() { paginatorModel.prevPage(); } },
		new IGetter<String>(){ public String get() { return "Loading previous " + paginatorModel.getCurrentPage().capacity() + " records..."; } },
		"images/navigation/02-prev.png", "Go to previous page", KeyEvent.VK_PAGE_UP);
    }

    private BlockingLayerCommand<List<? extends AbstractEntity>> createActionNext(final BlockingIndefiniteProgressLayer blockingLayer) {
	return createAction(blockingLayer,
		new IPageNavigator() { public void navigatePage() { paginatorModel.nextPage(); } },
		new IGetter<String>(){ public String get() { return "Loading next " + paginatorModel.getCurrentPage().capacity() + " records..."; } },
		"images/navigation/03-next.png", "Go to next page", KeyEvent.VK_PAGE_DOWN);
    }

    private BlockingLayerCommand<List<? extends AbstractEntity>> createActionLast(final BlockingIndefiniteProgressLayer blockingLayer) {
	return createAction(blockingLayer,
		new IPageNavigator() { public void navigatePage() { paginatorModel.lastPage(); } },
		new IGetter<String>(){ public String get() { return "Loading last page..."; } },
		"images/navigation/04-last.png", "Go to last page", KeyEvent.VK_END);
    }

    /**
     * Enables or Disables all navigation actions, which is required while data loading is in progress.
     */
    public void setEnableActions(final boolean enable, final boolean locked) {
	enable(prev, enable && paginatorModel.getCurrentPage() != null && paginatorModel.getCurrentPage().hasPrev(), locked);
	enable(next, enable && paginatorModel.getCurrentPage() != null && paginatorModel.getCurrentPage().hasNext(), locked);
	enable(first, enable && prev.isEnabled(), locked);
	enable(last, enable && next.isEnabled(), locked);
	if (feedback != null) {
	    feedback.enableFeedback(enable);
	}
    }

    private void enable(final BlockingLayerCommand<List<? extends AbstractEntity>> action, final boolean enabled, final boolean locked){
	if (action != null){
	    action.setEnabled(enabled, locked);
	}
    }

    private void stateChanged(){
	SwingUtilitiesEx.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		setEnableActions(true, false);
		if (feedback != null) {
		    feedback.feedback(paginatorModel.getCurrentPage());
		}
	    }
	});
    }
}
