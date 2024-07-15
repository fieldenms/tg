package ua.com.fielden.platform.eql.meta;

import static ua.com.fielden.platform.entity.query.DbVersion.H2;
import static ua.com.fielden.platform.test.PlatformTestDomainTypes.entityTypes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import com.google.inject.Guice;

import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.query.metadata.PropertyCategory;
import ua.com.fielden.platform.entity.query.metadata.PropertyColumn;
import ua.com.fielden.platform.entity.query.metadata.PropertyMetadata;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.persistence.types.ColourType;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.types.HyperlinkType;
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
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;

public class BaseEntQueryTCase1 {
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
    protected static final Class<Boolean> BOOLEAN = boolean.class;
    protected static final Class<TgWorkshop> WORKSHOP = TgWorkshop.class;
    protected static final Class<String> STRING = String.class;
    protected static final Type H_BOOLEAN = StandardBasicTypes.YES_NO;
    protected static final Class<Date> DATE = Date.class;
    protected static final Class<Long> LONG = Long.class;
    protected static final Class<Integer> INTEGER = Integer.class;
    protected static final Class<BigInteger> BIG_INTEGER = BigInteger.class;
    protected static final Class<BigDecimal> BIG_DECIMAL = BigDecimal.class;
    protected static final Type H_LONG = StandardBasicTypes.LONG;
    protected static final Type H_STRING = StandardBasicTypes.STRING;
    protected static final Type H_BIG_DECIMAL = StandardBasicTypes.BIG_DECIMAL;
    protected static final Type H_BIG_INTEGER = StandardBasicTypes.BIG_INTEGER;

    public static final Map<Class, Class> hibTypeDefaults = new HashMap<>();

    protected static final DomainMetadata DOMAIN_METADATA;

    protected static final DomainMetadataAnalyser DOMAIN_METADATA_ANALYSER;

    static {
        hibTypeDefaults.put(Date.class, DateTimeType.class);
        hibTypeDefaults.put(Money.class, SimpleMoneyType.class);
        hibTypeDefaults.put(Date.class, DateTimeType.class);
        hibTypeDefaults.put(PropertyDescriptor.class, PropertyDescriptorType.class);
        hibTypeDefaults.put(Colour.class, ColourType.class);
        hibTypeDefaults.put(Hyperlink.class, HyperlinkType.class);

        DOMAIN_METADATA = new DomainMetadata(hibTypeDefaults, 
                Guice.createInjector(new HibernateUserTypesModule()), 
                entityTypes, 
                H2);
        
        DOMAIN_METADATA_ANALYSER = new DomainMetadataAnalyser(DOMAIN_METADATA);
    }
    
    public static PropertyMetadata ppi(final String name, final Class javaType, final boolean nullable, final Object hibType, final String column, final PropertyCategory type/*, final EntityTypeInfo <? extends AbstractEntity<?>> entityCategory*/) {
        return new PropertyMetadata.Builder(name, javaType, nullable, null /*entityCategory*/).column(new PropertyColumn(column)).hibType(hibType).category(type).build();
    }

    public static PropertyMetadata ppi(final String name, final Class javaType, final boolean nullable, final Object hibType, final List<PropertyColumn> columns, final PropertyCategory type/*, final EntityTypeInfo <? extends AbstractEntity<?>> entityCategory*/) {
        return new PropertyMetadata.Builder(name, javaType, nullable, null /*entityCategory*/).columns(columns).hibType(hibType).category(type).build();
    }
}