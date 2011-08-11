package ua.com.fielden.platform.ui.config.api.interaction;

import ua.com.fielden.platform.error.Result;

public interface ILocatorConfigurationManager {

    Result canSave(String locatorKey);

    Result canConfigure(String locatorKey);
}
