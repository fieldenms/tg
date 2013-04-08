package ua.com.fielden.platform.swing.categorychart;

import java.util.EventListener;

public interface IChartUpdateListener extends EventListener {

    void chartWasUpdated(ChartUpdateEvent event);
}
