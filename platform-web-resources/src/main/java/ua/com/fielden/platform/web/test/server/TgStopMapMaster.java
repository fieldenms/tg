package ua.com.fielden.platform.web.test.server;

import ua.com.fielden.platform.sample.domain.TgStopMap;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.impl.AbstractMapMaster;

/**
 * {@link IMaster} implementation for {@link TgStopMap}.
 *
 * @author TG Team
 */
public class TgStopMapMaster extends AbstractMapMaster<TgStopMap> {
    
    public TgStopMapMaster() {
        super(TgStopMap.class, "gis/stop/tg-stop-gis-component", "StopGisComponent");
    }
}