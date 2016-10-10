package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
/** 
 * DAO implementation for companion object {@link ITgMachineRealtimeMonitorMap}.
 * 
 * @author Developers
 *
 */
@EntityType(TgMachineRealtimeMonitorMap.class)
public class TgMachineRealtimeMonitorMapDao extends CommonEntityDao<TgMachineRealtimeMonitorMap> implements ITgMachineRealtimeMonitorMap {

    @Inject
    public TgMachineRealtimeMonitorMapDao(final IFilter filter) {
        super(filter);
    }

    @Override
    public IFetchProvider<TgMachineRealtimeMonitorMap> createFetchProvider() {
        return super.createFetchProvider()
                .with("key"); // this property is "required" (necessary during saving) -- should be declared as fetching property
    }
}