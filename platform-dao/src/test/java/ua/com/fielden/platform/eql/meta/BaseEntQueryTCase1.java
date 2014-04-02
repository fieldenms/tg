package ua.com.fielden.platform.eql.meta;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.type.Type;
import org.hibernate.type.TypeFactory;
import org.hibernate.type.YesNoType;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.PropertyColumn;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.dao.PropertyMetadata.PropertyCategory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.generation.elements.AbstractSource.PropResolutionInfo;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.s1.elements.EntParam1;
import ua.com.fielden.platform.eql.s1.elements.EntProp1;
import ua.com.fielden.platform.eql.s1.elements.EntQuery1;
import ua.com.fielden.platform.eql.s1.elements.EntValue1;
import ua.com.fielden.platform.eql.s1.elements.Expression1;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand1;
import ua.com.fielden.platform.eql.s1.elements.OperandsBasedSet1;
import ua.com.fielden.platform.eql.s1.processing.EntQueryGenerator1;
import ua.com.fielden.platform.eql.s1.processing.StandAloneExpressionBuilder1;
import ua.com.fielden.platform.eql.s2.elements.EntQuery2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
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
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.types.Money;

import com.google.inject.Guice;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

public class BaseEntQueryTCase1 {
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
    protected static final Class<TgWagonSlot> WAGON_SLOT = TgWagonSlot.class;
    protected static final Class<TgWorkshop> WORKSHOP = TgWorkshop.class;
    protected static final Class<String> STRING = String.class;
    protected static final Class<Date> DATE = Date.class;
    protected static final Class<Long> LONG = Long.class;
    protected static final Class<Integer> INTEGER = Integer.class;
    protected static final Class<BigInteger> BIG_INTEGER = BigInteger.class;
    protected static final Class<BigDecimal> BIG_DECIMAL = BigDecimal.class;
    protected static final Type H_LONG = Hibernate.LONG;
    protected static final Type H_STRING = Hibernate.STRING;
    protected static final Type H_BIG_DECIMAL = Hibernate.BIG_DECIMAL;
    protected static final Type H_BIG_INTEGER = Hibernate.BIG_INTEGER;

    public static final Map<Class, Class> hibTypeDefaults = new HashMap<Class, Class>();
    public static final Map<Class<? extends AbstractEntity<?>>, EntityInfo> metadata = new HashMap<>();

    static {
        hibTypeDefaults.put(boolean.class, YesNoType.class);
        hibTypeDefaults.put(Boolean.class, YesNoType.class);
        hibTypeDefaults.put(Date.class, DateTimeType.class);
        hibTypeDefaults.put(Money.class, SimpleMoneyType.class);
    }

    protected static Type hibtype(final String name) {
        return TypeFactory.basic(name);
    }

    protected static final DomainMetadata DOMAIN_METADATA = new DomainMetadata(hibTypeDefaults, Guice.createInjector(new HibernateUserTypesModule()), PlatformTestDomainTypes.entityTypes, DbVersion.H2);

    protected static final DomainMetadataAnalyser DOMAIN_METADATA_ANALYSER = new DomainMetadataAnalyser(DOMAIN_METADATA);

    protected static final EntQueryGenerator1 qb = new EntQueryGenerator1(DOMAIN_METADATA_ANALYSER);

    //private static final EntQueryGenerator1 qbwf = new EntQueryGenerator1(DOMAIN_METADATA_ANALYSER, new SimpleUserFilter(), null);

    protected static EntQuery1 entSourceQry(final QueryModel qryModel) {
        return qb.generateEntQueryAsSourceQuery(qryModel, null);
    }

    //    protected static EntQuery1 entSourceQry(final QueryModel qryModel, final Map<String, Object> paramValues) {
    //	return qb.generateEntQueryAsSourceQuery(qryModel, paramValues, null);
    //    }

    protected static Expression1 entQryExpression(final ExpressionModel exprModel) {
        return (Expression1) new StandAloneExpressionBuilder1(qb, exprModel).getResult().getValue();
    }

