package ua.com.fielden.platform.swing.review.development;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.QueryExecutionModel.Builder;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.SearchBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

public class EnhancedLocatorEntityQueryCriteria<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends EntityQueryCriteria<ILocatorDomainTreeManagerAndEnhancer, T, DAO> {

    private static final long serialVersionUID = -9199540944743417928L;

    @Inject
    public EnhancedLocatorEntityQueryCriteria(final IValueMatcherFactory valueMatcherFactory) {
	super(valueMatcherFactory);
    }

    @SuppressWarnings("unchecked")
    public final List<T> runLocatorQuery(final int resultSize, final Object kerOrDescValue, final fetch<?> fetch, final Pair<String, Object>... otherPropValues){
	final SearchBy searchBy = getCentreDomainTreeMangerAndEnhancer().getSearchBy();
	ICompoundCondition0 compondCondition = null;
	switch(searchBy){
	case KEY:
	    compondCondition = where().prop(createNotInitialisedQueryProperty("key").getConditionBuildingName()).like().val(kerOrDescValue);
	    break;
	case DESC :
	    compondCondition = where().prop(createNotInitialisedQueryProperty("desc").getConditionBuildingName()).like().val(kerOrDescValue);
	    break;
	case DESC_AND_KEY:
	    compondCondition = where().begin().prop(createNotInitialisedQueryProperty("key").getConditionBuildingName()).like().val(kerOrDescValue)//
	    /*			       */.or().prop(createNotInitialisedQueryProperty("desc").getConditionBuildingName()).like().val(kerOrDescValue).end();
	    break;
	}
	for(final Pair<String, Object> conditionPair : otherPropValues){
	    compondCondition = compondCondition.and().prop(createNotInitialisedQueryProperty(conditionPair.getKey()).getConditionBuildingName()).eq().val(conditionPair.getValue());
	}
	final Builder<T, EntityResultQueryModel<T>> builderModel = from((EntityResultQueryModel<T>)compondCondition.model()).with(createOrderingModel());
	return firstPage(fetch == null ? builderModel.build() : builderModel.with((fetch<T>)fetch).build(), resultSize).data();
    }

    /**
     * Initialises the query for entity locator.
     * 
     * @return
     */
    private IWhere0 where(){
	final ICompleted notOrderedQuery = DynamicQueryBuilder.createQuery(getManagedType(), createQueryProperties());
	if(notOrderedQuery instanceof IJoin){
	    return ((IJoin) notOrderedQuery).where();
	} else {
	    return ((ICompoundCondition0) notOrderedQuery).and();
	}
    }
}