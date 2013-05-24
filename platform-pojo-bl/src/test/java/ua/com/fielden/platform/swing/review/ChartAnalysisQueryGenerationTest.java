package ua.com.fielden.platform.swing.review;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchOnly;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.HashSet;

import junit.framework.Assert;

import org.junit.Test;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer.AnalysisType;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisAddToAggregationTickManager;
import ua.com.fielden.platform.domaintree.centre.analyses.IAnalysisDomainTreeManager.IAnalysisAddToDistributionTickManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.testing.ClassProviderForTestingPurposes;
import ua.com.fielden.platform.domaintree.testing.TgKryoForDomainTreesTestingPurposes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.report.query.generation.AnalysisResultClass;
import ua.com.fielden.platform.report.query.generation.AnalysisResultClassBundle;
import ua.com.fielden.platform.report.query.generation.ChartAnalysisQueryGenerator;
import ua.com.fielden.platform.report.query.generation.IReportQueryGenerator;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;

import com.google.inject.Injector;

@SuppressWarnings("unchecked")
public class ChartAnalysisQueryGenerationTest {

    private static final String ALIAS = "alias_for_main_criteria_type";

    private final ISerialiser serialiser = createSerialiser(createFactory());

    private EntityFactory createFactory() {
	final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
	final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
	return injector.getInstance(EntityFactory.class);
    }

    private ISerialiser createSerialiser(final EntityFactory factory) {
	return new TgKryoForDomainTreesTestingPurposes(factory, new ClassProviderForTestingPurposes());
    }

    @SuppressWarnings("serial")
    private final ICentreDomainTreeManagerAndEnhancer cdtme = new CentreDomainTreeManagerAndEnhancer(serialiser, new HashSet<Class<?>>(){{ add(MasterDomainEntity.class); }});;
    private final IReportQueryGenerator<MasterDomainEntity> queryGenerator;

    private final Class<AbstractEntity<?>> masterKlass, slaveKlass, evenSlaveKlass, stringKeyKlass;