    protected static EntQuery1 entResultQry(final QueryModel qryModel) {
        if (qryModel instanceof EntityResultQueryModel) {
            return qb.generateEntQueryAsResultQuery(from((EntityResultQueryModel) qryModel).model());
        } else if (qryModel instanceof AggregatedResultQueryModel) {
            return qb.generateEntQueryAsResultQuery(from((AggregatedResultQueryModel) qryModel).model());
        } else {
            throw new IllegalArgumentException("Instance of incorrect QueryModel descendant");
        }
    }

    protected static EntQuery2 entResultQry2(final QueryModel qryModel, final TransformatorToS2 transformator) {
        if (qryModel instanceof EntityResultQueryModel) {
            return qb.generateEntQueryAsResultQuery(from((EntityResultQueryModel) qryModel).model()).transform(transformator);
        } else if (qryModel instanceof AggregatedResultQueryModel) {
            return qb.generateEntQueryAsResultQuery(from((AggregatedResultQueryModel) qryModel).model()).transform(transformator);
        }
        throw new IllegalStateException("Not implemented yet");
    }

    protected static EntQuery1 entResultQry(final EntityResultQueryModel qryModel, final OrderingModel orderModel) {
        return qb.generateEntQueryAsResultQuery(from(qryModel).with(orderModel).model());
    }

    protected static EntQuery1 entResultQry(final QueryModel qryModel, final Map<String, Object> paramValues) {
        if (qryModel instanceof EntityResultQueryModel) {
            return qb.generateEntQueryAsResultQuery(from((EntityResultQueryModel) qryModel).with(paramValues).model());
        } else if (qryModel instanceof AggregatedResultQueryModel) {
            return qb.generateEntQueryAsResultQuery(from((AggregatedResultQueryModel) qryModel).with(paramValues).model());
        } else {
            throw new IllegalArgumentException("Instance of incorrect QueryModel descendant");
        }
    }

    protected static EntQuery1 entSubQry(final QueryModel qryModel) {
        return qb.generateEntQueryAsSubquery(qryModel);
    }

    //    protected static EntQuery1 entResultQryWithUserFilter(final QueryModel qryModel) {
    //	if (qryModel instanceof EntityResultQueryModel) {
    //	    return qbwf.generateEntQueryAsResultQuery(from((EntityResultQueryModel)qryModel).model());
    //	} else if (qryModel instanceof AggregatedResultQueryModel) {
    //	    return qbwf.generateEntQueryAsResultQuery(from((AggregatedResultQueryModel)qryModel).model());
    //	} else {
    //	    throw new IllegalArgumentException("Instance of incorrect QueryModel descendant");
    //	}
    //    }

    protected static EntProp1 prop(final String propName) {
        return new EntProp1(propName);
    }

    protected static EntValue1 val(final Object value) {
        return new EntValue1(value);
    }

    protected static EntParam1 param(final String value) {
        return new EntParam1(value);
    }

    protected static EntValue1 iVal(final Object value) {
        return new EntValue1(value, true);
    }

    protected static EntParam1 iParam(final String value) {
        return new EntParam1(value, true);
    }

    protected static OperandsBasedSet1 set(final ISingleOperand1<? extends ISingleOperand2>... operands) {
        return new OperandsBasedSet1(Arrays.asList(operands));
    }

    //    protected static PurePropInfo ppi(final String name, final Class type, final Object hibType, final boolean nullable) {
    //	return new PurePropInfo(name, type, hibType, nullable);
    //    }
    //
    //    protected static PropResolutionInfo propResInf(final String propName, final String aliasPart, final PurePropInfo propPart, final PurePropInfo explicitPropPart) {
    //	return new PropResolutionInfo(prop(propName), aliasPart, propPart, explicitPropPart);
    //    }
    //
    //    protected static PropResolutionInfo propResInf(final String propName, final String aliasPart, final PurePropInfo propPart) {
    //	return new PropResolutionInfo(prop(propName), aliasPart, propPart, propPart);
    //    }
    //
    //    protected static PropResolutionInfo impIdPropResInf(final String propName, final String aliasPart, final PurePropInfo propPart, final PurePropInfo explicitPropPart) {
    //	return new PropResolutionInfo(prop(propName), aliasPart, propPart, explicitPropPart, true);
    //    }
    //
    //    protected static PropResolutionInfo impIdPropResInf(final String propName, final String aliasPart, final PurePropInfo propPart) {
    //	return new PropResolutionInfo(prop(propName), aliasPart, propPart, propPart, true);
    //    }
    //
    //    protected List<List<PropResolutionInfo>> getSourcesReferencingProps(final EntQuery entQry) {
    //	final List<List<PropResolutionInfo>> result = new ArrayList<List<PropResolutionInfo>>();
    //	for (final ISource source : entQry.getSources().getAllSources()) {
    //	    if (!source.generated()) {
    //		result.add(source.getReferencingProps());
    //	    }
    //	}
    //
    //	return result;
    //    }

