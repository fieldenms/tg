package ua.com.fielden.platform.test.ioc;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.ioc.BasicWebServerModule;
import ua.com.fielden.platform.migration.controller.IMigrationErrorDao;
import ua.com.fielden.platform.migration.controller.IMigrationHistoryDao;
import ua.com.fielden.platform.migration.controller.IMigrationRunDao;
import ua.com.fielden.platform.migration.dao.MigrationErrorDao;
import ua.com.fielden.platform.migration.dao.MigrationHistoryDao;
import ua.com.fielden.platform.migration.dao.MigrationRunDao;
import ua.com.fielden.platform.persistence.types.EntityWithMoney;
import ua.com.fielden.platform.sample.domain.controller.ITgMeterReading;
import ua.com.fielden.platform.sample.domain.controller.ITgTimesheet;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleMake;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleModel;
import ua.com.fielden.platform.security.provider.SecurityTokenProvider;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.test.domain.entities.daos.AdviceDao;
import ua.com.fielden.platform.test.domain.entities.daos.BogieClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.BogieDao;
import ua.com.fielden.platform.test.domain.entities.daos.IAdviceDao;
import ua.com.fielden.platform.test.domain.entities.daos.IBogieClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.IBogieDao;
import ua.com.fielden.platform.test.domain.entities.daos.IPersonDao;
import ua.com.fielden.platform.test.domain.entities.daos.IRotableClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.IRotableDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWagonSlotDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWheelsetClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWheelsetDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkorderDao;
import ua.com.fielden.platform.test.domain.entities.daos.IWorkshopDao;
import ua.com.fielden.platform.test.domain.entities.daos.PersonDao;
import ua.com.fielden.platform.test.domain.entities.daos.RotableClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.RotableDao;
import ua.com.fielden.platform.test.domain.entities.daos.TgMeterReadingDao;
import ua.com.fielden.platform.test.domain.entities.daos.TgVehicleMakeDao;
import ua.com.fielden.platform.test.domain.entities.daos.TgVehicleModelDao;
import ua.com.fielden.platform.test.domain.entities.daos.WagonClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.WagonDao;
import ua.com.fielden.platform.test.domain.entities.daos.WagonSlotDao;
import ua.com.fielden.platform.test.domain.entities.daos.WheelsetClassDao;
import ua.com.fielden.platform.test.domain.entities.daos.WheelsetDao;
import ua.com.fielden.platform.test.domain.entities.daos.WorkorderDao;
import ua.com.fielden.platform.test.domain.entities.daos.WorkshopDao;
import ua.com.fielden.platform.test.entities.daos.IWorkorderableDao;
import ua.com.fielden.platform.test.entities.daos.TgTimesheetDao;
import ua.com.fielden.platform.test.entities.daos.WorkorderableDao;

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
	    final List<Class<? extends AbstractEntity>> applicationEntityTypes,//
	    final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
	    final Class<? extends IFilter> automaticDataFilterType, //
	    final SecurityTokenProvider tokenProvider,//
	    final Properties props) throws Exception {
	super(defaultHibernateTypes, applicationEntityTypes, serialisationClassProviderType, automaticDataFilterType, tokenProvider, props);
    }

    public PlatformTestServerModule(
	    final Map<Class, Class> defaultHibernateTypes, //
	    final List<Class<? extends AbstractEntity>> applicationEntityTypes,//
	    final Class<? extends ISerialisationClassProvider> serialisationClassProviderType, //
	    final Class<? extends IFilter> automaticDataFilterType, //
	    final Properties props) throws Exception {
	super(defaultHibernateTypes, applicationEntityTypes, serialisationClassProviderType, automaticDataFilterType, null, props);
    }

    @Override
    protected void configure() {
	super.configure();
	// bind DAO
	bind(IPersonDao.class).to(PersonDao.class);
	bind(IBogieDao.class).to(BogieDao.class);
	bind(IWheelsetDao.class).to(WheelsetDao.class);
	bind(IRotableDao.class).to(RotableDao.class);
	bind(IWorkshopDao.class).to(WorkshopDao.class);
	bind(IWagonClassDao.class).to(WagonClassDao.class);
	bind(IBogieClassDao.class).to(BogieClassDao.class);
	bind(IWheelsetClassDao.class).to(WheelsetClassDao.class);
	bind(IWagonDao.class).to(WagonDao.class);
	bind(IWagonSlotDao.class).to(WagonSlotDao.class);
	bind(IWorkorderDao.class).to(WorkorderDao.class);
	bind(IWorkorderableDao.class).to(WorkorderableDao.class);
	bind(IAdviceDao.class).to(AdviceDao.class);
	bind(IRotableClassDao.class).to(RotableClassDao.class);

	bind(ITgTimesheet.class).to(TgTimesheetDao.class);
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
