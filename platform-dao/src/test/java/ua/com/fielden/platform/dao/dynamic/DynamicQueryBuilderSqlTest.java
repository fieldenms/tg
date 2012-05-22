package ua.com.fielden.platform.dao.dynamic;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.swing.review.DynamicQueryBuilder.createQuery;
import static ua.com.fielden.platform.swing.review.DynamicQueryBuilder.getEmptyValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.cfg.Configuration;
import org.hibernate.type.YesNoType;
import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.dao.DomainPersistenceMetadata;
import ua.com.fielden.platform.dao.HibernateMappingsGenerator;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.testing.TgKryo1;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.DateRangeSelectorEnum;
import ua.com.fielden.snappy.DateUtilities;
import ua.com.fielden.snappy.MnemonicEnum;

import com.google.inject.Injector;

/**
 * A test for {@link DynamicQueryBuilder}.
 *
 * @author TG Team
 *
 */
public class DynamicQueryBuilderSqlTest {
    private final static ISerialiser serialiser = createSerialiser(createFactory());

    private static EntityFactory createFactory() {
	final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
	final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
	return injector.getInstance(EntityFactory.class);
    }

    private static ISerialiser createSerialiser(final EntityFactory factory) {
	return new TgKryo1(factory, new ProvidedSerialisationClassProvider());
    }

    private final String alias;
    private final Class<? extends AbstractEntity<?>> masterKlass, slaveCollectionType, evenSlaverCollectionType;
    private final IJoin iJoin;
    private final Map<String, QueryProperty> queryProperties;

