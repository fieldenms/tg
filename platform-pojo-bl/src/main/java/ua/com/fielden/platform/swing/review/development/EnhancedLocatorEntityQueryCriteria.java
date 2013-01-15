package ua.com.fielden.platform.swing.review.development;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.lang.reflect.Field;
import java.util.List;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.dao.QueryExecutionModel.Builder;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToResultTickManager;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.SearchBy;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.swing.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

public class EnhancedLocatorEntityQueryCriteria<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends EntityQueryCriteria<ILocatorDomainTreeManagerAndEnhancer, T, DAO> {

    private static final long serialVersionUID = -9199540944743417928L;

    @Inject
    public EnhancedLocatorEntityQueryCriteria(final IValueMatcherFactory valueMatcherFactory, final IGeneratedEntityController generatedEntityController, final ISerialiser serialiser, final ICompanionObjectFinder controllerProvider) {
	super(valueMatcherFactory, generatedEntityController, serialiser, controllerProvider);
    }

    @SuppressWarnings("unchecked")
    public final List<T> runLocatorQuery(final int resultSize, final Object kerOrDescValue, final fetch<?> fetch, final Pair<String, Object>... otherPropValues) {
	final Class<?> root = getEntityClass();
	final IAddToResultTickManager tickManager = getCentreDomainTreeMangerAndEnhancer().getSecondTick();
	final IDomainTreeEnhancer enhancer = getCentreDomainTreeMangerAndEnhancer().getEnhancer();
	final SearchBy searchBy = getCentreDomainTreeMangerAndEnhancer().getSearchBy();
	ICompoundCondition0<T> compondCondition = null;

	switch (searchBy) {
	case KEY: {
	    // Entity's key may have a composite nature.
	    // In order to support more intuitive autocompletion for such entities, it is necessary to enhance matching query
	    final Pair<Class<?>, String> pair = PropertyTypeDeterminator.transform(getManagedType(), AbstractEntity.KEY);
	    if (DynamicEntityKey.class != AnnotationReflector.getKeyType(pair.getKey())) { // the key is not composite
		compondCondition = where().//
		prop(EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(getManagedType(), AbstractEntity.KEY).getConditionBuildingName()).//
		iLike().val(kerOrDescValue); // key matching
	    } else { // the key is composite and thus it needs special handling
		final List<Field> keyMembers = Finder.getKeyMembers(pair.getKey());
		final String additionalSearchProp = keyMembers.get(keyMembers.size() - 1).getName();
		compondCondition = where().//
		begin().//
		/*  */prop(EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(getManagedType(), AbstractEntity.KEY).getConditionBuildingName()).//
		/*  */iLike().val(kerOrDescValue).or().// key matching
		/*  */prop(EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(getManagedType(), additionalSearchProp).getConditionBuildingName()).//
		/*  */iLike().val(kerOrDescValue).// last key member matching
		end();
	    }
	    break;
	}
	case DESC:
	    compondCondition = where().prop(EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(getManagedType(), AbstractEntity.DESC).getConditionBuildingName()).iLike().val(kerOrDescValue);
	    break;
	case DESC_AND_KEY: {
	    // Entity's key may have a composite nature.
	    // In order to support more intuitive autocompletion for such entities, it is necessary to enhance matching query
	    final Pair<Class<?>, String> pair = PropertyTypeDeterminator.transform(getManagedType(), AbstractEntity.KEY);
	    if (DynamicEntityKey.class != AnnotationReflector.getKeyType(pair.getKey())) { // the key is not composite
		compondCondition = where().begin().//
		/*  */prop(EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(getManagedType(), AbstractEntity.KEY).getConditionBuildingName()).
		/*  */iLike().val(kerOrDescValue).or().// key matching
		/*  */prop(EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(getManagedType(), AbstractEntity.DESC).getConditionBuildingName()).
		/*  */iLike().val(kerOrDescValue).// desc matching
		end();
	    } else {
		final List<Field> keyMembers = Finder.getKeyMembers(pair.getKey());
		final String additionalSearchProp = keyMembers.get(keyMembers.size() - 1).getName();
		compondCondition = where().//
		begin().//
		/*  */prop(EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(getManagedType(), AbstractEntity.KEY).getConditionBuildingName()).//
		/*  */iLike().val(kerOrDescValue).or().// key matching
		/*  */prop(EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(getManagedType(), additionalSearchProp).getConditionBuildingName()).//
		/*  */iLike().val(kerOrDescValue).or().// last key member matching
		/*  */prop(EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(getManagedType(), AbstractEntity.DESC).getConditionBuildingName()).//
		/*  */iLike().val(kerOrDescValue).// desc matching
		end();
	    }
	    break;
	}
	}
	for (final Pair<String, Object> conditionPair : otherPropValues) {
	    compondCondition = compondCondition.and().prop(EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(getManagedType(), conditionPair.getKey())//
	    .getConditionBuildingName()).eq().val(conditionPair.getValue());
	}
	final Builder<T, EntityResultQueryModel<T>> builderModel = from(compondCondition.model())//
	.with(DynamicOrderingBuilder.createOrderingModel(getManagedType(), tickManager.orderedProperties(root)));
	return firstPage(fetch == null ? builderModel.model() : builderModel.with((fetch<T>) fetch).model(), resultSize).data();
    }

    /**
     * Initialises the query for entity locator.
     *
     * @return
     */
    private IWhere0<T> where() {
	final ICompleted<T> notOrderedQuery = DynamicQueryBuilder.createQuery(getManagedType(), createQueryProperties());
	if (notOrderedQuery instanceof IJoin) {
	    return ((IJoin<T>) notOrderedQuery).where();
	} else {
	    return ((ICompoundCondition0<T>) notOrderedQuery).and();
	}
    }
}