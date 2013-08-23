package ua.com.fielden.platform.swing.review.report.analysis.configuration;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.Action;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.actions.BlockingLayerCommand;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.model.ICloseGuard;
import ua.com.fielden.platform.swing.review.details.IDetails;
import ua.com.fielden.platform.swing.review.details.customiser.IDetailsCustomiser;
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.analysis.wizard.AnalysisWizardView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.report.events.AnalysisConfigurationEvent;
import ua.com.fielden.platform.swing.review.report.events.AnalysisConfigurationEvent.AnalysisConfigurationAction;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IAnalysisConfigurationEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;
import ua.com.fielden.platform.swing.view.ICloseHook;

/**
 * The base class for all type of analysis.
 *
 * @author TG Team
 *
 * @param <T> - The entity type for which this analysis was created.
 * @param <CDTME> - The type of {@link ICentreDomainTreeManagerAndEnhancer} that represent the centre/locator that owns this analysis.
 * @param <ADTM> - The type of {@link IAbstractAnalysisDomainTreeManager} that holds information for this analysis.
 * @param <VT> - The type of {@link AbstractAnalysisReview} that represent the analysis view.
 */
public abstract class AbstractAnalysisConfigurationView<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer, ADTME extends IAbstractAnalysisDomainTreeManager, VT extends AbstractAnalysisReview<T, CDTME, ADTME>> extends AbstractConfigurationView<VT, AnalysisWizardView<T, CDTME>> {

    private static final long serialVersionUID = -7493238859906828458L;

    /**
     * The entity centre that owns this analysis.
     */
    private final AbstractEntityCentre<T, CDTME> owner;

    private final Map<Object, DetailsFrame> detailsCache;
    private final IDetailsCustomiser detailsCustomiser;

    /**
     * Analysis related actions:
     * <ul>
     * <li> save - saves the analysis configuration.</li>
     * <li> remove - removes the analysis configuration.</li>
     * </ul>
     *
     */
    private final Action save, remove;

    public AbstractAnalysisConfigurationView(//
	    final AbstractAnalysisConfigurationModel<T, CDTME> model, //
	    final Map<Object, DetailsFrame> detailsCache, //
	    final IDetailsCustomiser detailsCustomiser, //
	    final AbstractEntityCentre<T, CDTME> owner, //
	    final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
	this.detailsCache = detailsCache;
	this.detailsCustomiser = detailsCustomiser;
	this.owner = owner;
	this.save = createSaveAction();
	this.remove = createRemoveAction();
	addSelectionEventListener(createSelectionListener());
    }

