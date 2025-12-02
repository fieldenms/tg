package ua.com.fielden.platform.dao.dynamic;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.junit.Before;
import org.junit.Test;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity_centre.mnemonics.DateMnemonicUtils;
import ua.com.fielden.platform.entity_centre.mnemonics.DateRangePrefixEnum;
import ua.com.fielden.platform.entity_centre.mnemonics.DateRangeSelectorEnum;
import ua.com.fielden.platform.entity_centre.mnemonics.MnemonicEnum;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder;
import ua.com.fielden.platform.eql.dbschema.HibernateMappingsGenerator;
import ua.com.fielden.platform.eql.dbschema.PropertyInlinerImpl;
import ua.com.fielden.platform.eql.meta.EqlTables;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.meta.DomainMetadataBuilder;
import ua.com.fielden.platform.meta.DomainMetadataUtils;
import ua.com.fielden.platform.persistence.types.PlatformHibernateTypeMappings;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.interception.MasterEntity_CanRead_unauthorisedProp_Token;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.SecurityRoleAssociationCo;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.test_config.AbstractDaoTestCase;
import ua.com.fielden.platform.utils.IDates;

import java.io.ByteArrayInputStream;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.query.IDbVersionProvider.constantDbVersion;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.*;
import static ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteriaUtils.createCompletedQuery;
import static ua.com.fielden.platform.utils.CollectionUtil.setOf;

/// A test for [DynamicQueryBuilder].
///
@SuppressWarnings({"unchecked"})
public class DynamicQueryBuilderSqlTest extends AbstractDaoTestCase {

    private IDates dates;

    private String alias;
    private Class<? extends AbstractEntity<?>> masterKlass, slaveCollectionType, evenSlaverCollectionType;
    private IJoin<? extends AbstractEntity<?>> iJoin;
    private Map<String, QueryProperty> queryProperties;

