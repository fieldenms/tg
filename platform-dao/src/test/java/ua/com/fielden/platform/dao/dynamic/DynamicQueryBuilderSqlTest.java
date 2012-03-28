package ua.com.fielden.platform.dao.dynamic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.cfg.Configuration;
import org.hibernate.type.YesNoType;
import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.dao.MappingExtractor;
import ua.com.fielden.platform.dao2.DomainPersistenceMetadata;
import ua.com.fielden.platform.dao2.HibernateMappingsGenerator;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompleted;
import ua.com.fielden.platform.equery.interfaces.IMain.IJoin;
import ua.com.fielden.platform.equery.interfaces.IMappingExtractor;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.reflection.EntityDescriptor;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder;
import ua.com.fielden.platform.swing.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.DateRangeSelectorEnum;
import ua.com.fielden.snappy.DateUtilities;
import ua.com.fielden.snappy.MnemonicEnum;
import static org.junit.Assert.assertEquals;

import static ua.com.fielden.platform.equery.equery.select;

import static ua.com.fielden.platform.swing.review.DynamicQueryBuilder.buildConditions;
import static ua.com.fielden.platform.swing.review.DynamicQueryBuilder.getEmptyValue;

/**
 * A test for {@link DynamicQueryBuilder}.
 *
 * @author TG Team
 *
 */
public class DynamicQueryBuilderSqlTest {
    private final String alias = "alias";
    private final Class<MasterEntity> masterKlass = MasterEntity.class;
    private final IJoin iJoin = select(masterKlass, alias);
    private final IMappingExtractor mappingExtractor;
    private final Map<String, QueryProperty> queryProperties = new LinkedHashMap<String, QueryProperty>();


    {
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
	mappingExtractor = new MappingExtractor(hibConf);
	final List<String> propertyNames = Arrays.asList(new String [] {
		"integerProp",
		"doubleProp",
		"bigDecimalProp",
		"moneyProp",
		"dateProp",
		"booleanProp",
		"stringProp",
		"entityProp",
		"entityProp.masterEntityProp",
		"entityProp.integerProp",
		"entityProp.doubleProp",
		"entityProp.bigDecimalProp",
		"entityProp.moneyProp",
		"entityProp.dateProp",
		"entityProp.booleanProp",
		"entityProp.stringProp",
		"collection.masterEntityProp",
		"collection.integerProp",
		"collection.doubleProp",
		"collection.bigDecimalProp",
		"collection.dateProp",
		"entityProp.collection.slaveEntityProp",
		"entityProp.collection.integerProp",
		"entityProp.collection.doubleProp",
		"entityProp.collection.bigDecimalProp",
		"entityProp.collection.dateProp"
		});
	for (final String propertyName : propertyNames) {
	    final QueryProperty qp = new QueryProperty(masterKlass, propertyName, alias);
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
	    qp.setAll(null);
	}
    }

