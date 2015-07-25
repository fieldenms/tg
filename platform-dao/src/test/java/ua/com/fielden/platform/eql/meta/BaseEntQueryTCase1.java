package ua.com.fielden.platform.eql.meta;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.PropertyCategory;
import ua.com.fielden.platform.dao.PropertyColumn;
import ua.com.fielden.platform.dao.PropertyMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.generation.elements.AbstractSource.PropResolutionInfo;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.entities.TgtDivision;
import ua.com.fielden.platform.eql.entities.TgtFuelUsage;
import ua.com.fielden.platform.eql.entities.TgtSector;
import ua.com.fielden.platform.eql.entities.TgtStation;
import ua.com.fielden.platform.eql.entities.TgtVehicle;
import ua.com.fielden.platform.eql.entities.TgtVehicleMake;
import ua.com.fielden.platform.eql.entities.TgtVehicleModel;
import ua.com.fielden.platform.eql.entities.TgtWorkOrder;
import ua.com.fielden.platform.eql.entities.TgtZone;
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
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.test.PlatformTestDomainTypes2;
import ua.com.fielden.platform.types.Money;

import com.google.inject.Guice;

public class BaseEntQueryTCase1 {
    protected static final Class<TgtVehicle> VEHICLE = TgtVehicle.class;
    protected static final Class<TgtWorkOrder> WORK_ORDER = TgtWorkOrder.class;
    protected static final Class<TgtVehicleModel> MODEL = TgtVehicleModel.class;
    protected static final Class<TgtVehicleMake> MAKE = TgtVehicleMake.class;
    protected static final Class<TgtFuelUsage> FUEL_USAGE = TgtFuelUsage.class;
    protected static final Class<TgtStation> ORG4 = TgtStation.class;
    protected static final Class<TgtZone> ORG3 = TgtZone.class;
    protected static final Class<TgtSector> ORG2 = TgtSector.class;
    protected static final Class<TgtDivision> ORG1 = TgtDivision.class;
    protected static final Class<String> STRING = String.class;
    protected static final Class<Date> DATE = Date.class;
    protected static final Class<Long> LONG = Long.class;
    protected static final Class<Integer> INTEGER = Integer.class;
    protected static final Class<BigInteger> BIG_INTEGER = BigInteger.class;
    protected static final Class<BigDecimal> BIG_DECIMAL = BigDecimal.class;
    protected static final Type H_LONG = StandardBasicTypes.LONG;
    protected static final Type H_STRING = StandardBasicTypes.STRING;
    protected static final Type H_BIG_DECIMAL = StandardBasicTypes.BIG_DECIMAL;
    protected static final Type H_BIG_INTEGER = StandardBasicTypes.BIG_INTEGER;
    protected static final TypeResolver typeResolver = new TypeResolver();

    public static final Map<Class, Class> hibTypeDefaults = new HashMap<Class, Class>();
    public static final Map<Class<? extends AbstractEntity<?>>, EntityInfo> metadata = new HashMap<>();


    protected static Type hibtype(final String name) {
        return typeResolver.basic(name);
    }

    protected static final DomainMetadata DOMAIN_METADATA = new DomainMetadata(hibTypeDefaults, Guice.createInjector(new HibernateUserTypesModule()), PlatformTestDomainTypes2.entityTypes, AnnotationReflector.getAnnotation(User.class, MapEntityTo.class), DbVersion.H2);

    protected static final DomainMetadataAnalyser DOMAIN_METADATA_ANALYSER = new DomainMetadataAnalyser(DOMAIN_METADATA);

    protected static final EntQueryGenerator1 qb = new EntQueryGenerator1(DOMAIN_METADATA_ANALYSER);

    static {
        hibTypeDefaults.put(Date.class, DateTimeType.class);
        hibTypeDefaults.put(Money.class, SimpleMoneyType.class);
        try {
            metadata.putAll((new MetadataGenerator(qb)).generate(new HashSet<>(PlatformTestDomainTypes2.entityTypes)));
        } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    protected static EntQuery1 entSourceQry(final QueryModel qryModel) {
        return qb.generateEntQueryAsSourceQuery(qryModel, null);
    }

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

    public static void assertModelsDifferent(final QueryModel shortcutModel, final QueryModel explicitModel) {
        final EntQuery1 shortcutQry = entResultQry(shortcutModel);
        final EntQuery1 explicitQry = entResultQry(explicitModel);
        assertFalse(("Query models are equal! exp: " + shortcutQry.toString() + " act: " + explicitQry.toString()), shortcutQry.equals(explicitQry));
    }
    public static Type hibType(final String name) {
        return typeResolver.basic(name);
    }

    public static PropertyMetadata ppi(final String name, final Class javaType, final boolean nullable, final Object hibType, final String column, final PropertyCategory type) {
        return new PropertyMetadata.Builder(name, javaType, nullable).column(new PropertyColumn(column)).hibType(hibType).type(type).build();
    }

    public static PropertyMetadata ppi(final String name, final Class javaType, final boolean nullable, final Object hibType, final List<PropertyColumn> columns, final PropertyCategory type) {
        return new PropertyMetadata.Builder(name, javaType, nullable).columns(columns).hibType(hibType).type(type).build();
    }
}