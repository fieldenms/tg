package ua.com.fielden.platform.eql.meta;

import static java.util.Collections.emptyMap;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.generation.ioc.HelperIocModule;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadata;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
import ua.com.fielden.platform.eql.stage1.builders.EntQueryGenerator;
import ua.com.fielden.platform.eql.stage3.elements.Table;
import ua.com.fielden.platform.ioc.HibernateUserTypesModule;
import ua.com.fielden.platform.persistence.types.DateTimeType;
import ua.com.fielden.platform.persistence.types.SimpleMoneyType;
import ua.com.fielden.platform.sample.domain.TeVehicle;
import ua.com.fielden.platform.sample.domain.TeVehicleMake;
import ua.com.fielden.platform.sample.domain.TeVehicleModel;
import ua.com.fielden.platform.sample.domain.TeWorkOrder;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgFuelUsage;
import ua.com.fielden.platform.sample.domain.TgOrgUnit1;
import ua.com.fielden.platform.sample.domain.TgOrgUnit2;
import ua.com.fielden.platform.sample.domain.TgOrgUnit3;
import ua.com.fielden.platform.sample.domain.TgOrgUnit4;
import ua.com.fielden.platform.sample.domain.TgOrgUnit5;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.sample.domain.TgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.TgWagonSlot;
import ua.com.fielden.platform.sample.domain.TgWorkshop;
import ua.com.fielden.platform.test.PlatformTestDomainTypes;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.utils.IDates;

public class EqlTestCase {
    protected static final Class<TeWorkOrder> WORK_ORDER = TeWorkOrder.class;
    protected static final Class<TeVehicle> VEHICLE = TeVehicle.class;
    protected static final Class<TgVehicleFinDetails> VEHICLE_FIN_DETAILS = TgVehicleFinDetails.class;
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
    protected static final TypeResolver typeResolver = new TypeResolver();

    public static final Map<Class, Class> hibTypeDefaults = new HashMap<>();
    private static Injector injector = Guice.createInjector(new HibernateUserTypesModule(), new HelperIocModule());//Guice.createInjector(new HibernateUserTypesModule());
    
    protected static final DomainMetadata DOMAIN_METADATA;

    public static final Map<String, Table> tables = new HashMap<>();
    public static final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> metadata = new HashMap<>();

    
    static {
        hibTypeDefaults.put(Date.class, DateTimeType.class);
        hibTypeDefaults.put(Money.class, SimpleMoneyType.class);
        
        DOMAIN_METADATA = new DomainMetadata(hibTypeDefaults, 
                injector, 
                PlatformTestDomainTypes.entityTypes, 
                DbVersion.H2);

        final MetadataGenerator mdg = new MetadataGenerator(DOMAIN_METADATA, null, null, injector.getInstance(IDates.class), emptyMap());
        
        try {
            metadata.putAll(mdg.generate(new HashSet<>(PlatformTestDomainTypes.entityTypes)));
            tables.putAll(mdg.generateTables(PlatformTestDomainTypes.entityTypes));
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
    
    protected static final EntQueryGenerator qb() {
        return qb(null, null, injector.getInstance(IDates.class), emptyMap());
    }

    protected static final EntQueryGenerator qb(final Map<String, Object> paramValues) {
        return qb(null, null, injector.getInstance(IDates.class), paramValues);
    }
    
    protected static final EntQueryGenerator qb(final IFilter filter, final String username, final IDates dates, final Map<String, Object> paramValues) {
        return new EntQueryGenerator(new DomainMetadataAnalyser(DOMAIN_METADATA), filter, username, dates, paramValues);
    }
    
    protected static final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> metadata() {
        return metadata(null, null, injector.getInstance(IDates.class), emptyMap());
    }

    protected static final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> metadata(final Map<String, Object> paramValues) {
        return metadata(null, null, injector.getInstance(IDates.class), paramValues);
    }

    protected static final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> metadata(final IFilter filter, final String username, final IDates dates, final Map<String, Object> paramValues) {
        final Map<Class<? extends AbstractEntity<?>>, EntityInfo<?>> result = new HashMap<>();
        final MetadataGenerator mdg = new MetadataGenerator(DOMAIN_METADATA, filter, username, dates, paramValues);
        try {
            result.putAll(mdg.generate(new HashSet<>(PlatformTestDomainTypes.entityTypes)));    
        } catch (final Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
}