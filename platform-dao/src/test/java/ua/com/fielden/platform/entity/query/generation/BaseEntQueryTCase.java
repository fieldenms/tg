package ua.com.fielden.platform.entity.query.generation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;
import org.hibernate.type.YesNoType;

import com.google.inject.Guice;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.generation.elements.AbstractSource.PropResolutionInfo;
import ua.com.fielden.platform.entity.query.generation.elements.AbstractSource.PurePropInfo;
import ua.com.fielden.platform.entity.query.generation.elements.EntProp;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.EntValue;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.ISource;
import ua.com.fielden.platform.entity.query.generation.elements.OperandsBasedSet;
import ua.com.fielden.platform.entity.query.generation.ioc.HelperIocModule;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.metadata.EntityTypeInfo;
import ua.com.fielden.platform.entity.query.metadata.PropertyCategory;
import ua.com.fielden.platform.entity.query.metadata.PropertyColumn;
import ua.com.fielden.platform.entity.query.metadata.PropertyMetadata;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.persistence.types.ColourType;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.persistence.types.HyperlinkType;
import ua.com.fielden.platform.persistence.types.PropertyDescriptorType;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgAverageFuelUsage;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.IDates;

public class BaseEntQueryTCase {
    public static final Map<Class, Class> hibTypeDefaults = new HashMap<>();

    protected static final Class<TgWorkOrder> WORK_ORDER = TgWorkOrder.class;
    protected static final Class<TgVehicle> VEHICLE = TgVehicle.class;
    protected static final Class<TgVehicleFinDetails> VEHICLE_FIN_DETAILS = TgVehicleFinDetails.class;
    protected static final Class<TgVehicleModel> MODEL = TgVehicleModel.class;
    protected static final Class<TgVehicleMake> MAKE = TgVehicleMake.class;
    protected static final Class<TgPersonName> PERSON_NAME = TgPersonName.class;
    protected static final Class<TgAuthor> AUTHOR = TgAuthor.class;
    protected static final Class<TgFuelUsage> FUEL_USAGE = TgFuelUsage.class;
    protected static final Class<TgAverageFuelUsage> AVERAGE_FUEL_USAGE = TgAverageFuelUsage.class;
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
    protected static final Class<Boolean> BOOLEAN = boolean.class;
    protected static final Class<Integer> INTEGER = Integer.class;
    protected static final Class<BigInteger> BIG_INTEGER = BigInteger.class;
    protected static final Class<BigDecimal> BIG_DECIMAL = BigDecimal.class;
    protected static final Type H_LONG = StandardBasicTypes.LONG;
    protected static final Type H_STRING = StandardBasicTypes.STRING;
    protected static final Type H_BOOLEAN = StandardBasicTypes.YES_NO;
    protected static final Type H_BIG_DECIMAL = StandardBasicTypes.BIG_DECIMAL;
    protected static final Type H_BIG_INTEGER = StandardBasicTypes.BIG_INTEGER;
    protected static final TypeResolver typeResolver = new TypeResolver();

    static {
        hibTypeDefaults.put(boolean.class, YesNoType.class);
        hibTypeDefaults.put(Boolean.class, YesNoType.class);
        hibTypeDefaults.put(Date.class, DateTimeType.class);
        hibTypeDefaults.put(Money.class, SimpleMoneyType.class);
        hibTypeDefaults.put(PropertyDescriptor.class, PropertyDescriptorType.class);
        hibTypeDefaults.put(Colour.class, ColourType.class);
        hibTypeDefaults.put(Hyperlink.class, HyperlinkType.class);
    }

    protected static final DomainMetadata DOMAIN_METADATA = new DomainMetadata(hibTypeDefaults, Guice.createInjector(new HibernateUserTypesModule(), new HelperIocModule()), PlatformTestDomainTypes.entityTypes, DbVersion.H2);

    protected static final DomainMetadataAnalyser DOMAIN_METADATA_ANALYSER = new DomainMetadataAnalyser(DOMAIN_METADATA);

    private static final EntQueryGenerator qb = new EntQueryGenerator(DOMAIN_METADATA_ANALYSER, null, null, Guice.createInjector(new HibernateUserTypesModule(), new HelperIocModule()).getInstance(IDates.class));

    private static final EntQueryGenerator qbwf = new EntQueryGenerator(DOMAIN_METADATA_ANALYSER, new SimpleUserFilter(), null, Guice.createInjector(new HibernateUserTypesModule(), new HelperIocModule()).getInstance(IDates.class));

    protected static EntQuery entSourceQry(final QueryModel qryModel) {
        return qb.generateEntQueryAsSourceQuery(qryModel, new HashMap<String, Object>(), qryModel.getResultType());
    }

    protected static EntQuery entSourceQry(final QueryModel qryModel, final Map<String, Object> paramValues) {
        return qb.generateEntQueryAsSourceQuery(qryModel, paramValues, qryModel.getResultType());
    }

    protected static EntQuery entResultQry(final QueryModel qryModel) {
        if (qryModel instanceof EntityResultQueryModel) {
            return qb.generateEntQueryAsResultQuery((EntityResultQueryModel) qryModel, null, qryModel.getResultType(), null, new HashMap<String, Object>());
        } else if (qryModel instanceof AggregatedResultQueryModel) {
            return qb.generateEntQueryAsResultQuery((AggregatedResultQueryModel) qryModel, null, qryModel.getResultType(), null, new HashMap<String, Object>());
        } else {
            throw new IllegalArgumentException("Instance of incorrect QueryModel descendant");
        }
    }

