package ua.com.fielden.platform.swing.review.development;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.events.ReviewEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IReview;
import ua.com.fielden.platform.swing.review.report.interfaces.ReviewEventListener;
import ua.com.fielden.platform.swing.view.BasePanel;

public class EntityCentre<T extends AbstractEntity> extends BasePanel implements IReview {

    private static final long serialVersionUID = -8984113615241551583L;

    private final EntityCentreModel<T> model;

    private final BlockingIndefiniteProgressLayer progressLayer;

    private final Action configureAction, saveAction, saveAsAction, removeAction;

    public EntityCentre(final EntityCentreModel<T> model, final BlockingIndefiniteProgressLayer progressLayer){
	this.model = model;
	this.progressLayer = progressLayer;
	this.configureAction = createConfigureAction();
	this.saveAction = createSaveAction();
	this.saveAsAction = createSaveAsAction();
	this.removeAction = createRemoveAction();

	initView();
    }

    protected Action createRemoveAction() {
	return createReviewAction("Remove", ReviewAction.PRE_REMOVE, ReviewAction.REMOVE, ReviewAction.POST_REMOVE);
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

    protected void initView(){

	//Creating the main components of the entity centre.
	//	final JComponent toolBar = createToolBar();
	//	final JComponent criteriaPanel = createCriteriaPanel();
	//	final JComponent actionPanel = createActionPanel();
	//	final JComponent review = createReview();
	//
	//	//Setting the entity centre components' layout.
	//	final String rowConstraints = (toolBar == null ? "" : "[fill]") + (criteriaPanel == null ? "" : "[fill]")
	//	/*                  */+(actionPanel == null ? "" : "[fill]") + (review == null ? "" : "[:400:, fill, grow]");
	//
	//
	//	setLayout(new MigLayout("fill, insets 5", "[:400: ,fill, grow]", isEmpty(rowConstraints) ? "[fill, grow]" : rowConstraints));
	//
	//	add(toolBar, "wrap");
	//	add(criteriaPanel, "wrap");
	//	add(actionPanel, "wrap");
	//	add(review);

	//	String rowConstraints = "";
	//	final List<JComponent> components = new ArrayList<JComponent>();
	//
	//	if (actionPanel != null && actionPanel.getComponentCount() > 0) {
	//	    rowConstraints += "[fill]";
	//	    components.add(actionPanel);
	//	}
	//	if (criteriaPanel != null) {
	//	    rowConstraints += "[fill]";
	//	    components.add(criteriaPanel);
	//	}
	//	components.add(buttonPanel);
	//	components.add(getProgressLayer());
	//	rowConstraints += "[fill][:400:, fill, grow]";
	//	setLayout(new MigLayout("fill, insets 5", "[:400:, fill, grow]", rowConstraints));
	//	for (int componentIndex = 0; componentIndex < components.size() - 1; componentIndex++) {
	//	    add(components.get(componentIndex), "wrap");
	//	}
	//	add(components.get(components.size() - 1));
    }

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
		return notifyReviewAction(new ReviewEvent(EntityCentre.this, preAction));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		notifyReviewAction(new ReviewEvent(EntityCentre.this, action));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		notifyReviewAction(new ReviewEvent(EntityCentre.this, postAction));
		super.postAction(value);
	    }

	};
    }

    protected boolean notifyReviewAction(final ReviewEvent ev) {
	// Guaranteed to return a non-null array
	final ReviewEventListener[] listeners = getListeners(ReviewEventListener.class);
	// Process the listeners last to first, notifying
	// those that are interested in this event
	boolean result = true;

	for (final ReviewEventListener listener : listeners) {
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

    public final Action getSaveAsAction(){
	return saveAsAction;
    }

    public final Action getRemoveAction() {
	return removeAction;
    }

    @Override
    public void addReviewEventListener(final ReviewEventListener l) {
	listenerList.add(ReviewEventListener.class, l);
    }

    @Override
    public void removeReviewEventListener(final ReviewEventListener l) {
	listenerList.remove(ReviewEventListener.class, l);
    }

    @Override
    public String getInfo() {
	return "Entity centre";
    }

    public EntityCentreModel<T> getModel() {
	return model;
    }

}