    private void init(final EntityFactory entityFactory) {
        alias = "alias_for_main_criteria_type";
        // enhance domain with ALL / ANY calc properties
        final IDomainTreeEnhancer dte = new DomainTreeEnhancer(entityFactory, Set.of(MasterEntity.class));
        dte.addCalculatedProperty(MasterEntity.class, "collection", "masterEntityProp", "Any of masterEntityProp", "Desc", CalculatedPropertyAttribute.ANY, "masterEntityProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dte.addCalculatedProperty(MasterEntity.class, "collection", "bigDecimalProp", "Any of bigDecimalProp", "Desc", CalculatedPropertyAttribute.ANY, "bigDecimalProp", 19, 4);
        dte.addCalculatedProperty(MasterEntity.class, "collection", "dateProp", "Any of dateProp", "Desc", CalculatedPropertyAttribute.ANY, "dateProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dte.addCalculatedProperty(MasterEntity.class, "collection", "integerProp", "Any of integerProp", "Desc", CalculatedPropertyAttribute.ANY, "integerProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dte.addCalculatedProperty(MasterEntity.class, "collection", "moneyProp", "Any of moneyProp", "Desc", CalculatedPropertyAttribute.ANY, "moneyProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dte.addCalculatedProperty(MasterEntity.class, "collection", "masterEntityProp", "All of masterEntityProp", "Desc", CalculatedPropertyAttribute.ALL, "masterEntityProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dte.addCalculatedProperty(MasterEntity.class, "collection", "bigDecimalProp", "All of bigDecimalProp", "Desc", CalculatedPropertyAttribute.ALL, "bigDecimalProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dte.addCalculatedProperty(MasterEntity.class, "collection", "dateProp", "All of dateProp", "Desc", CalculatedPropertyAttribute.ALL, "dateProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dte.addCalculatedProperty(MasterEntity.class, "entityProp.collection", "slaveEntityProp", "All of slaveEntityProp", "Desc", CalculatedPropertyAttribute.ALL, "slaveEntityProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dte.addCalculatedProperty(MasterEntity.class, "entityProp.collection", "bigDecimalProp", "All of bigDecimalProp", "Desc", CalculatedPropertyAttribute.ALL, "bigDecimalProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dte.addCalculatedProperty(MasterEntity.class, "entityProp.collection", "dateProp", "All of dateProp", "Desc", CalculatedPropertyAttribute.ALL, "dateProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dte.addCalculatedProperty(MasterEntity.class, "entityProp.collection", "integerProp", "Any of integerProp", "Desc", CalculatedPropertyAttribute.ANY, "integerProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dte.addCalculatedProperty(MasterEntity.class, "entityProp.collection", "moneyProp", "Any of moneyProp", "Desc", CalculatedPropertyAttribute.ANY, "moneyProp", IsProperty.DEFAULT_PRECISION, IsProperty.DEFAULT_SCALE);
        dte.apply();

        masterKlass = (Class<? extends AbstractEntity<?>>) dte.getManagedType(MasterEntity.class);
        slaveCollectionType = (Class<? extends AbstractEntity<?>>) PropertyTypeDeterminator.determinePropertyType(masterKlass, "collection");
        evenSlaverCollectionType = (Class<? extends AbstractEntity<?>>) PropertyTypeDeterminator.determinePropertyType(masterKlass, "entityProp.collection");

        iJoin = select(select(masterKlass).model().setShouldMaterialiseCalcPropsAsColumnsInSqlQuery(true)).as(alias);
        queryProperties = new LinkedHashMap<>();

        final Configuration hibConf = new Configuration();

        final List<Class<? extends AbstractEntity<?>>> domainTypes = List.of(MasterEntity.class, SlaveEntity.class, EvenSlaverEntity.class);
        final IApplicationDomainProvider appDomain = () -> domainTypes;

        final var dbVersionProvider = constantDbVersion(DbVersion.H2);
        final var domainMetadata = new DomainMetadataBuilder(
                new PlatformHibernateTypeMappings.Provider(dbVersionProvider).get(), domainTypes, dbVersionProvider)
                .build();
        final var domainMetadataUtils = new DomainMetadataUtils(appDomain, domainMetadata);
        try {
            hibConf.addInputStream(new ByteArrayInputStream(
                    new HibernateMappingsGenerator(domainMetadata, domainMetadataUtils,
                                                   dbVersionProvider,
                                                   new EqlTables(domainMetadata, domainMetadataUtils),
                                                   new PropertyInlinerImpl(domainMetadata))
                            .generateMappings().getBytes(UTF_8)));
        } catch (final MappingException e) {
            throw new HibernateException("Could not add mappings.", e);
        }

        final var propertyNames = List.of(
                "integerProp",
                "bigDecimalProp",
                "moneyProp",
                "dateProp",
                "booleanProp",
                "stringProp",
                "entityProp",
                "authorisedProp",
                "unauthorisedProp",
                "entityProp.masterEntityProp",
                "entityProp.integerProp",
                "entityProp.bigDecimalProp",
                "entityProp.moneyProp",
                "entityProp.dateProp",
                "entityProp.booleanProp",
                "entityProp.stringProp",
                "collection.masterEntityProp",
                "collection.integerProp",
                "collection.bigDecimalProp",
                "collection.dateProp",
                "collection.stringProp",
                "collection.booleanProp",
                "collection.anyOfMasterEntityProp",
                "collection.anyOfBigDecimalProp",
                "collection.anyOfDateProp",
                "collection.anyOfIntegerProp",
                "collection.anyOfMoneyProp",
                "collection.allOfMasterEntityProp",
                "collection.allOfBigDecimalProp",
                "collection.allOfDateProp",
                "entityProp.collection.slaveEntityProp",
                "entityProp.collection.integerProp",
                "entityProp.collection.bigDecimalProp",
                "entityProp.collection.dateProp",
                "entityProp.collection.stringProp",
                "entityProp.collection.booleanProp",
                "entityProp.collection.allOfSlaveEntityProp",
                "entityProp.collection.allOfBigDecimalProp",
                "entityProp.collection.allOfDateProp",
                "entityProp.collection.anyOfIntegerProp",
                "entityProp.collection.anyOfMoneyProp");

        for (final var propertyName : propertyNames) {
            final var qp = new QueryProperty(masterKlass, propertyName);
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
        final EntityFactory entityFactory = getInstance(EntityFactory.class);
        dates = getInstance(IDates.class);

        init(entityFactory);

        // make the values to be default for all properties before any test
        for (final var qp : queryProperties.values()) {
            qp.setValue(getEmptyValue(qp.getType(), qp.isSingle()));
            qp.setValue2(getEmptyValue(qp.getType(), qp.isSingle()));
            qp.setExclusive(null);
            qp.setExclusive2(null);
            qp.setDatePrefix(null);
            qp.setDateMnemonic(null);
            qp.setAndBefore(null);
            qp.setOrNull(null);
            qp.setNot(null);
            qp.setOrGroup(null);
        }
    }

    @Test
    public void test_empty_query_composition_for_empty_query_properties() {
        final ICompleted<? extends AbstractEntity<?>> expected = iJoin;
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
        //assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_empty_query_composition_for_boolean_property_with_both_false_values() {
        final QueryProperty property = queryProperties.get("booleanProp");
        property.setValue(false);
        property.setValue2(false);

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin; //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
        //assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void test_empty_query_composition_for_empty_property_with_OrNull_false() {
        final QueryProperty property = queryProperties.get("dateProp");
        property.setValue(null);
        property.setValue2(null);
        property.setOrNull(false);

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin; //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
        //assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void empty_queryProperties_generate_empty_query_even_with_non_empty_orGroups() {
        queryProperties.get("dateProp").setOrGroup(1);
        queryProperties.get("entityProp").setOrGroup(1);
        queryProperties.get("booleanProp").setOrGroup(2);

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin; //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);
        assertEquals(expected.model(), actual.model());
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

        final Date currentDate = new Date();
        final Date from = DateMnemonicUtils.dateOfRangeThatIncludes(currentDate, DateRangeSelectorEnum.BEGINNING, property.getDatePrefix(), property.getDateMnemonic(), dates), //
        /*         */to = DateMnemonicUtils.dateOfRangeThatIncludes(currentDate, DateRangeSelectorEnum.ENDING, property.getDatePrefix(), property.getDateMnemonic(), dates);

        final String cbn = property.getConditionBuildingName();

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(cbn).isNotNull().and() //
        /*    */.condition(cond().prop(cbn).ge().iVal(from).and().prop(cbn).lt().iVal(to).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final Date currentDate = new Date();
        final Date to = DateMnemonicUtils.dateOfRangeThatIncludes(currentDate, DateRangeSelectorEnum.ENDING, property.getDatePrefix(), property.getDateMnemonic(), dates);

        final String cbn = property.getConditionBuildingName();

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(cbn).isNotNull().and() //
        /*    */.condition(cond().prop(cbn).ge().iVal(null).and().prop(cbn).lt().iVal(to).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final Date currentDate = new Date();
        final Date from = DateMnemonicUtils.dateOfRangeThatIncludes(currentDate, DateRangeSelectorEnum.BEGINNING, property.getDatePrefix(), property.getDateMnemonic(), dates); //

        final String cbn = property.getConditionBuildingName();

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(cbn).isNotNull().and() //
        /*    */.condition(cond().prop(cbn).ge().iVal(from).and().prop(cbn).lt().iVal(null).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
        //assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_range_type_with_both_boundaries(final String propertyName) {
        set_up();
        final QueryProperty property = queryProperties.get(propertyName);
        property.setValue(3);
        property.setValue2(7);

        final String cbn = property.getConditionBuildingName();

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(cbn).isNotNull().and() //
        /*    */.condition(cond().prop(cbn).ge().iVal(3).and().prop(cbn).le().iVal(7).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(cbn).isNotNull().and() //
        /*    */.condition(cond().prop(cbn).gt().iVal(3).and().prop(cbn).le().iVal(7).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(cbn).isNotNull().and() //
        /*    */.condition(cond().prop(cbn).ge().iVal(3).and().prop(cbn).lt().iVal(7).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(cbn).isNotNull().and() //
        /*    */.condition(cond().prop(cbn).gt().iVal(3).and().prop(cbn).lt().iVal(7).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(cbn).isNotNull().and() //
        /*    */.condition(cond().prop(cbn).eq().val(false).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
        //assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    private void test_atomic_query_composition_for_boolean_type_with_right_value_as_False(final String propertyName) {
        set_up();
        final QueryProperty property = queryProperties.get(propertyName);
        property.setValue2(false);

        final String cbn = property.getConditionBuildingName();

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(cbn).isNotNull().and() //
        /*    */.condition(cond().prop(cbn).eq().val(true).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
        //assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    ////////////////////////////////// 1.3.-1.4. String / Entity types /////////////////////////////////////////

    @Test
    public void query_composition_for_property_of_type_string_without_wildchards_in_crit_value_automatically_injects_wildcards_at_the_beginning_and_end() {
        //"entityProp.stringProp"
        set_up();
        final QueryProperty property = queryProperties.get("stringProp");
        property.setValue("Some string value");

        final String cbn = property.getConditionBuildingName();

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(cbn).isNotNull().and() //
        /*    */.condition(cond().prop(cbn).iLike().anyOfValues(new Object[] { "%Some string value%" }).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
    }

    @Test
    public void query_composition_for_property_of_type_string_with_wildchards_in_crit_value_preserves_the_original_placement_of_wildcards_and_does_not_inject_them_at_the_beginning_and_end() {
        //"entityProp.stringProp"
        set_up();
        final QueryProperty property = queryProperties.get("stringProp");
        final String critValue = "Some string value*,*Some string value,Some string *value,*Some string value*";
        property.setValue(critValue);

        final String cbn = property.getConditionBuildingName();

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(cbn).isNotNull().and() //
        /*    */.condition(cond().prop(cbn).iLike().anyOfValues(critValue.replace("*", "%").split(",")).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
    }

    @Test
    public void query_composition_for_property_of_type_string_with_and_without_wildchards_in_crit_values_retain_original_whildcards_and_autoinject_wildcards_at_the_beginning_and_end_for_values_with_no_wildcards() {
        //"entityProp.stringProp"
        set_up();
        final QueryProperty property = queryProperties.get("stringProp");
        property.setValue("Some string value,*Some string value,Some string *value,*Some string* value");

        final String cbn = property.getConditionBuildingName();

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(cbn).isNotNull().and() //
        /*    */.condition(cond().prop(cbn).iLike().anyOfValues(new String[] {"%Some string value%", "%Some string value", "Some string %value", "%Some string% value"}).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
    }


    @Test
    public void query_composition_for_properties_of_entity_type_with_wildcard_selection_crit_value_uses_iLike_operator() {
        final String propertyName = "entityProp";

        set_up();
        final QueryProperty property = queryProperties.get(propertyName);
        final var values = List.of("some val 1*", "some val 2*");
        property.setValue(values);

        final String cbn = property.getConditionBuildingName();
        final var cbnNoKey = getPropertyNameWithoutKeyPart(cbn);

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(cbnNoKey).isNotNull().and() //
        /*    */.condition(cond().prop(cbnNoKey).in().model(select(SlaveEntity.class).where().prop(KEY).iLike().anyOfValues(prepCritValuesForEntityTypedProp(values)).model()).model())
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
    }

    @Test
    public void query_composition_for_properties_of_entity_type_without_wildcard_selection_crit_value_uses_in_operator_with_subselect() {
        final String propertyName = "entityProp";

        set_up();
        final QueryProperty property = queryProperties.get(propertyName);
        final String[] critValues = new String[] {"some val 1", "some val 2"};
        property.setValue(Arrays.asList(critValues));


        final String cbn = property.getConditionBuildingName();
        final String cbnNoKey = cbn.substring(0, cbn.length() - 4); // cut off ".key" from the name

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn)).isNotNull().and() //
        /*  */.condition(cond().prop(cbnNoKey).in().model(select(SlaveEntity.class).where().prop("key").in().values("some val 1", "some val 2").model()).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
    }

    @Test
    public void query_composition_for_properties_of_entity_type_with_and_without_wildcard_selection_crit_value_uses_combination_of_in_operator_with_subselect_and_iLike_operator() {
        final String propertyName = "entityProp";

        set_up();
        final QueryProperty property = queryProperties.get(propertyName);
        final String[] critValues = new String[] {"some val 1", "some val 2*", "some val 3*", "some val 4",};
        property.setValue(Arrays.asList(critValues));

        final String[] critValuesWithWildcard = new String[] {"some val 2*", "some val 3*"};

        final String cbn = property.getConditionBuildingName();
        final String cbnNoKey = cbn.substring(0, cbn.length() - 4); // cut off ".key" from the name


        final EntityResultQueryModel<SlaveEntity> subSelect = select(SlaveEntity.class).where().prop("key").in().values("some val 1", "some val 4").model();
        final ConditionModel whereCondition = cond()
                .condition(
                        cond().prop(getPropertyNameWithoutKeyPart(cbn)).isNotNull()
                            .and()
                            .condition(
                                cond().prop(cbnNoKey).in().model(subSelect)
                                .or().prop(cbnNoKey).in().model(select(SlaveEntity.class).where().prop(KEY).iLike().anyOfValues(prepCritValuesForEntityTypedProp(Arrays.asList(critValuesWithWildcard))).model())
                                .model())
                        .model())
                .model();

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(whereCondition);

        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
    }


    //---------------------------------------------------------------------------------------//
    //------------------------------ 2. Property level (Negation / Null) --------------------//
    //---------------------------------------------------------------------------------------//

    //------------------------------- 2.1. Is [Not] Null ------------------------------------//
    @Test
    public void test_property_query_composition_with_missing_value() {
        final String propertyName = "dateProp";
        final QueryProperty property = queryProperties.get(propertyName);
        property.setOrNull(true);

        final String cbn = property.getConditionBuildingName();

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn)).isNull() //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn)).isNotNull() //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
        //assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    //-------------------- 2.2. Direct conditions and Negation / Nulls ----------------------//

    @Test
    public void test_property_query_composition_with_direct_condition() {
        final String propertyName = "bigDecimalProp";
        final QueryProperty property = queryProperties.get(propertyName);
        property.setValue(3);
        property.setValue2(7);

        final String cbn = property.getConditionBuildingName();

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn)).isNotNull().and() //
        /*    */.condition(cond().prop(cbn).ge().iVal(3).and().prop(cbn).le().iVal(7).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn)).isNull().or() //
        /*    */.negatedCondition(cond().prop(cbn).ge().iVal(3).and().prop(cbn).le().iVal(7).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn)).isNull().or() //
        /*    */.condition(cond().prop(cbn).ge().iVal(3).and().prop(cbn).le().iVal(7).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn)).isNotNull().and() //
        /*    */.negatedCondition(cond().prop(cbn).ge().iVal(3).and().prop(cbn).le().iVal(7).model()) //
        /*  */.model()) //
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
        //assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    //---------------------------------------------------------------------------------------//
    //-------------------------------- 3. Conditions level ----------------------------------//
    //---------------------------------------------------------------------------------------//

    //------------------------------- 3.1. Non-collectional ---------------------------------//
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

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn2)).isNull().model()).and() // integerProp
        /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn1)).isNotNull().and() // bigDecimalProp
        /*    */.condition(cond().prop(cbn1).ge().iVal(3).and().prop(cbn1).le().iVal(7).model()) //
        /*  */.model()).and() //
        /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn3)).isNotNull().model()) // dateProp
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
        //assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void query_is_composed_from_a_sequence_of_nonOrGrouped_conditions_followed_by_orGroup_conditions_with_increasing_group_number_and_wrapped_by_separate_EQL_condition_construct() {
        // Properties sequence is the following:
        //
        // integerProp
        // bigDecimalProp
        // moneyProp
        // dateProp
        // booleanProp
        // stringProp
        // entityProp
        // ...
        // entityProp.booleanProp
        // ...

        // with OR groups
        queryProperties.get("bigDecimalProp").setValue(3);
        queryProperties.get("bigDecimalProp").setValue2(7);
        queryProperties.get("bigDecimalProp").setOrGroup(1);

        queryProperties.get("integerProp").setOrNull(true);
        queryProperties.get("integerProp").setOrGroup(9);

        queryProperties.get("dateProp").setOrNull(true);
        queryProperties.get("dateProp").setNot(true);
        queryProperties.get("dateProp").setOrGroup(1);

        // with invalid OR groups
        queryProperties.get("moneyProp").setOrNull(true);
        queryProperties.get("moneyProp").setOrGroup(0);

        queryProperties.get("entityProp.booleanProp").setOrNull(true);
        queryProperties.get("entityProp.booleanProp").setOrGroup(10);

        // without OR groups
        queryProperties.get("stringProp").setOrNull(true);

        queryProperties.get("entityProp").setOrNull(true);

        final String moneyProp = queryProperties.get("moneyProp").getConditionBuildingName();
        final String stringProp = queryProperties.get("stringProp").getConditionBuildingName();
        final String entityProp = queryProperties.get("entityProp").getConditionBuildingName();
        final String entityProp_booleanProp = queryProperties.get("entityProp.booleanProp").getConditionBuildingName();
        final String bigDecimalProp = queryProperties.get("bigDecimalProp").getConditionBuildingName();
        final String dateProp = queryProperties.get("dateProp").getConditionBuildingName();
        final String integerProp = queryProperties.get("integerProp").getConditionBuildingName();

        final ICompleted<? extends AbstractEntity<?>> expected =
        /**/iJoin.where().condition(cond()
        /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(moneyProp)).isNull().model())
        /*  */.and()
        /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(stringProp)).isNull().model())
        /*  */.and()
        /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(entityProp)).isNull().model())
        /*  */.and()
        /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(entityProp_booleanProp)).isNull().model())
        /*  */.and()
        /*  */.condition(cond() // Group 1
        /*    */.condition(cond().prop(getPropertyNameWithoutKeyPart(bigDecimalProp)).isNotNull().and()
        /*      */.condition(cond().prop(bigDecimalProp).ge().iVal(3).and().prop(bigDecimalProp).le().iVal(7).model())
        /*    */.model())
        /*    */.or()
        /*    */.condition(cond().prop(getPropertyNameWithoutKeyPart(dateProp)).isNotNull().model())
        /*  */.model())
        /*  */.and()
        /*  */.condition(cond() // Group 9
        /*    */.condition(cond().prop(getPropertyNameWithoutKeyPart(integerProp)).isNull().model())
        /*  */.model())
        /**/.model());
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals(expected.model(), actual.model());
    }

    ////////////////////////////////// 3.2. Collectional ///////////////////////////////////
    @Test
    public void test_conditions_query_composition_with_a_couple_of_collectional_conditions_that_are_irrelevant_without_ALL_or_ANY_condition() {
        // Collection FILTERING properties
        queryProperties.get("collection.bigDecimalProp").setOrNull(true);
        queryProperties.get("collection.dateProp").setOrNull(true);
        queryProperties.get("collection.masterEntityProp").setOrNull(true);
        queryProperties.get("collection.stringProp").setOrNull(true);

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin; //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond() // collection begins
        /*    */.condition(cond() // ANY block begins
        /*      */.exists( //
        /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
        /*          */.model()).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn1)).isNull().model()) //
        /*          */.model()).model() //
        /*      */) //
        /*    */.model()) // ANY block ends
        /*  */.model()) // collection ends
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond() // collection begins
        /*    */.condition(cond() // ANY block begins
        /*      */.exists( //
        /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
        /*          */.model()).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn1)).isNull().model()).and() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn2)).isNull().model()).and() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn3)).isNull().model()) //
        /*          */.model()).model() //
        /*      */) //
        /*    */.model()) // ANY block ends
        /*  */.model()) // collection ends
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond() // collection begins
        /*    */.condition(cond() // ALL block begins
        /*      */.notExists( //
        /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
        /*          */.model()).model() //
        /*      */) //
        /*      */.or() //
        /*      */.exists( //
        /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
        /*          */.model()).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn1)).isNull().model()) //
        /*          */.model()).model() //
        /*      */) //
        /*      */.and() //
        /*      */.notExists( //
        /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
        /*          */.model()).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn1)).isNotNull().model()) //
        /*          */.model()).model() //
        /*      */) //
        /*    */.model()) // ALL block ends
        /*  */.model()) // collection ends
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond() // collection begins
        /*    */.condition(cond() // ALL block begins
        /*      */.notExists( //
        /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
        /*          */.model()).model() //
        /*      */) //
        /*      */.or() //
        /*      */.exists( //
        /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
        /*          */.model()).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn3)).isNull().model()).and() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn1)).isNull().model()).and() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn2)).isNull().model()) //
        /*          */.model()).model() //
        /*      */) //
        /*      */.and() //
        /*      */.notExists( //
        /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
        /*          */.model()).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn3)).isNotNull().model()).or() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn1)).isNotNull().model()).or() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn2)).isNotNull().model()) //
        /*          */.model()).model() //
        /*      */) //
        /*    */.model()) // ALL block ends
        /*  */.model()) // collection ends
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*  */.condition(cond() // collection begins
        /*    */.condition(cond() // ANY block begins
        /*      */.exists( //
        /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
        /*          */.model()).and() //
        /*          */.condition(cond() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn4)).isNull().model()).and() //
        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn5)).isNull().model()) //
        /*          */.model()).model() //
        /*      */) //
        /*    */.model()) // ANY block ends
        /*    */
                /*    */.and() //
                /*    */
                /*    */.condition(cond() // ALL block begins
                /*      */.notExists( //
                /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
                /*          */.condition(cond() //
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
                /*          */.model()).model() //
                /*      */) //
                /*      */.or() //
                /*      */.exists( //
                /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
                /*          */.condition(cond() //
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
                /*          */.model()).and() //
                /*          */.condition(cond() //
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn3)).isNull().model()).and() //
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn1)).isNull().model()).and() //
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn2)).isNull().model()) //
                /*          */.model()).model() //
                /*      */) //
                /*      */.and() //
                /*      */.notExists( //
                /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and() //
                /*          */.condition(cond() //
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
                /*          */.model()).and() //
                /*          */.condition(cond() //
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn3)).isNotNull().model()).or() //
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn1)).isNotNull().model()).or() //
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn2)).isNotNull().model()) //
                /*          */.model()).model() //
                /*      */) //
                /*    */.model()) // ALL block ends
                /*  */.model()) // collection ends
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

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

        final ICompleted<? extends AbstractEntity<?>> expected = //
        /**/iJoin.where().condition(cond() //
        /*                          simple properties below                             */
                /*                          simple properties below                             */
                /*                          simple properties below                             */
                /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn02)).isNull().model()).and() // integerProp
                /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn01)).isNotNull().and() // bigDecimalProp
                /*    */.condition(cond().prop(cbn01).ge().iVal(3).and().prop(cbn01).le().iVal(7).model()) //
                /*  */.model()).and() //
                /*  */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn03)).isNotNull().model()).and() // dateProp
                /*  */
                /*                          master collection below                             */
                /*                          master collection below                             */
                /*                          master collection below                             */
                /*  */
                /*  */.condition(cond() // collection begins
                /*    */.condition(cond() // ANY block begins
                /*      */.exists( //
                /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and().condition(cond()
                        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn4)).isNull().model()).and() //
                        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn5)).isNull().model()) //
                        /*          */.model()).model() //
                /*      */) //
                /*    */.model()) // ANY block ends
                /*    */
                        /*    */.and() //
                        /*    */
                        /*    */.condition(cond() // ALL block begins
                        /*      */.notExists( //
                        /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).model() //
                        /*      */) //
                        /*      */.or() //
                        /*      */.exists( //
                        /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and().condition(cond()
                                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn3)).isNull().model()).and() //
                                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn1)).isNull().model()).and() //
                                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn2)).isNull().model()) //
                                /*          */.model()).model() //
                        /*      */) //
                        /*      */.and() //
                        /*      */.notExists( //
                        /*          */select(slaveCollectionType).where().prop("masterEntityProp").eq().prop(alias).and().condition(cond()
                                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn3)).isNotNull().model()).or() //
                                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn1)).isNotNull().model()).or() //
                                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn2)).isNotNull().model()) //
                                /*          */.model()).model() //
                        /*      */) //
                        /*    */.model()) // ALL block ends
                        /*  */.model()) // collection ends
                /**/
                /**/
                /**/.and() //
                /**/
                /*                           slave collection below                             */
                /*                           slave collection below                             */
                /*                           slave collection below                             */
                /**/
                /*  */.condition(cond() // collection begins
                /*    */.condition(cond() // ANY block begins
                /*      */.exists( //
                /*          */select(evenSlaverCollectionType).where().prop("slaveEntityProp").eq().prop(alias + ".entityProp").and() //
                /*          */.condition(cond() //
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
                /*          */.model()).and() //
                /*          */.condition(cond() //
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn9)).isNull().model()).and() //
                /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn10)).isNull().model()) //
                /*          */.model()).model() //
                /*      */) //
                /*    */.model()) // ANY block ends
                /*    */
                        /*    */.and() //
                        /*    */
                        /*    */.condition(cond() // ALL block begins
                        /*      */.notExists( //
                        /*          */select(evenSlaverCollectionType).where().prop("slaveEntityProp").eq().prop(alias + ".entityProp").and() //
                        /*          */.condition(cond() //
                        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
                        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
                        /*          */.model()).model() //
                        /*      */) //
                        /*      */.or() //
                        /*      */.exists( //
                        /*          */select(evenSlaverCollectionType).where().prop("slaveEntityProp").eq().prop(alias + ".entityProp").and() //
                        /*          */.condition(cond() //
                        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
                        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
                        /*          */.model()).and() //
                        /*          */.condition(cond() //
                        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn8)).isNull().model()).and() //
                        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn6)).isNull().model()).and() //
                        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn7)).isNull().model()) //
                        /*          */.model()).model() //
                        /*      */) //
                        /*      */.and() //
                        /*      */.notExists( //
                        /*          */select(evenSlaverCollectionType).where().prop("slaveEntityProp").eq().prop(alias + ".entityProp").and() //
                        /*          */.condition(cond() //
                        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering1)).isNull().model()).and() // FILTERING
                        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbnFiltering2)).isNull().model()) // FILTERING
                        /*          */.model()).and() //
                        /*          */.condition(cond() //
                        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn8)).isNotNull().model()).or() //
                        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn6)).isNotNull().model()).or() //
                        /*            */.condition(cond().prop(getPropertyNameWithoutKeyPart(cbn7)).isNotNull().model()) //
                        /*          */.model()).model() //
                        /*      */) //
                        /*    */.model()) // ALL block ends
                        /*  */.model()) // collection ends
                /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(masterKlass, new ArrayList<>(queryProperties.values()), dates);

        assertEquals("Incorrect query model has been built.", expected.model(), actual.model());
        //assertEquals("Incorrect query parameter values has been built.", expected.model().getFinalModelResult(mappingExtractor).getParamValues(), actual.model().getFinalModelResult(mappingExtractor).getParamValues());
    }

    @Test
    public void conditions_for_union_members_are_combined_with_logical_AND() {
        final List<String> propertyNames = List.of(
                "",
                "desc",
                "location",
                "location.workshop",
                "location.workshop.desc",
                "location.wagonSlot",
                "location.wagonSlot.wagon",
                "location.wagonSlot.position");
        final Map<String, QueryProperty> unionProps = new LinkedHashMap<>();
        for (final String propertyName : propertyNames) {
            final QueryProperty qp = new QueryProperty(TgBogie.class, propertyName);
            unionProps.put(propertyName, qp);
            qp.setValue(getEmptyValue(qp.getType(), qp.isSingle()));
            qp.setValue2(getEmptyValue(qp.getType(), qp.isSingle()));
        }

        QueryProperty property = null;

        property = unionProps.get("");
        property.setValue(List.of("some val 1*", "some val 2*"));

        property = unionProps.get("desc");
        property.setValue("Some string value");

        property = unionProps.get("location");
        property.setValue(List.of("some val 1*", "some val 2*"));

        property = unionProps.get("location.workshop");
        property.setValue(List.of("some val 1*", "some val 2*"));

        property = unionProps.get("location.workshop.desc");
        property.setValue("Some string value");

        property = unionProps.get("location.wagonSlot");
        property.setValue(List.of("some val 1*", "some val 2*"));

        property = unionProps.get("location.wagonSlot.wagon");
        property.setValue(List.of("some val 1*", "some val 2*"));

        property = unionProps.get("location.wagonSlot.position");
        property.setValue(3);
        property.setValue2(7);

        final ICompleted<? extends AbstractEntity<?>> expected =
        /**/select(select(TgBogie.class).model().setShouldMaterialiseCalcPropsAsColumnsInSqlQuery(true)).as(alias).where().condition(cond() //
        /*  */.condition(cond().prop(alias).isNotNull().and() //
           /*    */.condition(cond().prop(alias).in().model(select(TgBogie.class).where().prop(KEY).iLike().anyOfValues("some val 1%", "some val 2%").model()).model())//
        /*  */.model()).and()//
        /*  */.condition(cond().prop(alias + ".desc").isNotNull().and() //
        /*    */.condition(cond().prop(alias + ".desc").iLike().anyOfValues("%Some string value%").model()) //
        /*  */.model()).and()//
        /*  */.condition(cond().prop(alias + ".location").isNotNull().and() //
        /*    */.condition(cond().prop(alias + ".location.id").in().model(select(TgBogieLocation.class).where().prop(KEY).iLike().anyOfValues("some val 1%", "some val 2%").model()).model())//
        /*  */.model()).and()//
        /*    */.condition(cond().prop(alias + ".location.workshop").isNotNull().and()//
        /*	    */.condition(cond().prop(alias + ".location.workshop").in().model(select(TgWorkshop.class).where().prop(KEY).iLike().anyOfValues("some val 1%", "some val 2%").model()).model())//
        /*	  */.model()).and()//
        /*	  */.condition(cond().prop(alias + ".location.workshop.desc").isNotNull().and()//
        /*	    */.condition(cond().prop(alias + ".location.workshop.desc").iLike().anyOfValues("%Some string value%").model())
                /*	  */.model())//
        /*    */.and() // this used to be OR
        /*	  */.condition(cond().prop(alias + ".location.wagonSlot").isNotNull().and()//
        /*	    */.condition(cond().prop(alias + ".location.wagonSlot").in().model(select(TgWagonSlot.class).where().prop(KEY).iLike().anyOfValues("some val 1%", "some val 2%").model()).model())//
        /*	  */.model()).and()//
        /*	  */.condition(cond().prop(alias + ".location.wagonSlot.wagon").isNotNull().and()//
        /*	    */.condition(cond().prop(alias + ".location.wagonSlot.wagon").in().model(select(TgWagon.class).where().prop(KEY).iLike().anyOfValues("some val 1%", "some val 2%").model()).model())//
        /*	  */.model()).and()//
        /*	  */.condition(cond().prop(alias + ".location.wagonSlot.position").isNotNull().and()//
        /*	    */.condition(cond().prop(alias + ".location.wagonSlot.position").ge().iVal(3).and().prop(alias + ".location.wagonSlot.position").le().iVal(7).model())//
        /*	  */.model())//
        /**/.model()); //
        final ICompleted<? extends AbstractEntity<?>> actual = createQuery(TgBogie.class, new ArrayList<>(unionProps.values()), dates);
        assertEquals("Incorrect query model for union entities has been built.", expected.model(), actual.model());
    }

    //-------------------------  property authorisation tests -------------------------------//
    @Test
    public void authorised_prop_should_be_present_in_query() {
        test_atomic_query_composition_for_range_type("authorisedProp");
    }

    @Test
    public void unauthorised_prop_throws_authorisation_exception() {
        removeAccessTo(MasterEntity_CanRead_unauthorisedProp_Token.class);

        final QueryProperty property = queryProperties.get("unauthorisedProp");
        property.setValue(3);
        property.setValue2(7);

        assertThatCode(() -> createCompletedQuery(MasterEntity.class, (Class<MasterEntity>) masterKlass, new ArrayList<>(queryProperties.values()), empty(), empty(), empty(), dates))
                .hasMessage("Permission denied due to token [Master Entity Can Read property [unauthorisedProp]] restriction.");
    }

    @Test
    public void unauthorised_prop_initiated_with_mnemonics_throws_authorisation_exception() {
        removeAccessTo(MasterEntity_CanRead_unauthorisedProp_Token.class);

        final QueryProperty property = queryProperties.get("unauthorisedProp");
        property.setOrNull(true);

        assertThatCode(() -> createCompletedQuery(MasterEntity.class, (Class<MasterEntity>) masterKlass, new ArrayList<>(queryProperties.values()), empty(), empty(), empty(), dates))
                .hasMessage("Permission denied due to token [Master Entity Can Read property [unauthorisedProp]] restriction.");

    }

    /// This is more like just in case `token` would get associated with `UNIT_TEST_ROLE` by the base test class logic.
    ///
    private void removeAccessTo(final Class<? extends ISecurityToken> token) {
        final SecurityRoleAssociationCo co = co(SecurityRoleAssociation.class);
        co.removeAssociations(setOf(
                co.new_()
                        .setRole(co(UserRole.class).findByKey(UNIT_TEST_ROLE))
                        .setSecurityToken(token)
        ));
    }

}
