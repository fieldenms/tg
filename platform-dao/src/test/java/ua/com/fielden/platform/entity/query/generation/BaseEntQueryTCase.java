package ua.com.fielden.platform.entity.query.generation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.hibernate.type.TypeFactory;
import org.hibernate.type.YesNoType;

import ua.com.fielden.platform.dao.DomainPersistenceMetadata;
import ua.com.fielden.platform.dao.DomainPersistenceMetadataAnalyser;
import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.generation.elements.AbstractSource.PropResolutionInfo;
import ua.com.fielden.platform.entity.query.generation.elements.AbstractSource.PurePropInfo;
import ua.com.fielden.platform.entity.query.generation.elements.ComparisonTest;
import ua.com.fielden.platform.entity.query.generation.elements.EntProp;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.EntValue;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.ISource;
import ua.com.fielden.platform.entity.query.generation.elements.OperandsBasedSet;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.types.Money;

import com.google.inject.Guice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BaseEntQueryTCase {
    protected static final Class<TgWorkOrder> WORK_ORDER = TgWorkOrder.class;
    protected static final Class<TgVehicle> VEHICLE = TgVehicle.class;
    protected static final Class<TgVehicleFinDetails> VEHICLE_FIN_DETAILS = TgVehicleFinDetails.class;
    protected static final Class<TgVehicleModel> MODEL = TgVehicleModel.class;
    protected static final Class<TgVehicleMake> MAKE = TgVehicleMake.class;
    protected static final Class<TgFuelUsage> FUEL_USAGE = TgFuelUsage.class;
    protected static final Class<TgOrgUnit5> ORG5 = TgOrgUnit5.class;
    protected static final Class<TgOrgUnit4> ORG4 = TgOrgUnit4.class;
    protected static final Class<TgOrgUnit3> ORG3 = TgOrgUnit3.class;
    protected static final Class<TgOrgUnit2> ORG2 = TgOrgUnit2.class;
    protected static final Class<TgOrgUnit1> ORG1 = TgOrgUnit1.class;
    protected static final Class<String> STRING = String.class;
    protected static final Class<Date> DATE = Date.class;
    protected static final Class<Long> LONG = Long.class;
    protected static final Class<BigInteger> BIG_INTEGER = BigInteger.class;
    protected static final Class<BigDecimal> BIG_DECIMAL = BigDecimal.class;
    protected static final Type H_LONG = Hibernate.LONG;
    protected static final Type H_STRING = Hibernate.STRING;
    protected static final Type H_BIG_DECIMAL = Hibernate.BIG_DECIMAL;
    protected static final Type H_BIG_INTEGER = Hibernate.BIG_INTEGER;

    public static final Map<Class, Class> hibTypeDefaults = new HashMap<Class, Class>();

    static {
	hibTypeDefaults.put(boolean.class, YesNoType.class);
	hibTypeDefaults.put(Boolean.class, YesNoType.class);
	hibTypeDefaults.put(Date.class, DateTimeType.class);
	hibTypeDefaults.put(Money.class, SimpleMoneyType.class);
    }

    protected static Type hibtype(final String name) {
	return TypeFactory.basic(name);
    }

    private static final DomainPersistenceMetadata DOMAIN_PERSISTENCE_METADATA = new DomainPersistenceMetadata(hibTypeDefaults, Guice.createInjector(new HibernateUserTypesModule()), PlatformTestDomainTypes.entityTypes);

    protected static final DomainPersistenceMetadataAnalyser DOMAIN_PERSISTENCE_METADATA_ANALYSER = new DomainPersistenceMetadataAnalyser(DOMAIN_PERSISTENCE_METADATA);

    private static final EntQueryGenerator qb = new EntQueryGenerator(DbVersion.H2, DOMAIN_PERSISTENCE_METADATA_ANALYSER, null, null);

    private static final EntQueryGenerator qbwf = new EntQueryGenerator(DbVersion.H2, DOMAIN_PERSISTENCE_METADATA_ANALYSER, new SimpleUserFilter(), null);

    protected static EntQuery entSourceQry(final QueryModel qryModel) {
	return qb.generateEntQueryAsSourceQuery(qryModel);
    }

    protected static EntQuery entSourceQry(final QueryModel qryModel, final Map<String, Object> paramValues) {
	return qb.generateEntQueryAsSourceQuery(qryModel, paramValues);
    }

    protected static EntQuery entResultQry(final QueryModel qryModel) {
	return qb.generateEntQueryAsResultQuery(qryModel);
    }

    protected static EntQuery entResultQry(final QueryModel qryModel, final OrderingModel orderModel) {
	return qb.generateEntQueryAsResultQuery(qryModel, orderModel, new HashMap<String, Object>());
    }

    protected static EntQuery entResultQry(final QueryModel qryModel, final Map<String, Object> paramValues) {
	return qb.generateEntQueryAsResultQuery(qryModel, paramValues);
    }

    protected static EntQuery entSubQry(final QueryModel qryModel) {
	return qb.generateEntQueryAsSubquery(qryModel);
    }

    protected static EntQuery entResultQryWithUserFilter(final QueryModel qryModel) {
	return qbwf.generateEntQueryAsResultQuery(qryModel);
    }

    protected static EntProp prop(final String propName) {
	return new EntProp(propName);
    }

    protected static EntValue val(final Object value) {
	return new EntValue(value);
    }

    protected static OperandsBasedSet set(final ISingleOperand ... operands) {
	return new OperandsBasedSet(Arrays.asList(operands));
    }

    protected static PurePropInfo ppi(final String name, final Class type, final Object hibType, final boolean nullable) {
	return new PurePropInfo(name, type, hibType, nullable);
    }

    protected static PropResolutionInfo propResInf(final String propName, final String aliasPart, final PurePropInfo propPart, final PurePropInfo explicitPropPart) {
	return new PropResolutionInfo(prop(propName), aliasPart, propPart, explicitPropPart);
    }

    protected static PropResolutionInfo propResInf(final String propName, final String aliasPart, final PurePropInfo propPart) {
	return new PropResolutionInfo(prop(propName), aliasPart, propPart, propPart);
    }

    protected static PropResolutionInfo impIdPropResInf(final String propName, final String aliasPart, final PurePropInfo propPart, final PurePropInfo explicitPropPart) {
	return new PropResolutionInfo(prop(propName), aliasPart, propPart, explicitPropPart, true);
    }

    protected static PropResolutionInfo impIdPropResInf(final String propName, final String aliasPart, final PurePropInfo propPart) {
	return new PropResolutionInfo(prop(propName), aliasPart, propPart, propPart, true);
    }

    protected final ComparisonTest alwaysTrueCondition = new ComparisonTest(new EntValue(0), ComparisonOperator.EQ, new EntValue(0));

    protected List<List<PropResolutionInfo>> getSourcesReferencingProps(final EntQuery entQry) {
	final List<List<PropResolutionInfo>> result = new ArrayList<List<PropResolutionInfo>>();
	for (final ISource source : entQry.getSources().getAllSources()) {
	    if (!source.generated()) {
		result.add(source.getReferencingProps());
	    }
	}

	return result;
    }

    protected List<List<PropResolutionInfo>> getSourcesFinalReferencingProps(final EntQuery entQry) {
	final List<List<PropResolutionInfo>> result = new ArrayList<List<PropResolutionInfo>>();
	for (final ISource source : entQry.getSources().getAllSources()) {
	    // FIXME result.add(source.getFinalReferencingProps());
	}

	return result;
    }

    protected final List<PropResolutionInfo> prepare(final PropResolutionInfo ...infos ) {
	return Arrays.asList(infos);
    }

    protected final List<List<PropResolutionInfo>> compose(final List<PropResolutionInfo> ...srcLists) {
	return Arrays.asList(srcLists);
    }

    public static void assertModelsEquals(final QueryModel shortcutModel, final QueryModel explicitModel) {
	final EntQuery shortcutQry = entResultQry(shortcutModel);
	final EntQuery explicitQry = entResultQry(explicitModel);
	assertTrue(("Query models are different!\nShortcut:\n" + shortcutQry.toString() + "\nExplicit:\n" + explicitQry.toString()), shortcutQry.equals(explicitQry));

    }

    public static void assertModelsEqualsAccordingUserDataFiltering(final QueryModel shortcutModel, final QueryModel explicitModel) {
	final EntQuery shortcutQry = entResultQryWithUserFilter(shortcutModel);
	final EntQuery explicitQry = entResultQry(explicitModel);
	assertTrue(("Query models are different!\nShortcut:\n" + shortcutQry.toString() + "\nExplicit:\n" + explicitQry.toString()), shortcutQry.equals(explicitQry));

    }

    public static void assertModelsDifferent(final QueryModel shortcutModel, final QueryModel explicitModel) {
	final EntQuery shortcutQry = entResultQry(shortcutModel);
	final EntQuery explicitQry = entResultQry(explicitModel);
	assertFalse(("Query models are equal! exp: " + shortcutQry.toString() + " act: " + explicitQry.toString()), shortcutQry.equals(explicitQry));
    }

    public static void assertPropInfoEquals(final QueryModel qryModel, final String propName, final PropResolutionInfo propResInfo) {
	final PropResolutionInfo act = entResultQry(qryModel).getSources().getMain().containsProperty(prop(propName));
	assertEquals(("Prop resolution infos are different! exp: " + propResInfo.toString() + " act: " + act.toString()), propResInfo, act);
    }

    public static void assertPropInfoDifferent(final QueryModel qryModel, final String propName, final PropResolutionInfo propResInfo) {
	final PropResolutionInfo act = entResultQry(qryModel).getSources().getMain().containsProperty(prop(propName));
	assertFalse(("Prop resolution infos are equal! exp: " + propResInfo.toString() + " act: " + act.toString()), propResInfo.equals(act));
    }

}