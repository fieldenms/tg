package ua.com.fielden.platform.swing.review.report.analysis.configuration;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.actions.Command;
import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationModel;
import ua.com.fielden.platform.swing.review.report.events.AnalysisConfigurationEvent;
import ua.com.fielden.platform.swing.review.report.events.AnalysisConfigurationEvent.AnalysisConfigurationAction;
import ua.com.fielden.platform.swing.review.report.interfaces.IAnalysisConfigurationEventListener;

public abstract class AbstractAnalysisConfigurationModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractConfigurationModel {

    private final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria;

    /**
     * The page holder for this analysis.
     */
    private final PageHolder pageHolder;

    /**
     * Analysis related actions:
     * <ul>
     * <li> save - saves the analysis configuration.</li>
     * <li> remove - removes the analysis configuration.</li>
     * </ul>
     *
     */
    private final Action save, remove;

    /**
     * The name of the analysis.
     */
    private final String name;

    public AbstractAnalysisConfigurationModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria, final String name){
	this.criteria = criteria;
	this.name = name;
	this.pageHolder = new PageHolder();
	this.save = createSaveAction();
	this.remove = createRemoveAction();
    }

    /**
     * Saves this analysis configuration.
     */
    public void save(){
	save.actionPerformed(null);
    }

    /**
     * Removes this analysis configuration.
     */
    public void remove(){
	remove.actionPerformed(null);
    }

    /**
     * Returns the {@link PageHolder} instance for this analysis configuration view.
     *
     * @return
     */
    public PageHolder getPageHolder() {
	return pageHolder;
    }

    /**
     * Returns the centres {@link EntityQueryCriteria} instance.
     *
     * @return
     */
    public EntityQueryCriteria<CDTME, T, IEntityDao<T>> getCriteria() {
	return criteria;
    }

    /**
     * Returns the name for this analysis.
     *
     * @return
     */
    public String getName() {
	return name;
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
    public void removeCentreConfigurationEventListener(final IAnalysisConfigurationEventListener l){
	listenerList.remove(IAnalysisConfigurationEventListener.class, l);
    }

    private Action createSaveAction() {
	return new Command<Void>("Save") {

	    private static final long serialVersionUID = 7912294028797678105L;

	    @Override
	    protected boolean preAction() {
		final boolean result= super.preAction();
		if(!result){
		    return false;
		}
		return fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationModel.this, AnalysisConfigurationAction.PRE_SAVE));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		getCriteria().getCentreDomainTreeMangerAndEnhancer().acceptAnalysisManager(getName());
		fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationModel.this, AnalysisConfigurationAction.SAVE));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationModel.this, AnalysisConfigurationAction.POST_SAVE));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationModel.this, AnalysisConfigurationAction.SAVE_FAILED));
	    }
	};
    }

    private Action createRemoveAction() {
	return new Command<Void>("Remove") {

	    private static final long serialVersionUID = -1316746113497694217L;

	    @Override
	    protected boolean preAction() {
		final boolean result = super.preAction();
		if(!result){
		    return false;
		}
		return fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationModel.this, AnalysisConfigurationAction.PRE_REMOVE));
	    }

	    @Override
	    protected Void action(final ActionEvent e) throws Exception {
		getCriteria().getCentreDomainTreeMangerAndEnhancer().removeAnalysisManager(getName());
		fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationModel.this, AnalysisConfigurationAction.REMOVE));
		return null;
	    }

	    @Override
	    protected void postAction(final Void value) {
		super.postAction(value);
		fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationModel.this, AnalysisConfigurationAction.POST_REMOVE));
	    }

	    @Override
	    protected void handlePreAndPostActionException(final Throwable ex) {
		super.handlePreAndPostActionException(ex);
		fireAnalysisConfigurationEvent(new AnalysisConfigurationEvent(AbstractAnalysisConfigurationModel.this, AnalysisConfigurationAction.REMOVE_FAILED));
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