    @Test
    public void test_empty_query_composition_for_empty_query_properties() {
	final ICompleted expected = //
	/**/iJoin; //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_empty_query_composition_for_boolean_property_with_both_false_values() {
	final QueryProperty property = queryProperties.get("booleanProp");
	property.setValue(false);
	property.setValue2(false);

	final ICompleted expected = //
	/**/iJoin; //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_empty_query_composition_for_empty_property_with_OrNull_false() {
	final QueryProperty property = queryProperties.get("dateProp");
	property.setValue(null);
	property.setValue2(null);
	property.setOrNull(false);

	final ICompleted expected = //
	/**/iJoin; //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	test_atomic_query_composition_for_range_type_with_left_boundary(propertyName);
	test_atomic_query_composition_for_range_type_with_left_boundary_and_left_Exclusive_flag_assigned(propertyName);
	test_atomic_query_composition_for_range_type_with_right_boundary(propertyName);
	test_atomic_query_composition_for_range_type_with_right_boundary_and_right_Exclusive_flag_assigned(propertyName);
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
	/*    */.begin().prop(cbn).ge().val(from).and().prop(cbn).lt().val(to).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	/*    */.begin().prop(cbn).lt().val(to).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	/*    */.begin().prop(cbn).ge().val(from).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	/*    */.begin().prop(cbn).ge().val(3).and().prop(cbn).le().val(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	/*    */.begin().prop(cbn).gt().val(3).and().prop(cbn).le().val(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	/*    */.begin().prop(cbn).ge().val(3).and().prop(cbn).lt().val(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	/*    */.begin().prop(cbn).gt().val(3).and().prop(cbn).lt().val(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_range_type_with_left_boundary(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue(3);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(cbn).isNotNull().and() //
	/*    */.begin().prop(cbn).ge().val(3).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_range_type_with_left_boundary_and_left_Exclusive_flag_assigned(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue(3);
	property.setExclusive(true);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(cbn).isNotNull().and() //
	/*    */.begin().prop(cbn).gt().val(3).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_range_type_with_right_boundary(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue2(7);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(cbn).isNotNull().and() //
	/*    */.begin().prop(cbn).le().val(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_range_type_with_right_boundary_and_right_Exclusive_flag_assigned(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue2(7);
	property.setExclusive2(true);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(cbn).isNotNull().and() //
	/*    */.begin().prop(cbn).lt().val(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	/*    */.begin().prop(cbn).isFalse().end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_boolean_type_with_right_value_as_False(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue2(false);

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(cbn).isNotNull().and() //
	/*    */.begin().prop(cbn).isTrue().end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	/*    */.begin().prop(cbn).like().val(DynamicEntityQueryCriteria.prepare((String) property.getValue())).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_entity_type(final String propertyName) {
	set_up();
	final QueryProperty property = queryProperties.get(propertyName);
	property.setValue(Arrays.asList("some val 1", "some val 2"));

	final String cbn = property.getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn)).isNotNull().and() //
	/*    */.begin().prop(cbn).like().val(DynamicEntityQueryCriteria.prepare((List<String>) property.getValue())).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	/*    */.begin().prop(cbn).ge().val(3).and().prop(cbn).le().val(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	/*    */.notBegin().prop(cbn).ge().val(3).and().prop(cbn).le().val(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	/*    */.begin().prop(cbn).ge().val(3).and().prop(cbn).le().val(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	/*    */.notBegin().prop(cbn).ge().val(3).and().prop(cbn).le().val(7).end() //
	/*  */.end() //
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
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
	/*    */.begin().prop(cbn1).ge().val(3).and().prop(cbn1).le().val(7).end() //
	/*  */.end().and() //
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn3)).isNotNull().end() // dateProp
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    ////////////////////////////////// 3.2. Collectional ///////////////////////////////////
    @Test
    public void test_conditions_query_composition_with_a_single_ANY_condition_from_single_collection() {
	queryProperties.get("collection.bigDecimalProp").setOrNull(true);
	final String cbn1 = queryProperties.get("collection.bigDecimalProp").getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin() // collection begins
	/*    */.begin() // ANY block begins
	/*      */.exists( //
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).and().begin()
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ANY block ends
	/*  */.end() // collection ends
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_conditions_query_composition_with_a_couple_of_ANY_conditions_from_single_collection() {
	queryProperties.get("collection.bigDecimalProp").setOrNull(true);
	queryProperties.get("collection.dateProp").setOrNull(true);
	queryProperties.get("collection.masterEntityProp").setOrNull(true);
	final String cbn1 = queryProperties.get("collection.bigDecimalProp").getConditionBuildingName();
	final String cbn2 = queryProperties.get("collection.dateProp").getConditionBuildingName();
	final String cbn3 = queryProperties.get("collection.masterEntityProp").getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin() // collection begins
	/*    */.begin() // ANY block begins
	/*      */.exists( //
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).and().begin()
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn3)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn2)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ANY block ends
	/*  */.end() // collection ends
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_conditions_query_composition_with_a_single_ALL_condition_from_single_collection() {
	queryProperties.get("collection.bigDecimalProp").setOrNull(true);
	queryProperties.get("collection.bigDecimalProp").setAll(true);
	final String cbn1 = queryProperties.get("collection.bigDecimalProp").getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin() // collection begins
	/*    */.begin() // ALL block begins
	/*      */.notExists( //
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).model() //
	/*      */) //
	/*      */.or() //
	/*      */.exists( //
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).and().begin()
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*      */.and() //
	/*      */.notExists( //
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).and().begin()
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNotNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ALL block ends
	/*  */.end() // collection ends
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_conditions_query_composition_with_a_couple_of_ALL_conditions_from_single_collection() {
	// ALL properties
	queryProperties.get("collection.bigDecimalProp").setOrNull(true);
	queryProperties.get("collection.bigDecimalProp").setAll(true);
	queryProperties.get("collection.dateProp").setOrNull(true);
	queryProperties.get("collection.dateProp").setAll(true);
	queryProperties.get("collection.masterEntityProp").setOrNull(true);
	queryProperties.get("collection.masterEntityProp").setAll(true);
	final String cbn1 = queryProperties.get("collection.bigDecimalProp").getConditionBuildingName();
	final String cbn2 = queryProperties.get("collection.dateProp").getConditionBuildingName();
	final String cbn3 = queryProperties.get("collection.masterEntityProp").getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin() // collection begins
	/*    */.begin() // ALL block begins
	/*      */.notExists( //
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).model() //
	/*      */) //
	/*      */.or() //
	/*      */.exists( //
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).and().begin()
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn3)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn2)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*      */.and() //
	/*      */.notExists( //
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).and().begin()
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn3)).isNotNull().end().or() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNotNull().end().or() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn2)).isNotNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ALL block ends
	/*  */.end() // collection ends
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_conditions_query_composition_with_a_couple_of_ALL_and_ANY_conditions_from_single_collection() {
	// ALL properties
	queryProperties.get("collection.bigDecimalProp").setOrNull(true);
	queryProperties.get("collection.bigDecimalProp").setAll(true);
	queryProperties.get("collection.dateProp").setOrNull(true);
	queryProperties.get("collection.dateProp").setAll(true);
	queryProperties.get("collection.masterEntityProp").setOrNull(true);
	queryProperties.get("collection.masterEntityProp").setAll(true);
	final String cbn1 = queryProperties.get("collection.bigDecimalProp").getConditionBuildingName();
	final String cbn2 = queryProperties.get("collection.dateProp").getConditionBuildingName();
	final String cbn3 = queryProperties.get("collection.masterEntityProp").getConditionBuildingName();

	// ANY properties
	queryProperties.get("collection.integerProp").setOrNull(true);
	queryProperties.get("collection.integerProp").setAll(false);
	queryProperties.get("collection.doubleProp").setOrNull(true);
	final String cbn4 = queryProperties.get("collection.integerProp").getConditionBuildingName();
	final String cbn5 = queryProperties.get("collection.doubleProp").getConditionBuildingName();

	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*  */.begin() // collection begins
	/*    */.begin() // ANY block begins
	/*      */.exists( //
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).and().begin()
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
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).model() //
	/*      */) //
	/*      */.or() //
	/*      */.exists( //
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).and().begin()
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn3)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn2)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*      */.and() //
	/*      */.notExists( //
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).and().begin()
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn3)).isNotNull().end().or() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNotNull().end().or() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn2)).isNotNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ALL block ends
	/*  */.end() // collection ends
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_conditions_query_composition_with_a_couple_of_ALL_and_ANY_conditions_from_a_bunch_of_collections_of_different_levels_and_a_bunch_of_simple_properties() { // :)
	/////// Simple properties ///////
	queryProperties.get("bigDecimalProp").setValue(3);
	queryProperties.get("bigDecimalProp").setValue2(7);
	queryProperties.get("integerProp").setOrNull(true);
	queryProperties.get("dateProp").setOrNull(true);
	queryProperties.get("dateProp").setNot(true);

	final String cbn01 = queryProperties.get("bigDecimalProp").getConditionBuildingName();
	final String cbn02 = queryProperties.get("integerProp").getConditionBuildingName();
	final String cbn03 = queryProperties.get("dateProp").getConditionBuildingName();

	////// Collection from master entity //////
	// ALL properties
	queryProperties.get("collection.bigDecimalProp").setOrNull(true);
	queryProperties.get("collection.bigDecimalProp").setAll(true);
	queryProperties.get("collection.dateProp").setOrNull(true);
	queryProperties.get("collection.dateProp").setAll(true);
	queryProperties.get("collection.masterEntityProp").setOrNull(true);
	queryProperties.get("collection.masterEntityProp").setAll(true);
	final String cbn1 = queryProperties.get("collection.bigDecimalProp").getConditionBuildingName();
	final String cbn2 = queryProperties.get("collection.dateProp").getConditionBuildingName();
	final String cbn3 = queryProperties.get("collection.masterEntityProp").getConditionBuildingName();

	// ANY properties
	queryProperties.get("collection.integerProp").setOrNull(true);
	queryProperties.get("collection.integerProp").setAll(false);
	queryProperties.get("collection.doubleProp").setOrNull(true);
	final String cbn4 = queryProperties.get("collection.integerProp").getConditionBuildingName();
	final String cbn5 = queryProperties.get("collection.doubleProp").getConditionBuildingName();

	////// Collection from slave entity //////
	// ALL properties
	queryProperties.get("entityProp.collection.bigDecimalProp").setOrNull(true);
	queryProperties.get("entityProp.collection.bigDecimalProp").setAll(true);
	queryProperties.get("entityProp.collection.dateProp").setOrNull(true);
	queryProperties.get("entityProp.collection.dateProp").setAll(true);
	queryProperties.get("entityProp.collection.slaveEntityProp").setOrNull(true);
	queryProperties.get("entityProp.collection.slaveEntityProp").setAll(true);
	final String cbn6 = queryProperties.get("entityProp.collection.bigDecimalProp").getConditionBuildingName();
	final String cbn7 = queryProperties.get("entityProp.collection.dateProp").getConditionBuildingName();
	final String cbn8 = queryProperties.get("entityProp.collection.slaveEntityProp").getConditionBuildingName();

	// ANY properties
	queryProperties.get("entityProp.collection.integerProp").setOrNull(true);
	queryProperties.get("entityProp.collection.integerProp").setAll(false);
	queryProperties.get("entityProp.collection.doubleProp").setOrNull(true);
	final String cbn9 = queryProperties.get("entityProp.collection.integerProp").getConditionBuildingName();
	final String cbn10 = queryProperties.get("entityProp.collection.doubleProp").getConditionBuildingName();


	final ICompleted expected = //
	/**/iJoin.where().begin() //
	/*                          simple properties below                             */
	/*                          simple properties below                             */
	/*                          simple properties below                             */
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn02)).isNull().end().and() // integerProp
	/*  */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn01)).isNotNull().and() // bigDecimalProp
	/*    */.begin().prop(cbn01).ge().val(3).and().prop(cbn01).le().val(7).end() //
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
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).and().begin()
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
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).model() //
	/*      */) //
	/*      */.or() //
	/*      */.exists( //
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).and().begin()
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn3)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn1)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn2)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*      */.and() //
	/*      */.notExists( //
	/*          */select(SlaveEntity.class).where().prop("masterEntityProp").eq().prop(alias).and().begin()
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
	/*          */select(EvenSlaverEntity.class).where().prop("slaveEntityProp").eq().prop(alias + ".entityProp").and().begin()
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
	/*          */select(EvenSlaverEntity.class).where().prop("slaveEntityProp").eq().prop(alias + ".entityProp").model() //
	/*      */) //
	/*      */.or() //
	/*      */.exists( //
	/*          */select(EvenSlaverEntity.class).where().prop("slaveEntityProp").eq().prop(alias + ".entityProp").and().begin()
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn8)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn6)).isNull().end().and() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn7)).isNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*      */.and() //
	/*      */.notExists( //
	/*          */select(EvenSlaverEntity.class).where().prop("slaveEntityProp").eq().prop(alias + ".entityProp").and().begin()
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn8)).isNotNull().end().or() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn6)).isNotNull().end().or() //
	/*            */.begin().prop(EntityDescriptor.getPropertyNameWithoutKeyPart(cbn7)).isNotNull().end() //
	/*          */.end().model() //
	/*      */) //
	/*    */.end() // ALL block ends
	/*  */.end() // collection ends
	/**/.end(); //
	final ICompleted actual = buildConditions(iJoin, new ArrayList<QueryProperty>(queryProperties.values()), alias);

	// TODO assertEquals("Incorrect query has been built.", expected.model(), actual.model());
	assertEquals("Incorrect query sql has been built.", expected.model().getFinalModelResult(mappingExtractor).getSql(), actual.model().getFinalModelResult(mappingExtractor).getSql());
	assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

}
