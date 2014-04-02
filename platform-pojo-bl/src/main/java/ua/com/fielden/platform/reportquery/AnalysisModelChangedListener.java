package ua.com.fielden.platform.reportquery;

import java.util.EventListener;

/**
 * {@link EventListener} that is responsible for responding on the analysis data changed events.
 * 
 * @author TG Team
 * 
 */
public interface AnalysisModelChangedListener extends EventListener {

    /**
     * Is notified after the chart model changed.
     * 
     * @param event
     */
    void cahrtModelChanged(AnalysisModelChangedEvent event);
}
