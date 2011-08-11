package ua.com.fielden.platform.swing.categorychart;

import java.util.EventListener;

public interface MultipleChartPanelListener extends EventListener {

    void valueChanged(MultipleChartPanelEvent event);
}
