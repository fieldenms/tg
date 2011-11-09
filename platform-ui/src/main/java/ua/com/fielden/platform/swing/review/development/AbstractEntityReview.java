package ua.com.fielden.platform.swing.review.development;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.events.ReviewEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IReview;
import ua.com.fielden.platform.swing.review.report.interfaces.IReviewEventListener;
import ua.com.fielden.platform.swing.view.BasePanel;

public abstract class AbstractEntityReview<T extends AbstractEntity, DTM extends IDomainTreeManager> extends BasePanel implements IReview {

    private static final long serialVersionUID = -8984113615241551583L;

    private final AbstractEntityReviewModel<T, DTM> model;

    private final BlockingIndefiniteProgressLayer progressLayer;

    private final Action configureAction, saveAction, saveAsAction, saveAsDefaultAction, removeAction;

    public AbstractEntityReview(final AbstractEntityReviewModel<T, DTM> model, final BlockingIndefiniteProgressLayer progressLayer){
	this.model = model;
	this.progressLayer = progressLayer;
	this.configureAction = createConfigureAction();
	this.saveAction = createSaveAction();
	this.saveAsAction = createSaveAsAction();
	this.saveAsDefaultAction = createSaveAsDefaultAction();
	this.removeAction = createRemoveAction();

	initView();
    }

    protected Action createRemoveAction() {
	return createReviewAction("Remove", ReviewAction.PRE_REMOVE, ReviewAction.REMOVE, ReviewAction.POST_REMOVE);
    }

    protected Action createSaveAsDefaultAction() {
	return createReviewAction("Save as default", ReviewAction.PRE_SAVE_AS_DEFAULT, ReviewAction.SAVE_AS_DEFAULT, ReviewAction.POST_SAVE_AS_DEFAULT);
    }

    protected Action createSaveAsAction() {
	return createReviewAction("Save as", ReviewAction.PRE_SAVE_AS, ReviewAction.SAVE_AS, ReviewAction.POST_SAVE_AS);
    }

    protected Action createSaveAction() {
	return createReviewAction("Save", ReviewAction.PRE_SAVE, ReviewAction.SAVE, ReviewAction.POST_SAVE);
    }

    protected Action createConfigureAction(){
	return createReviewAction("Configure", ReviewAction.PRE_CONFIGURE, ReviewAction.CONFIGURE, ReviewAction.POST_CONFIGURE);
    }

    protected abstract void initView();

    /**
     * Creates on of the review action: configure, save, save as or remove.
     * 
     * @param name - the caption for action.
     * @param preAction
     * @param action
     * @param postAction
     * @return
     */
    private Action createReviewAction(final String name, final ReviewAction preAction, final ReviewAction action, final ReviewAction postAction){
	return new BlockingLayerCommand<Void>(name, progressLayer){

	    private static final long serialVersionUID = 4502256665545168359L;

	    @Override
	    protected boolean preAction() {
		final boolean result = super.preAction();
		if(!result){
		    return false;
		}
		return notifyReviewAction(new ReviewEvent(AbstractEntityReview.this, preAction));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		notifyReviewAction(new ReviewEvent(AbstractEntityReview.this, action));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		notifyReviewAction(new ReviewEvent(AbstractEntityReview.this, postAction));
		super.postAction(value);
	    }

	};
    }

    protected boolean notifyReviewAction(final ReviewEvent ev) {
	// Guaranteed to return a non-null array
	final IReviewEventListener[] listeners = getListeners(IReviewEventListener.class);
	// Process the listeners last to first, notifying
	// those that are interested in this event
	boolean result = true;

	for (final IReviewEventListener listener : listeners) {
	    result &= listener.configureActionPerformed(ev);
	}
	return result;
    }

    public final Action getConfigureAction(){
	return configureAction;
    }

    public final Action getSaAction(){
	return saveAction;
    }

    public final Action getSaveAsDefaultAction() {
	return saveAsDefaultAction;
    }

    public final Action getSaveAsAction(){
	return saveAsAction;
    }

    public final Action getRemoveAction() {
	return removeAction;
    }

    @Override
    public void addReviewEventListener(final IReviewEventListener l) {
	listenerList.add(IReviewEventListener.class, l);
    }

    @Override
    public void removeReviewEventListener(final IReviewEventListener l) {
	listenerList.remove(IReviewEventListener.class, l);
    }

    @Override
    public String getInfo() {
	return "Entity centre";
    }

    public AbstractEntityReviewModel<T, DTM> getModel() {
	return model;
    }

}
