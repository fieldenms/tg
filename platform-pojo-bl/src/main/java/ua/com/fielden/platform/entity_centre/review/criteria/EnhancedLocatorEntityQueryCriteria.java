package ua.com.fielden.platform.entity_centre.review.criteria;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Stream;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.IGeneratedEntityController;
import ua.com.fielden.platform.dao.QueryExecutionModel.Builder;
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
import ua.com.fielden.platform.entity_centre.exceptions.EntityCentreExecutionException;
import ua.com.fielden.platform.entity_centre.review.DynamicOrderingBuilder;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.utils.Pair;

public class EnhancedLocatorEntityQueryCriteria<T extends AbstractEntity<?>, DAO extends IEntityDao<T>> extends EntityQueryCriteria<ILocatorDomainTreeManagerAndEnhancer, T, DAO> {

    @Inject
    public EnhancedLocatorEntityQueryCriteria(final IValueMatcherFactory valueMatcherFactory, final IGeneratedEntityController generatedEntityController, final ISerialiser serialiser, final ICompanionObjectFinder controllerProvider) {
        super(valueMatcherFactory, generatedEntityController, serialiser, controllerProvider);
    }

    @SuppressWarnings("unchecked")
    public final List<T> runLocatorQuery(final int resultSize, final Object kerOrDescValue, final fetch<?> fetch, final Pair<String, Object>... otherPropValues) {
        final Class<?> root = getEntityClass();
        final IAddToResultTickManager tickManager = getCentreDomainTreeMangerAndEnhancer().getSecondTick();
        final SearchBy searchBy = getCentreDomainTreeMangerAndEnhancer().getSearchBy();
        final ICompoundCondition0<T> compondCondition;

        switch (searchBy) {
        case KEY:
            compondCondition = mkSearchByKeyCondition(kerOrDescValue);
            break;
        case DESC:
            compondCondition = where().prop(EntityQueryCriteriaUtils.createNotInitialisedQueryProperty(getManagedType(), AbstractEntity.DESC).getConditionBuildingName()).iLike().val(kerOrDescValue);
            break;
        case DESC_AND_KEY:
            compondCondition = mkSearchByDescAndKeyCondition(kerOrDescValue);
            break;
        default:
            throw new EntityCentreExecutionException(format("Unrecognized search-by option [%s].", searchBy));
        }
        final ICompoundCondition0<T> finalCondition = Stream.of(otherPropValues)
                                                        .reduce(compondCondition, 
                                                                (condition, conditionPair) -> condition.and().prop(DynamicQueryBuilder.createConditionProperty(conditionPair.getKey())).eq().iVal(conditionPair.getValue()), 
                                                                (p1, p2) -> {throw new EntityCentreExecutionException("No merging of conditions should be required.");});
        final Builder<T, EntityResultQueryModel<T>> builderModel = from(finalCondition.model())//
        .with(DynamicOrderingBuilder.createOrderingModel(getManagedType(), tickManager.orderedProperties(root)));
        return getFirstEntities(fetch == null ? builderModel.model() : builderModel.with((fetch<T>) fetch).model(), resultSize);
    }

    private ICompoundCondition0<T> mkSearchByDescAndKeyCondition(final Object kerOrDescValue) {
        final ICompoundCondition0<T> compondCondition;
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
        return compondCondition;
    }

    private ICompoundCondition0<T> mkSearchByKeyCondition(final Object kerOrDescValue) {
        final ICompoundCondition0<T> compondCondition;
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
        return compondCondition;
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