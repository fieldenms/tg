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
import ua.com.fielden.platform.swing.review.report.ReportMode;
import ua.com.fielden.platform.swing.review.report.analysis.customiser.IAnalysisCustomiser;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReview;
import ua.com.fielden.platform.swing.review.report.analysis.wizard.AnalysisWizardView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationView;
import ua.com.fielden.platform.swing.review.report.events.AnalysisConfigurationEvent;
import ua.com.fielden.platform.swing.review.report.events.AnalysisConfigurationEvent.AnalysisConfigurationAction;
import ua.com.fielden.platform.swing.review.report.events.SelectionEvent;
import ua.com.fielden.platform.swing.review.report.interfaces.IAnalysisConfigurationEventListener;
import ua.com.fielden.platform.swing.review.report.interfaces.ISelectionEventListener;

/**
 * The base class for all type of analysis.
 *
 * @author TG Team
 *
 * @param <T> - The entity type for which this analysis was created.
 * @param <CDTME> - The type of {@link ICentreDomainTreeManagerAndEnhancer} that represent the centre/locator that owns this analysis.
 * @param <ADTM> - The type of {@link IAbstractAnalysisDomainTreeManager} that holds information for this analysis.
 * @param <LDT> - The type of data that this analysis returns after it's execution.
 * @param <VT> - The type of {@link AbstractAnalysisReview} that represent the analysis view.
 */
public abstract class AbstractAnalysisConfigurationView<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer, ADTME extends IAbstractAnalysisDomainTreeManager, LDT, VT extends AbstractAnalysisReview<T, CDTME, ADTME, LDT>> extends AbstractConfigurationView<VT, AnalysisWizardView<T, CDTME>> {

    private static final long serialVersionUID = -7493238859906828458L;

    /**
     * The entity centre that owns this analysis.
     */
    private final AbstractEntityCentre<T, CDTME> owner;

    /**
     * Holds all details frame associated with entity and details report.
     */
    private final Map<Object, DetailsFrame> detailsCache;

    private final IAnalysisCustomiser<VT> analysisCustomiser;

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
	    final IAnalysisCustomiser<VT> analysisCustomiser, //
	    final Map<Object, DetailsFrame> detailsCache, //
	    final AbstractEntityCentre<T, CDTME> owner, //
	    final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
	this.detailsCache = detailsCache;
	this.analysisCustomiser = analysisCustomiser;
	this.owner = owner;
	this.save = createSaveAction();
	this.remove = createRemoveAction();
	addSelectionEventListener(createSelectionListener());
    }

    public IAnalysisCustomiser<VT> getAnalysisCustomiser() {
	return analysisCustomiser;
    }

    /**
     * Returns the details cache.
     *
     * @return
     */
    public Map<Object, DetailsFrame> getDetailsCache() {
	return detailsCache;
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
    public final AbstractEntityCentre<T, CDTME> getOwner() {
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
	    getDetailsCache().put(frame.getAssociatedEntity(), frame);
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
	return Collections.unmodifiableList(new ArrayList<DetailsFrame>(getDetailsCache().values()));
    }

    /**
     * Returns pop up window associated with this analysis report and specified object
     *
     * @param associatedObject
     * @return
     */
    public final DetailsFrame getDetailsFrame(final Object associatedObject) {
	return getDetailsCache().get(associatedObject);
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
