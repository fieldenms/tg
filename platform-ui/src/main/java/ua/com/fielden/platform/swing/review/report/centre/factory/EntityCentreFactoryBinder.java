package ua.com.fielden.platform.swing.review.report.centre.factory;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.AnalysisType;
import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * Convenient entity centre and analysis factory binder
 *
 * @author TG Team
 *
 * @param <T>
 */
public class EntityCentreFactoryBinder<T extends AbstractEntity<?>> {

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
    public EntityCentreFactoryBinder(){
	bindEntityCentreTo(new DefaultEntityCentreFactory<T>());
	bindDefaultAnalysisTo(new DefaultGridAnalysisFactory<T>());
	bindAnalysisTo(AnalysisType.SIMPLE, new DefaultChartAnalysisFactory<T>());
	bindAnalysisTo(AnalysisType.PIVOT, new DefaultPivotAnalysisFactory<T>());
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
}
