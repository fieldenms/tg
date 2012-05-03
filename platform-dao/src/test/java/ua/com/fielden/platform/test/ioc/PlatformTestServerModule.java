package ua.com.fielden.platform.test.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.ioc.BasicWebServerModule;
import ua.com.fielden.platform.migration.controller.IMigrationErrorDao;
import ua.com.fielden.platform.migration.controller.IMigrationHistoryDao;
import ua.com.fielden.platform.migration.controller.IMigrationRunDao;
import ua.com.fielden.platform.migration.dao.MigrationErrorDao;
import ua.com.fielden.platform.migration.dao.MigrationHistoryDao;
import ua.com.fielden.platform.migration.dao.MigrationRunDao;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.controller.ITgBogieClass;
import ua.com.fielden.platform.sample.domain.controller.ITgFuelUsage;
import ua.com.fielden.platform.sample.domain.controller.ITgMeterReading;
import ua.com.fielden.platform.sample.domain.controller.ITgOrgUnit1;
import ua.com.fielden.platform.sample.domain.controller.ITgOrgUnit2;
import ua.com.fielden.platform.sample.domain.controller.ITgOrgUnit3;
import ua.com.fielden.platform.sample.domain.controller.ITgOrgUnit4;
import ua.com.fielden.platform.sample.domain.controller.ITgOrgUnit5;
import ua.com.fielden.platform.sample.domain.controller.ITgTimesheet;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicle;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleFinDetails;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleMake;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleModel;
import ua.com.fielden.platform.sample.domain.controller.ITgWagon;
import ua.com.fielden.platform.sample.domain.controller.ITgWagonClass;
import ua.com.fielden.platform.sample.domain.controller.ITgWagonClassCompatibility;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.test.domain.entities.daos.TgMeterReadingDao;
import ua.com.fielden.platform.test.domain.entities.daos.TgVehicleMakeDao;
import ua.com.fielden.platform.test.domain.entities.daos.TgVehicleModelDao;
import ua.com.fielden.platform.test.entities.daos.TgBogieClassDao;
import ua.com.fielden.platform.test.entities.daos.TgFuelUsageDao;
import ua.com.fielden.platform.test.entities.daos.TgOrgUnit1Dao;
import ua.com.fielden.platform.test.entities.daos.TgOrgUnit2Dao;
import ua.com.fielden.platform.test.entities.daos.TgOrgUnit3Dao;
import ua.com.fielden.platform.test.entities.daos.TgOrgUnit4Dao;
import ua.com.fielden.platform.test.entities.daos.TgOrgUnit5Dao;
import ua.com.fielden.platform.test.entities.daos.TgTimesheetDao;
import ua.com.fielden.platform.test.entities.daos.TgVehicleDao;
import ua.com.fielden.platform.test.entities.daos.TgVehicleFinDetailsDao;
import ua.com.fielden.platform.test.entities.daos.TgWagonClassCompatibilityDao;
import ua.com.fielden.platform.test.entities.daos.TgWagonClassDao;
import ua.com.fielden.platform.test.entities.daos.TgWagonDao;

import com.google.inject.TypeLiteral;


/**
 * Serve IoC module for platform related testing.
 *
 * @author TG Team
 *
 */
public class PlatformTestServerModule extends BasicWebServerModule {

    public PlatformTestServerModule(
	    final Map<Class, Class> defaultHibernateTypes, //
	    final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes,//
	    final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
	    final Class<? extends IFilter> automaticDataFilterType, //
	    final SecurityTokenProvider tokenProvider,//
	    final Properties props) throws Exception {
	super(defaultHibernateTypes, applicationEntityTypes, serialisationClassProviderType, automaticDataFilterType, tokenProvider, props);
    }

    public PlatformTestServerModule(
	    final Map<Class, Class> defaultHibernateTypes, //
	    final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes,//
	    final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
	    final Class<? extends IFilter> automaticDataFilterType, //
	    final Properties props) throws Exception {
	super(defaultHibernateTypes, applicationEntityTypes, serialisationClassProviderType, automaticDataFilterType, null, props);
    }

    @Override
    protected void configure() {
	super.configure();
	// bind DAO
//	bind(IBogieDao.class).to(BogieDao.class);
//	bind(IWheelsetDao.class).to(WheelsetDao.class);
//	bind(IWorkshopDao2.class).to(WorkshopDao2.class);
//	bind(IWheelsetClassDao.class).to(WheelsetClassDao.class);
//	bind(IWagonSlotDao.class).to(WagonSlotDao.class);
//	bind(IWorkorderDao.class).to(WorkorderDao.class);
//	bind(IWorkorderableDao.class).to(WorkorderableDao.class);
//	bind(IAdviceDao.class).to(AdviceDao.class);

	bind(ITgOrgUnit1.class).to(TgOrgUnit1Dao.class);
	bind(ITgOrgUnit2.class).to(TgOrgUnit2Dao.class);
	bind(ITgOrgUnit3.class).to(TgOrgUnit3Dao.class);
	bind(ITgOrgUnit4.class).to(TgOrgUnit4Dao.class);
	bind(ITgOrgUnit5.class).to(TgOrgUnit5Dao.class);

	bind(ITgBogieClass.class).to(TgBogieClassDao.class);
	bind(ITgWagon.class).to(TgWagonDao.class);
	bind(ITgWagonClass.class).to(TgWagonClassDao.class);
	bind(ITgWagonClassCompatibility.class).to(TgWagonClassCompatibilityDao.class);
	bind(ITgTimesheet.class).to(TgTimesheetDao.class);
	bind(ITgVehicle.class).to(TgVehicleDao.class);
	bind(ITgVehicleFinDetails.class).to(TgVehicleFinDetailsDao.class);
	bind(ITgFuelUsage.class).to(TgFuelUsageDao.class);
	bind(ITgVehicleModel.class).to(TgVehicleModelDao.class);
	bind(ITgVehicleMake.class).to(TgVehicleMakeDao.class);
	bind(ITgMeterReading.class).to(TgMeterReadingDao.class);
	bind(IMigrationErrorDao.class).to(MigrationErrorDao.class);
	bind(IMigrationRunDao.class).to(MigrationRunDao.class);
	bind(IMigrationHistoryDao.class).to(MigrationHistoryDao.class);

	bind(new TypeLiteral<IEntityDao<EntityWithMoney>>() {
	}).to(EntityWithMoneyDao.class);
    }
}
