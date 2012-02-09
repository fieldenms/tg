package ua.com.fielden.platform.swing.review.report.interfaces;

import java.util.EventListener;

import ua.com.fielden.platform.swing.review.report.events.LocatorConfigurationEvent;

public interface ILocatorConfigurationEventListener extends EventListener {

    boolean locatorConfigurationEventPerformed(LocatorConfigurationEvent event);
}
