package ua.com.fielden.platform.web.test.server;

import ua.com.fielden.platform.sample.domain.TgPolygonMap;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.impl.AbstractMapMaster;

/**
 * {@link IMaster} implementation for {@link TgPolygonMap}.
 *
 * @author TG Team
 */
public class TgPolygonMapMaster extends AbstractMapMaster<TgPolygonMap> {
    
    public TgPolygonMapMaster() {
        super(TgPolygonMap.class, "gis/polygon/tg-polygon-gis-component", "PolygonGisComponent");
    }
}
