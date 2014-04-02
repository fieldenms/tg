package ua.com.fielden.platform.swing.categorychart;

import java.util.EventListener;

/**
 * {@link EventListener} contract that provides ability to listen chart type change events.
 * 
 * @author TG Team
 * 
 */
public interface ICurrentChartTypeChangedListener extends EventListener {

    void chartTypeChanged(ChartTypeChangedEvent event);
}
