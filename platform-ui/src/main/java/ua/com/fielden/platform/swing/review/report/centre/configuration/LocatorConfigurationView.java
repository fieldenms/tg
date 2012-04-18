package ua.com.fielden.platform.swing.review.report.centre.configuration;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JOptionPane;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.centre.SingleAnalysisEntityLocator;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.report.events.AbstractConfigurationViewEvent;
import ua.com.fielden.platform.swing.review.report.events.LocatorConfigurationEvent;
import ua.com.fielden.platform.swing.review.report.events.LocatorConfigurationEvent.LocatorConfigurationAction;
import ua.com.fielden.platform.swing.review.report.events.LocatorEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IAbstractConfigurationViewEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ILocatorConfigurationEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ILocatorEventListener;
import ua.com.fielden.platform.swing.review.report.locator.wizard.EntityLocatorWizard;
import ua.com.fielden.platform.utils.ResourceLoader;

public class LocatorConfigurationView<T extends AbstractEntity<?>, R extends AbstractEntity<?>> extends AbstractConfigurationView<SingleAnalysisEntityLocator<T>, EntityLocatorWizard<T, R>> {

    private static final long serialVersionUID = 7422543091832971730L;

    /**
     * Actions those allows to save, save as default and load default locator's configuration. There are also actions those are use to save or save as default locator's configuration when it is closing.
     */
    private final Action save, saveAsDefault, loadDefault;

    /**
     * Determines whether this locator is multiple selection or single selection.
     */
    private final boolean isMultipleSelection;

    public LocatorConfigurationView(final LocatorConfigurationModel<T, R> model, final BlockingIndefiniteProgressLayer progressLayer, final boolean isMultipleSelection) {
	super(model, progressLayer);
	addOpenEventListener(createOpenEventListener());
	this.isMultipleSelection = isMultipleSelection;
	this.save = createSaveAction();
	this.saveAsDefault = createSaveAsDefaultAction();
	this.loadDefault = createLoadDefaultAction();
    }

    @Override
    public String getInfo() {
	return "Locator configuration panel.";
    }

    public void addLocatorEventListener(final ILocatorEventListener l){
	listenerList.add(ILocatorEventListener.class, l);
    }

    public void removeLocatorEventListener(final ILocatorEventListener l){
	listenerList.remove(ILocatorEventListener.class, l);
    }

    public boolean isMultipleSelection() {
	return isMultipleSelection;
    }

    /**
     * Returns the "save" action.
     * 
     * @return
     */
    public Action getSave() {
	return save;
    }

    /**
     * Returns the "save as default" action.
     * 
     * @return
     */
    public Action getSaveAsDefault() {
	return saveAsDefault;
    }

