package ua.com.fielden.platform.test;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.eql.entities.TgtAuthor;
import ua.com.fielden.platform.eql.entities.TgtAuthorRoyalty;
import ua.com.fielden.platform.eql.entities.TgtAuthorship;
import ua.com.fielden.platform.eql.entities.TgtAverageFuelUsage;
import ua.com.fielden.platform.eql.entities.TgtDivision;
import ua.com.fielden.platform.eql.entities.TgtFuelUsage;
import ua.com.fielden.platform.eql.entities.TgtPersonName;
import ua.com.fielden.platform.eql.entities.TgtPublishedYearly;
import ua.com.fielden.platform.eql.entities.TgtSector;
import ua.com.fielden.platform.eql.entities.TgtStation;
import ua.com.fielden.platform.eql.entities.TgtVehicle;
import ua.com.fielden.platform.eql.entities.TgtVehicleMake;
import ua.com.fielden.platform.eql.entities.TgtVehicleModel;
import ua.com.fielden.platform.eql.entities.TgtZone;

/**
 * A class to enlist platform test domain entities. Should be replaced with runtime generation via reflection.
 * 
 * @author TG Team
 * 
 */
public class PlatformTestDomainTypes2 implements IApplicationDomainProvider {
    public static final List<Class<? extends AbstractEntity<?>>> entityTypes = new ArrayList<Class<? extends AbstractEntity<?>>>();

    static void add(final Class<? extends AbstractEntity<?>> domainType) {
        entityTypes.add(domainType);
    }

    static {
        add(TgtVehicle.class);
        add(TgtVehicleModel.class);
        add(TgtVehicleMake.class);
        add(TgtFuelUsage.class);
        add(TgtDivision.class);
        add(TgtSector.class);
        add(TgtZone.class);
        add(TgtStation.class);
        add(TgtAuthor.class);
        add(TgtAuthorRoyalty.class);
        add(TgtAuthorship.class);
        add(TgtAverageFuelUsage.class);
        add(TgtPersonName.class);
        add(TgtPublishedYearly.class);
    }

    @Override
    public List<Class<? extends AbstractEntity<?>>> entityTypes() {
        return entityTypes;
    }
}