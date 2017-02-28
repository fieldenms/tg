package ua.com.fielden.platform.web.test.server;

import ua.com.fielden.platform.sample.domain.TgMessageMap;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.impl.AbstractMapMaster;

/**
 * {@link IMaster} implementation for {@link TgMessageMap}.
 *
 * @author TG Team
 */
public class TgMessageMapMaster extends AbstractMapMaster<TgMessageMap> {
    public TgMessageMapMaster() {
        super(TgMessageMap.class, "gis/message/tg-message-gis-component", "MessageGisComponent");
    }
}
