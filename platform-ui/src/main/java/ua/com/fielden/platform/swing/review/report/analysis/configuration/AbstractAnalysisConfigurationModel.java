package ua.com.fielden.platform.swing.review.report.analysis.configuration;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.pagination.model.development.PageHolder;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.configuration.AbstractConfigurationModel;

public abstract class AbstractAnalysisConfigurationModel<T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> extends AbstractConfigurationModel {

    private final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria;

    /**
     * The page holder for this analysis.
     */
    private final PageHolder pageHolder;

    /**
     * The name of the analysis.
     */
    private final String name;

    public AbstractAnalysisConfigurationModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> criteria, final String name){
	this.criteria = criteria;
	this.name = name;
	this.pageHolder = new PageHolder();
    }

    /**
     * Saves this analysis configuration.
     */
    public void save(){
	getCriteria().getCentreDomainTreeMangerAndEnhancer().acceptAnalysisManager(getName());
    }

    /**
     * Removes this analysis configuration.
     */
    public void remove(){
	getCriteria().getCentreDomainTreeMangerAndEnhancer().removeAnalysisManager(getName());
    }

    /**
     * Discards this analysis manager.
     */
    public void discard(){
	getCriteria().getCentreDomainTreeMangerAndEnhancer().discardAnalysisManager(getName());
    }

    /**
     * Returns value that determines whether this analysis is freeze or not.
     *
     * @return
     */
    public boolean isFreeze(){
	return getCriteria().getCentreDomainTreeMangerAndEnhancer().isFreezedAnalysisManager(getName());
    }

    /**
     * Freezes this analysis.
     */
    public void freeze(){
	getCriteria().getCentreDomainTreeMangerAndEnhancer().freezeAnalysisManager(getName());
    }

    /**
     * Returns the instance of {@link IAbstractAnalysisDomainTreeManagerAndEnhancer} that is associated with this analysis.
     *
     * @return
     */
    public IAbstractAnalysisDomainTreeManagerAndEnhancer getAnalysisManager(){
	return getCriteria().getCentreDomainTreeMangerAndEnhancer().getAnalysisManager(getName());
    }

    /**
     * Initialises the analysis manager for this analysis.
     *
     * @param analysisType
     */
    public void initAnalysisManager(final AnalysisType analysisType){
	getCriteria().getCentreDomainTreeMangerAndEnhancer().initAnalysisManagerByDefault(getName(), analysisType);
    }

    /**
     * Makes analysis visible or invisible.
     *
     * @param visible
     */
    public void setAnalysisVisible(final boolean visible){
	final IAbstractAnalysisDomainTreeManager adtm = getCriteria().getCentreDomainTreeMangerAndEnhancer().getAnalysisManager(getName());
	if(adtm != null){
	    adtm.setVisible(visible);
	}
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
     * Returns the name for this analysis.
     *
     * @return
     */
    public String getName() {
	return name;
    }

    /**
     * Returns the centres {@link EntityQueryCriteria} instance.
     *
     * @return
     */
    public EntityQueryCriteria<CDTME, T, IEntityDao<T>> getCriteria() {
	return criteria;
    }



}
