package ua.com.fielden.platform.swing.review.report.centre.factory;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IGlobalDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.swing.review.report.analysis.configuration.AbstractAnalysisConfigurationView;
import ua.com.fielden.platform.swing.review.report.centre.AbstractEntityCentre;
import ua.com.fielden.platform.swing.review.report.centre.configuration.CentreConfigurationView;

import com.google.inject.Inject;

/**
 * Convenient entity centre and analysis factory binder
 *
 * @author TG Team
 *
 * @param <T>
 */
public class EntityCentreFactoryBinder<T extends AbstractEntity<?>> implements IEntityCentreBuilder<T>, IAnalysisBuilder<T>{

    //Entity centre related properties.
    private final IGlobalDomainTreeManager gdtm;
    private final EntityFactory entityFactory;
    private final IEntityMasterManager masterManager;
    private final ICriteriaGenerator criteriaGenerator;

    /**
     * Represents the entity centre factory.
     */
    private IEntityCentreFactory<T> entityCentreFactory;

    /**
     * Represents the analysis factories, mapped to the analysis type.
     */
    private final Map<AnalysisType, IAnalysisFactory<T, ?>> analysisFactoryMap = new HashMap<AnalysisType, IAnalysisFactory<T,?>>();


    /**
     * Initialises {@link EntityCentreFactoryBinder} with default entity centre and analysis factories.
     */
    @Inject
    public EntityCentreFactoryBinder(//
	    final IGlobalDomainTreeManager gdtm,//
	    final EntityFactory entityFactory,//
	    final IEntityMasterManager masterManager,
	    final ICriteriaGenerator criteriaGenerator){
	this.gdtm = gdtm;
	this.entityFactory = entityFactory;
	this.masterManager = masterManager;
	this.criteriaGenerator = criteriaGenerator;
	bindEntityCentreTo(new DefaultEntityCentreFactory<T>());
	bindDefaultAnalysisTo(new DefaultGridAnalysisFactory<T>());
	bindAnalysisTo(AnalysisType.SIMPLE, new DefaultChartAnalysisFactory<T>());
	bindAnalysisTo(AnalysisType.LIFECYCLE, new DefaultLifecycleAnalysisFactory<T>());
	bindAnalysisTo(AnalysisType.PIVOT, new DefaultPivotAnalysisFactory<T>());
	bindAnalysisTo(AnalysisType.SENTINEL, new SentinelChartAnalysisFactory<T>());
    }

    /**
     * Specifies the entity centre factory.
     *
     * @param entityCentreFactory
     */
    public EntityCentreFactoryBinder<T> bindEntityCentreTo(final IEntityCentreFactory<T> entityCentreFactory){
	if(entityCentreFactory == null){
	    throw new NullPointerException("Entity centre fctory can not be null!");
	}
	this.entityCentreFactory = entityCentreFactory;
	return this;
    }

    /**
     * Returns the entity centre factory. If the entity centre factory wasn't specified
     *
     * @return
     */
    public IEntityCentreFactory<T> getEntityCentreFactory(){
	return entityCentreFactory;
    }

    /**
     * Binds analysis type to the specified {@link IAnalysisFactory} instance.
     *
     * @param analysisType
     * @param analysisFactory
     * @return
     */
    public EntityCentreFactoryBinder<T> bindAnalysisTo(final AnalysisType analysisType, final IAnalysisFactory<T, ?> analysisFactory){
	if(analysisFactory == null){
	    throw new NullPointerException("The analysis factory can not be null!");
	}
	analysisFactoryMap.put(analysisType, analysisFactory);
	return this;
    }

    /**
     * Binds the default analysis to the specified {@link IAnalysisFactory} instance.
     * (Default analysis - it's an analysis that will be open when the one opens entity centre. Also this analysis can not be closed or removed.)
     *
     * @param analysisFactory
     * @return
     */
    public EntityCentreFactoryBinder<T> bindDefaultAnalysisTo(final IAnalysisFactory<T, ?> analysisFactory){
	bindAnalysisTo(null, analysisFactory);
	return this;
    }

    /**
     * Returns the {@link IAnalysisFactory} instance associated with specified analysis type.
     *
     * @param analysisType
     * @return
     */
    public IAnalysisFactory<T, ?> getAnalysisFactoryFor(final AnalysisType analysisType){
	return analysisFactoryMap.get(analysisType);
    }

    /**
     * Returns the default analysis factory.
     * (Default analysis - it's an analysis that will be open when the one opens entity centre. Also this analysis can not be closed or removed.)
     *
     * @return
     */
    public IAnalysisFactory<T, ?> getDefaultAnalysisFactory(){
	return getAnalysisFactoryFor(null);
    }

    @Override
    public AbstractAnalysisConfigurationView<T, ICentreDomainTreeManagerAndEnhancer, ? extends IAbstractAnalysisDomainTreeManager, ?, ?> createAnalysis(//
	    final AnalysisType analysisType, //
	    final String name, //
	    final Map<Object, DetailsFrame> detailsCache,//
	    final AbstractEntityCentre<T, ICentreDomainTreeManagerAndEnhancer> owner, //
	    final EntityQueryCriteria<ICentreDomainTreeManagerAndEnhancer, T, IEntityDao<T>> criteria, //
	    final BlockingIndefiniteProgressLayer progressLayer) {
	return getAnalysisFactoryFor(analysisType).createAnalysis(owner, criteria, name, detailsCache, progressLayer);
    }

    @Override
    public CentreConfigurationView<T, ?> createEntityCentre(//
	    final Class<? extends MiWithConfigurationSupport<T>> menuItemType, //
		    final String name, //
		    final BlockingIndefiniteProgressLayer progressLayer) {
	return getEntityCentreFactory().createEntityCentre(//
		menuItemType, //
		name, //
		this, //
		gdtm, entityFactory, masterManager, criteriaGenerator,//
		progressLayer);
    }
}
