package ua.com.fielden.platform.web.test.server;

import ua.com.fielden.platform.web.centre.api.resultset.toolbar.impl.CentreToolbar;
import ua.com.fielden.platform.web.minijs.JsCode;

public class TgMachineRealtimeMonitorCentreToolbar extends CentreToolbar {

    @Override
    public JsCode code(final Class<?> entityType) {
        return new JsCode(
                  "var oldDataHandler = self.dataHandler;\n"
                + "self.dataHandler = (function (data) {\n"
                + "    console.debug('CUSTOM DATA HANDLER for', data);\n"
                + "    this.querySelector('tg-map')._gisComponent._markerCluster.setShouldFitToBounds(false);\n" // here fittingToBounds should be turned off to be able to observe marker movements without changing map zoom factor
                + "    oldDataHandler(data);\n" // TODO please, revisit generic sse dataHandler and provide it own version of centre refreshing based on multiple machineIds to be refreshed or other custom logic
                + "}).bind(self);\n");
    }
}
