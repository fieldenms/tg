package ua.com.fielden.platform.entity.query;

import java.util.List;

import org.hibernate.Session;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.dao2.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.generation.DbVersion;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;


public class EntityFetcher<E extends AbstractEntity<?>> extends AbstractFetcher<E> {


    public EntityFetcher(final Session session, final EntityFactory entityFactory, final MappingsGenerator mappingsGenerator, final DbVersion dbVersion, final IFilter filter, final String username) {
	super(session, entityFactory, mappingsGenerator, dbVersion, filter, username);
    }

    /**
     * Fetches the results of the specified page based on the request of the given instance of IQueryOrderedModel.
     *
     * @param queryModel
     * @param pageNumber
     * @param pageCapacity
     * @return
     */
    @SessionRequired
    public List<E> list(final QueryExecutionModel<E> queryModel, final Integer pageNumber, final Integer pageCapacity) {
	try {
	    return instantiateFromContainers(listContainers(queryModel, pageNumber, pageCapacity), queryModel.isLightweight());
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new IllegalStateException(e);
	}
    }

    public List<E> list(final QueryExecutionModel<E> queryModel) {
	return list(queryModel, null, null);
    }

    @SessionRequired
    protected List<EntityContainer<E>> listContainers(final QueryExecutionModel<E> queryModel, final Integer pageNumber, final Integer pageCapacity) throws Exception {
	final QueryModelResult<E> modelResult = new ModelResultProducer().getModelResult(queryModel, getDbVersion(), getMappingsGenerator(), getFilter(), getUsername());
	final List<EntityContainer<E>> result = listContainersAsIs(modelResult, pageNumber, pageCapacity);
	return getEntityEnhancer().enhance(result, enhanceFetchModelWithKeyProperties(queryModel.getFetchModel(), modelResult.getResultType()));
    }

    private fetch<E> enhanceFetchModelWithKeyProperties(final fetch<E> fetchModel, final Class<E> entitiesType) {
	final fetch<E> enhancedFetchModel = fetchModel != null ? fetchModel : new fetch<E>(entitiesType);
	final List<String> keyMemberNames = Finder.getFieldNames(Finder.getKeyMembers(entitiesType));
	for (final String keyProperty : keyMemberNames) {
	    final Class propType = PropertyTypeDeterminator.determinePropertyType(entitiesType, keyProperty);
	    if (AbstractEntity.class.isAssignableFrom(propType) && !enhancedFetchModel.getFetchModels().containsKey(keyProperty)) {
		enhancedFetchModel.with(keyProperty, new fetch(propType));
	    }
	}
	return enhancedFetchModel;
    }
}