package ua.com.fielden.platform.swing.review.report.interfaces;

import java.util.EventListener;

import ua.com.fielden.platform.swing.review.report.events.DataLoadedEvent;

public interface IDataLoadedEventListener extends EventListener {

    void dataLoaded(DataLoadedEvent e);

}
