package ua.com.fielden.platform.swing.review.report.centre.configuration;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.centre.wizard.EntityCentreWizard;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.report.events.AbstractConfigurationViewEvent;
import ua.com.fielden.platform.swing.review.report.events.CentreConfigurationEvent;
import ua.com.fielden.platform.swing.review.report.events.CentreConfigurationEvent.CentreConfigurationAction;
import ua.com.fielden.platform.swing.review.report.interfaces.IAbstractConfigurationViewEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ICentreConfigurationEventListener;
import ua.com.fielden.platform.swing.savereport.SaveReportDialog;
import ua.com.fielden.platform.swing.savereport.SaveReportDialogModel;
import ua.com.fielden.platform.swing.savereport.SaveReportOptions;

public abstract class CentreConfigurationView<T extends AbstractEntity<?>, C extends AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer>> extends AbstractConfigurationView<C, EntityCentreWizard<T, C>> {

    private static final long serialVersionUID = -2895046742734467746L;

    private final Action save, saveAs, remove;

    /**
     * Initialises this {@link CentreConfigurationView} instance with specified model and progress layer.
     *
     * @param model
     * @param progressLayer
     */
    public CentreConfigurationView(final CentreConfigurationModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
	addOpenEventListener(createCentreOpenEventListener());
	this.save = createSaveAction();
	this.saveAs = createSaveAsAction();
	this.remove = createRemoveAction();
    }

    @Override
    public String getInfo() {
	return "Centre configuration panel.";
    }

    @SuppressWarnings("unchecked")
    @Override
    public final CentreConfigurationModel<T> getModel() {
	return (CentreConfigurationModel<T>)super.getModel();
    }

    @Override
    public final ICloseGuard canClose() {
	final ICloseGuard closeGuard = super.canClose();
	if(closeGuard != null){
	    return closeGuard;
	}
	if(getModel().getEntityCentreManager() == null){
	    return null;
	}
	final String title = StringUtils.isEmpty(getModel().getName()) ? TitlesDescsGetter.getEntityTitleAndDesc(getModel().getEntityType()).getKey() : getModel().getName();
	boolean isChanged = getModel().isChanged();
	final boolean wasFreezed = getModel().isFreezed();
	boolean isFreezed = wasFreezed;
	if(!isChanged && wasFreezed){
	    getModel().discard();
	    isFreezed = false;
	    isChanged = getModel().isChanged();
	}
	if(isChanged){
	    switch(JOptionPane.showConfirmDialog(null, "Would you like to save changes"
		    + (!StringUtils.isEmpty(title) ? " for the " + title : "") + " before closing?", "Save report", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)){
		    case JOptionPane.YES_OPTION:
			closeSaveAction();
			return null;
		    case JOptionPane.NO_OPTION:
			closeDiscardAction();
			return null;
		    case JOptionPane.CANCEL_OPTION:
			createCloseCancelAction(wasFreezed).actionPerformed(null);
			return this;
	    };
	}
	if(isFreezed){
	    getModel().discard();
	}
	return null;
    }

    /**
     * Returns the save action.
     *
     * @return
     */
    public Action getSave() {
	return save;
    }

    /**
     * Returns the save as action.
     *
     * @return
     */
    public Action getSaveAs() {
	return saveAs;
    }

    /**
     * Returns the remove action.
     *
     * @return
     */
    public Action getRemove() {
	return remove;
    }

    /**
     * Registers the {@link ICentreConfigurationEventListener} to listen the centre configuration event.
     *
     * @param l
     */
    public void addCentreConfigurationEventListener(final ICentreConfigurationEventListener l){
	listenerList.add(ICentreConfigurationEventListener.class, l);
    }

    /**
     * Removes the specified {@link ICentreConfigurationEventListener} from the list of registered listeners.
     *
     * @param l
     */
    public void removeCentreConfigurationEventListener(final ICentreConfigurationEventListener l){
	listenerList.remove(ICentreConfigurationEventListener.class, l);
    }

    @Override
    protected EntityCentreWizard<T, C> createWizardView() {
	return new EntityCentreWizard<T, C>(this, getModel().createDomainTreeEditorModel());
    }

    /**
     * Saves the entity centre configuration when closing.
     *
     * @return
     */
    private void closeSaveAction() {
	if(getModel().isFreezed()){
	    getModel().save();
	}
	getModel().save();
    }

    /**
     * Discards the entity centre's changes when closing.
     *
     * @return
     */
    private void closeDiscardAction(){
	if(getModel().isFreezed()){
	    getModel().discard();
	}
	getModel().discard();
    }