    {
	alias = "alias_for_main_criteria_type";
	// enhance domain with ALL / ANY calc properties
	final IDomainTreeEnhancer dte = new DomainTreeEnhancer(serialiser, new HashSet<Class<?>>() {{ add(MasterEntity.class); }});
	dte.addCalculatedProperty(MasterEntity.class, "collection", "masterEntityProp", "Any of masterEntityProp", "Desc", CalculatedPropertyAttribute.ANY, "masterEntityProp");
	dte.addCalculatedProperty(MasterEntity.class, "collection", "bigDecimalProp", "Any of bigDecimalProp", "Desc", CalculatedPropertyAttribute.ANY, "bigDecimalProp");
	dte.addCalculatedProperty(MasterEntity.class, "collection", "dateProp", "Any of dateProp", "Desc", CalculatedPropertyAttribute.ANY, "dateProp");
	dte.addCalculatedProperty(MasterEntity.class, "collection", "integerProp", "Any of integerProp", "Desc", CalculatedPropertyAttribute.ANY, "integerProp");
	dte.addCalculatedProperty(MasterEntity.class, "collection", "moneyProp", "Any of moneyProp", "Desc", CalculatedPropertyAttribute.ANY, "moneyProp");
	dte.addCalculatedProperty(MasterEntity.class, "collection", "masterEntityProp", "All of masterEntityProp", "Desc", CalculatedPropertyAttribute.ALL, "masterEntityProp");
	dte.addCalculatedProperty(MasterEntity.class, "collection", "bigDecimalProp", "All of bigDecimalProp", "Desc", CalculatedPropertyAttribute.ALL, "bigDecimalProp");
	dte.addCalculatedProperty(MasterEntity.class, "collection", "dateProp", "All of dateProp", "Desc", CalculatedPropertyAttribute.ALL, "dateProp");
	dte.addCalculatedProperty(MasterEntity.class, "entityProp.collection", "slaveEntityProp", "All of slaveEntityProp", "Desc", CalculatedPropertyAttribute.ALL, "slaveEntityProp");
	dte.addCalculatedProperty(MasterEntity.class, "entityProp.collection", "bigDecimalProp", "All of bigDecimalProp", "Desc", CalculatedPropertyAttribute.ALL, "bigDecimalProp");
	dte.addCalculatedProperty(MasterEntity.class, "entityProp.collection", "dateProp", "All of dateProp", "Desc", CalculatedPropertyAttribute.ALL, "dateProp");
	dte.addCalculatedProperty(MasterEntity.class, "entityProp.collection", "integerProp", "Any of integerProp", "Desc", CalculatedPropertyAttribute.ANY, "integerProp");
	dte.addCalculatedProperty(MasterEntity.class, "entityProp.collection", "moneyProp", "Any of moneyProp", "Desc", CalculatedPropertyAttribute.ANY, "moneyProp");
	dte.apply();

	masterKlass = (Class<? extends AbstractEntity<?>>) dte.getManagedType(MasterEntity.class);
	slaveCollectionType = (Class<? extends AbstractEntity<?>>) PropertyTypeDeterminator.determinePropertyType(masterKlass, "collection");
	evenSlaverCollectionType = (Class<? extends AbstractEntity<?>>) PropertyTypeDeterminator.determinePropertyType(masterKlass, "entityProp.collection");

	iJoin = select(masterKlass).as(alias);
	queryProperties = new LinkedHashMap<String, QueryProperty>();

	final Configuration hibConf = new Configuration();

	final Map<Class, Class> hibTypeMap = new HashMap<Class, Class>();
	hibTypeMap.put(boolean.class, YesNoType.class);
	hibTypeMap.put(Boolean.class, YesNoType.class);
	hibTypeMap.put(Date.class, DateTimeType.class);
	hibTypeMap.put(Money.class, SimpleMoneyType.class);
	final List<Class<? extends AbstractEntity<?>>> domainTypes = new ArrayList<Class<? extends AbstractEntity<?>>>();
	domainTypes.add(MasterEntity.class);
	domainTypes.add(SlaveEntity.class);
	domainTypes.add(EvenSlaverEntity.class);
	hibConf.addXML(new HibernateMappingsGenerator((new DomainPersistenceMetadata(hibTypeMap, null, domainTypes)).getHibTypeInfosMap()).generateMappings());
	final List<String> propertyNames = Arrays.asList(new String[] { //
	"integerProp", //
		"doubleProp", //
		"bigDecimalProp", //
		"moneyProp", //
		"dateProp", //
		"booleanProp", //
		"stringProp", //
		"entityProp", //
		"entityProp.masterEntityProp", //
		"entityProp.integerProp", //
		"entityProp.doubleProp", //
		"entityProp.bigDecimalProp", //
		"entityProp.moneyProp", //
		"entityProp.dateProp", //
		"entityProp.booleanProp", //
		"entityProp.stringProp", //
		"collection.masterEntityProp", //
		"collection.integerProp", //
		"collection.doubleProp", //
		"collection.bigDecimalProp", //
		"collection.dateProp", //
		"collection.stringProp", //
		"collection.booleanProp", //
		"collection.anyOfMasterEntityProp", //
		"collection.anyOfBigDecimalProp", //
		"collection.anyOfDateProp", //
		"collection.anyOfIntegerProp", //
		"collection.anyOfMoneyProp", //
		"collection.allOfMasterEntityProp", //
		"collection.allOfBigDecimalProp", //
		"collection.allOfDateProp", //
		"entityProp.collection.slaveEntityProp", //
		"entityProp.collection.integerProp", //
		"entityProp.collection.doubleProp", //
		"entityProp.collection.bigDecimalProp", //
		"entityProp.collection.dateProp", //
		"entityProp.collection.stringProp", //
		"entityProp.collection.booleanProp", //
		"entityProp.collection.allOfSlaveEntityProp", //
		"entityProp.collection.allOfBigDecimalProp", //
		"entityProp.collection.allOfDateProp", //
		"entityProp.collection.anyOfIntegerProp", //
		"entityProp.collection.anyOfMoneyProp" //

	});
	for (final String propertyName : propertyNames) {
	    final QueryProperty qp = new QueryProperty(masterKlass, propertyName);
	    queryProperties.put(propertyName, qp);
	    qp.setValue(getEmptyValue(qp.getType(), qp.isSingle()));
	    qp.setValue2(getEmptyValue(qp.getType(), qp.isSingle()));
	}
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////// Test query composition //////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    @Before
    public void set_up() {
	// make the values to be default for all properties before any test
	for (final QueryProperty qp : queryProperties.values()) {
	    qp.setValue(getEmptyValue(qp.getType(), qp.isSingle()));
	    qp.setValue2(getEmptyValue(qp.getType(), qp.isSingle()));
	    qp.setExclusive(null);
	    qp.setExclusive2(null);
	    qp.setDatePrefix(null);
	    qp.setDateMnemonic(null);
	    qp.setAndBefore(null);
	    qp.setOrNull(null);
	    qp.setNot(null);
	}
    }

    @Test
    public void test_empty_query_composition_for_empty_query_properties() {
	final ICompleted expected = //
	/**/iJoin; //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_empty_query_composition_for_boolean_property_with_both_false_values() {
	final QueryProperty property = queryProperties.get("booleanProp");
	property.setValue(false);
	property.setValue2(false);

	final ICompleted expected = //
	/**/iJoin; //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_empty_query_composition_for_empty_property_with_OrNull_false() {
	final QueryProperty property = queryProperties.get("dateProp");
	property.setValue(null);
	property.setValue2(null);
	property.setOrNull(false);

	final ICompleted expected = //
	/**/iJoin; //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////// 1. Atomic level /////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////// 1.1. Range type /////////////////////////////////////////

    @Test
    public void test_atomic_query_composition_for_integer_range_type() {
	test_atomic_query_composition_for_range_type("integerProp");
	test_atomic_query_composition_for_range_type("entityProp.integerProp");
    }

    @Test
    public void test_atomic_query_composition_for_double_range_type() {
	test_atomic_query_composition_for_range_type("doubleProp");
	test_atomic_query_composition_for_range_type("entityProp.doubleProp");
    }

    @Test
    public void test_atomic_query_composition_for_bigDecimal_range_type() {
	test_atomic_query_composition_for_range_type("bigDecimalProp");
	test_atomic_query_composition_for_range_type("entityProp.bigDecimalProp");
    }

    @Test
    public void test_atomic_query_composition_for_money_range_type() {
	test_atomic_query_composition_for_range_type("moneyProp");
	test_atomic_query_composition_for_range_type("entityProp.moneyProp");
    }

    @Test
    public void test_atomic_query_composition_for_date_range_type() {
	test_atomic_query_composition_for_range_type("dateProp");
	test_atomic_query_composition_for_range_type("entityProp.dateProp");
    }

    @Test
    public void test_atomic_query_composition_for_date_range_type_with_mnemonics_assigned() {
	test_atomic_query_composition_for_date_range_type_with_mnemonic_assigned("dateProp");
	test_atomic_query_composition_for_date_range_type_with_mnemonic_assigned("entityProp.dateProp");
    }

    private void test_atomic_query_composition_for_date_range_type_with_mnemonic_assigned(final String propertyName) {
	test_atomic_query_composition_for_date_range_type_with_with_only_mnemonic_assigned(propertyName);
	test_atomic_query_composition_for_date_range_type_with_with_mnemonic_assigned_and_AndBefore_equals_True(propertyName);
	test_atomic_query_composition_for_date_range_type_with_with_mnemonic_assigned_and_AndBefore_equals_False(propertyName);
    }

    private void test_atomic_query_composition_for_range_type(final String propertyName) {
	test_atomic_query_composition_for_range_type_with_both_boundaries(propertyName);
	test_atomic_query_composition_for_range_type_with_both_boundaries_and_left_Exclusive_flag_assigned(propertyName);
	test_atomic_query_composition_for_range_type_with_both_boundaries_and_right_Exclusive_flag_assigned(propertyName);
	test_atomic_query_composition_for_range_type_with_both_boundaries_and_both_Exclusive_flags_assigned(propertyName);
    }

    private void test_atomic_query_composition_for_date_range_type_with_with_only_mnemonic_assigned(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	// set a state values (anything) that should be ignored during query composition
	property.setValue("just anything");
	property.setExclusive(true);
	property.setValue2("another anything");
	property.setExclusive2(false);
	// set a state values that should be considered during query composition
	property.setDatePrefix(DateRangePrefixEnum.PREV);
	property.setDateMnemonic(MnemonicEnum.QRT2);

	final DateUtilities du = new DateUtilities();
	final Date currentDate = new Date();
	final Date from = du.dateOfRangeThatIncludes(currentDate, DateRangeSelectorEnum.BEGINNING, property.getDatePrefix(), property.getDateMnemonic()), //
	/*         */to = du.dateOfRangeThatIncludes(currentDate, DateRangeSelectorEnum.ENDING, property.getDatePrefix(), property.getDateMnemonic());

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(cbn).isNotNull().and() //
	/*    */.begin().prop(cbn).ge().iVal(from).and().prop(cbn).lt().iVal(to).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_date_range_type_with_with_mnemonic_assigned_and_AndBefore_equals_True(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	// set a state values (anything) that should be ignored during query composition
	property.setValue("just anything");
	property.setExclusive(true);
	property.setValue2("another anything");
	property.setExclusive2(false);
	// set a state values that should be considered during query composition
	property.setDatePrefix(DateRangePrefixEnum.PREV);
	property.setDateMnemonic(MnemonicEnum.QRT2);
	property.setAndBefore(true);

	final DateUtilities du = new DateUtilities();
	final Date currentDate = new Date();
	final Date to = du.dateOfRangeThatIncludes(currentDate, DateRangeSelectorEnum.ENDING, property.getDatePrefix(), property.getDateMnemonic());

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(cbn).isNotNull().and() //
	/*    */.begin().prop(cbn).ge().iVal(null).and().prop(cbn).lt().iVal(to).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_date_range_type_with_with_mnemonic_assigned_and_AndBefore_equals_False(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	// set a state values (anything) that should be ignored during query composition
	property.setValue("just anything");
	property.setExclusive(true);
	property.setValue2("another anything");
	property.setExclusive2(false);
	// set a state values that should be considered during query composition
	property.setDatePrefix(DateRangePrefixEnum.PREV);
	property.setDateMnemonic(MnemonicEnum.QRT2);
	property.setAndBefore(false);

	final DateUtilities du = new DateUtilities();
	final Date currentDate = new Date();
	final Date from = du.dateOfRangeThatIncludes(currentDate, DateRangeSelectorEnum.BEGINNING, property.getDatePrefix(), property.getDateMnemonic()); //

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(cbn).isNotNull().and() //
	/*    */.begin().prop(cbn).ge().iVal(from).and().prop(cbn).lt().iVal(null).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_range_type_with_both_boundaries(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue(3);
	property.setValue2(7);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(cbn).isNotNull().and() //
	/*    */.begin().prop(cbn).ge().iVal(3).and().prop(cbn).le().iVal(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_range_type_with_both_boundaries_and_left_Exclusive_flag_assigned(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue(3);
	property.setExclusive(true);
	property.setValue2(7);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(cbn).isNotNull().and() //
	/*    */.begin().prop(cbn).gt().iVal(3).and().prop(cbn).le().iVal(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_range_type_with_both_boundaries_and_right_Exclusive_flag_assigned(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue(3);
	property.setValue2(7);
	property.setExclusive2(true);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(cbn).isNotNull().and() //
	/*    */.begin().prop(cbn).ge().iVal(3).and().prop(cbn).lt().iVal(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_range_type_with_both_boundaries_and_both_Exclusive_flags_assigned(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue(3);
	property.setExclusive(true);
	property.setValue2(7);
	property.setExclusive2(true);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(cbn).isNotNull().and() //
	/*    */.begin().prop(cbn).gt().iVal(3).and().prop(cbn).lt().iVal(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    ////////////////////////////////// 1.2. Boolean type /////////////////////////////////////////
    @Test
    public void test_atomic_query_composition_for_boolean_type() {
	test_atomic_query_composition_for_boolean_type("booleanProp");
	test_atomic_query_composition_for_boolean_type("entityProp.booleanProp");
    }

    private void test_atomic_query_composition_for_boolean_type(final String propertyName) {
	test_atomic_query_composition_for_boolean_type_with_left_value_as_False(propertyName);
	test_atomic_query_composition_for_boolean_type_with_right_value_as_False(propertyName);
    }

    private void test_atomic_query_composition_for_boolean_type_with_left_value_as_False(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue(false);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(cbn).isNotNull().and() //
	/*    */.begin().prop(cbn).eq().val(false).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_boolean_type_with_right_value_as_False(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue2(false);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(cbn).isNotNull().and() //
	/*    */.begin().prop(cbn).eq().val(true).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    ////////////////////////////////// 1.3.-1.4. String / Entity types /////////////////////////////////////////
    @Test
    public void test_atomic_query_composition_for_string_type() {
	test_atomic_query_composition_for_string_type("stringProp");
	test_atomic_query_composition_for_string_type("entityProp.stringProp");
    }

    @Test
    public void test_atomic_query_composition_for_entity_type() {
	test_atomic_query_composition_for_entity_type("entityProp");
    }

    private void test_atomic_query_composition_for_string_type(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue("Some string value");

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(cbn).isNotNull().and() //
	/*    */.begin().prop(cbn).like().anyOfValues(DynamicQueryBuilder.prepare((String) property.getValue())).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_entity_type(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue(Arrays.asList("some val 1", "some val 2"));

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn)).isNotNull().and() //
	/*    */.begin().prop(cbn).like().anyOfValues(DynamicQueryBuilder.prepare((List<String>) property.getValue())).end() //
	/*  */.end() //s
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////// 2. Property level (Negation / Null) /////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////// 2.1. Is [Not] Null //////////////////////////////////////
    @Test
    public void test_property_query_composition_with_missing_value() {
	final String propertyName = "dateProp";
	final QueryProperty property = queryProperties.get(propertyName);
	property.setOrNull(true);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn)).isNull() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_property_query_composition_with_missing_value_and_negated() {
	final String propertyName = "entityProp";
	final QueryProperty property = queryProperties.get(propertyName);
	property.setOrNull(true);
	property.setNot(true);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn)).isNotNull() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    ////////////////////////////////// 2.2. Direct conditions and Negation / Nulls //////////////////////////////////////

    @Test
    public void test_property_query_composition_with_direct_condition() {
	final String propertyName = "bigDecimalProp";
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue(3);
	property.setValue2(7);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn)).isNotNull().and() //
	/*    */.begin().prop(cbn).ge().iVal(3).and().prop(cbn).le().iVal(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_property_query_composition_with_direct_condition_and_negated() {
	final String propertyName = "bigDecimalProp";
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue(3);
	property.setValue2(7);
	property.setNot(true);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn)).isNull().or() //
	/*    */.notBegin().prop(cbn).ge().iVal(3).and().prop(cbn).le().iVal(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_property_query_composition_with_direct_condition_and_missing_value() {
	final String propertyName = "bigDecimalProp";
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue(3);
	property.setValue2(7);
	property.setOrNull(true);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn)).isNull().or() //
	/*    */.begin().prop(cbn).ge().iVal(3).and().prop(cbn).le().iVal(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_property_query_composition_with_direct_condition_and_missing_value_and_negated() {
	final String propertyName = "bigDecimalProp";
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue(3);
	property.setValue2(7);
	property.setOrNull(true);
	property.setNot(true);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn)).isNotNull().and() //
	/*    */.notBegin().prop(cbn).ge().iVal(3).and().prop(cbn).le().iVal(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////// 3. Conditions level /////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////// 3.1. Non-collectional ///////////////////////////////////
    @Test
    public void test_conditions_query_composition_with_a_couple_of_non_collectional_conditions() {
	queryProperties.get("bigDecimalProp").setValue(3);
	queryProperties.get("bigDecimalProp").setValue2(7);
	queryProperties.get("integerProp").setOrNull(true);
	queryProperties.get("dateProp").setOrNull(true);
	queryProperties.get("dateProp").setNot(true);

	final String cbn1 = queryProperties.get("bigDecimalProp").getConditionBuildingName();
	final String cbn2 = queryProperties.get("integerProp").getConditionBuildingName();
	final String cbn3 = queryProperties.get("dateProp").getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn2)).isNull().end().and() // integerProp
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNotNull().and() // bigDecimalProp
	/*    */.begin().prop(cbn1).ge().iVal(3).and().prop(cbn1).le().iVal(7).end() //
	/*  */.end().and() //
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn3)).isNotNull().end() // dateProp
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    ////////////////////////////////// 3.2. Collectional ///////////////////////////////////
    @Test
    public void test_conditions_query_composition_with_a_couple_of_collectional_conditions_that_are_irrelevant_without_ALL_or_ANY_condition() {
	// Collection FILTERING properties
	queryProperties.get("collection.bigDecimalProp").setOrNull(true);
	queryProperties.get("collection.dateProp").setOrNull(true);
	queryProperties.get("collection.masterEntityProp").setOrNull(true);
	queryProperties.get("collection.stringProp").setOrNull(true);

	final ICompleted expected = //
	/**/iJoin; //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_conditions_query_composition_with_a_single_ANY_condition_from_single_collection_and_a_couple_of_FILTERING_conditions() {
	queryProperties.get("collection.anyOfBigDecimalProp").setOrNull(true);
	final String cbn1 = queryProperties.get("collection.anyOfBigDecimalProp").getConditionBuildingName();
	// FILTERING
	queryProperties.get("collection.stringProp").setOrNull(true);
	final String cbnFiltering1 = queryProperties.get("collection.stringProp").getConditionBuildingName();
	queryProperties.get("collection.booleanProp").setOrNull(true);
	final String cbnFiltering2 = queryProperties.get("collection.booleanProp").getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin() // collection begins
	/*    */.begin() // ANY block begins
	/*      */.exists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ANY block ends
	/*  */.end() // collection ends
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_conditions_query_composition_with_a_couple_of_ANY_conditions_from_single_collection_and_a_couple_of_FILTERING_conditions() {
	queryProperties.get("collection.anyOfMasterEntityProp").setOrNull(true);
	final String cbn1 = queryProperties.get("collection.anyOfMasterEntityProp").getConditionBuildingName();
	queryProperties.get("collection.anyOfBigDecimalProp").setOrNull(true);
	final String cbn2 = queryProperties.get("collection.anyOfBigDecimalProp").getConditionBuildingName();
	queryProperties.get("collection.anyOfDateProp").setOrNull(true);
	final String cbn3 = queryProperties.get("collection.anyOfDateProp").getConditionBuildingName();
	// FILTERING
	queryProperties.get("collection.stringProp").setOrNull(true);
	final String cbnFiltering1 = queryProperties.get("collection.stringProp").getConditionBuildingName();
	queryProperties.get("collection.booleanProp").setOrNull(true);
	final String cbnFiltering2 = queryProperties.get("collection.booleanProp").getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin() // collection begins
	/*    */.begin() // ANY block begins
	/*      */.exists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn2)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn3)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ANY block ends
	/*  */.end() // collection ends
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_conditions_query_composition_with_a_single_ALL_condition_from_single_collection_and_a_couple_of_FILTERING_conditions() {
	queryProperties.get("collection.allOfBigDecimalProp").setOrNull(true);
	final String cbn1 = queryProperties.get("collection.allOfBigDecimalProp").getConditionBuildingName();
	// FILTERING
	queryProperties.get("collection.stringProp").setOrNull(true);
	final String cbnFiltering1 = queryProperties.get("collection.stringProp").getConditionBuildingName();
	queryProperties.get("collection.booleanProp").setOrNull(true);
	final String cbnFiltering2 = queryProperties.get("collection.booleanProp").getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin() // collection begins
	/*    */.begin() // ALL block begins
	/*      */.notExists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().model() //
	/*      */) //
	/*      */.or() //
	/*      */.exists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*      */.and() //
	/*      */.notExists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNotNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ALL block ends
	/*  */.end() // collection ends
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_conditions_query_composition_with_a_couple_of_ALL_conditions_from_single_collection_and_a_couple_of_FILTERING_conditions() {
	// ALL properties
	queryProperties.get("collection.allOfBigDecimalProp").setOrNull(true);
	final String cbn1 = queryProperties.get("collection.allOfBigDecimalProp").getConditionBuildingName();
	queryProperties.get("collection.allOfDateProp").setOrNull(true);
	final String cbn2 = queryProperties.get("collection.allOfDateProp").getConditionBuildingName();
	queryProperties.get("collection.allOfMasterEntityProp").setOrNull(true);
	final String cbn3 = queryProperties.get("collection.allOfMasterEntityProp").getConditionBuildingName();
	// FILTERING
	queryProperties.get("collection.stringProp").setOrNull(true);
	final String cbnFiltering1 = queryProperties.get("collection.stringProp").getConditionBuildingName();
	queryProperties.get("collection.booleanProp").setOrNull(true);
	final String cbnFiltering2 = queryProperties.get("collection.booleanProp").getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin() // collection begins
	/*    */.begin() // ALL block begins
	/*      */.notExists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().model() //
	/*      */) //
	/*      */.or() //
	/*      */.exists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn3)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn2)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*      */.and() //
	/*      */.notExists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn3)).isNotNull().end().or() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNotNull().end().or() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn2)).isNotNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ALL block ends
	/*  */.end() // collection ends
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_conditions_query_composition_with_a_couple_of_ALL_and_ANY_conditions_from_single_collection_and_a_couple_of_FILTERING_conditions() {
	// ALL properties
	queryProperties.get("collection.allOfBigDecimalProp").setOrNull(true);
	final String cbn1 = queryProperties.get("collection.allOfBigDecimalProp").getConditionBuildingName();
	queryProperties.get("collection.allOfDateProp").setOrNull(true);
	final String cbn2 = queryProperties.get("collection.allOfDateProp").getConditionBuildingName();
	queryProperties.get("collection.allOfMasterEntityProp").setOrNull(true);
	final String cbn3 = queryProperties.get("collection.allOfMasterEntityProp").getConditionBuildingName();
	// ANY properties
	queryProperties.get("collection.anyOfIntegerProp").setOrNull(true);
	final String cbn4 = queryProperties.get("collection.anyOfIntegerProp").getConditionBuildingName();
	queryProperties.get("collection.anyOfMoneyProp").setOrNull(true);
	final String cbn5 = queryProperties.get("collection.anyOfMoneyProp").getConditionBuildingName();
	// FILTERING
	queryProperties.get("collection.stringProp").setOrNull(true);
	final String cbnFiltering1 = queryProperties.get("collection.stringProp").getConditionBuildingName();
	queryProperties.get("collection.booleanProp").setOrNull(true);
	final String cbnFiltering2 = queryProperties.get("collection.booleanProp").getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin() // collection begins
	/*    */.begin() // ANY block begins
	/*      */.exists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn4)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn5)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ANY block ends
	/*    */
	/*    */.and() //
	/*    */
	/*    */.begin() // ALL block begins
	/*      */.notExists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().model() //
	/*      */) //
	/*      */.or() //
	/*      */.exists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn3)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn2)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*      */.and() //
	/*      */.notExists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn3)).isNotNull().end().or() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNotNull().end().or() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn2)).isNotNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ALL block ends
	/*  */.end() // collection ends
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_conditions_query_composition_with_a_couple_of_ALL_and_ANY_conditions_from_a_bunch_of_collections_of_different_levels_and_a_bunch_of_simple_properties() { // :)
	/////// Simple properties ///////
	queryProperties.get("bigDecimalProp").setValue(3);
	queryProperties.get("bigDecimalProp").setValue2(7);
	final String cbn01 = queryProperties.get("bigDecimalProp").getConditionBuildingName();
	queryProperties.get("integerProp").setOrNull(true);
	final String cbn02 = queryProperties.get("integerProp").getConditionBuildingName();
	queryProperties.get("dateProp").setOrNull(true);
	queryProperties.get("dateProp").setNot(true);
	final String cbn03 = queryProperties.get("dateProp").getConditionBuildingName();

	////// Collection from master entity //////
	// ALL properties
	queryProperties.get("collection.allOfBigDecimalProp").setOrNull(true);
	final String cbn1 = queryProperties.get("collection.allOfBigDecimalProp").getConditionBuildingName();
	queryProperties.get("collection.allOfDateProp").setOrNull(true);
	final String cbn2 = queryProperties.get("collection.allOfDateProp").getConditionBuildingName();
	queryProperties.get("collection.allOfMasterEntityProp").setOrNull(true);
	final String cbn3 = queryProperties.get("collection.allOfMasterEntityProp").getConditionBuildingName();

	// ANY properties
	queryProperties.get("collection.anyOfIntegerProp").setOrNull(true);
	final String cbn4 = queryProperties.get("collection.anyOfIntegerProp").getConditionBuildingName();
	queryProperties.get("collection.anyOfMoneyProp").setOrNull(true);
	final String cbn5 = queryProperties.get("collection.anyOfMoneyProp").getConditionBuildingName();

	////// Collection from slave entity //////
	// ALL properties
	queryProperties.get("entityProp.collection.allOfBigDecimalProp").setOrNull(true);
	final String cbn6 = queryProperties.get("entityProp.collection.allOfBigDecimalProp").getConditionBuildingName();
	queryProperties.get("entityProp.collection.allOfDateProp").setOrNull(true);
	final String cbn7 = queryProperties.get("entityProp.collection.allOfDateProp").getConditionBuildingName();
	queryProperties.get("entityProp.collection.allOfSlaveEntityProp").setOrNull(true);
	final String cbn8 = queryProperties.get("entityProp.collection.allOfSlaveEntityProp").getConditionBuildingName();

	// ANY properties
	queryProperties.get("entityProp.collection.anyOfIntegerProp").setOrNull(true);
	final String cbn9 = queryProperties.get("entityProp.collection.anyOfIntegerProp").getConditionBuildingName();
	queryProperties.get("entityProp.collection.anyOfMoneyProp").setOrNull(true);
	final String cbn10 = queryProperties.get("entityProp.collection.anyOfMoneyProp").getConditionBuildingName();

	// FILTERING
	queryProperties.get("entityProp.collection.stringProp").setOrNull(true);
	final String cbnFiltering1 = queryProperties.get("entityProp.collection.stringProp").getConditionBuildingName();
	queryProperties.get("entityProp.collection.booleanProp").setOrNull(true);
	final String cbnFiltering2 = queryProperties.get("entityProp.collection.booleanProp").getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*                          simple properties below                             */
	/*                          simple properties below                             */
	/*                          simple properties below                             */
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn02)).isNull().end().and() // integerProp
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn01)).isNotNull().and() // bigDecimalProp
	/*    */.begin().prop(cbn01).ge().iVal(3).and().prop(cbn01).le().iVal(7).end() //
	/*  */.end().and() //
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn03)).isNotNull().end().and() // dateProp
	/*  */
	/*                          master collection below                             */
	/*                          master collection below                             */
	/*                          master collection below                             */
	/*  */
	/*  */.begin() // collection begins
	/*    */.begin() // ANY block begins
	/*      */.exists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and().begin()
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn4)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn5)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ANY block ends
	/*    */
	/*    */.and() //
	/*    */
	/*    */.begin() // ALL block begins
	/*      */.notExists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).model() //
	/*      */) //
	/*      */.or() //
	/*      */.exists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and().begin()
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn3)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn2)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*      */.and() //
	/*      */.notExists( //
	/*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and().begin()
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn3)).isNotNull().end().or() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNotNull().end().or() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn2)).isNotNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ALL block ends
	/*  */.end() // collection ends
	/**/
	/**/
	/**/.and() //
	/**/
	/*                           slave collection below                             */
	/*                           slave collection below                             */
	/*                           slave collection below                             */
	/**/
	/*  */.begin() // collection begins
	/*    */.begin() // ANY block begins
	/*      */.exists( //
	/*          */select(evenSlaverCollectionType).where().prop("slaveEntityProp").eq().prop(alias + ".entityProp").and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn9)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn10)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ANY block ends
	/*    */
	/*    */.and() //
	/*    */
	/*    */.begin() // ALL block begins
	/*      */.notExists( //
	/*          */select(evenSlaverCollectionType).where().prop("slaveEntityProp").eq().prop(alias + ".entityProp").and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().model() //
	/*      */) //
	/*      */.or() //
	/*      */.exists( //
	/*          */select(evenSlaverCollectionType).where().prop("slaveEntityProp").eq().prop(alias + ".entityProp").and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn8)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn6)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn7)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*      */.and() //
	/*      */.notExists( //
	/*          */select(evenSlaverCollectionType).where().prop("slaveEntityProp").eq().prop(alias + ".entityProp").and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().end().and() // FILTERING
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().end() // FILTERING
	/*          */.end().and() //
	/*          */.begin() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn8)).isNotNull().end().or() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn6)).isNotNull().end().or() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn7)).isNotNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ALL block ends
	/*  */.end() // collection ends
	/**/.end(); //
	final ICompleted actual = createQuery(masterKlass, new ArrayList<QueryProperty>(queryProperties.values()));

	assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
	//assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }
}