    /**
     * Returns the "load default" action.
     * 
     * @return
     */
    public Action getLoadDefault() {
	return loadDefault;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final LocatorConfigurationModel<T, R> getModel() {
	return (LocatorConfigurationModel<T, R>)super.getModel();
    }

    @Override
    protected final SingleAnalysisEntityLocator<T> createConfigurableView() {
	final SingleAnalysisEntityLocator<T> entityLocator = new SingleAnalysisEntityLocator<T>(getModel().createEntityCentreModel(), this, isMultipleSelection);
	entityLocator.addLocatorEventListener(new ILocatorEventListener() {

	    @Override
	    public void locatorActionPerformed(final LocatorEvent event) {
		fireLocatorEvent(event);
	    }
	});
	return entityLocator;
    }

    /**
     * Registers the {@link ILocatorConfigurationEventListener} to listen the locator configuration event.
     *
     * @param l
     */
    public void addLocatorConfigurationEventListener(final ILocatorConfigurationEventListener l){
	listenerList.add(ILocatorConfigurationEventListener.class, l);
    }

    /**
     * Removes the specified {@link ILocatorConfigurationEventListener} from the list of registered listeners.
     *
     * @param l
     */
    public void removeLocatorConfigurationEventListener(final ILocatorConfigurationEventListener l){
	listenerList.remove(ILocatorConfigurationEventListener.class, l);
    }

    @Override
    protected final EntityLocatorWizard<T, R> createWizardView() {
	final EntityLocatorWizard<T, R> newWizardView = new EntityLocatorWizard<T, R>(this, getModel().createDomainTreeEditorModel());
	return newWizardView;
    }

    private void fireLocatorEvent(final LocatorEvent event){
	for(final ILocatorEventListener listener : listenerList.getListeners(ILocatorEventListener.class)){
	    listener.locatorActionPerformed(event);
	}
    }

    //    /**
    //     * Returns specific {@link IReviewEventListener} for the locator.
    //     *
    //     * @return
    //     */
    //    private IReviewEventListener createLocatorEventListener() {
    //	return new IReviewEventListener() {
    //
    //	    @Override
    //	    public boolean configureActionPerformed(final ReviewEvent e) {
    //		switch (e.getReviewAction()) {
    //		case CONFIGURE:
    //		    getModel().locatorManager.freezeLocatorManager(getModel().rootType, getModel().name);
    //		    break;
    //		}
    //		return true;
    //	    }
    //	};
    //    }

    private IAbstractConfigurationViewEventListener createOpenEventListener() {
	return new IAbstractConfigurationViewEventListener() {

	    @Override
	    public Result abstractConfigurationViewEventPerformed(final AbstractConfigurationViewEvent event) {
		switch (event.getEventAction()) {
		case OPEN:
		    getModel().refresh();
		    if(getModel().getLocator() == null){
			return new Result(LocatorConfigurationView.this, new IllegalStateException("The locator can not be initialized!"));
		    }
		    return getModel().canSetMode(ReportMode.REPORT);

		default:
		    return Result.successful(LocatorConfigurationView.this);
		}
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
		return fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationView.this, LocatorConfigurationAction.PRE_SAVE));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		getModel().save();
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationView.this, LocatorConfigurationAction.SAVE));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		open();
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationView.this, LocatorConfigurationAction.POST_SAVE));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationView.this, LocatorConfigurationAction.SAVE_FAILED));
	    }
	};
    }

    private Action createSaveAsDefaultAction() {
	return new BlockingLayerCommand<Void>("Save As Default", getProgressLayer()) {

	    private static final long serialVersionUID = 7462084429292050025L;

	    @Override
	    protected boolean preAction() {
		final boolean result= super.preAction();
		if(!result){
		    return false;
		}
		return fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationView.this, LocatorConfigurationAction.PRE_SAVE_AS_DEFAULT));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		getModel().saveGlobally();
		getModel().save();
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationView.this, LocatorConfigurationAction.SAVE_AS_DEFAULT));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		open();
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationView.this, LocatorConfigurationAction.POST_SAVE_AS_DEFAULT));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationView.this, LocatorConfigurationAction.SAVE_AS_DEFAULT_FAILED));
	    }
	};
    }

    private Action createLoadDefaultAction() {
	return new Command<Void>("Load Default") {

	    private static final long serialVersionUID = -1337109555032877767L;

	    @Override
	    protected boolean preAction() {
		final boolean result= super.preAction();
		if(!result){
		    return false;
		}
		final int option = JOptionPane.showConfirmDialog(null, "All changes will be lost. Would you like to continue?", "Load default entity locator configuration", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, ResourceLoader.getIcon("images/tg-icon.png"));
		if(option == JOptionPane.NO_OPTION){
		    return false;
		}
		return fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationView.this, LocatorConfigurationAction.PRE_LOAD_DEFAULT));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		getModel().discard();
		getModel().reset();
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationView.this, LocatorConfigurationAction.LOAD_DEFAULT));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		open();
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationView.this, LocatorConfigurationAction.POST_LOAD_DEFAULT));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireLocatorConfigurationEvent(new LocatorConfigurationEvent(LocatorConfigurationView.this, LocatorConfigurationAction.LOAD_DEFAULT_FAILED));
	    }
	};
    }

    /**
     * Iterates through the list of {@link ILocatorConfigurationEventListener} listeners and delegates the event to every listener.
     *
     * @param event
     *
     * @return
     */
    private boolean fireLocatorConfigurationEvent(final LocatorConfigurationEvent event){
	boolean result = true;
	for(final ILocatorConfigurationEventListener listener : listenerList.getListeners(ILocatorConfigurationEventListener.class)){
	    result &= listener.locatorConfigurationEventPerformed(event);
	}
	return result;
    }
}