    protected static EntQuery entResultQry(final EntityResultQueryModel qryModel, final OrderingModel orderModel) {
        return qb.generateEntQueryAsResultQuery(qryModel, orderModel, qryModel.getResultType(), null, new HashMap<String, Object>());
    }

    protected static EntQuery entResultQry(final QueryModel qryModel, final Map<String, Object> paramValues) {
        if (qryModel instanceof EntityResultQueryModel || qryModel instanceof AggregatedResultQueryModel) {
            return qb.generateEntQueryAsResultQuery(qryModel, null, qryModel.getResultType(), null, paramValues);
//        } 
//        else if (qryModel instanceof AggregatedResultQueryModel) {
//            return qb.generateEntQueryAsResultQuery(qryModel).with(paramValues).model());
        } else {
            throw new IllegalArgumentException("Instance of incorrect QueryModel descendant");
        }
    }

    protected static EntQuery entSubQry(final QueryModel qryModel) {
        return qb.generateEntQueryAsSubquery(qryModel, new HashMap<String, Object>());
    }

    protected static EntQuery entResultQryWithUserFilter(final QueryModel qryModel) {
        if (qryModel instanceof EntityResultQueryModel || qryModel instanceof AggregatedResultQueryModel) {
            return qbwf.generateEntQueryAsResultQuery(qryModel, null, qryModel.getResultType(), null, new HashMap<String, Object>());
//        } else if (qryModel instanceof AggregatedResultQueryModel) {
//            return qbwf.generateEntQueryAsResultQuery(from((AggregatedResultQueryModel) qryModel).model());
        } else {
            throw new IllegalArgumentException("Instance of incorrect QueryModel descendant");
        }
    }

    protected static EntProp prop(final String propName) {
        return new EntProp(propName);
    }

    protected static EntValue val(final Object value) {
        return new EntValue(value);
    }

    protected static EntValue iVal(final Object value) {
        return new EntValue(value, true);
    }

    protected static OperandsBasedSet set(final ISingleOperand... operands) {
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

    protected List<List<PropResolutionInfo>> getSourcesReferencingProps(final EntQuery entQry) {
        final List<List<PropResolutionInfo>> result = new ArrayList<>();
        for (final ISource source : entQry.getSources().getAllSources()) {
            if (!source.generated()) {
                result.add(source.getReferencingProps());
            }
        }

        return result;
    }

    protected final List<PropResolutionInfo> prepare(final PropResolutionInfo... infos) {
        return Arrays.asList(infos);
    }

    protected final List<List<PropResolutionInfo>> compose(final List<PropResolutionInfo>... srcLists) {
        return Arrays.asList(srcLists);
    }

    public static void assertModelsEquals(final QueryModel shortcutModel, final QueryModel explicitModel) {
        final EntQuery shortcutQry = entResultQry(shortcutModel);
        final EntQuery explicitQry = entResultQry(explicitModel);
        assertTrue(("Query models are different!\nShortcut:\n" + shortcutQry.toString() + "\nExplicit:\n" + explicitQry.toString()), shortcutQry.equals(explicitQry));
    }

    public static void assertSubQueryModelsEquals(final QueryModel shortcutModel, final QueryModel explicitModel) {
        final EntQuery shortcutQry = entSubQry(shortcutModel);
        final EntQuery explicitQry = entSubQry(explicitModel);
        assertTrue(("Query models are different!\nShortcut:\n" + shortcutQry.toString() + "\nExplicit:\n" + explicitQry.toString()), shortcutQry.equals(explicitQry));
    }

    public static void assertModelsEqualsAccordingUserDataFiltering(final QueryModel shortcutModel, final QueryModel explicitModel) {
        final EntQuery shortcutQry = entResultQryWithUserFilter(shortcutModel.setFilterable(true));
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

    public static Type hibType(final String name) {
        return typeResolver.basic(name);
    }

    public static PropertyMetadata ppi(final String name, final Class javaType, final boolean nullable, final Object hibType, final String column, final PropertyCategory category, final EntityTypeInfo <? extends AbstractEntity<?>> entityCategory) {
        return ppi(name, javaType, nullable, hibType, column, null, null, null, category, entityCategory);
    }

    public static PropertyMetadata ppi(final String name, final Class javaType, final boolean nullable, final Object hibType, final String column, final Integer length, final Integer precision, final Integer scale, final PropertyCategory category, final EntityTypeInfo <? extends AbstractEntity<?>> entityCategory) {
        return new PropertyMetadata.Builder(name, javaType, nullable, entityCategory).column(new PropertyColumn(column, length, precision, scale)).hibType(hibType).category(category).build();
    }
    
    public static PropertyMetadata ppi(final String name, final Class javaType, final boolean nullable, final Object hibType, final List<PropertyColumn> columns, final PropertyCategory category, final EntityTypeInfo <? extends AbstractEntity<?>> entityCategory) {
        return new PropertyMetadata.Builder(name, javaType, nullable, entityCategory).columns(columns).hibType(hibType).category(category).build();
    }
    
    public static PropertyMetadata ppi(final String name, final Class javaType, final ExpressionModel expressionModel, final Object hibType, final PropertyCategory category, final EntityTypeInfo <? extends AbstractEntity<?>> entityCategory, final boolean aggregatedExpression) {
        return new PropertyMetadata.Builder(name, javaType, true, entityCategory).expression(expressionModel).hibType(hibType).category(category).aggregatedExpression(aggregatedExpression).build();
    }
}