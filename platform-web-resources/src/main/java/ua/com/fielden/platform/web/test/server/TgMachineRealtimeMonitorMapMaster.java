package ua.com.fielden.platform.web.test.server;

import ua.com.fielden.platform.sample.domain.TgMachineRealtimeMonitorMap;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.impl.AbstractMapMaster;

/**
 * {@link IMaster} implementation for {@link TgMachineRealtimeMonitorMap}.
 *
 * @author TG Team
 *
 */
public class TgMachineRealtimeMonitorMapMaster extends AbstractMapMaster<TgMachineRealtimeMonitorMap> {
    
    public TgMachineRealtimeMonitorMapMaster() {
        super(TgMachineRealtimeMonitorMap.class, "gis/realtimemonitor/tg-realtime-monitor-gis-component", "RealtimeMonitorGisComponent");
    }
}
