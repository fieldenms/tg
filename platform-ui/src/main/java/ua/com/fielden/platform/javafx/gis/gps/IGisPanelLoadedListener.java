package ua.com.fielden.platform.javafx.gis.gps;

import java.util.EventListener;

import ua.com.fielden.platform.javafx.gis.GisViewPanel;

/**
 * {@link EventListener} that listens the {@link GisViewPanel}'s load events.
 *
 * @author TG Team
 *
 */
public interface IGisPanelLoadedListener extends EventListener {

    void gisPanelLoaded(GisPanelLoadEvent e);
}
