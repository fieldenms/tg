package ua.com.fielden.platform.test.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ua.com.fielden.platform.dao.EntityWithMoneyDao2;
import ua.com.fielden.platform.dao2.IEntityDao2;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.ioc.BasicWebServerModule2;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.controller.ITgFuelUsage;
import ua.com.fielden.platform.sample.domain.controller.ITgTimesheet;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicle;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleMake2;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleModel2;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.test.domain.entities.daos.TgVehicleMakeDao2;
import ua.com.fielden.platform.test.domain.entities.daos.TgVehicleModelDao2;
import ua.com.fielden.platform.test.entities.daos.TgFuelUsageDao;
import ua.com.fielden.platform.test.entities.daos.TgTimesheetDao;
import ua.com.fielden.platform.test.entities.daos.TgVehicleDao;

import com.google.inject.TypeLiteral;


/**
 * Serve IoC module for platform related testing.
 *
 * @author TG Team
 *
 */
public class PlatformTestServerModule2 extends BasicWebServerModule2 {

    public PlatformTestServerModule2(
	    final Map<Class, Class> defaultHibernateTypes, //
	    final List<Class<? extends AbstractEntity<?>>> applicationEntityTypes,//
	    final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
	    final Class<? extends IFilter> automaticDataFilterType, //
	    final SecurityTokenProvider tokenProvider,//
	    final Properties props) throws Exception {
	super(defaultHibernateTypes, applicationEntityTypes, serialisationClassProviderType, automaticDataFilterType, tokenProvider, props);
    }

    public PlatformTestServerModule2(
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
//	bind(IRotableDao.class).to(RotableDao.class);
//	bind(IWorkshopDao2.class).to(WorkshopDao2.class);
//	bind(IWagonClassDao.class).to(WagonClassDao.class);
//	bind(IBogieClassDao.class).to(BogieClassDao.class);
//	bind(IWheelsetClassDao.class).to(WheelsetClassDao.class);
//	bind(IWagonDao.class).to(WagonDao.class);
//	bind(IWagonSlotDao.class).to(WagonSlotDao.class);
//	bind(IWorkorderDao.class).to(WorkorderDao.class);
//	bind(IWorkorderableDao.class).to(WorkorderableDao.class);
//	bind(IAdviceDao.class).to(AdviceDao.class);
//	bind(IRotableClassDao.class).to(RotableClassDao.class);

	bind(ITgTimesheet.class).to(TgTimesheetDao.class);
	bind(ITgVehicle.class).to(TgVehicleDao.class);
	bind(ITgFuelUsage.class).to(TgFuelUsageDao.class);
	bind(ITgVehicleModel2.class).to(TgVehicleModelDao2.class);
	bind(ITgVehicleMake2.class).to(TgVehicleMakeDao2.class);
//	bind(ITgMeterReading.class).to(TgMeterReadingDao.class);
//	bind(IMigrationErrorDao.class).to(MigrationErrorDao.class);
//	bind(IMigrationRunDao.class).to(MigrationRunDao.class);
//	bind(IMigrationHistoryDao.class).to(MigrationHistoryDao.class);

	bind(new TypeLiteral<IEntityDao2<EntityWithMoney>>() {
	}).to(EntityWithMoneyDao2.class);
    }
}
