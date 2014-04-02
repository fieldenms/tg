package ua.com.fielden.platform.javafx.gis.gps;

import java.util.EventListener;

import javafx.scene.web.WebView;

/**
 * {@link EventListener} that listens the {@link WebView}'s load events.
 *
 * @author TG Team
 *
 */
public interface IWebViewLoadedListener extends EventListener {

    void webViewLoaded(final WebViewLoadEvent e);
}