    @Override
    public void showLoadingMessage(final String msg) {
	getOwner().getOwner().getProgressLayer().setText(msg);
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
     * Returns the remove action.
     *
     * @return
     */
    public Action getRemove() {
	return remove;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AbstractAnalysisConfigurationModel<T, CDTME> getModel() {
	return (AbstractAnalysisConfigurationModel<T, CDTME>)super.getModel();
    }

    /**
     * Registers the {@link IAnalysisConfigurationEventListener} to listen the analysis configuration event.
     *
     * @param l
     */
    public void addAnalysisConfigurationEventListener(final IAnalysisConfigurationEventListener l){
	listenerList.add(IAnalysisConfigurationEventListener.class, l);
    }

    /**
     * Removes the specified {@link IAnalysisConfigurationEventListener} from the list of registered listeners.
     *
     * @param l
     */
    public void removeAnalysisConfigurationEventListener(final IAnalysisConfigurationEventListener l){
	listenerList.remove(IAnalysisConfigurationEventListener.class, l);
    }

    /**
     * Returns the entity centre that owns this analysis.
     *
     * @return
     */
    public AbstractEntityCentre<T, CDTME> getOwner() {
	return owner;
    }

    @Override
    public ICloseGuard canClose() {
	final ICloseGuard result= super.canClose();
	if(result != null){
	    return result;
	}
	return getModel().getMode() == ReportMode.WIZARD ? this : null;
    }

    @Override
    public String whyCannotClose() {
        return "Please save or cancel changes for " + getModel().getName() + " analysis";
    }

    @Override
    public void close() {
        super.close();
        for(final DetailsFrame frame : getDetailsFrames()){
            removeDetailsFrame(frame);
            frame.close();
        }
    }


    /**
     * Adds new pop up window specified with {@code frame} to this analysis report.
     *
     * @param frame
     */
    public final void addDetailsFrame(final DetailsFrame frame) {
	if(frame != null){
	    detailsCache.put(frame.getAssociatedEntity(), frame);
	}
    }

    /**
     * Removes specified pop up window.
     *
     * @param frame
     */
    public final void removeDetailsFrame(final DetailsFrame frame) {
	if (frame != null) {
	    detailsCache.remove(frame.getAssociatedEntity());
	}
    }

    /**
     * Returns list of pop up windows associated with this analysis report.
     *
     * @return
     */
    public final List<DetailsFrame> getDetailsFrames() {
	return Collections.unmodifiableList(new ArrayList<DetailsFrame>(detailsCache.values()));
    }

    /**
     * Returns pop up window associated with this analysis report and specified object.
     *
     * @param associatedObject
     * @return
     */
    public final DetailsFrame getDetailsFrame(final Object associatedObject) {
	return detailsCache.get(associatedObject);
    }

    /**
     * Creates and shows details for the specified details parameter and it's type.
     *
     * @param detailsParam
     */
    public final <DT> void showDetails(final DT detailsParam, final Class<DT> detailsParamType) {
	if(detailsCache == null || detailsCustomiser == null){
	    throw new IllegalStateException("The details can not be created for this analysis, because details cache or details customiser is absent");
	}
	new BlockingLayerCommand<Void>("Details", getProgressLayer()) {

	    private static final long serialVersionUID = 1986658954874008023L;

	    @Override
	    protected final Void action(final ActionEvent e) throws Exception {
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		DetailsFrame detailsFrame = getDetailsFrame(detailsParam);
		if (detailsFrame == null) {
		    final ICloseHook<DetailsFrame> detailsCloseHook = new ICloseHook<DetailsFrame>() {

			@Override
			public void closed(final DetailsFrame frame) {
			    removeDetailsFrame(frame);
			}
		    };
		    final IDetails<DT> details = detailsCustomiser.getDetails(detailsParamType);
		    if(details == null){
			throw new IllegalArgumentException("There are no details contract for the details parameter of " + detailsParamType.getSimpleName() + " type.");
		    }
		    detailsFrame = details.createDetailsView(detailsParam, detailsCloseHook);
		    if(detailsFrame == null){
			throw new IllegalArgumentException("The details frame can not be created for the specified details parameter " + detailsParam.toString());
		    }
		    addDetailsFrame(detailsFrame);
		}
		detailsFrame.setVisible(true);
	    }
	}.actionPerformed(null);
    }

    @Override
    protected void blockLoadingView(final boolean lock) {
	//Must first see whether owner was loaded or not.
	//If The owner is not loaded then entity centre is still loading and blocking pane is locked or unlocked,
	//Otherwise it can be locked because only analysis is loading or it configure, build, cancel was pressed.
	if(getOwner().isLoaded()) {
	    getOwner().getOwner().getProgressLayer().setLocked(lock);
	}
    }

    @Override
    protected AnalysisWizardView<T, CDTME> createWizardView() {
	return new AnalysisWizardView<T, CDTME>(this, getModel().getAnalysisManager());
    }

    /**
     * Returns the selection listener that is responsible for selecting the this analysis configurable view.
     *
     * @return
     */
    private ISelectionEventListener createSelectionListener() {
	return new ISelectionEventListener() {

	    @Override
	    public void viewWasSelected(final SelectionEvent event) {
		switch(getModel().getMode()){
		case REPORT : getPreviousView().select(); break;
		case WIZARD : getPreviousWizard().select();break;
		default:
		    break;
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
		return fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationView.this, AnalysisConfigurationAction.PRE_SAVE));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		getModel().save();
		fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationView.this, AnalysisConfigurationAction.SAVE));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationView.this, AnalysisConfigurationAction.POST_SAVE));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationView.this, AnalysisConfigurationAction.SAVE_FAILED));
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
		return fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationView.this, AnalysisConfigurationAction.PRE_REMOVE));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		getModel().remove();
		fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationView.this, AnalysisConfigurationAction.REMOVE));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationView.this, AnalysisConfigurationAction.POST_REMOVE));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationView.this, AnalysisConfigurationAction.REMOVE_FAILED));
	    }
	};
    }

    /**
     * Iterates through the list of {@link IAnalysisConfigurationEventListener} listeners and delegates the event to every listener.
     *
     * @param event
     *
     * @return
     */
    private boolean fireAnalysisConfigurationEvent(final AnalysisConfigurationEvent event){
	boolean result = true;
	for(final IAnalysisConfigurationEventListener listener : listenerList.getListeners(IAnalysisConfigurationEventListener.class)){
	    result &= listener.analysisConfigurationEventPerformed(event);
	}
	return result;
    }
}