    /**
     * This action cancels the closing operation.
     *
     * @param shouldFreez
     * @return
     */
    private Action createCloseCancelAction(final boolean shouldFreez){
	return new BlockingLayerCommand<Void>("Cancel", getProgressLayer()) {

	    private static final long serialVersionUID = 6219947950780861547L;

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		if(shouldFreez){
		    getModel().freez();
		}
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		if(shouldFreez){
		    getModel().setMode(ReportMode.WIZARD);
		}
	    }
	};
    }

    private Action createRemoveAction() {
	return new BlockingLayerCommand<Void>("Remove", getProgressLayer()) {

	    private static final long serialVersionUID = -1316746113497694217L;

	    @Override
	    protected boolean preAction() {
		final boolean result = super.preAction();
		if(!result){
		    return false;
		}
		return fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationView.this, null, null, CentreConfigurationAction.PRE_REMOVE));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		getModel().remove();
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationView.this, null, null, CentreConfigurationAction.REMOVE));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationView.this, null, null, CentreConfigurationAction.POST_REMOVE));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationView.this, null, ex, CentreConfigurationAction.REMOVE_FAILED));
	    }
	};
    }

    private Action createSaveAsAction() {
	return new BlockingLayerCommand<Void>("Save As", getProgressLayer()) {

	    private static final long serialVersionUID = -1316746113497694217L;

	    private String saveAsName = null;

	    private final SaveReportDialog saveReportDialog = new SaveReportDialog(new SaveReportDialogModel<T>(getModel()));

	    @Override
	    protected boolean preAction() {
		final boolean result= super.preAction();
		if(!result){
		    return false;
		}
		final boolean shouldSave = SaveReportOptions.APPROVE.equals(saveReportDialog.showDialog());
		if(shouldSave){
		    saveAsName = saveReportDialog.getEnteredFileName();
		    return fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationView.this, saveAsName, null, CentreConfigurationAction.PRE_SAVE_AS));
		} else {
		    saveAsName = null;
		    return false;
		}
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		getModel().saveAs(saveAsName);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationView.this, saveAsName, null, CentreConfigurationAction.SAVE_AS));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationView.this, saveAsName, null, CentreConfigurationAction.POST_SAVE_AS));

	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationView.this, saveAsName, ex, CentreConfigurationAction.SAVE_AS_FAILED));
	    }

	};
    }

    private Action createSaveAction() {
	return new BlockingLayerCommand<Void>("Save", getProgressLayer()) {

	    private static final long serialVersionUID = 7912294028797678105L;

	    @Override
	    protected boolean preAction() {
		final boolean result= super.preAction();
		if(!result){
		    return false;
		}
		return fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationView.this, null, null, CentreConfigurationAction.PRE_SAVE));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		getModel().save();
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationView.this, null, null, CentreConfigurationAction.SAVE));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationView.this, null, null, CentreConfigurationAction.POST_SAVE));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireCentreConfigurationEvent(new CentreConfigurationEvent(CentreConfigurationView.this, null, ex, CentreConfigurationAction.SAVE_FAILED));
	    }
	};
    }


    /**
     * Creates the open event listener that determines whether centre can be opened in report mode or not
     *
     * @return
     */
    private IAbstractConfigurationViewEventListener createCentreOpenEventListener() {
	return new IAbstractConfigurationViewEventListener() {

	    @Override
	    public Result abstractConfigurationViewEventPerformed(final AbstractConfigurationViewEvent event) {
		switch (event.getEventAction()) {
		case OPEN:
		    ICentreDomainTreeManager cdtm = getModel().getEntityCentreManager();
		    if(cdtm == null){
			getModel().initEntityCentreManager();
			cdtm = getModel().getEntityCentreManager();
		    }
		    if(cdtm == null){
			return new Result(CentreConfigurationView.this, new IllegalStateException("The centre can not be initialized!"));
		    }
		    return getModel().canSetMode(ReportMode.REPORT);

		default:
		    return Result.successful(CentreConfigurationView.this);
		}
	    }
	};
    }

    /**
     * Iterates through the list of {@link ICentreConfigurationEventListener} listeners and delegates the event to every listener.
     *
     * @param event
     *
     * @return
     */
    private boolean fireCentreConfigurationEvent(final CentreConfigurationEvent event){
	boolean result = true;
	for(final ICentreConfigurationEventListener listener : listenerList.getListeners(ICentreConfigurationEventListener.class)){
	    result &= listener.centerConfigurationEventPerformed(event);
	}
	return result;
    }
}
