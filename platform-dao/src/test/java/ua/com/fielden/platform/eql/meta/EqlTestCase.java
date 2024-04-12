package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.emptyMap;
import static ua.com.fielden.platform.entity.query.DbVersion.H2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.type.YesNoType;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.generation.ioc.HelperIocModule;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.eql.retrieval.QueryNowValue;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.persistence.HibernateHelpers;
import ua.com.fielden.platform.persistence.types.ColourType;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.persistence.types.HyperlinkType;
import ua.com.fielden.platform.persistence.types.PropertyDescriptorType;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.sample.domain.TeVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TeVehicleFuelUsage;
import ua.com.fielden.platform.sample.domain.TeVehicleMake;
import ua.com.fielden.platform.sample.domain.TeVehicleModel;
import ua.com.fielden.platform.sample.domain.TeWorkOrder;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgBogie;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.IDates;

public abstract class EqlTestCase {
    protected static final Class<TeWorkOrder> WORK_ORDER = TeWorkOrder.class;
    protected static final Class<TeVehicle> VEHICLE = TeVehicle.class;
    protected static final Class<TeVehicleFuelUsage> VEHICLE_FUEL_USAGE = TeVehicleFuelUsage.class;
    protected static final Class<TeVehicleFinDetails> VEHICLE_FIN_DETAILS = TeVehicleFinDetails.class;
    protected static final Class<TeVehicleModel> MODEL = TeVehicleModel.class;
    protected static final Class<TeVehicleMake> MAKE = TeVehicleMake.class;
    protected static final Class<TgPersonName> PERSON_NAME = TgPersonName.class;
    protected static final Class<TgAuthor> AUTHOR = TgAuthor.class;
    protected static final Class<TgFuelUsage> FUEL_USAGE = TgFuelUsage.class;
    protected static final Class<TgOrgUnit5> ORG5 = TgOrgUnit5.class;
    protected static final Class<TgOrgUnit4> ORG4 = TgOrgUnit4.class;
    protected static final Class<TgOrgUnit3> ORG3 = TgOrgUnit3.class;
    protected static final Class<TgOrgUnit2> ORG2 = TgOrgUnit2.class;
    protected static final Class<TgOrgUnit1> ORG1 = TgOrgUnit1.class;
    protected static final Class<TgWagonSlot> WAGON_SLOT = TgWagonSlot.class;
    protected static final Class<TgWorkshop> WORKSHOP = TgWorkshop.class;
    protected static final Class<TgBogie> BOGIE = TgBogie.class;
    protected static final Class<Boolean> BOOLEAN = boolean.class;
    protected static final Class<String> STRING = String.class;
    protected static final Type H_BOOLEAN = StandardBasicTypes.YES_NO;
    protected static final Class<Date> DATE = Date.class;
    protected static final Class<Long> LONG = Long.class;
    protected static final Class<Integer> INTEGER = Integer.class;
    protected static final Class<BigInteger> BIG_INTEGER = BigInteger.class;
    protected static final Class<BigDecimal> BIG_DECIMAL = BigDecimal.class;
    protected static final Type H_LONG = StandardBasicTypes.LONG;
    protected static final Type H_INTEGER = StandardBasicTypes.INTEGER;
    protected static final Type H_STRING = StandardBasicTypes.STRING;
    protected static final Type H_BIG_DECIMAL = StandardBasicTypes.BIG_DECIMAL;
    protected static final Type H_BIG_INTEGER = StandardBasicTypes.BIG_INTEGER;

    public static final Map<Class, Class> hibTypeDefaults = new HashMap<>();
    private static Injector injector = Guice.createInjector(new HibernateUserTypesModule(), new HelperIocModule());
    protected static final IDates dates = injector.getInstance(IDates.class);
    protected static final IFilter filter = new SimpleUserFilter();
    
    private static final DomainMetadata DOMAIN_METADATA;
    protected static final DomainMetadataAnalyser DOMAIN_METADATA_ANALYSER;

    static {
        hibTypeDefaults.put(boolean.class, YesNoType.class);
        hibTypeDefaults.put(Boolean.class, YesNoType.class);
        hibTypeDefaults.put(Date.class, DateTimeType.class);
        hibTypeDefaults.put(Money.class, SimpleMoneyType.class);
        hibTypeDefaults.put(PropertyDescriptor.class, PropertyDescriptorType.class);
        hibTypeDefaults.put(Colour.class, ColourType.class);
        hibTypeDefaults.put(Hyperlink.class, HyperlinkType.class);
        
        DOMAIN_METADATA = new DomainMetadata(hibTypeDefaults, 
                injector, 
                PlatformTestDomainTypes.entityTypes,
                HibernateHelpers.getDialect(H2));
        DOMAIN_METADATA_ANALYSER = new DomainMetadataAnalyser(DOMAIN_METADATA);
    }
    
    protected static final QueryModelToStage1Transformer qb() {
        return qb(new SimpleUserFilter(), null, injector.getInstance(IDates.class), emptyMap());
    }

    protected static final QueryModelToStage1Transformer qb(final Map<String, Object> paramValues) {
        return qb(new SimpleUserFilter(), null, injector.getInstance(IDates.class), paramValues);
    }
    
    protected static final QueryModelToStage1Transformer qb(final IFilter filter, final String username, final IDates dates, final Map<String, Object> paramValues) {
        return new QueryModelToStage1Transformer(filter, username, new QueryNowValue(dates), paramValues);
    }
    
    protected static final EqlDomainMetadata metadata() {
        return DOMAIN_METADATA.eqlDomainMetadata;
    }
    
    protected static final QuerySourceInfoProvider querySourceInfoProvider() {
        return DOMAIN_METADATA.eqlDomainMetadata.querySourceInfoProvider;
    }
    
}