    {
	final IDomainTreeEnhancer dte = cdtme.getEnhancer();
	dte.addCalculatedProperty(MasterDomainEntity.class, "", "MONTH(dateProp)", "firstGroup", "firstGroup", CalculatedPropertyAttribute.NO_ATTR, "dateProp");
	dte.addCalculatedProperty(MasterDomainEntity.class, "", "SUM(integerProp)", "sumInt", "Int Summary", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dte.addCalculatedProperty(MasterDomainEntity.class, "", "AVG(integerProp)", "avgInt", "Int Average", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dte.addCalculatedProperty(MasterDomainEntity.class, "entityProp.anotherSimpleEntityProp", "SUM(integerProp)", "mutIntSum", "Integer another summary", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dte.addCalculatedProperty(MasterDomainEntity.class, "entityProp.entityProp", "DAY(dateProp)", "secondGroup", "secondGroup", CalculatedPropertyAttribute.NO_ATTR, "dateProp");
	dte.addCalculatedProperty(MasterDomainEntity.class, "entityProp.entityProp.simpleEntityProp", "SUM(integerProp)", "propIntSum", "Property int summary", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dte.addCalculatedProperty(MasterDomainEntity.class, "entityProp.entityProp.simpleEntityProp", "AVG(integerProp)", "propIntAvg", "Property Int average", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dte.addCalculatedProperty(MasterDomainEntity.class, "entityProp.entityProp.simpleEntityProp", "MIN(integerProp)", "propIntMin", "Property Int minimum", CalculatedPropertyAttribute.NO_ATTR, "integerProp");
	dte.apply();

	cdtme.initAnalysisManagerByDefault("simple analysis", AnalysisType.SIMPLE);
	cdtme.acceptAnalysisManager("simple analysis");

	final IAnalysisDomainTreeManager analysis = (IAnalysisDomainTreeManager)cdtme.getAnalysisManager("simple analysis");

	final IAnalysisAddToDistributionTickManager firstTick = analysis.getFirstTick();
	firstTick.check(MasterDomainEntity.class, "firstGroup", true);
	firstTick.check(MasterDomainEntity.class, "entityProp.entityProp.secondGroup", true);
	firstTick.check(MasterDomainEntity.class, "entityProp.entityProp.simpleEntityProp", true);

	final IAnalysisAddToAggregationTickManager secondTick = analysis.getSecondTick();
	secondTick.check(MasterDomainEntity.class, "sumInt", true);
	secondTick.check(MasterDomainEntity.class, "avgInt", true);
	secondTick.check(MasterDomainEntity.class, "mutIntSum", true);
	secondTick.check(MasterDomainEntity.class, "propIntSum", true);
	secondTick.check(MasterDomainEntity.class, "propIntAvg", true);
	secondTick.check(MasterDomainEntity.class, "propIntMin", true);

	cdtme.acceptAnalysisManager("simple analysis");

	queryGenerator = new ChartAnalysisQueryGenerator<>(MasterDomainEntity.class, cdtme, analysis);

	masterKlass = (Class<AbstractEntity<?>>) dte.getManagedType(MasterDomainEntity.class);
 	slaveKlass = (Class<AbstractEntity<?>>) PropertyTypeDeterminator.determinePropertyType(masterKlass, "entityProp");
 	evenSlaveKlass = (Class<AbstractEntity<?>>) PropertyTypeDeterminator.determinePropertyType(masterKlass, "entityProp.entityProp");
 	stringKeyKlass = (Class<AbstractEntity<?>>) PropertyTypeDeterminator.determinePropertyType(masterKlass, "entityProp.entityProp.simpleEntityProp");
    }

    @Test
    public void test_that_analysis_query_for_first_level_props_was_composed_correctly(){

	final IAnalysisDomainTreeManager analysis = (IAnalysisDomainTreeManager)cdtme.getAnalysisManager("simple analysis");

	final IAnalysisAddToDistributionTickManager firstTick = analysis.getFirstTick();
	firstTick.use(MasterDomainEntity.class, "firstGroup", true);

	final IAnalysisAddToAggregationTickManager secondTick = analysis.getSecondTick();
	secondTick.use(MasterDomainEntity.class, "sumInt", true);
	secondTick.use(MasterDomainEntity.class, "avgInt", true);

	secondTick.toggleOrdering(MasterDomainEntity.class, "sumInt");
	secondTick.toggleOrdering(MasterDomainEntity.class, "avgInt");

	final AnalysisResultClassBundle<MasterDomainEntity> resultBundle = queryGenerator.generateQueryModel();

	final Class<AbstractEntity<?>> managedType = (Class<AbstractEntity<?>>)cdtme.getEnhancer().getManagedType(MasterDomainEntity.class);
	final EntityResultQueryModel<AbstractEntity<?>> subQueryModel = select(managedType).as(ALIAS).model();

	final EntityResultQueryModel<MasterDomainEntity> queryModel = select(subQueryModel).as(ALIAS)//
		.groupBy().prop(ALIAS + ".firstGroup")//
		.yield().prop(ALIAS + ".firstGroup").as(AnalysisResultClass.getAnalysisPropertyName("firstGroup"))//
		.yield().prop(ALIAS + ".sumInt").as(AnalysisResultClass.getAnalysisPropertyName("sumInt"))//
		.yield().prop(ALIAS + ".avgInt").as(AnalysisResultClass.getAnalysisPropertyName("avgInt")).modelAsEntity(resultBundle.getGeneratedClass());

	final OrderingModel orderingModel = orderBy().yield(AnalysisResultClass.getAnalysisPropertyName("sumInt")).asc()//
		.yield(AnalysisResultClass.getAnalysisPropertyName("avgInt")).asc().model();
	final fetch<MasterDomainEntity> fetchModel = fetchOnly(resultBundle.getGeneratedClass()).with(AnalysisResultClass.getAnalysisPropertyName("firstGroup"))//
		.with(AnalysisResultClass.getAnalysisPropertyName("sumInt")).with(AnalysisResultClass.getAnalysisPropertyName("avgInt"));

	final QueryExecutionModel<MasterDomainEntity, EntityResultQueryModel<MasterDomainEntity>> resultQuery = from(queryModel)
		.with(orderingModel)//
		.with(fetchModel).model();

	Assert.assertEquals("The composed query model for analysis is incorrect", resultQuery, resultBundle.getQueries().get(0).composeQuery());

	firstTick.use(MasterDomainEntity.class, "firstGroup", false);

	secondTick.toggleOrdering(MasterDomainEntity.class, "sumInt");
	secondTick.toggleOrdering(MasterDomainEntity.class, "sumInt");
	secondTick.toggleOrdering(MasterDomainEntity.class, "avgInt");
	secondTick.toggleOrdering(MasterDomainEntity.class, "avgInt");

	secondTick.use(MasterDomainEntity.class, "sumInt", false);
	secondTick.use(MasterDomainEntity.class, "avgInt", false);
    }

    @Test
    public void test_that_analysis_query_for_high_level_props_was_composed_correctly(){
	final IAnalysisDomainTreeManager analysis = (IAnalysisDomainTreeManager)cdtme.getAnalysisManager("simple analysis");

	final IAnalysisAddToDistributionTickManager firstTick = analysis.getFirstTick();
	firstTick.use(MasterDomainEntity.class, "entityProp.entityProp.secondGroup", true);

	final IAnalysisAddToAggregationTickManager secondTick = analysis.getSecondTick();
	secondTick.use(MasterDomainEntity.class, "propIntSum", true);
	secondTick.use(MasterDomainEntity.class, "propIntAvg", true);

	final AnalysisResultClassBundle<MasterDomainEntity> resultBundle = queryGenerator.generateQueryModel();

	final Class<AbstractEntity<?>> managedType = (Class<AbstractEntity<?>>)cdtme.getEnhancer().getManagedType(MasterDomainEntity.class);
	final EntityResultQueryModel<AbstractEntity<?>> subQueryModel = select(managedType).as(ALIAS).model();

	final EntityResultQueryModel<MasterDomainEntity> queryModel = select(subQueryModel).as(ALIAS)//
		.groupBy().prop(ALIAS + ".entityProp.entityProp.secondGroup")//
		.yield().prop(ALIAS + ".entityProp.entityProp.secondGroup").as(AnalysisResultClass.getAnalysisPropertyName("entityProp.entityProp.secondGroup"))//
		.yield().prop(ALIAS + ".propIntSum").as(AnalysisResultClass.getAnalysisPropertyName("propIntSum"))//
		.yield().prop(ALIAS + ".propIntAvg").as(AnalysisResultClass.getAnalysisPropertyName("propIntAvg")).modelAsEntity(resultBundle.getGeneratedClass());

	final OrderingModel orderingModel = orderBy().yield(AnalysisResultClass.getAnalysisPropertyName("entityProp.entityProp.secondGroup")).asc().model();
	final fetch<MasterDomainEntity> fetchModel = fetchOnly(resultBundle.getGeneratedClass()).with(AnalysisResultClass.getAnalysisPropertyName("entityProp.entityProp.secondGroup"))//
		.with(AnalysisResultClass.getAnalysisPropertyName("propIntSum")).with(AnalysisResultClass.getAnalysisPropertyName("propIntAvg"));

	final QueryExecutionModel<MasterDomainEntity, EntityResultQueryModel<MasterDomainEntity>> resultQuery = from(queryModel)
		.with(orderingModel)//
		.with(fetchModel).model();

	Assert.assertEquals("The composed query model for analysis is incorrect", resultQuery, resultBundle.getQueries().get(0).composeQuery());

	firstTick.use(MasterDomainEntity.class, "entityProp.entityProp.secondGroup", false);

	secondTick.use(MasterDomainEntity.class, "propIntSum", false);
	secondTick.use(MasterDomainEntity.class, "propIntAvg", false);
    }

    @Test
    public void test_that_analysis_query_for_high_level_props_with_simple_group_was_composed_correctly(){
	final IAnalysisDomainTreeManager analysis = (IAnalysisDomainTreeManager)cdtme.getAnalysisManager("simple analysis");

	final IAnalysisAddToDistributionTickManager firstTick = analysis.getFirstTick();
	firstTick.use(MasterDomainEntity.class, "entityProp.entityProp.simpleEntityProp", true);

	final IAnalysisAddToAggregationTickManager secondTick = analysis.getSecondTick();
	secondTick.use(MasterDomainEntity.class, "propIntSum", true);
	secondTick.use(MasterDomainEntity.class, "propIntAvg", true);
	secondTick.use(MasterDomainEntity.class, "propIntMin", true);

	secondTick.toggleOrdering(MasterDomainEntity.class, "propIntSum");
	secondTick.toggleOrdering(MasterDomainEntity.class, "propIntAvg");
	secondTick.toggleOrdering(MasterDomainEntity.class, "propIntAvg");

	final AnalysisResultClassBundle<MasterDomainEntity> resultBundle = queryGenerator.generateQueryModel();

	final Class<AbstractEntity<?>> managedType = (Class<AbstractEntity<?>>)cdtme.getEnhancer().getManagedType(MasterDomainEntity.class);
	final EntityResultQueryModel<AbstractEntity<?>> subQueryModel = select(managedType).as(ALIAS).model();

	final EntityResultQueryModel<MasterDomainEntity> queryModel = select(subQueryModel).as(ALIAS)//
		.groupBy().prop(ALIAS + ".entityProp.entityProp.simpleEntityProp")//
		.yield().prop(ALIAS + ".entityProp.entityProp.simpleEntityProp").as(AnalysisResultClass.getAnalysisPropertyName("entityProp.entityProp.simpleEntityProp"))//
		.yield().prop(ALIAS + ".propIntSum").as(AnalysisResultClass.getAnalysisPropertyName("propIntSum"))//
		.yield().prop(ALIAS + ".propIntAvg").as(AnalysisResultClass.getAnalysisPropertyName("propIntAvg"))//
		.yield().prop(ALIAS + ".propIntMin").as(AnalysisResultClass.getAnalysisPropertyName("propIntMin"))//
		.modelAsEntity(resultBundle.getGeneratedClass());

	final OrderingModel orderingModel = orderBy().yield(AnalysisResultClass.getAnalysisPropertyName("propIntSum")).asc().yield(AnalysisResultClass.getAnalysisPropertyName("propIntAvg")).desc().model();
	final fetch<AbstractEntity<?>> simpleEntityFetch = fetchOnly(stringKeyKlass).with("key").with("desc");
	final fetch<MasterDomainEntity> fetchModel = fetchOnly(resultBundle.getGeneratedClass()).with(AnalysisResultClass.getAnalysisPropertyName("entityProp.entityProp.simpleEntityProp"), simpleEntityFetch)//
		.with(AnalysisResultClass.getAnalysisPropertyName("propIntSum")).with(AnalysisResultClass.getAnalysisPropertyName("propIntAvg"))//
		.with(AnalysisResultClass.getAnalysisPropertyName("propIntMin"));

	final QueryExecutionModel<MasterDomainEntity, EntityResultQueryModel<MasterDomainEntity>> resultQuery = from(queryModel)
		.with(orderingModel)//
		.with(fetchModel).model();
	Assert.assertEquals("The composed query model for analysis is incorrect", resultQuery, resultBundle.getQueries().get(0).composeQuery());


	firstTick.use(MasterDomainEntity.class, "entityProp.entityProp.simpleEntityProp", false);

	secondTick.toggleOrdering(MasterDomainEntity.class, "propIntSum");
	secondTick.toggleOrdering(MasterDomainEntity.class, "propIntSum");
	secondTick.toggleOrdering(MasterDomainEntity.class, "propIntAvg");

	secondTick.use(MasterDomainEntity.class, "propIntSum", false);
	secondTick.use(MasterDomainEntity.class, "propIntAvg", false);
	secondTick.use(MasterDomainEntity.class, "propIntMin", false);
    }
}