    protected final List<PropResolutionInfo> prepare(final PropResolutionInfo... infos) {
        return Arrays.asList(infos);
    }

    protected final List<List<PropResolutionInfo>> compose(final List<PropResolutionInfo>... srcLists) {
        return Arrays.asList(srcLists);
    }

    public static void assertModelsEquals(final QueryModel shortcutModel, final QueryModel explicitModel) {
        final EntQuery1 shortcutQry = entResultQry(shortcutModel);
        final EntQuery1 explicitQry = entResultQry(explicitModel);
        assertTrue(("Query models are different!\nShortcut:\n" + shortcutQry.toString() + "\nExplicit:\n" + explicitQry.toString()), shortcutQry.equals(explicitQry));
    }

    public static void assertSubQueryModelsEquals(final QueryModel shortcutModel, final QueryModel explicitModel) {
        final EntQuery1 shortcutQry = entSubQry(shortcutModel);
        final EntQuery1 explicitQry = entSubQry(explicitModel);
        assertTrue(("Query models are different!\nShortcut:\n" + shortcutQry.toString() + "\nExplicit:\n" + explicitQry.toString()), shortcutQry.equals(explicitQry));
    }

    //    public static void assertModelsEqualsAccordingUserDataFiltering(final QueryModel shortcutModel, final QueryModel explicitModel) {
    //	shortcutModel.setFilterable(true);
    //	final EntQuery1 shortcutQry = entResultQryWithUserFilter(shortcutModel);
    //	final EntQuery1 explicitQry = entResultQry(explicitModel);
    //	assertTrue(("Query models are different!\nShortcut:\n" + shortcutQry.toString() + "\nExplicit:\n" + explicitQry.toString()), shortcutQry.equals(explicitQry));
    //    }

    public static void assertModelsDifferent(final QueryModel shortcutModel, final QueryModel explicitModel) {
        final EntQuery1 shortcutQry = entResultQry(shortcutModel);
        final EntQuery1 explicitQry = entResultQry(explicitModel);
        assertFalse(("Query models are equal! exp: " + shortcutQry.toString() + " act: " + explicitQry.toString()), shortcutQry.equals(explicitQry));
    }

    //    public static void assertPropInfoEquals(final QueryModel qryModel, final String propName, final PropResolutionInfo propResInfo) {
    //	final PropResolutionInfo act = entResultQry(qryModel).getSources().getMain().containsProperty(prop(propName));
    //	assertEquals(("Prop resolution infos are different! exp: " + propResInfo.toString() + " act: " + act.toString()), propResInfo, act);
    //    }
    //
    //    public static void assertPropInfoDifferent(final QueryModel qryModel, final String propName, final PropResolutionInfo propResInfo) {
    //	final PropResolutionInfo act = entResultQry(qryModel).getSources().getMain().containsProperty(prop(propName));
    //	assertFalse(("Prop resolution infos are equal! exp: " + propResInfo.toString() + " act: " + act.toString()), propResInfo.equals(act));
    //    }

    public static Type hibType(final String name) {
        return TypeFactory.basic(name);
    }

    public static PropertyMetadata ppi(final String name, final Class javaType, final boolean nullable, final Object hibType, final String column, final PropertyCategory type) {
        return new PropertyMetadata.Builder(name, javaType, nullable).column(new PropertyColumn(column)).hibType(hibType).type(type).build();
    }

    public static PropertyMetadata ppi(final String name, final Class javaType, final boolean nullable, final Object hibType, final List<PropertyColumn> columns, final PropertyCategory type) {
        return new PropertyMetadata.Builder(name, javaType, nullable).columns(columns).hibType(hibType).type(type).build();
    }
}