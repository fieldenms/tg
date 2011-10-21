package ua.com.fielden.platform.reportquery;

import java.util.EventListener;

public interface ChartModelChangedListener extends EventListener {

    void cahrtModelChanged(ChartModelChangedEvent event);
}
