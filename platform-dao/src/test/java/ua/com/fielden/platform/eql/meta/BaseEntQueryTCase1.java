package ua.com.fielden.platform.eql.meta;

import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import ua.com.fielden.platform.meta.DomainMetadataBuilder;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.sample.domain.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import static ua.com.fielden.platform.entity.query.DbVersion.H2;
import static ua.com.fielden.platform.entity.query.IDbVersionProvider.constantDbVersion;
import static ua.com.fielden.platform.persistence.types.PlatformHibernateTypeMappings.PLATFORM_HIBERNATE_TYPE_MAPPINGS;
import static ua.com.fielden.platform.test.PlatformTestDomainTypes.entityTypes;

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

    protected static final IDomainMetadata DOMAIN_METADATA;

    static {
        DOMAIN_METADATA = new DomainMetadataBuilder(PLATFORM_HIBERNATE_TYPE_MAPPINGS, entityTypes, constantDbVersion(H2)).build();
    }

}
