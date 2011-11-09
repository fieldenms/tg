package ua.com.fielden.platform.swing.review.report.interfaces;

import java.util.EventListener;

import ua.com.fielden.platform.swing.review.report.events.PageLoadedEvent;

public interface IPageLoadedEventListener extends EventListener {

    void pageLoaded(PageLoadedEvent e);
}
